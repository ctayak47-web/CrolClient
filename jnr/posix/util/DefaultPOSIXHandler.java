
package jnr.posix.util;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jnr.constants.platform.Errno;
import jnr.posix.POSIXHandler;

public class DefaultPOSIXHandler
implements POSIXHandler {
    @Override
    public void error(Errno error, String extraData) {
        throw new RuntimeException("native error " + error.description() + " " + extraData);
    }

    @Override
    public void error(Errno error, String methodName, String extraData) {
        throw new RuntimeException("native error calling " + methodName + ": " + error.description() + " " + extraData);
    }

    @Override
    public void unimplementedError(String methodName) {
        throw new IllegalStateException(methodName + " is not implemented in jnr-posix");
    }

    @Override
    public void warn(POSIXHandler.WARNING_ID id, String message, Object ... data) {
        String msg;
        try {
            msg = String.format(message, data);
        }
        catch (IllegalFormatException e) {
            msg = message + " " + Arrays.toString(data);
        }
        Logger.getLogger("jnr-posix").log(Level.WARNING, msg);
    }

    @Override
    public boolean isVerbose() {
        return false;
    }

    @Override
    public File getCurrentWorkingDirectory() {
        return new File(".");
    }

    @Override
    public String[] getEnv() {
        String[] envp = new String[System.getenv().size()];
        int i = 0;
        for (Map.Entry<String, String> pair : System.getenv().entrySet()) {
            envp[i++] = pair.getKey() + "=" + pair.getValue();
        }
        return envp;
    }

    @Override
    public InputStream getInputStream() {
        return System.in;
    }

    @Override
    public PrintStream getOutputStream() {
        return System.out;
    }

    @Override
    public int getPID() {
        return 0;
    }

    @Override
    public PrintStream getErrorStream() {
        return System.err;
    }
}

