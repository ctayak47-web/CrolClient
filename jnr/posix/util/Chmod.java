
package jnr.posix.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Chmod {
    private static final boolean CHMOD_API_AVAILABLE;
    private static final Method setWritable;
    private static final Method setReadable;
    private static final Method setExecutable;

    public static int chmod(File file, String mode) {
        if (CHMOD_API_AVAILABLE) {
            char other = '0';
            if (mode.length() >= 1) {
                other = mode.charAt(mode.length() - 1);
            }
            char user = '0';
            if (mode.length() >= 3) {
                user = mode.charAt(mode.length() - 3);
            }
            if (!Chmod.setPermissions(file, other, false)) {
                return -1;
            }
            if (!Chmod.setPermissions(file, user, true)) {
                return -1;
            }
            return 0;
        }
        try {
            Process chmod = Runtime.getRuntime().exec("/bin/chmod " + mode + " " + file.getAbsolutePath());
            chmod.waitFor();
            return chmod.exitValue();
        }
        catch (IOException chmod) {
        }
        catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        return -1;
    }

    private static boolean setPermissions(File file, char permChar, boolean userOnly) {
        int permValue = Character.digit(permChar, 8);
        try {
            if ((permValue & 1) != 0) {
                setExecutable.invoke((Object)file, Boolean.TRUE, userOnly);
            } else {
                setExecutable.invoke((Object)file, Boolean.FALSE, userOnly);
            }
            if ((permValue & 2) != 0) {
                setWritable.invoke((Object)file, Boolean.TRUE, userOnly);
            } else {
                setWritable.invoke((Object)file, Boolean.FALSE, userOnly);
            }
            if ((permValue & 4) != 0) {
                setReadable.invoke((Object)file, Boolean.TRUE, userOnly);
            } else {
                setReadable.invoke((Object)file, Boolean.FALSE, userOnly);
            }
            return true;
        }
        catch (IllegalAccessException illegalAccessException) {
        }
        catch (InvocationTargetException invocationTargetException) {
            
        }
        return false;
    }

    static {
        boolean apiAvailable = false;
        Method setWritableVar = null;
        Method setReadableVar = null;
        Method setExecutableVar = null;
        try {
            setWritableVar = File.class.getMethod("setWritable", Boolean.TYPE, Boolean.TYPE);
            setReadableVar = File.class.getMethod("setReadable", Boolean.TYPE, Boolean.TYPE);
            setExecutableVar = File.class.getMethod("setExecutable", Boolean.TYPE, Boolean.TYPE);
            apiAvailable = true;
        }
        catch (Exception exception) {
            
        }
        setWritable = setWritableVar;
        setReadable = setReadableVar;
        setExecutable = setExecutableVar;
        CHMOD_API_AVAILABLE = apiAvailable;
    }
}

