
package com.kenai.jffi;

import com.kenai.jffi.Foreign;
import com.kenai.jffi.Util;
import java.util.Locale;

public abstract class Platform {
    private static final Locale LOCALE = Locale.ENGLISH;
    private final OS os;
    private final int javaVersionMajor;

    private static final OS determineOS() {
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
        if (Platform.startsWithIgnoreCase(osName, "os/400") || Platform.startsWithIgnoreCase(osName, "os400")) {
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
        return OS.UNKNOWN;
    }

    private static final Platform determinePlatform(OS os) {
        switch (os) {
            case DARWIN: {
                return Platform.newDarwinPlatform();
            }
            case WINDOWS: {
                return Platform.newWindowsPlatform();
            }
        }
        return Platform.newDefaultPlatform(os);
    }

    private static Platform newDarwinPlatform() {
        return new Darwin();
    }

    private static Platform newWindowsPlatform() {
        return new Windows();
    }

    private static Platform newDefaultPlatform(OS os) {
        return new Default(os);
    }

    private Platform(OS os) {
        this.os = os;
        int version = 8;
        try {
            String versionString = System.getProperty("java.version");
            if (versionString != null) {
                String v = versionString.split("[^0-9.]")[0];
                int dot = v.indexOf(46);
                if (dot != -1) {
                    v = v.substring(dot + 1);
                }
                version = Integer.valueOf(v);
            }
        }
        catch (Exception ex) {
            version = 8;
        }
        this.javaVersionMajor = version;
    }

    public static final Platform getPlatform() {
        return SingletonHolder.PLATFORM;
    }

    public final OS getOS() {
        return this.os;
    }

    public final CPU getCPU() {
        return ArchHolder.cpu;
    }

    public final int getJavaMajorVersion() {
        return this.javaVersionMajor;
    }

    public abstract int longSize();

    public final int addressSize() {
        return this.getCPU().dataModel;
    }

    public final long addressMask() {
        return this.getCPU().addressMask;
    }

    public String getName() {
        String osName = System.getProperty("os.name").split(" ")[0];
        return this.getCPU().name().toLowerCase(LOCALE) + "-" + osName;
    }

    public String mapLibraryName(String libName) {
        if (libName.matches(this.getLibraryNamePattern())) {
            return libName;
        }
        if (OS.IBMI.equals((Object)this.getOS())) {
            return "lib" + libName + ".so";
        }
        return System.mapLibraryName(libName);
    }

    public String getLibraryNamePattern() {
        return "lib.*\\.so.*$";
    }

    public boolean isSupported() {
        int version = Foreign.getInstance().getVersion();
        if ((version & 0xFFFF00) == (Foreign.VERSION_MAJOR << 16 | Foreign.VERSION_MINOR << 8)) {
            return true;
        }
        throw new UnsatisfiedLinkError("Incorrect native library version");
    }

    private static boolean startsWithIgnoreCase(String s1, String s2) {
        return s1.startsWith(s2) || s1.toUpperCase(LOCALE).startsWith(s2.toUpperCase(LOCALE)) || s1.toLowerCase(LOCALE).startsWith(s2.toLowerCase(LOCALE));
    }

    static /* synthetic */ OS access$100() {
        return Platform.determineOS();
    }

    static /* synthetic */ Platform access$200(OS x0) {
        return Platform.determinePlatform(x0);
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
        UNKNOWN;

        public String toString() {
            return this.name().toLowerCase(LOCALE);
        }
    }

    private static final class Darwin
    extends Platform {
        public Darwin() {
            super(OS.DARWIN);
        }

        @Override
        public String mapLibraryName(String libName) {
            if (libName.matches(this.getLibraryNamePattern())) {
                return libName;
            }
            return "lib" + libName + ".dylib";
        }

        @Override
        public String getLibraryNamePattern() {
            return "lib.*\\.(dylib|jnilib)$";
        }

        @Override
        public String getName() {
            return "Darwin";
        }

        @Override
        public final int longSize() {
            return this.getCPU().dataModel;
        }
    }

    private static final class Windows
    extends Platform {
        public Windows() {
            super(OS.WINDOWS);
        }

        @Override
        public String getLibraryNamePattern() {
            return ".*\\.dll$";
        }

        @Override
        public final int longSize() {
            return 32;
        }
    }

    private static final class Default
    extends Platform {
        public Default(OS os) {
            super(os);
        }

        @Override
        public final int longSize() {
            return this.getCPU().dataModel;
        }
    }

    private static final class SingletonHolder {
        static final Platform PLATFORM = Platform.access$200(Platform.access$100());

        private SingletonHolder() {
        }
    }

    private static final class ArchHolder {
        public static final CPU cpu = ArchHolder.determineCPU();

        private ArchHolder() {
        }

        private static CPU determineCPU() {
            String archString = null;
            try {
                archString = Foreign.getInstance().getArch();
            }
            catch (UnsatisfiedLinkError unsatisfiedLinkError) {
                
            }
            if (archString == null || "unknown".equals(archString)) {
                archString = System.getProperty("os.arch", "unknown");
            }
            if (Util.equalsIgnoreCase("x86", archString, LOCALE) || Util.equalsIgnoreCase("i386", archString, LOCALE) || Util.equalsIgnoreCase("i86pc", archString, LOCALE)) {
                return CPU.I386;
            }
            if (Util.equalsIgnoreCase("x86_64", archString, LOCALE) || Util.equalsIgnoreCase("amd64", archString, LOCALE)) {
                return CPU.X86_64;
            }
            if (Util.equalsIgnoreCase("ppc", archString, LOCALE) || Util.equalsIgnoreCase("powerpc", archString, LOCALE)) {
                return CPU.PPC;
            }
            if (Util.equalsIgnoreCase("ppc64", archString, LOCALE) || Util.equalsIgnoreCase("powerpc64", archString, LOCALE)) {
                return CPU.PPC64;
            }
            if (Util.equalsIgnoreCase("ppc64le", archString, LOCALE) || Util.equalsIgnoreCase("powerpc64le", archString, LOCALE)) {
                return CPU.PPC64LE;
            }
            if (Util.equalsIgnoreCase("s390", archString, LOCALE) || Util.equalsIgnoreCase("s390x", archString, LOCALE)) {
                return CPU.S390X;
            }
            if (Util.equalsIgnoreCase("arm", archString, LOCALE) || Util.equalsIgnoreCase("armv7l", archString, LOCALE)) {
                return CPU.ARM;
            }
            if (Util.equalsIgnoreCase("aarch64", archString, LOCALE)) {
                return CPU.AARCH64;
            }
            if (Util.equalsIgnoreCase("loongarch64", archString, LOCALE)) {
                return CPU.LOONGARCH64;
            }
            if (Util.equalsIgnoreCase("mipsel", archString, LOCALE)) {
                return CPU.MIPSEL;
            }
            if (Util.equalsIgnoreCase("mips64", archString, LOCALE) || Util.equalsIgnoreCase("mips64el", archString, LOCALE)) {
                return CPU.MIPS64EL;
            }
            if (Util.equalsIgnoreCase("riscv64", archString, LOCALE)) {
                return CPU.RISCV64;
            }
            for (CPU cpu : CPU.values()) {
                if (!cpu.name().equalsIgnoreCase(archString)) continue;
                return cpu;
            }
            return CPU.UNKNOWN;
        }
    }

    public static enum CPU {
        I386(32),
        X86_64(64),
        PPC(32),
        PPC64(64),
        PPC64LE(64),
        SPARC(32),
        SPARCV9(64),
        S390X(64),
        ARM(32),
        AARCH64(64),
        LOONGARCH64(64),
        MIPSEL(32),
        MIPS64EL(64),
        RISCV64(64),
        UNKNOWN(64);

        public final int dataModel;
        public final long addressMask;

        private CPU(int dataModel) {
            this.dataModel = dataModel;
            this.addressMask = dataModel == 32 ? 0xFFFFFFFFL : -1L;
        }

        public String toString() {
            return this.name().toLowerCase(LOCALE);
        }
    }
}

