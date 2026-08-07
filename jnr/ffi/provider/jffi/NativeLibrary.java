
package jnr.ffi.provider.jffi;

import com.kenai.jffi.Library;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jnr.ffi.LibraryOption;
import jnr.ffi.Platform;
import jnr.ffi.Runtime;
import jnr.ffi.provider.jffi.NativeRuntime;
import jnr.ffi.provider.jffi.SymbolNotFoundError;

public class NativeLibrary {
    private final List<String> libraryNames;
    private final List<String> searchPaths;
    private final List<String> successfulPaths = new ArrayList<String>();
    private final Map<LibraryOption, Object> options;
    private volatile List<Library> nativeLibraries = Collections.emptyList();
    private static final Pattern BAD_ELF = Pattern.compile("(.*): (invalid ELF header|file too short|invalid file format)");
    private static final Pattern ELF_GROUP = Pattern.compile("GROUP\\s*\\(\\s*(\\S*).*\\)");

    NativeLibrary(Collection<String> libraryNames, Collection<String> searchPaths, Map<LibraryOption, Object> options) {
        this.libraryNames = Collections.unmodifiableList(new ArrayList<String>(libraryNames));
        this.searchPaths = Collections.unmodifiableList(new ArrayList<String>(searchPaths));
        this.options = options;
        if (options.containsKey((Object)LibraryOption.LoadNow)) {
            this.getNativeLibraries();
        }
    }

    private String locateLibrary(String libraryName) {
        return Platform.getNativePlatform().locateLibrary(libraryName, this.searchPaths, this.options);
    }

    long getSymbolAddress(String name) {
        for (Library l : this.getNativeLibraries()) {
            long address = l.getSymbolAddress(name);
            if (address == 0L) continue;
            return address;
        }
        return 0L;
    }

    long findSymbolAddress(String name) {
        long address = this.getSymbolAddress(name);
        if (address == 0L) {
            throw new SymbolNotFoundError(Library.getLastError());
        }
        return address;
    }

    private synchronized List<Library> getNativeLibraries() {
        if (!this.nativeLibraries.isEmpty()) {
            return this.nativeLibraries;
        }
        this.nativeLibraries = this.loadNativeLibraries();
        return this.nativeLibraries;
    }

    private synchronized List<Library> loadNativeLibraries() {
        ArrayList<Library> libs = new ArrayList<Library>();
        for (String libraryName : this.libraryNames) {
            String path;
            if (libraryName == null) continue;
            if (libraryName.equals("RTLD_DEFAULT")) {
                libs.add(Library.getDefault());
                continue;
            }
            Library lib = NativeLibrary.openLibrary(libraryName, this.successfulPaths);
            if (lib == null && !libraryName.equals(path = this.locateLibrary(libraryName))) {
                lib = NativeLibrary.openLibrary(path, this.successfulPaths);
            }
            if (lib == null) {
                throw new UnsatisfiedLinkError(Library.getLastError() + "\nLibrary names\n" + this.libraryNames.toString() + "\nSearch paths:\n" + this.searchPaths.toString());
            }
            libs.add(lib);
        }
        this.putLibraryIntoRuntime();
        return Collections.unmodifiableList(libs);
    }

    private static Library openLibrary(String path, List<String> successfulPaths) {
        Matcher sharedObject;
        File f;
        Library lib = Library.getCachedInstance(path, 9);
        if (lib != null) {
            successfulPaths.add(path);
            return lib;
        }
        Matcher badElf = BAD_ELF.matcher(Library.getLastError());
        if (badElf.lookingAt() && (f = new File(badElf.group(1))).isFile() && f.length() < 4096L && (sharedObject = ELF_GROUP.matcher(NativeLibrary.readAll(f))).find()) {
            lib = Library.getCachedInstance(sharedObject.group(1), 9);
            if (lib != null) {
                successfulPaths.add(path);
            }
            return lib;
        }
        return null;
    }

    private static String readAll(File f) {
        BufferedReader br = null;
        try {
            String line;
            br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String string = sb.toString();
            return string;
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        finally {
            if (br != null) {
                try {
                    br.close();
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void putLibraryIntoRuntime() {
        if (Runtime.getSystemRuntime() instanceof NativeRuntime) {
            ((NativeRuntime)Runtime.getSystemRuntime()).loadedLibraries.put(this, new LoadedLibraryData(this.libraryNames, this.searchPaths, this.successfulPaths));
        }
    }

    public static class LoadedLibraryData {
        private final List<String> libraryNames;
        private final List<String> searchPaths;
        private final List<String> successfulPaths;

        LoadedLibraryData(List<String> libraryNames, List<String> searchPaths, List<String> successfulPaths) {
            this.libraryNames = Collections.unmodifiableList(libraryNames);
            this.searchPaths = Collections.unmodifiableList(searchPaths);
            this.successfulPaths = Collections.unmodifiableList(successfulPaths);
        }

        public List<String> getLibraryNames() {
            return this.libraryNames;
        }

        public List<String> getSearchPaths() {
            return this.searchPaths;
        }

        public List<String> getSuccessfulPaths() {
            return this.successfulPaths;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof LoadedLibraryData)) {
                return false;
            }
            LoadedLibraryData that = (LoadedLibraryData)o;
            return Objects.equals(this.libraryNames, that.libraryNames) && Objects.equals(this.searchPaths, that.searchPaths) && Objects.equals(this.successfulPaths, that.successfulPaths);
        }

        public int hashCode() {
            return Objects.hash(this.libraryNames, this.searchPaths, this.successfulPaths);
        }

        public String toString() {
            return "LoadedLibraryData {libraryNames=" + this.libraryNames + ", searchPaths=" + this.searchPaths + ", successfulPaths=" + this.successfulPaths + '}';
        }
    }
}

