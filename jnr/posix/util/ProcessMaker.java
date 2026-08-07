
package jnr.posix.util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ProcessMaker {
    public List<String> command();

    public ProcessMaker command(List<String> var1);

    public ProcessMaker command(String ... var1);

    public File directory();

    public ProcessMaker directory(File var1);

    public Map<String, String> environment();

    public ProcessMaker environment(String[] var1);

    public ProcessMaker inheritIO();

    public Redirect redirectError();

    public ProcessMaker redirectError(File var1);

    public ProcessMaker redirectError(Redirect var1);

    public boolean redirectErrorStream();

    public ProcessMaker redirectErrorStream(boolean var1);

    public Redirect redirectInput();

    public ProcessMaker redirectInput(File var1);

    public ProcessMaker redirectInput(Redirect var1);

    public Redirect redirectOutput();

    public ProcessMaker redirectOutput(File var1);

    public ProcessMaker redirectOutput(Redirect var1);

    public Process start() throws IOException;

    public static class Redirect {
        public static final Redirect INHERIT = new Redirect(Type.INHERIT);
        public static final Redirect PIPE = new Redirect(Type.PIPE);
        private final Type type;
        private final File file;

        private Redirect(Type type) {
            this(type, null);
        }

        private Redirect(Type type, File file) {
            this.type = type;
            this.file = file;
        }

        public static Redirect appendTo(File file) {
            return new Redirect(Type.APPEND, file);
        }

        public static Redirect from(File file) {
            return new Redirect(Type.READ, file);
        }

        public static Redirect to(File file) {
            return new Redirect(Type.WRITE, file);
        }

        public File file() {
            return this.file;
        }

        public Type type() {
            return this.type;
        }

        private static enum Type {
            APPEND,
            INHERIT,
            PIPE,
            READ,
            WRITE;

        }
    }
}

