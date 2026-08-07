
package jnr.ffi;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jnr.ffi.LibraryLoader;
import jnr.ffi.LibraryOption;

public abstract class Platform {
    private static final Locale LOCALE = Locale.ENGLISH;
    private final OS os;
    private final CPU cpu;
    private final int addressSize;
    private final int longSize;
    protected final Pattern libPattern;

    private static OS determineOS() {
        String osName = System.getProperty("os.name").split(" ")[0];
        if (Platform.startsWithIgnoreCase(osName, "mac") || Platform.startsWithIgnoreCase(osName, "darwin")) {
            return OS.DARWIN;
        }
        if (Platform.startsWithIgnoreCase(osName, "linux")) {
            return OS.LINUX;
        }
        if (Platform.startsWithIgnoreCase(osName, "sunos") || Platform.startsWithIgnoreCase(osName, "solaris")) {
            return OS.SOLARIS;
        }
        if (Platform.startsWithIgnoreCase(osName, "aix")) {
            return OS.AIX;
        }
        if (Platform.startsWithIgnoreCase(osName, "os400") || Platform.startsWithIgnoreCase(osName, "os/400")) {
            return OS.IBMI;
        }
        if (Platform.startsWithIgnoreCase(osName, "openbsd")) {
            return OS.OPENBSD;
        }
        if (Platform.startsWithIgnoreCase(osName, "freebsd")) {
            return OS.FREEBSD;
        }
        if (Platform.startsWithIgnoreCase(osName, "dragonfly")) {
            return OS.DRAGONFLY;
        }
        if (Platform.startsWithIgnoreCase(osName, "windows")) {
            return OS.WINDOWS;
        }
        if (Platform.startsWithIgnoreCase(osName, "midnightbsd")) {
            return OS.MIDNIGHTBSD;
        }
        return OS.UNKNOWN;
    }

    private static Platform determinePlatform(OS os) {
        switch (os) {
            case DARWIN: {
                return new Darwin();
            }
            case LINUX: {
                return new Linux();
            }
            case WINDOWS: {
                return new Windows();
            }
            case IBMI: {
                return new IbmI();
            }
            case UNKNOWN: {
                return new Unsupported(os);
            }
        }
        return new Default(os);
    }

    private static Platform determinePlatform() {
        String providerName = System.getProperty("jnr.ffi.provider");
        try {
            Class<?> c = Class.forName(providerName + "$Platform");
            return (Platform)c.newInstance();
        }
        catch (ClassNotFoundException ex) {
            return Platform.determinePlatform(Platform.determineOS());
        }
        catch (IllegalAccessException ex) {
            throw new ExceptionInInitializerError(ex);
        }
        catch (InstantiationException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static CPU determineCPU() {
        String archString = System.getProperty("os.arch");
        if (Platform.equalsIgnoreCase("x86", archString) || Platform.equalsIgnoreCase("i386", archString) || Platform.equalsIgnoreCase("i86pc", archString) || Platform.equalsIgnoreCase("i686", archString)) {
            return CPU.I386;
        }
        if (Platform.equalsIgnoreCase("x86_64", archString) || Platform.equalsIgnoreCase("amd64", archString)) {
            return CPU.X86_64;
        }
        if (Platform.equalsIgnoreCase("ppc", archString) || Platform.equalsIgnoreCase("powerpc", archString)) {
            if (OS.IBMI.equals((Object)Platform.determineOS())) {
                return CPU.PPC64;
            }
            return CPU.PPC;
        }
        if (Platform.equalsIgnoreCase("ppc64", archString) || Platform.equalsIgnoreCase("powerpc64", archString)) {
            if ("little".equals(System.getProperty("sun.cpu.endian"))) {
                return CPU.PPC64LE;
            }
            return CPU.PPC64;
        }
        if (Platform.equalsIgnoreCase("ppc64le", archString) || Platform.equalsIgnoreCase("powerpc64le", archString)) {
            return CPU.PPC64LE;
        }
        if (Platform.equalsIgnoreCase("s390", archString) || Platform.equalsIgnoreCase("s390x", archString)) {
            return CPU.S390X;
        }
        if (Platform.equalsIgnoreCase("aarch64", archString)) {
            return CPU.AARCH64;
        }
        if (Platform.equalsIgnoreCase("arm", archString) || Platform.equalsIgnoreCase("armv7l", archString)) {
            return CPU.ARM;
        }
        if (Platform.equalsIgnoreCase("mips64", archString) || Platform.equalsIgnoreCase("mips64el", archString)) {
            return CPU.MIPS64EL;
        }
        if (Platform.equalsIgnoreCase("loongarch64", archString)) {
            return CPU.LOONGARCH64;
        }
        if (Platform.equalsIgnoreCase("riscv64", archString)) {
            return CPU.RISCV64;
        }
        for (CPU cpu : CPU.values()) {
            if (!Platform.equalsIgnoreCase(cpu.name(), archString)) continue;
            return cpu;
        }
        return CPU.UNKNOWN;
    }

    public Platform(OS os, CPU cpu, int addressSize, int longSize, String libPattern) {
        this.os = os;
        this.cpu = cpu;
        this.addressSize = addressSize;
        this.longSize = longSize;
        this.libPattern = Pattern.compile(libPattern);
    }

    private Platform(OS os) {
        String libpattern;
        this.os = os;
        this.cpu = Platform.determineCPU();
        switch (os) {
            case WINDOWS: {
                libpattern = ".*\\.dll$";
                break;
            }
            case DARWIN: {
                libpattern = "lib.*\\.(dylib|jnilib)$";
                break;
            }
            case IBMI: {
                libpattern = "lib.*\\.(so|a\\(shr.o\\)|a\\(shr_64.o\\)|a|so.[\\.0-9]+)$";
                break;
            }
            default: {
                libpattern = "lib.*\\.so.*$";
            }
        }
        this.libPattern = Pattern.compile(libpattern);
        this.addressSize = Platform.calculateAddressSize(this.cpu);
        this.longSize = os == OS.WINDOWS ? 32 : this.addressSize;
    }

    private static int calculateAddressSize(CPU cpu) {
        Integer dataModel = Integer.getInteger("sun.arch.data.model");
        if (dataModel == null || dataModel != 32 && dataModel != 64) {
            switch (cpu) {
                case I386: 
                case PPC: 
                case SPARC: {
                    dataModel = 32;
                    break;
                }
                case X86_64: 
                case PPC64: 
                case PPC64LE: 
                case SPARCV9: 
                case S390X: 
                case AARCH64: 
                case MIPS64EL: 
                case LOONGARCH64: 
                case RISCV64: {
                    dataModel = 64;
                    break;
                }
                default: {
                    throw new ExceptionInInitializerError("Cannot determine cpu address size");
                }
            }
        }
        return dataModel;
    }

    public static Platform getNativePlatform() {
        return SingletonHolder.PLATFORM;
    }

    @Deprecated
    public static Platform getPlatform() {
        return SingletonHolder.PLATFORM;
    }

    public final OS getOS() {
        return this.os;
    }

    public final CPU getCPU() {
        return this.cpu;
    }

    public final boolean isBSD() {
        return this.os == OS.FREEBSD || this.os == OS.OPENBSD || this.os == OS.NETBSD || this.os == OS.DARWIN || this.os == OS.DRAGONFLY | this.os == OS.MIDNIGHTBSD;
    }

    public final boolean isUnix() {
        return this.os != OS.WINDOWS;
    }

    public final int longSize() {
        return this.longSize;
    }

    public final int addressSize() {
        return this.addressSize;
    }

    public final boolean is32Bit() {
        return this.addressSize == 32;
    }

    public final boolean is64Bit() {
        return this.addressSize == 64;
    }

    public final boolean isLittleEndian() {
        return "little".equals(System.getProperty("sun.cpu.endian"));
    }

    public final boolean isBigEndian() {
        return "big".equals(System.getProperty("sun.cpu.endian"));
    }

    public final String getOSName() {
        return System.getProperty("os.name", null);
    }

    public String getName() {
        return (Object)((Object)this.cpu) + "-" + (Object)((Object)this.os);
    }

    public String getVersion() {
        return System.getProperty("os.version", null);
    }

    private List<String> getVersionNumbers() {
        String version = this.getVersion();
        if (version == null) {
            return Collections.emptyList();
        }
        Matcher matcher = Pattern.compile("[\\d]+").matcher(version);
        ArrayList<String> result = new ArrayList<String>();
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }

    public int getVersionMajor() {
        List<String> versionNumbers = this.getVersionNumbers();
        return versionNumbers.size() < 1 ? -1 : Integer.parseInt(versionNumbers.get(0));
    }

    public int getVersionMinor() {
        List<String> versionNumbers = this.getVersionNumbers();
        return versionNumbers.size() < 2 ? -1 : Integer.parseInt(versionNumbers.get(1));
    }

    public String getStandardCLibraryName() {
        switch (this.os) {
            case LINUX: {
                return "libc.so.6";
            }
            case SOLARIS: {
                return "c";
            }
            case DRAGONFLY: 
            case FREEBSD: 
            case MIDNIGHTBSD: 
            case NETBSD: {
                return "c";
            }
            case IBMI: 
            case AIX: {
                return this.addressSize == 32 ? "libc.a(shr.o)" : "libc.a(shr_64.o)";
            }
            case WINDOWS: {
                return "msvcrt";
            }
        }
        return "c";
    }

    public String mapLibraryName(String libName) {
        if (this.libPattern.matcher(libName).find()) {
            return libName;
        }
        return System.mapLibraryName(libName);
    }

    public String locateLibrary(String libName, List<String> libraryPath) {
        String mappedName = this.mapLibraryName(libName);
        for (String path : libraryPath) {
            File libFile = new File(path, mappedName);
            if (!libFile.exists()) continue;
            return libFile.getAbsolutePath();
        }
        return mappedName;
    }

    public String locateLibrary(String libName, List<String> libraryPaths, Map<LibraryOption, Object> options) {
        return this.locateLibrary(libName, libraryPaths);
    }

    public List<String> libraryLocations(String libName, List<String> additionalPaths) {
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<String> libDirs = new ArrayList<String>();
        if (additionalPaths != null) {
            libDirs.addAll(additionalPaths);
        }
        libDirs.addAll(LibraryLoader.DefaultLibPaths.PATHS);
        String name = new File(this.locateLibrary(libName, libDirs)).getName();
        for (String libDir : libDirs) {
            File libFile = new File(libDir, name);
            if (!libFile.exists()) continue;
            result.add(libFile.getAbsolutePath());
        }
        return result;
    }

    private static boolean startsWithIgnoreCase(String s1, String s2) {
        return s1.startsWith(s2) || s1.toUpperCase(LOCALE).startsWith(s2.toUpperCase(LOCALE)) || s1.toLowerCase(LOCALE).startsWith(s2.toLowerCase(LOCALE));
    }

    private static boolean equalsIgnoreCase(String s1, String s2) {
        return s1.equalsIgnoreCase(s2) || s1.toUpperCase(LOCALE).equals(s2.toUpperCase(LOCALE)) || s1.toLowerCase(LOCALE).equals(s2.toLowerCase(LOCALE));
    }

    static /* synthetic */ Platform access$000() {
        return Platform.determinePlatform();
    }

    public static enum OS {
        DARWIN,
        FREEBSD,
        NETBSD,
        OPENBSD,
        DRAGONFLY,
        LINUX,
        SOLARIS,
        WINDOWS,
        AIX,
        IBMI,
        ZLINUX,
        MIDNIGHTBSD,
        UNKNOWN;

        public String toString() {
            return this.name().toLowerCase(LOCALE);
        }
    }

    private static final class Darwin
    extends Supported {
        public Darwin() {
            super(OS.DARWIN);
        }

        @Override
        public String mapLibraryName(String libName) {
            if (this.libPattern.matcher(libName).find()) {
                return libName;
            }
            return "lib" + libName + ".dylib";
        }
    }

    static final class Linux
    extends Supported {
        public Linux() {
            super(OS.LINUX);
        }

        @Override
        public String locateLibrary(String libName, List<String> libraryPaths) {
            return this.locateLibrary(libName, libraryPaths, null);
        }

        @Override
        public String locateLibrary(String libName, List<String> libraryPaths, Map<LibraryOption, Object> options) {
            List<Match> matches = this.getMatches(libName, libraryPaths);
            if (matches.isEmpty()) {
                return this.mapLibraryName(libName);
            }
            boolean preferCustom = options != null && options.containsKey((Object)LibraryOption.PreferCustomPaths);
            Collections.sort(matches);
            Match best = null;
            if (preferCustom) {
                for (Match match : matches) {
                    if (!match.isCustom) continue;
                    best = match;
                    break;
                }
            }
            return best != null ? best.path : matches.get((int)0).path;
        }

        private List<Match> getMatches(String libName, List<String> libraryPaths) {
            ArrayList<String> customPaths = new ArrayList<String>();
            if (LibraryLoader.DefaultLibPaths.PATHS.size() > 0 && libraryPaths.size() >= LibraryLoader.DefaultLibPaths.PATHS.size()) {
                String firstSystemPath = LibraryLoader.DefaultLibPaths.PATHS.get(0);
                int firstSystemPathIndex = libraryPaths.lastIndexOf(firstSystemPath);
                for (int i = 0; i < firstSystemPathIndex; ++i) {
                    customPaths.add(libraryPaths.get(i));
                }
            } else {
                customPaths.addAll(libraryPaths);
            }
            Pattern exclude = this.getCPU() == CPU.X86_64 ? Pattern.compile(".*(lib[a-z]*32|i[0-9]86).*") : Pattern.compile(".*(lib[a-z]*64|amd64|x86_64).*");
            final Pattern versionedLibPattern = Pattern.compile("lib" + libName + "\\.so((?:\\.[0-9]+)*)$");
            FilenameFilter filter = new FilenameFilter(){

                @Override
                public boolean accept(File dir, String name) {
                    return versionedLibPattern.matcher(name).matches();
                }
            };
            ArrayList<Match> matches = new ArrayList<Match>();
            for (String path : libraryPaths) {
                File libraryPath;
                File[] files;
                if (exclude.matcher(path).matches() || (files = (libraryPath = new File(path)).listFiles(filter)) == null) continue;
                for (File file : files) {
                    int[] version;
                    String versionString;
                    Matcher matcher = versionedLibPattern.matcher(file.getName());
                    String string = versionString = matcher.matches() ? matcher.group(1) : "";
                    if (versionString == null || versionString.isEmpty()) {
                        version = new int[]{};
                    } else {
                        String[] parts = versionString.split("\\.");
                        version = new int[parts.length - 1];
                        for (int i = 1; i < parts.length; ++i) {
                            version[i - 1] = Integer.parseInt(parts[i]);
                        }
                    }
                    Match match = new Match();
                    match.path = file.getAbsolutePath();
                    match.version = version;
                    match.isCustom = customPaths.contains(path);
                    matches.add(match);
                }
            }
            return matches;
        }

        private static int compareVersions(int[] version1, int[] version2) {
            if (version1 == null) {
                return version2 == null ? 0 : -1;
            }
            if (version2 == null) {
                return 1;
            }
            int commonLength = Math.min(version1.length, version2.length);
            for (int i = 0; i < commonLength; ++i) {
                if (version1[i] < version2[i]) {
                    return -1;
                }
                if (version1[i] <= version2[i]) continue;
                return 1;
            }
            return Integer.compare(version1.length, version2.length);
        }

        @Override
        public String mapLibraryName(String libName) {
            return "c".equals(libName) || "libc.so".equals(libName) ? "libc.so.6" : super.mapLibraryName(libName);
        }

        private static class Match
        implements Comparable<Match> {
            String path;
            int[] version;
            boolean isCustom;

            private Match() {
            }

            @Override
            public int compareTo(Match o) {
                return Linux.compareVersions(o.version, this.version);
            }
        }
    }

    private static class Windows
    extends Supported {
        private static final String WINDOWS_SERVER = "server";
        private static final String WINDOWS_VISTA = "windows vista";
        private static final String WINDOWS_7 = "windows 7";
        private static final String WINDOWS_8 = "windows 8";
        private static final String WINDOWS_10 = "windows 10";
        private static final String WINDOWS_11 = "windows 11";

        public Windows() {
            super(OS.WINDOWS);
        }

        private String osName() {
            return System.getProperty("os.name").toLowerCase();
        }

        public boolean isServer() {
            return this.osName().contains(WINDOWS_SERVER);
        }

        public boolean isVista() {
            return this.osName().contains(WINDOWS_VISTA);
        }

        public boolean is7() {
            return this.osName().contains(WINDOWS_7);
        }

        public boolean is8() {
            return this.osName().contains(WINDOWS_8);
        }

        public boolean is10() {
            return this.osName().contains(WINDOWS_10);
        }

        public boolean is11() {
            return this.osName().contains(WINDOWS_11);
        }
    }

    static final class IbmI
    extends Supported {
        public IbmI() {
            super(OS.IBMI);
        }

        @Override
        public String mapLibraryName(String libName) {
            if (this.libPattern.matcher(libName).find()) {
                return libName;
            }
            return "lib" + libName + ".a(shr_64.o)";
        }

        @Override
        public String locateLibrary(String libName, List<String> libraryPaths) {
            final Pattern versionedLibPattern = Pattern.compile("lib" + libName + "\\.so((?:\\.[0-9]+)*)$");
            final Pattern dotAorSoPattern = Pattern.compile("lib" + libName + "\\.(a|so)$");
            LinkedList<File> dotAorSoFiles = new LinkedList<File>();
            LinkedList<String> searchPaths = new LinkedList<String>();
            searchPaths.addAll(libraryPaths);
            searchPaths.add("/QOpenSys/pkgs/lib");
            searchPaths.add("/QOpenSys/usr/lib");
            FilenameFilter filter = new FilenameFilter(){

                @Override
                public boolean accept(File dir, String name) {
                    return dotAorSoPattern.matcher(name).matches() || versionedLibPattern.matcher(name).matches();
                }
            };
            LinkedHashMap<String, int[]> matches = new LinkedHashMap<String, int[]>();
            for (String path : searchPaths) {
                Object libraryPath;
                File[] files;
                if (path.toLowerCase(LOCALE).startsWith("/qsys") || (files = ((File)(libraryPath = new File(path))).listFiles(filter)) == null) continue;
                for (File file : files) {
                    int[] version;
                    String versionString;
                    if (dotAorSoPattern.matcher(file.getName()).matches()) {
                        dotAorSoFiles.add(file);
                        continue;
                    }
                    Matcher matcher = versionedLibPattern.matcher(file.getName());
                    String string = versionString = matcher.matches() ? matcher.group(1) : "";
                    if (versionString == null || versionString.isEmpty()) {
                        version = new int[]{};
                    } else {
                        String[] parts = versionString.split("\\.");
                        version = new int[parts.length - 1];
                        for (int i = 1; i < parts.length; ++i) {
                            version[i - 1] = Integer.parseInt(parts[i]);
                        }
                    }
                    matches.put(file.getAbsolutePath(), version);
                }
            }
            int[] bestVersion = null;
            String bestMatch = null;
            for (Map.Entry entry : matches.entrySet()) {
                String file = (String)entry.getKey();
                int[] fileVersion = (int[])entry.getValue();
                if (Linux.compareVersions(fileVersion, bestVersion) <= 0) continue;
                bestMatch = file;
                bestVersion = fileVersion;
            }
            if (null != bestMatch) {
                return bestMatch;
            }
            if (!dotAorSoFiles.isEmpty()) {
                String qualifiedAorSo = ((File)dotAorSoFiles.get(0)).getAbsolutePath();
                if (qualifiedAorSo.endsWith(".a")) {
                    qualifiedAorSo = qualifiedAorSo + "(shr_64.o)";
                }
                return qualifiedAorSo;
            }
            return this.mapLibraryName(libName);
        }
    }

    private static class Unsupported
    extends Platform {
        public Unsupported(OS os) {
            super(os);
        }
    }

    private static final class Default
    extends Supported {
        public Default(OS os) {
            super(os);
        }
    }

    public static enum CPU {
        I386,
        X86_64,
        PPC,
        PPC64,
        PPC64LE,
        SPARC,
        SPARCV9,
        S390X,
        MIPS32,
        ARM,
        AARCH64,
        MIPS64EL,
        LOONGARCH64,
        RISCV64,
        UNKNOWN;

        public String toString() {
            return this.name().toLowerCase(LOCALE);
        }
    }

    private static final class SingletonHolder {
        static final Platform PLATFORM = Platform.access$000();

        private SingletonHolder() {
        }
    }

    private static class Supported
    extends Platform {
        public Supported(OS os) {
            super(os);
        }
    }
}

