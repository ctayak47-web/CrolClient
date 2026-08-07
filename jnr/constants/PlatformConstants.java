
package jnr.constants;

import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

public final class PlatformConstants {
    private static final PlatformConstants INSTANCE = new PlatformConstants();
    public static final boolean FAKE = Boolean.valueOf(System.getProperty("jnr.constants.fake", "true"));
    public static final Map<String, String> OS_NAMES = new HashMap<String, String>(){
        public static final long serialVersionUID = 1L;
        {
            this.put("Mac OS X", "darwin");
            this.put("SunOS", "solaris");
        }
    };
    public static final Map<String, String> ARCH_NAMES = new HashMap<String, String>(){
        public static final long serialVersionUID = 1L;
        {
            this.put("x86", "i386");
        }
    };
    public static final String ARCH = PlatformConstants.initArchitecture();
    public static final String OS = PlatformConstants.initOperatingSystem();
    public static final String NAME = String.format("%s-%s", ARCH, OS);
    public static final int BIG_ENDIAN = 4321;
    public static final int LITTLE_ENDIAN = 1234;
    public static final int BYTE_ORDER = ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN) ? 4321 : 1234;

    public static PlatformConstants getPlatform() {
        return INSTANCE;
    }

    private PlatformConstants() {
    }

    private static String getConstantsPackageName() {
        return PackageNameResolver.PACKAGE_NAME;
    }

    public String[] getPackagePrefixes() {
        if (FAKE) {
            return new String[]{this.getArchPackageName(), this.getOSPackageName(), this.getFakePackageName()};
        }
        return new String[]{this.getArchPackageName(), this.getOSPackageName()};
    }

    public String getArchPackageName() {
        return String.format("%s.platform.%s.%s", PlatformConstants.getConstantsPackageName(), OS, ARCH);
    }

    public String getOSPackageName() {
        return String.format("%s.platform.%s", PlatformConstants.getConstantsPackageName(), OS);
    }

    public String getFakePackageName() {
        return String.format("%s.platform.fake", PlatformConstants.getConstantsPackageName());
    }

    private static String initOperatingSystem() {
        String osname = PlatformConstants.getProperty("os.name", "unknown").toLowerCase();
        for (String s : OS_NAMES.keySet()) {
            if (!s.equalsIgnoreCase(osname)) continue;
            return OS_NAMES.get(s);
        }
        if (osname.startsWith("windows")) {
            return "windows";
        }
        return osname;
    }

    private static final String initArchitecture() {
        String arch = PlatformConstants.getProperty("os.arch", "unknown").toLowerCase();
        for (String s : ARCH_NAMES.keySet()) {
            if (!s.equalsIgnoreCase(arch)) continue;
            return ARCH_NAMES.get(s);
        }
        return arch;
    }

    private static String getProperty(String property, String defValue) {
        try {
            return System.getProperty(property, defValue);
        }
        catch (SecurityException se) {
            return defValue;
        }
    }

    private static final class PackageNameResolver {
        public static final String PACKAGE_NAME = new PackageNameResolver().inferPackageName();

        private PackageNameResolver() {
        }

        private String inferPackageName() {
            try {
                Class<?> cls = this.getClass();
                Package pkg = cls.getPackage();
                return pkg != null ? pkg.getName() : cls.getName().substring(0, cls.getName().lastIndexOf(46));
            }
            catch (NullPointerException npe) {
                return "jnr.constants";
            }
        }
    }
}

