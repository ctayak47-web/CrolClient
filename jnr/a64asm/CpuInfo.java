
package jnr.a64asm;

public class CpuInfo {
    final Vendor vendor;
    final int family;
    public static final CpuInfo GENERIC = new CpuInfo(Vendor.GENERIC, 0);

    public CpuInfo(Vendor vendor, int family) {
        this.vendor = vendor;
        this.family = family;
    }

    public static enum Vendor {
        INTEL,
        AMD,
        ARM,
        GENERIC;

    }
}

