
package jnr.posix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;
import jnr.constants.platform.Confstr;
import jnr.constants.platform.Errno;
import jnr.constants.platform.Fcntl;
import jnr.constants.platform.Pathconf;
import jnr.constants.platform.Signal;
import jnr.constants.platform.Sysconf;
import jnr.ffi.Pointer;
import jnr.posix.FileStat;
import jnr.posix.Group;
import jnr.posix.JavaFileStat;
import jnr.posix.JavaLibCHelper;
import jnr.posix.JavaTimes;
import jnr.posix.LibC;
import jnr.posix.MsgHdr;
import jnr.posix.POSIX;
import jnr.posix.POSIXHandler;
import jnr.posix.Passwd;
import jnr.posix.RLimit;
import jnr.posix.SignalHandler;
import jnr.posix.SocketMacros;
import jnr.posix.SpawnAttribute;
import jnr.posix.SpawnFileAction;
import jnr.posix.Times;
import jnr.posix.Timeval;
import jnr.posix.util.Java5ProcessMaker;
import jnr.posix.util.MethodName;
import jnr.posix.util.Platform;
import jnr.posix.util.ProcessMaker;
import jnr.posix.util.SunMiscSignal;

final class JavaPOSIX
implements POSIX {
    private final POSIXHandler handler;
    private final JavaLibCHelper helper;

    JavaPOSIX(POSIXHandler handler) {
        this.handler = handler;
        this.helper = new JavaLibCHelper(handler);
    }

    @Override
    public ProcessMaker newProcessMaker(String ... command) {
        return new Java5ProcessMaker(this.handler, command);
    }

    @Override
    public ProcessMaker newProcessMaker() {
        return new Java5ProcessMaker(this.handler);
    }

    @Override
    public FileStat allocateStat() {
        return new JavaFileStat(this, this.handler);
    }

    @Override
    public MsgHdr allocateMsgHdr() {
        this.handler.unimplementedError(MethodName.getCallerMethodName());
        return null;
    }

    public SocketMacros socketMacros() {
        this.handler.unimplementedError(MethodName.getCallerMethodName());
        return null;
    }

    @Override
    public int chmod(String filename, int mode) {
        return this.helper.chmod(filename, mode);
    }

    @Override
    public int fchmod(int fd, int mode) {
        this.handler.unimplementedError("No fchmod in Java (yet)");
        return -1;
    }

    @Override
    public int chown(String filename, int user, int group) {
        return this.helper.chown(filename, user, group);
    }

    @Override
    public int fchown(int fd, int user, int group) {
        this.handler.unimplementedError("No fchown in Java (yet)");
        return -1;
    }

    @Override
    public CharSequence crypt(CharSequence key, CharSequence salt) {
        return JavaLibCHelper.crypt(key, salt);
    }

    @Override
    public byte[] crypt(byte[] key, byte[] salt) {
        return JavaLibCHelper.crypt(key, salt);
    }

    @Override
    public int exec(String path, String ... argv) {
        this.handler.unimplementedError("No exec in Java (yet)");
        return -1;
    }

    @Override
    public int exec(String path, String[] argv, String[] envp) {
        this.handler.unimplementedError("No exec in Java (yet)");
        return -1;
    }

    @Override
    public int execv(String path, String[] argv) {
        this.handler.unimplementedError("No execv in Java (yet)");
        return -1;
    }

    @Override
    public int execve(String path, String[] argv, String[] envp) {
        this.handler.unimplementedError("No execve in Java (yet)");
        return -1;
    }

    @Override
    public FileStat fstat(FileDescriptor descriptor2) {
        this.handler.unimplementedError("fstat unimplemented");
        return null;
    }

    @Override
    public FileStat fstat(int descriptor2) {
        this.handler.unimplementedError("fstat unimplemented");
        return null;
    }

    @Override
    public int fstat(int fd, FileStat stat) {
        this.handler.unimplementedError("fstat unimplemented");
        return -1;
    }

    @Override
    public int fstat(FileDescriptor descriptor2, FileStat stat) {
        this.handler.unimplementedError("fstat unimplemented");
        return -1;
    }

    @Override
    public int getegid() {
        return LoginInfo.GID;
    }

    @Override
    public int geteuid() {
        return LoginInfo.UID;
    }

    @Override
    public int getgid() {
        return LoginInfo.GID;
    }

    @Override
    public int getdtablesize() {
        this.handler.unimplementedError("getdtablesize unimplemented");
        return -1;
    }

    @Override
    public String getlogin() {
        return this.helper.getlogin();
    }

    @Override
    public int getpgid() {
        return this.unimplementedInt("getpgid");
    }

    @Override
    public int getpgrp() {
        return this.unimplementedInt("getpgrp");
    }

    @Override
    public int getpid() {
        return this.helper.getpid();
    }

    @Override
    public int getppid() {
        return this.unimplementedInt("getppid");
    }

    @Override
    public Passwd getpwent() {
        return this.helper.getpwent();
    }

    @Override
    public Passwd getpwuid(int which) {
        return this.helper.getpwuid(which);
    }

    @Override
    public Group getgrgid(int which) {
        this.handler.unimplementedError("getgrgid unimplemented");
        return null;
    }

    @Override
    public Passwd getpwnam(String which) {
        this.handler.unimplementedError("getpwnam unimplemented");
        return null;
    }

    @Override
    public Group getgrnam(String which) {
        this.handler.unimplementedError("getgrnam unimplemented");
        return null;
    }

    @Override
    public Group getgrent() {
        this.handler.unimplementedError("getgrent unimplemented");
        return null;
    }

    @Override
    public int setpwent() {
        return this.helper.setpwent();
    }

    @Override
    public int endpwent() {
        return this.helper.endpwent();
    }

    @Override
    public int setgrent() {
        return this.unimplementedInt("setgrent");
    }

    @Override
    public int endgrent() {
        return this.unimplementedInt("endgrent");
    }

    @Override
    public Pointer environ() {
        this.handler.unimplementedError("environ");
        return null;
    }

    @Override
    public String getenv(String envName) {
        return this.helper.getEnv().get(envName);
    }

    @Override
    public int getuid() {
        return LoginInfo.UID;
    }

    @Override
    public int getrlimit(int resource, RLimit rlim) {
        return this.unimplementedInt("getrlimit");
    }

    @Override
    public int getrlimit(int resource, Pointer rlim) {
        return this.unimplementedInt("getrlimit");
    }

    @Override
    public RLimit getrlimit(int resource) {
        this.handler.unimplementedError("getrlimit");
        return null;
    }

    @Override
    public int setrlimit(int resource, RLimit rlim) {
        return this.unimplementedInt("setrlimit");
    }

    @Override
    public int setrlimit(int resource, Pointer rlim) {
        return this.unimplementedInt("setrlimit");
    }

    @Override
    public int setrlimit(int resource, long rlimCur, long rlimMax) {
        return this.unimplementedInt("setrlimit");
    }

    @Override
    public int fork() {
        return -1;
    }

    @Override
    public boolean isatty(FileDescriptor fd) {
        return fd == FileDescriptor.in || fd == FileDescriptor.out || fd == FileDescriptor.err;
    }

    @Override
    public int isatty(int fd) {
        return fd == 0 || fd == 1 || fd == 2 ? 1 : 0;
    }

    @Override
    public int kill(int pid, int signal) {
        return this.unimplementedInt("kill");
    }

    @Override
    public int kill(long pid, int signal) {
        return this.unimplementedInt("kill");
    }

    @Override
    public SignalHandler signal(Signal sig, SignalHandler handler) {
        return SunMiscSignal.signal(sig, handler);
    }

    @Override
    public int raise(int sig) {
        return this.unimplementedInt("raise");
    }

    @Override
    public int lchmod(String filename, int mode) {
        return this.unimplementedInt("lchmod");
    }

    @Override
    public int lchown(String filename, int user, int group) {
        return this.unimplementedInt("lchown");
    }

    @Override
    public int link(String oldpath, String newpath) {
        return this.helper.link(oldpath, newpath);
    }

    @Override
    public FileStat lstat(String path) {
        FileStat stat = this.allocateStat();
        if (this.lstat(path, stat) < 0) {
            this.handler.error(Errno.ENOENT, "lstat", path);
        }
        return stat;
    }

    @Override
    public int lstat(String path, FileStat stat) {
        return this.helper.lstat(path, stat);
    }

    @Override
    public int mkdir(String path, int mode) {
        return this.helper.mkdir(path, mode);
    }

    @Override
    public int rmdir(String path) {
        return this.helper.rmdir(path);
    }

    @Override
    public String readlink(String path) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocateDirect(256);
        int result = this.helper.readlink(path, buffer, buffer.capacity());
        if (result == -1) {
            return null;
        }
        buffer.position(0);
        buffer.limit(result);
        return Charset.forName("ASCII").decode(buffer).toString();
    }

    @Override
    public int readlink(CharSequence path, byte[] buf, int bufsize) {
        this.handler.unimplementedError("readlink");
        return -1;
    }

    @Override
    public int readlink(CharSequence path, ByteBuffer buf, int bufsize) {
        this.handler.unimplementedError("readlink");
        return -1;
    }

    @Override
    public int readlink(CharSequence path, Pointer bufPtr, int bufsize) {
        this.handler.unimplementedError("readlink");
        return -1;
    }

    @Override
    public int setenv(String envName, String envValue, int overwrite) {
        Map<String, String> env = this.helper.getEnv();
        if (envName.contains("=")) {
            this.handler.error(Errno.EINVAL, "setenv", envName);
            return -1;
        }
        if (overwrite == 0 && env.containsKey(envName)) {
            return 0;
        }
        env.put(envName, envValue);
        return 0;
    }

    @Override
    public FileStat stat(String path) {
        FileStat stat = this.allocateStat();
        if (this.helper.stat(path, stat) < 0) {
            this.handler.error(Errno.ENOENT, "stat", path);
        }
        return stat;
    }

    @Override
    public int stat(String path, FileStat stat) {
        return this.helper.stat(path, stat);
    }

    @Override
    public int symlink(String oldpath, String newpath) {
        return this.helper.symlink(oldpath, newpath);
    }

    @Override
    public int setegid(int egid) {
        return this.unimplementedInt("setegid");
    }

    @Override
    public int seteuid(int euid) {
        return this.unimplementedInt("seteuid");
    }

    @Override
    public int setgid(int gid) {
        return this.unimplementedInt("setgid");
    }

    @Override
    public int getpgid(int pid) {
        return this.unimplementedInt("getpgid");
    }

    @Override
    public int setpgid(int pid, int pgid) {
        return this.unimplementedInt("setpgid");
    }

    @Override
    public int setpgrp(int pid, int pgrp) {
        return this.unimplementedInt("setpgrp");
    }

    @Override
    public int setsid() {
        return this.unimplementedInt("setsid");
    }

    @Override
    public int setuid(int uid) {
        return this.unimplementedInt("setuid");
    }

    @Override
    public int umask(int mask) {
        return 0;
    }

    @Override
    public int unsetenv(String envName) {
        if (this.helper.getEnv().remove(envName) == null) {
            this.handler.error(Errno.EINVAL, "unsetenv", envName);
            return -1;
        }
        return 0;
    }

    @Override
    public int utimes(String path, long[] atimeval, long[] mtimeval) {
        long mtimeMillis;
        if (mtimeval != null) {
            assert (mtimeval.length == 2);
            mtimeMillis = mtimeval[0] * 1000L + mtimeval[1] / 1000L;
        } else {
            mtimeMillis = System.currentTimeMillis();
        }
        new File(path).setLastModified(mtimeMillis);
        return 0;
    }

    @Override
    public int utimes(String path, Pointer times) {
        return this.unimplementedInt("utimes");
    }

    @Override
    public int futimes(int fd, long[] atimeval, long[] mtimeval) {
        this.handler.unimplementedError("futimes");
        return this.unimplementedInt("futimes");
    }

    @Override
    public int lutimes(String path, long[] atimeval, long[] mtimeval) {
        this.handler.unimplementedError("lutimes");
        return this.unimplementedInt("lutimes");
    }

    @Override
    public int utimensat(int dirfd, String path, long[] atimespec, long[] mtimespec, int flag) {
        long mtimeMillis;
        if (mtimespec != null) {
            assert (mtimespec.length == 2);
            mtimeMillis = mtimespec[0] * 1000L + mtimespec[1] / 1000000L;
        } else {
            mtimeMillis = System.currentTimeMillis();
        }
        new File(path).setLastModified(mtimeMillis);
        return 0;
    }

    @Override
    public int utimensat(int dirfd, String path, Pointer times, int flag) {
        return this.unimplementedInt("utimensat");
    }

    @Override
    public int futimens(int fd, long[] atimespec, long[] mtimespec) {
        this.handler.unimplementedError("futimens");
        return this.unimplementedInt("futimens");
    }

    @Override
    public int futimens(int fd, Pointer times) {
        this.handler.unimplementedError("futimens");
        return this.unimplementedInt("futimens");
    }

    @Override
    public int wait(int[] status) {
        return this.unimplementedInt("wait");
    }

    @Override
    public int waitpid(int pid, int[] status, int flags) {
        return this.unimplementedInt("waitpid");
    }

    @Override
    public int waitpid(long pid, int[] status, int flags) {
        return this.unimplementedInt("waitpid");
    }

    @Override
    public int getpriority(int which, int who) {
        return this.unimplementedInt("getpriority");
    }

    @Override
    public int setpriority(int which, int who, int prio) {
        return this.unimplementedInt("setpriority");
    }

    @Override
    public long posix_spawnp(String path, Collection<? extends SpawnFileAction> fileActions, Collection<? extends CharSequence> argv, Collection<? extends CharSequence> envp) {
        return this.unimplementedInt("posix_spawnp");
    }

    @Override
    public long posix_spawnp(String path, Collection<? extends SpawnFileAction> fileActions, Collection<? extends SpawnAttribute> spawnAttributes, Collection<? extends CharSequence> argv, Collection<? extends CharSequence> envp) {
        return this.unimplementedInt("posix_spawnp");
    }

    @Override
    public int errno() {
        return JavaLibCHelper.errno();
    }

    @Override
    public void errno(int value) {
        JavaLibCHelper.errno(value);
    }

    @Override
    public int chdir(String path) {
        return JavaLibCHelper.chdir(path);
    }

    @Override
    public boolean isNative() {
        return false;
    }

    @Override
    public LibC libc() {
        return null;
    }

    private int unimplementedInt(String message) {
        this.handler.unimplementedError(message);
        return -1;
    }

    @Override
    public long sysconf(Sysconf name) {
        switch (name) {
            case _SC_CLK_TCK: {
                return 1000L;
            }
        }
        this.errno(Errno.EOPNOTSUPP.intValue());
        return -1L;
    }

    @Override
    public int confstr(Confstr name, ByteBuffer buf, int len) {
        this.errno(Errno.EOPNOTSUPP.intValue());
        return -1;
    }

    @Override
    public int fpathconf(int fd, Pathconf name) {
        this.errno(Errno.EOPNOTSUPP.intValue());
        return -1;
    }

    @Override
    public Times times() {
        return new JavaTimes();
    }

    @Override
    public int flock(int fd, int mode) {
        return this.unimplementedInt("flock");
    }

    @Override
    public int dup(int fd) {
        return this.unimplementedInt("dup");
    }

    @Override
    public int dup2(int oldFd, int newFd) {
        return this.unimplementedInt("dup2");
    }

    @Override
    public int fcntlInt(int fd, Fcntl fcntlConst, int arg) {
        return this.unimplementedInt("fcntl");
    }

    @Override
    public int fcntl(int fd, Fcntl fcntlConst) {
        return this.unimplementedInt("fcntl");
    }

    @Override
    public int fcntl(int fd, Fcntl fcntlConst, int arg) {
        return this.unimplementedInt("fcntl");
    }

    @Override
    @Deprecated
    public int fcntl(int fd, Fcntl fcntlConst, int ... arg) {
        return this.unimplementedInt("fcntl");
    }

    @Override
    public int access(CharSequence path, int amode) {
        this.handler.unimplementedError("access");
        return -1;
    }

    @Override
    public int close(int fd) {
        return this.unimplementedInt("close");
    }

    @Override
    public int unlink(CharSequence path) {
        this.handler.unimplementedError("unlink");
        return -1;
    }

    @Override
    public int open(CharSequence path, int flags, int perm) {
        this.handler.unimplementedError("open");
        return -1;
    }

    @Override
    public long read(int fd, byte[] buf, long n) {
        this.handler.unimplementedError("read");
        return -1L;
    }

    @Override
    public long write(int fd, byte[] buf, long n) {
        this.handler.unimplementedError("write");
        return -1L;
    }

    @Override
    public long read(int fd, ByteBuffer buf, long n) {
        this.handler.unimplementedError("read");
        return -1L;
    }

    @Override
    public long write(int fd, ByteBuffer buf, long n) {
        this.handler.unimplementedError("write");
        return -1L;
    }

    @Override
    public long pread(int fd, byte[] buf, long n, long offset) {
        this.handler.unimplementedError("pread");
        return -1L;
    }

    @Override
    public long pwrite(int fd, byte[] buf, long n, long offset) {
        this.handler.unimplementedError("pwrite");
        return -1L;
    }

    @Override
    public long pread(int fd, ByteBuffer buf, long n, long offset) {
        this.handler.unimplementedError("pread");
        return -1L;
    }

    @Override
    public long pwrite(int fd, ByteBuffer buf, long n, long offset) {
        this.handler.unimplementedError("pwrite");
        return -1L;
    }

    @Override
    public int read(int fd, byte[] buf, int n) {
        this.handler.unimplementedError("read");
        return -1;
    }

    @Override
    public int write(int fd, byte[] buf, int n) {
        this.handler.unimplementedError("write");
        return -1;
    }

    @Override
    public int read(int fd, ByteBuffer buf, int n) {
        this.handler.unimplementedError("read");
        return -1;
    }

    @Override
    public int write(int fd, ByteBuffer buf, int n) {
        this.handler.unimplementedError("write");
        return -1;
    }

    @Override
    public int pread(int fd, byte[] buf, int n, int offset) {
        this.handler.unimplementedError("pread");
        return -1;
    }

    @Override
    public int pwrite(int fd, byte[] buf, int n, int offset) {
        this.handler.unimplementedError("pwrite");
        return -1;
    }

    @Override
    public int pread(int fd, ByteBuffer buf, int n, int offset) {
        this.handler.unimplementedError("pread");
        return -1;
    }

    @Override
    public int pwrite(int fd, ByteBuffer buf, int n, int offset) {
        this.handler.unimplementedError("pwrite");
        return -1;
    }

    @Override
    public int lseek(int fd, long offset, int whence) {
        this.handler.unimplementedError("lseek");
        return -1;
    }

    @Override
    public long lseekLong(int fd, long offset, int whence) {
        this.handler.unimplementedError("lseek");
        return -1L;
    }

    @Override
    public int pipe(int[] fds) {
        this.handler.unimplementedError("pipe");
        return -1;
    }

    @Override
    public int socketpair(int domain, int type, int protocol, int[] fds) {
        this.handler.unimplementedError("socketpair");
        return -1;
    }

    @Override
    public int sendmsg(int socket, MsgHdr message, int flags) {
        this.handler.unimplementedError("sendmsg");
        return -1;
    }

    @Override
    public int recvmsg(int socket, MsgHdr message, int flags) {
        this.handler.unimplementedError("recvmsg");
        return -1;
    }

    @Override
    public int truncate(CharSequence path, long length) {
        this.handler.unimplementedError("truncate");
        return -1;
    }

    @Override
    public int ftruncate(int fd, long offset) {
        this.handler.unimplementedError("ftruncate");
        return -1;
    }

    @Override
    public int rename(CharSequence oldName, CharSequence newName) {
        File newFile;
        File oldFile = new File(oldName.toString());
        if (oldFile.renameTo(newFile = new File(newName.toString()))) {
            return 0;
        }
        return -1;
    }

    @Override
    public String getcwd() {
        return System.getProperty("user.dir");
    }

    @Override
    public int fsync(int fd) {
        this.handler.unimplementedError("fsync");
        return this.unimplementedInt("fsync not available for Java");
    }

    @Override
    public int fdatasync(int fd) {
        this.handler.unimplementedError("fdatasync");
        return this.unimplementedInt("fdatasync not available for Java");
    }

    @Override
    public int mkfifo(String filename, int mode) {
        this.handler.unimplementedError("mkfifo");
        return this.unimplementedInt("mkfifo not available for Java");
    }

    @Override
    public int daemon(int nochdir, int noclose) {
        this.handler.unimplementedError("daemon");
        return this.unimplementedInt("daemon not available for Java");
    }

    @Override
    public long[] getgroups() {
        this.handler.unimplementedError("getgroups");
        return null;
    }

    @Override
    public int getgroups(int size, int[] groups2) {
        this.handler.unimplementedError("getgroups");
        return this.unimplementedInt("getgroups not available for Java");
    }

    @Override
    public String nl_langinfo(int item) {
        this.handler.unimplementedError("nl_langinfo");
        return null;
    }

    @Override
    public String setlocale(int category, String locale) {
        this.handler.unimplementedError("setlocale");
        return null;
    }

    @Override
    public String strerror(int code) {
        this.handler.unimplementedError("strerror");
        return null;
    }

    @Override
    public Timeval allocateTimeval() {
        this.handler.unimplementedError("allocateTimeval");
        return null;
    }

    @Override
    public int gettimeofday(Timeval tv) {
        this.handler.unimplementedError("gettimeofday");
        return -1;
    }

    @Override
    public String gethostname() {
        return this.helper.gethostname();
    }

    static final class LoginInfo {
        public static final int UID = IDHelper.getInt("-u");
        public static final int GID = IDHelper.getInt("-g");
        public static final String USERNAME = IDHelper.getString("-un");

        LoginInfo() {
        }
    }

    private static final class FakePasswd
    implements Passwd {
        private FakePasswd() {
        }

        @Override
        public String getLoginName() {
            return LoginInfo.USERNAME;
        }

        @Override
        public String getPassword() {
            return "";
        }

        @Override
        public long getUID() {
            return LoginInfo.UID;
        }

        @Override
        public long getGID() {
            return LoginInfo.GID;
        }

        @Override
        public int getPasswdChangeTime() {
            return 0;
        }

        @Override
        public String getAccessClass() {
            return "";
        }

        @Override
        public String getGECOS() {
            return this.getLoginName();
        }

        @Override
        public String getHome() {
            return "/";
        }

        @Override
        public String getShell() {
            return "/bin/sh";
        }

        @Override
        public int getExpire() {
            return -1;
        }
    }

    private static final class IDHelper {
        private static final String ID_CMD = Platform.IS_SOLARIS ? "/usr/xpg4/bin/id" : "/usr/bin/id";
        private static final int NOBODY = Platform.IS_WINDOWS ? 0 : Short.MAX_VALUE;

        private IDHelper() {
        }

        public static int getInt(String option) {
            try {
                Process p = Runtime.getRuntime().exec(new String[]{ID_CMD, option});
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                return Integer.parseInt(r.readLine());
            }
            catch (IOException ex) {
                return NOBODY;
            }
            catch (NumberFormatException ex) {
                return NOBODY;
            }
            catch (SecurityException ex) {
                return NOBODY;
            }
        }

        public static String getString(String option) {
            try {
                Process p = Runtime.getRuntime().exec(new String[]{ID_CMD, option});
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                return r.readLine();
            }
            catch (IOException ex) {
                return null;
            }
        }
    }
}

