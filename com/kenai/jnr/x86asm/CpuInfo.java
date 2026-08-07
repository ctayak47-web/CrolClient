
package com.kenai.jnr.x86asm;

@Deprecated
public class CpuInfo {
    final Vendor vendor;
    final int family;
    public static final CpuInfo GENERIC = new CpuInfo(Vendor.GENERIC, 0);

    public CpuInfo(Vendor vendor, int family) {
        this.vendor = vendor;
        this.family = family;
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    public static enum Vendor {
        INTEL,
        AMD,
        GENERIC;

    }
}

