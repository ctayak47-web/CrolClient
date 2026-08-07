
package jnr.ffi.provider.jffi;

import com.kenai.jffi.LastError;
import java.lang.reflect.Field;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jnr.ffi.NativeType;
import jnr.ffi.ObjectReferenceManager;
import jnr.ffi.Platform;
import jnr.ffi.Runtime;
import jnr.ffi.Type;
import jnr.ffi.TypeAlias;
import jnr.ffi.mapper.DefaultTypeMapper;
import jnr.ffi.mapper.SignatureTypeMapperAdapter;
import jnr.ffi.provider.AbstractRuntime;
import jnr.ffi.provider.BadType;
import jnr.ffi.provider.DefaultObjectReferenceManager;
import jnr.ffi.provider.jffi.NativeClosureManager;
import jnr.ffi.provider.jffi.NativeLibrary;
import jnr.ffi.provider.jffi.NativeMemoryManager;

public final class NativeRuntime
extends AbstractRuntime {
    private final NativeMemoryManager mm = new NativeMemoryManager(this);
    private final NativeClosureManager closureManager = new NativeClosureManager(this, new SignatureTypeMapperAdapter(new DefaultTypeMapper()));
    private final Type[] aliases;
    final WeakHashMap<NativeLibrary, NativeLibrary.LoadedLibraryData> loadedLibraries = new WeakHashMap();

    public static NativeRuntime getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public static List<NativeLibrary.LoadedLibraryData> getLoadedLibraries() {
        if (NativeRuntime.getSystemRuntime() instanceof NativeRuntime) {
            return new ArrayList<NativeLibrary.LoadedLibraryData>(((NativeRuntime)NativeRuntime.getSystemRuntime()).loadedLibraries.values());
        }
        return Collections.emptyList();
    }

    private NativeRuntime() {
        super(ByteOrder.nativeOrder(), NativeRuntime.buildTypeMap());
        NativeType[] nativeAliases = NativeRuntime.buildNativeTypeAliases();
        EnumSet<TypeAlias> typeAliasSet = EnumSet.allOf(TypeAlias.class);
        this.aliases = new Type[typeAliasSet.size()];
        for (TypeAlias alias : typeAliasSet) {
            if (nativeAliases.length > alias.ordinal() && nativeAliases[alias.ordinal()] != NativeType.VOID) {
                this.aliases[alias.ordinal()] = this.findType(nativeAliases[alias.ordinal()]);
                continue;
            }
            this.aliases[alias.ordinal()] = new BadType(alias.name());
        }
    }

    private static EnumMap<NativeType, Type> buildTypeMap() {
        EnumMap<NativeType, Type> typeMap = new EnumMap<NativeType, Type>(NativeType.class);
        EnumSet<NativeType> nativeTypes = EnumSet.allOf(NativeType.class);
        for (NativeType t : nativeTypes) {
            typeMap.put(t, NativeRuntime.jafflType(t));
        }
        return typeMap;
    }

    private static NativeType[] buildNativeTypeAliases() {
        Platform platform = Platform.getNativePlatform();
        Package pkg = NativeRuntime.class.getPackage();
        String cpu = platform.getCPU().toString();
        String os = platform.getOS().toString();
        EnumSet<TypeAlias> typeAliases = EnumSet.allOf(TypeAlias.class);
        NativeType[] aliases = new NativeType[]{};
        try {
            Class<?> cls = Class.forName(pkg.getName() + ".platform." + cpu + "." + os + ".TypeAliases");
            Field aliasesField = cls.getField("ALIASES");
            Map aliasMap = (Map)Map.class.cast(aliasesField.get(cls));
            aliases = new NativeType[typeAliases.size()];
            for (TypeAlias t : typeAliases) {
                aliases[t.ordinal()] = (NativeType)((Object)aliasMap.get((Object)t));
                if (aliases[t.ordinal()] != null) continue;
                aliases[t.ordinal()] = NativeType.VOID;
            }
        }
        catch (ClassNotFoundException cne) {
            Logger.getLogger(NativeRuntime.class.getName()).log(Level.SEVERE, "failed to load type aliases: " + cne);
        }
        catch (NoSuchFieldException nsfe) {
            Logger.getLogger(NativeRuntime.class.getName()).log(Level.SEVERE, "failed to load type aliases: " + nsfe);
        }
        catch (IllegalAccessException iae) {
            Logger.getLogger(NativeRuntime.class.getName()).log(Level.SEVERE, "failed to load type aliases: " + iae);
        }
        return aliases;
    }

    @Override
    public Type findType(TypeAlias type) {
        return this.aliases[type.ordinal()];
    }

    @Override
    public final NativeMemoryManager getMemoryManager() {
        return this.mm;
    }

    @Override
    public NativeClosureManager getClosureManager() {
        return this.closureManager;
    }

    public ObjectReferenceManager newObjectReferenceManager() {
        return new DefaultObjectReferenceManager(this);
    }

    @Override
    public int getLastError() {
        return LastError.getInstance().get();
    }

    @Override
    public void setLastError(int error) {
        LastError.getInstance().set(error);
    }

    @Override
    public boolean isCompatible(Runtime other) {
        return other instanceof NativeRuntime;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        NativeRuntime that = (NativeRuntime)o;
        return Arrays.equals(this.aliases, that.aliases) && this.closureManager.equals(that.closureManager) && this.mm.equals(that.mm);
    }

    public int hashCode() {
        int result = this.mm.hashCode();
        result = 31 * result + this.closureManager.hashCode();
        result = 31 * result + Arrays.hashCode(this.aliases);
        return result;
    }

    private static Type jafflType(NativeType type) {
        switch (type) {
            case VOID: {
                return new TypeDelegate(com.kenai.jffi.Type.VOID, NativeType.VOID);
            }
            case SCHAR: {
                return new TypeDelegate(com.kenai.jffi.Type.SCHAR, NativeType.SCHAR);
            }
            case UCHAR: {
                return new TypeDelegate(com.kenai.jffi.Type.UCHAR, NativeType.UCHAR);
            }
            case SSHORT: {
                return new TypeDelegate(com.kenai.jffi.Type.SSHORT, NativeType.SSHORT);
            }
            case USHORT: {
                return new TypeDelegate(com.kenai.jffi.Type.USHORT, NativeType.USHORT);
            }
            case SINT: {
                return new TypeDelegate(com.kenai.jffi.Type.SINT, NativeType.SINT);
            }
            case UINT: {
                return new TypeDelegate(com.kenai.jffi.Type.UINT, NativeType.UINT);
            }
            case SLONG: {
                return new TypeDelegate(com.kenai.jffi.Type.SLONG, NativeType.SLONG);
            }
            case ULONG: {
                return new TypeDelegate(com.kenai.jffi.Type.ULONG, NativeType.ULONG);
            }
            case SLONGLONG: {
                return new TypeDelegate(com.kenai.jffi.Type.SINT64, NativeType.SLONGLONG);
            }
            case ULONGLONG: {
                return new TypeDelegate(com.kenai.jffi.Type.UINT64, NativeType.ULONGLONG);
            }
            case FLOAT: {
                return new TypeDelegate(com.kenai.jffi.Type.FLOAT, NativeType.FLOAT);
            }
            case DOUBLE: {
                return new TypeDelegate(com.kenai.jffi.Type.DOUBLE, NativeType.DOUBLE);
            }
            case ADDRESS: {
                return new TypeDelegate(com.kenai.jffi.Type.POINTER, NativeType.ADDRESS);
            }
        }
        return new BadType(type.toString());
    }

    private static final class SingletonHolder {
        public static final NativeRuntime INSTANCE = new NativeRuntime();

        private SingletonHolder() {
        }
    }

    private static final class TypeDelegate
    extends Type {
        private final com.kenai.jffi.Type type;
        private final NativeType nativeType;

        public TypeDelegate(com.kenai.jffi.Type type, NativeType nativeType) {
            this.type = type;
            this.nativeType = nativeType;
        }

        @Override
        public int alignment() {
            return this.type.alignment();
        }

        @Override
        public int size() {
            return this.type.size();
        }

        @Override
        public NativeType getNativeType() {
            return this.nativeType;
        }

        public String toString() {
            return this.type.toString();
        }
    }
}

