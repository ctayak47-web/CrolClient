
package com.kenai.jffi;

import com.kenai.jffi.Foreign;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.printf("jffi jar version=%d.%d.%d\n", Foreign.VERSION_MAJOR, Foreign.VERSION_MINOR, Foreign.VERSION_MICRO);
            Foreign f = Foreign.getInstance();
            System.out.printf("jffi stub version=%d.%d.%d\n", Main.v(f, 16), Main.v(f, 8), Main.v(f, 0));
            System.out.println("memory fault protection enabled=" + Foreign.isMemoryProtectionEnabled());
            System.out.println("stub arch=" + f.getArch());
            System.out.printf("JNI version=%#x\n", f.getJNIVersion());
        }
        catch (Throwable t) {
            System.err.println("Error: " + t);
        }
    }

    private static int v(Foreign foreign, int shift) {
        return foreign.getVersion() >> shift & 0xFF;
    }
}

