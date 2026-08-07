
package jnr.ffi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import jnr.ffi.CallingConvention;
import jnr.ffi.LibraryOption;
import jnr.ffi.Platform;
import jnr.ffi.mapper.CompositeFunctionMapper;
import jnr.ffi.mapper.CompositeTypeMapper;
import jnr.ffi.mapper.DataConverter;
import jnr.ffi.mapper.FromNativeConverter;
import jnr.ffi.mapper.FunctionMapper;
import jnr.ffi.mapper.SignatureTypeMapper;
import jnr.ffi.mapper.SignatureTypeMapperAdapter;
import jnr.ffi.mapper.ToNativeConverter;
import jnr.ffi.mapper.TypeMapper;
import jnr.ffi.provider.FFIProvider;
import jnr.ffi.provider.LoadedLibrary;

public abstract class LibraryLoader<T> {
    public static final String DEFAULT_LIBRARY = "RTLD_DEFAULT";
    private final List<String> searchPaths = new ArrayList<String>();
    private final List<String> libraryNames = new ArrayList<String>();
    private final List<SignatureTypeMapper> typeMappers = new ArrayList<SignatureTypeMapper>();
    private final List<FunctionMapper> functionMappers = new ArrayList<FunctionMapper>();
    private final Map<LibraryOption, Object> optionMap = new EnumMap<LibraryOption, Object>(LibraryOption.class);
    private final TypeMapper.Builder typeMapperBuilder = new TypeMapper.Builder();
    private final FunctionMapper.Builder functionMapperBuilder = new FunctionMapper.Builder();
    private final Class<T> interfaceClass;
    private boolean failImmediately = false;

    public static <T> LibraryLoader<T> create(Class<T> interfaceClass) {
        return FFIProvider.getSystemProvider().createLibraryLoader(interfaceClass);
    }

    protected LibraryLoader(Class<T> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public static boolean saveError(Map<LibraryOption, ?> options, boolean methodHasSave, boolean methodHasIgnore) {
        boolean saveError;
        boolean bl = saveError = options.containsKey((Object)LibraryOption.SaveError) || !options.containsKey((Object)LibraryOption.IgnoreError);
        if (saveError) {
            if (methodHasIgnore && !methodHasSave) {
                saveError = false;
            }
        } else if (methodHasSave) {
            saveError = true;
        }
        return saveError;
    }

    public static <T> T loadLibrary(Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions, Map<String, List<String>> searchPaths, String ... libraryNames) {
        LibraryLoader<T> loader = FFIProvider.getSystemProvider().createLibraryLoader(interfaceClass);
        for (String libraryName : libraryNames) {
            if (libraryName.equals(DEFAULT_LIBRARY)) {
                loader.searchDefault();
                continue;
            }
            loader.library(libraryName);
            List<String> paths = searchPaths.get(libraryName);
            if (paths == null) continue;
            for (String path : paths) {
                loader.search(path);
            }
        }
        if (libraryOptions != null) {
            for (Map.Entry entry : libraryOptions.entrySet()) {
                loader.option((LibraryOption)((Object)entry.getKey()), entry.getValue());
            }
        }
        return loader.failImmediately().load();
    }

    public static <T> T loadLibrary(Class<T> interfaceClass, Map<LibraryOption, ?> libraryOptions, String ... libraryNames) {
        return LibraryLoader.loadLibrary(interfaceClass, libraryOptions, Collections.EMPTY_MAP, libraryNames);
    }

    public LibraryLoader<T> library(String libraryName) {
        if (libraryName.equals(DEFAULT_LIBRARY)) {
            return this.searchDefault();
        }
        this.libraryNames.add(libraryName);
        return this;
    }

    public LibraryLoader<T> searchDefault() {
        this.libraryNames.add(DEFAULT_LIBRARY);
        return this;
    }

    public LibraryLoader<T> search(String path) {
        this.searchPaths.add(path);
        return this;
    }

    public LibraryLoader<T> option(LibraryOption option, Object value) {
        switch (option) {
            case TypeMapper: {
                if (value instanceof SignatureTypeMapper) {
                    this.mapper((SignatureTypeMapper)value);
                    break;
                }
                if (value instanceof TypeMapper) {
                    this.mapper((TypeMapper)value);
                    break;
                }
                if (value == null) break;
                throw new IllegalArgumentException("invalid TypeMapper: " + value.getClass());
            }
            case FunctionMapper: {
                this.mapper((FunctionMapper)value);
                break;
            }
            default: {
                this.optionMap.put(option, value);
            }
        }
        return this;
    }

    public LibraryLoader<T> mapper(TypeMapper typeMapper) {
        this.typeMappers.add(new SignatureTypeMapperAdapter(typeMapper));
        return this;
    }

    public LibraryLoader<T> mapper(SignatureTypeMapper typeMapper) {
        this.typeMappers.add(typeMapper);
        return this;
    }

    public <J> LibraryLoader<T> map(Class<? extends J> javaType, ToNativeConverter<? extends J, ?> toNativeConverter) {
        this.typeMapperBuilder.map(javaType, toNativeConverter);
        return this;
    }

    public <J> LibraryLoader<T> map(Class<? extends J> javaType, FromNativeConverter<? extends J, ?> fromNativeConverter) {
        this.typeMapperBuilder.map(javaType, fromNativeConverter);
        return this;
    }

    public <J> LibraryLoader<T> map(Class<? extends J> javaType, DataConverter<? extends J, ?> dataConverter) {
        this.typeMapperBuilder.map(javaType, dataConverter);
        return this;
    }

    public LibraryLoader<T> mapper(FunctionMapper functionMapper) {
        this.functionMappers.add(functionMapper);
        return this;
    }

    public LibraryLoader<T> map(String javaName, String nativeFunction) {
        this.functionMapperBuilder.map(javaName, nativeFunction);
        return this;
    }

    public LibraryLoader<T> convention(CallingConvention convention) {
        this.optionMap.put(LibraryOption.CallingConvention, (Object)convention);
        return this;
    }

    public final LibraryLoader<T> stdcall() {
        return this.convention(CallingConvention.STDCALL);
    }

    public final LibraryLoader<T> failImmediately() {
        this.failImmediately = true;
        return this;
    }

    public T load(String libraryName) {
        return this.library(libraryName).load();
    }

    public T load() {
        if (this.libraryNames.isEmpty()) {
            throw new UnsatisfiedLinkError("no library names specified");
        }
        this.typeMappers.add(0, new SignatureTypeMapperAdapter(this.typeMapperBuilder.build()));
        this.optionMap.put(LibraryOption.TypeMapper, this.typeMappers.size() > 1 ? new CompositeTypeMapper(this.typeMappers) : this.typeMappers.get(0));
        this.functionMappers.add(0, this.functionMapperBuilder.build());
        this.optionMap.put(LibraryOption.FunctionMapper, this.functionMappers.size() > 1 ? new CompositeFunctionMapper(this.functionMappers) : this.functionMappers.get(0));
        try {
            return this.loadLibrary(this.interfaceClass, Collections.unmodifiableList(this.libraryNames), this.getSearchPaths(), Collections.unmodifiableMap(this.optionMap), this.failImmediately);
        }
        catch (LinkageError error) {
            if (this.failImmediately) {
                throw error;
            }
            return this.createErrorProxy(error);
        }
        catch (Exception ex) {
            RuntimeException re;
            RuntimeException runtimeException = re = ex instanceof RuntimeException ? (RuntimeException)ex : new RuntimeException(ex);
            if (this.failImmediately) {
                throw re;
            }
            return this.createErrorProxy(re);
        }
    }

    private T createErrorProxy(final Throwable ex) {
        return this.interfaceClass.cast(Proxy.newProxyInstance(this.interfaceClass.getClassLoader(), new Class[]{this.interfaceClass, LoadedLibrary.class}, new InvocationHandler(){

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                throw ex;
            }
        }));
    }

    private Collection<String> getSearchPaths() {
        ArrayList<String> paths = new ArrayList<String>(this.searchPaths);
        paths.addAll(DefaultLibPaths.PATHS);
        return Collections.unmodifiableList(paths);
    }

    protected abstract T loadLibrary(Class<T> var1, Collection<String> var2, Collection<String> var3, Map<LibraryOption, Object> var4, boolean var5);

    private static List<String> getPropertyPaths(String propName) {
        String value = System.getProperty(propName);
        if (value != null) {
            String[] paths = value.split(File.pathSeparator);
            return new ArrayList<String>(Arrays.asList(paths));
        }
        return Collections.emptyList();
    }

    static final class DefaultLibPaths {
        static final List<String> PATHS;

        DefaultLibPaths() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private static void addPathsFromFile(Collection<String> paths, File file) {
            if (!file.isFile() || !file.exists()) {
                return;
            }
            BufferedReader in = null;
            try {
                in = new BufferedReader(new FileReader(file));
                String line = in.readLine();
                while (line != null) {
                    if (!(line.trim().isEmpty() || line.startsWith("#") || line.startsWith("include "))) {
                        paths.add(line);
                    }
                    line = in.readLine();
                }
            }
            catch (IOException iOException) {
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    }
                    catch (IOException iOException) {}
                }
            }
        }

        static {
            LinkedHashSet<String> paths = new LinkedHashSet<String>();
            try {
                paths.addAll(LibraryLoader.getPropertyPaths("jnr.ffi.library.path"));
                paths.addAll(LibraryLoader.getPropertyPaths("jaffl.library.path"));
                paths.addAll(LibraryLoader.getPropertyPaths("jna.library.path"));
                paths.addAll(LibraryLoader.getPropertyPaths("java.library.path"));
            }
            catch (Exception exception) {
                
            }
            if (Platform.getNativePlatform().isUnix()) {
                paths.add("/usr/local/lib");
                paths.add("/usr/lib");
                paths.add("/lib");
            }
            switch (Platform.getNativePlatform().getOS()) {
                case FREEBSD: 
                case OPENBSD: 
                case NETBSD: 
                case LINUX: 
                case ZLINUX: 
                case MIDNIGHTBSD: {
                    File ldSoConf = new File("/etc/ld.so.conf");
                    File ldSoConfD = new File("/etc/ld.so.conf.d");
                    if (ldSoConf.exists()) {
                        DefaultLibPaths.addPathsFromFile(paths, ldSoConf);
                    }
                    if (!ldSoConfD.isDirectory()) break;
                    for (File file : ldSoConfD.listFiles()) {
                        DefaultLibPaths.addPathsFromFile(paths, file);
                    }
                    break;
                }
            }
            PATHS = Collections.unmodifiableList(new ArrayList(paths));
        }
    }
}

