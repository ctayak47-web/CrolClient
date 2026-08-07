
package jnr.posix;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import jnr.constants.platform.Errno;

public interface POSIXHandler {
    public void error(Errno var1, String var2);

    public void error(Errno var1, String var2, String var3);

    public void unimplementedError(String var1);

    public void warn(WARNING_ID var1, String var2, Object ... var3);

    public boolean isVerbose();

    public File getCurrentWorkingDirectory();

    public String[] getEnv();

    public InputStream getInputStream();

    public PrintStream getOutputStream();

    public int getPID();

    public PrintStream getErrorStream();

    public static enum WARNING_ID {
        DUMMY_VALUE_USED("DUMMY_VALUE_USED");

        private String messageID;

        private WARNING_ID(String messageID) {
            this.messageID = messageID;
        }
    }
}

