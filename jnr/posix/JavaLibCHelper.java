
package jnr.posix;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import jnr.constants.platform.Errno;
import jnr.posix.FileStat;
import jnr.posix.HANDLE;
import jnr.posix.JavaFileStat;
import jnr.posix.JavaPOSIX;
import jnr.posix.JavaPasswd;
import jnr.posix.JavaSecuredFile;
import jnr.posix.POSIXHandler;
import jnr.posix.Passwd;
import jnr.posix.util.Chmod;
import jnr.posix.util.ExecIt;
import jnr.posix.util.JavaCrypt;
import jnr.posix.util.Platform;

public class JavaLibCHelper {
    public static final int STDIN = 0;
    public static final int STDOUT = 1;
    public static final int STDERR = 2;
    private static final ThreadLocal<Integer> errno = new ThreadLocal();
    private final POSIXHandler handler;
    private final Map<String, String> env;
    ThreadLocal<Integer> pwIndex = new ThreadLocal<Integer>(){

        @Override
        protected Integer initialValue() {
            return 0;
        }
    };

    public JavaLibCHelper(POSIXHandler handler) {
        this.env = new HashMap<String, String>();
        this.handler = handler;
    }

    public static FileDescriptor getDescriptorFromChannel(Channel channel) {
        if (ReflectiveAccess.SEL_CH_IMPL_GET_FD != null && ReflectiveAccess.SEL_CH_IMPL.isInstance(channel)) {
            try {
                return (FileDescriptor)ReflectiveAccess.SEL_CH_IMPL_GET_FD.invoke((Object)channel, new Object[0]);
            }
            catch (Exception exception) {
            }
        } else if (ReflectiveAccess.FILE_CHANNEL_IMPL_FD != null && ReflectiveAccess.FILE_CHANNEL_IMPL.isInstance(channel)) {
            try {
                return (FileDescriptor)ReflectiveAccess.FILE_CHANNEL_IMPL_FD.get(channel);
            }
            catch (Exception exception) {
            }
        } else if (ReflectiveAccess.FILE_DESCRIPTOR_FD != null) {
            FileDescriptor unixFD = new FileDescriptor();
            try {
                Method getFD = channel.getClass().getMethod("getFD", new Class[0]);
                ReflectiveAccess.FILE_DESCRIPTOR_FD.set(unixFD, (Integer)getFD.invoke((Object)channel, new Object[0]));
                return unixFD;
            }
            catch (Exception exception) {
                
            }
        }
        return new FileDescriptor();
    }

    static int errno() {
        Integer errno = JavaLibCHelper.errno.get();
        return errno != null ? errno : 0;
    }

    static void errno(int errno) {
        JavaLibCHelper.errno.set(errno);
    }

    static void errno(Errno errno) {
        JavaLibCHelper.errno.set(errno.intValue());
    }

    public int chmod(String filename, int mode) {
        return Chmod.chmod(new JavaSecuredFile(filename), Integer.toOctalString(mode));
    }

    public int chown(String filename, int user, int group) {
        PosixExec launcher = new PosixExec(this.handler);
        int chownResult = -1;
        int chgrpResult = -1;
        try {
            if (user != -1) {
                chownResult = launcher.runAndWait("chown", "" + user, filename);
            }
            if (group != -1) {
                chgrpResult = launcher.runAndWait("chgrp ", "" + user, filename);
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        catch (Exception exception) {
            
        }
        return chownResult != -1 && chgrpResult != -1 ? 0 : 1;
    }

    public static CharSequence crypt(CharSequence original, CharSequence salt) {
        return JavaCrypt.crypt(original, salt);
    }

    public static byte[] crypt(byte[] original, byte[] salt) {
        return JavaCrypt.crypt(new String(original), new String(salt)).toString().getBytes();
    }

    public int getfd(FileDescriptor descriptor2) {
        return JavaLibCHelper.getfdFromDescriptor(descriptor2);
    }

    public static int getfdFromDescriptor(FileDescriptor descriptor2) {
        if (descriptor2 == null || ReflectiveAccess.FILE_DESCRIPTOR_FD == null) {
            return -1;
        }
        try {
            return ReflectiveAccess.FILE_DESCRIPTOR_FD.getInt(descriptor2);
        }
        catch (SecurityException securityException) {
        }
        catch (IllegalArgumentException illegalArgumentException) {
        }
        catch (IllegalAccessException illegalAccessException) {
            
        }
        return -1;
    }

    public static HANDLE gethandle(FileDescriptor descriptor2) {
        if (descriptor2 == null || ReflectiveAccess.FILE_DESCRIPTOR_HANDLE == null) {
            return HANDLE.valueOf(-1L);
        }
        try {
            return JavaLibCHelper.gethandle(ReflectiveAccess.FILE_DESCRIPTOR_HANDLE.getLong(descriptor2));
        }
        catch (SecurityException securityException) {
        }
        catch (IllegalArgumentException illegalArgumentException) {
        }
        catch (IllegalAccessException illegalAccessException) {
            
        }
        return HANDLE.valueOf(-1L);
    }

    public static HANDLE gethandle(long descriptor2) {
        return HANDLE.valueOf(descriptor2);
    }

    public String getlogin() {
        return System.getProperty("user.name");
    }

    public String gethostname() {
        String hn = System.getenv("HOSTNAME");
        if (hn == null) {
            hn = System.getenv("COMPUTERNAME");
        }
        return hn;
    }

    public int getpid() {
        try {
            return this.handler.getPID();
        }
        catch (UnsupportedOperationException uoe) {
            try {
                Class<?> processHandle = Class.forName("java.lang.ProcessHandle");
                Object current = processHandle.getMethod("current", new Class[0]).invoke(null, new Object[0]);
                return (int)((Long)processHandle.getMethod("pid", new Class[0]).invoke(current, new Object[0])).longValue();
            }
            catch (Exception processHandle) {
                try {
                    String runtimeName = ManagementFactory.getRuntimeMXBean().getName();
                    int index = runtimeName.indexOf(64);
                    if (index > 0) {
                        return (int)Long.parseLong(runtimeName.substring(0, index));
                    }
                }
                catch (Exception exception) {
                    
                }
                throw uoe;
            }
        }
    }

    public Passwd getpwent() {
        JavaPasswd retVal = this.pwIndex.get() == 0 ? new JavaPasswd(this.handler) : null;
        this.pwIndex.set(this.pwIndex.get() + 1);
        return retVal;
    }

    public int setpwent() {
        return 0;
    }

    public int endpwent() {
        this.pwIndex.set(0);
        return 0;
    }

    public Passwd getpwuid(int which) {
        return which == JavaPOSIX.LoginInfo.UID ? new JavaPasswd(this.handler) : null;
    }

    public int isatty(int fd) {
        return fd == 1 || fd == 0 || fd == 2 ? 1 : 0;
    }

    public int link(String oldpath, String newpath) {
        try {
            return new PosixExec(this.handler).runAndWait("ln", oldpath, newpath);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        catch (Exception exception) {
            
        }
        JavaLibCHelper.errno(Errno.EINVAL);
        return -1;
    }

    public int lstat(String path, FileStat stat) {
        JavaSecuredFile file = new JavaSecuredFile(path);
        if (!((File)file).exists()) {
            JavaLibCHelper.errno(Errno.ENOENT);
            return -1;
        }
        JavaFileStat jstat = (JavaFileStat)stat;
        jstat.setup(path);
        return 0;
    }

    public int mkdir(String path, int mode) {
        JavaSecuredFile dir = new JavaSecuredFile(path);
        if (!((File)dir).mkdir()) {
            return -1;
        }
        this.chmod(path, mode);
        return 0;
    }

    public int rmdir(String path) {
        return new JavaSecuredFile(path).delete() ? 0 : -1;
    }

    public static int chdir(String path) {
        System.setProperty("user.dir", path);
        return 0;
    }

    public int stat(String path, FileStat stat) {
        JavaFileStat jstat = (JavaFileStat)stat;
        try {
            JavaSecuredFile file = new JavaSecuredFile(path);
            if (!((File)file).exists()) {
                JavaLibCHelper.errno(Errno.ENOENT);
                return -1;
            }
            jstat.setup(((File)file).getCanonicalPath());
        }
        catch (IOException iOException) {
            
        }
        return 0;
    }

    public int symlink(String oldpath, String newpath) {
        try {
            return new PosixExec(this.handler).runAndWait("ln", "-s", oldpath, newpath);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        catch (Exception exception) {
            
        }
        JavaLibCHelper.errno(Errno.EEXIST);
        return -1;
    }

    public int readlink(String oldpath, ByteBuffer buffer, int length) throws IOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            new PosixExec(this.handler).runAndWait((OutputStream)baos, "readlink", oldpath);
            byte[] bytes = baos.toByteArray();
            if (bytes.length > length || bytes.length == 0) {
                return -1;
            }
            buffer.put(bytes, 0, bytes.length - 1);
            return buffer.position();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            JavaLibCHelper.errno(Errno.ENOENT);
            return -1;
        }
    }

    public Map<String, String> getEnv() {
        return this.env;
    }

    public static FileDescriptor toFileDescriptor(int fileDescriptor) {
        FileDescriptor descriptor2 = new FileDescriptor();
        try {
            ReflectiveAccess.FILE_DESCRIPTOR_FD.set(descriptor2, fileDescriptor);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return descriptor2;
    }

    public static FileDescriptor toFileDescriptor(HANDLE fileDescriptor) {
        FileDescriptor descriptor2 = new FileDescriptor();
        try {
            ReflectiveAccess.FILE_DESCRIPTOR_HANDLE.set(descriptor2, fileDescriptor.toPointer().address());
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return descriptor2;
    }

    private static class ReflectiveAccess {
        private static final Class SEL_CH_IMPL;
        private static final Method SEL_CH_IMPL_GET_FD;
        private static final Class FILE_CHANNEL_IMPL;
        private static final Field FILE_CHANNEL_IMPL_FD;
        private static final Field FILE_DESCRIPTOR_FD;
        private static final Field FILE_DESCRIPTOR_HANDLE;

        private ReflectiveAccess() {
        }

        static {
            Field ffd;
            Field fd;
            Class<?> fileChannelImpl;
            Method getFD;
            Class<?> selChImpl;
            try {
                selChImpl = Class.forName("sun.nio.ch.SelChImpl");
                try {
                    getFD = selChImpl.getMethod("getFD", new Class[0]);
                    getFD.setAccessible(true);
                }
                catch (Exception e) {
                    getFD = null;
                }
            }
            catch (Exception e) {
                selChImpl = null;
                getFD = null;
            }
            SEL_CH_IMPL = selChImpl;
            SEL_CH_IMPL_GET_FD = getFD;
            try {
                fileChannelImpl = Class.forName("sun.nio.ch.FileChannelImpl");
                try {
                    fd = fileChannelImpl.getDeclaredField("fd");
                    fd.setAccessible(true);
                }
                catch (Exception e) {
                    fd = null;
                }
            }
            catch (Exception e) {
                fileChannelImpl = null;
                fd = null;
            }
            FILE_CHANNEL_IMPL = fileChannelImpl;
            FILE_CHANNEL_IMPL_FD = fd;
            try {
                ffd = FileDescriptor.class.getDeclaredField("fd");
                ffd.setAccessible(true);
            }
            catch (Exception e) {
                ffd = null;
            }
            FILE_DESCRIPTOR_FD = ffd;
            if (Platform.IS_WINDOWS) {
                Field handle;
                try {
                    handle = FileDescriptor.class.getDeclaredField("handle");
                    handle.setAccessible(true);
                }
                catch (Exception e) {
                    handle = null;
                }
                FILE_DESCRIPTOR_HANDLE = handle;
            } else {
                FILE_DESCRIPTOR_HANDLE = null;
            }
        }
    }

    private static class PosixExec
    extends ExecIt {
        private final AtomicReference<Errno> errno = new AtomicReference<Errno>(Errno.EINVAL);
        private final ErrnoParsingOutputStream errorStream = new ErrnoParsingOutputStream(this.errno);

        public PosixExec(POSIXHandler handler) {
            super(handler);
        }

        private int parseResult(int result) {
            if (result == 0) {
                return result;
            }
            JavaLibCHelper.errno(this.errno.get());
            return -1;
        }

        @Override
        public int runAndWait(String ... args) throws IOException, InterruptedException {
            return this.runAndWait((OutputStream)this.handler.getOutputStream(), this.errorStream, args);
        }

        @Override
        public int runAndWait(OutputStream output, String ... args) throws IOException, InterruptedException {
            return this.runAndWait(output, this.errorStream, args);
        }

        @Override
        public int runAndWait(OutputStream output, OutputStream error, String ... args) throws IOException, InterruptedException {
            return this.parseResult(super.runAndWait(output, error, args));
        }
    }

    private static final class ErrnoParsingOutputStream
    extends OutputStream {
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        private final AtomicReference<Errno> errno;
        static Map<Pattern, Errno> errorPatterns = new HashMap<Pattern, Errno>();

        private ErrnoParsingOutputStream(AtomicReference<Errno> errno) {
            this.errno = errno;
        }

        @Override
        public void write(int b) throws IOException {
            if (b != 13 && b != 10 && b != -1) {
                this.baos.write(b);
            } else if (this.baos.size() > 0) {
                String errorString = this.baos.toString();
                this.baos.reset();
                this.parseError(errorString);
            }
        }

        void parseError(String errorString) {
            for (Map.Entry<Pattern, Errno> entry : errorPatterns.entrySet()) {
                if (!entry.getKey().matcher(errorString).find()) continue;
                this.errno.set(entry.getValue());
            }
        }

        static {
            errorPatterns.put(Pattern.compile("File exists"), Errno.EEXIST);
            errorPatterns.put(Pattern.compile("Operation not permitted"), Errno.EPERM);
            errorPatterns.put(Pattern.compile("No such file or directory"), Errno.ENOENT);
            errorPatterns.put(Pattern.compile("Input/output error"), Errno.EIO);
            errorPatterns.put(Pattern.compile("Not a directory"), Errno.ENOTDIR);
            errorPatterns.put(Pattern.compile("No space left on device"), Errno.ENOSPC);
            errorPatterns.put(Pattern.compile("Read-only file system"), Errno.EROFS);
            errorPatterns.put(Pattern.compile("Too many links"), Errno.EMLINK);
        }
    }
}

