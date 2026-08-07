
package jnr.posix;

import java.nio.ByteBuffer;
import jnr.constants.platform.Confstr;
import jnr.constants.platform.Pathconf;
import jnr.constants.platform.Sysconf;
import jnr.ffi.Pointer;
import jnr.ffi.Variable;
import jnr.ffi.annotations.Delegate;
import jnr.ffi.annotations.Direct;
import jnr.ffi.annotations.IgnoreError;
import jnr.ffi.annotations.In;
import jnr.ffi.annotations.Out;
import jnr.ffi.annotations.Transient;
import jnr.ffi.annotations.Variadic;
import jnr.ffi.byref.IntByReference;
import jnr.ffi.types.clock_t;
import jnr.ffi.types.intptr_t;
import jnr.ffi.types.off_t;
import jnr.ffi.types.size_t;
import jnr.ffi.types.ssize_t;
import jnr.ffi.types.u_int32_t;
import jnr.ffi.types.u_int64_t;
import jnr.posix.FileStat;
import jnr.posix.Flock;
import jnr.posix.MsgHdr;
import jnr.posix.NativeGroup;
import jnr.posix.NativePasswd;
import jnr.posix.NativeTimes;
import jnr.posix.RLimit;
import jnr.posix.Timespec;
import jnr.posix.Timeval;

public interface LibC {
    public int chmod(CharSequence var1, int var2);

    public int fchmod(int var1, int var2);

    public int chown(CharSequence var1, int var2, int var3);

    public int fchown(int var1, int var2, int var3);

    public int fstat(int var1, @Out @Transient FileStat var2);

    public int fstat64(int var1, @Out @Transient FileStat var2);

    public String getenv(CharSequence var1);

    @IgnoreError
    public int getegid();

    public int setegid(int var1);

    @IgnoreError
    public int geteuid();

    public int seteuid(int var1);

    @IgnoreError
    public int getgid();

    public String getlogin();

    public int setgid(int var1);

    public int getpgid();

    public int getpgid(int var1);

    public int setpgid(int var1, int var2);

    public int getpgrp();

    public int setpgrp(int var1, int var2);

    @IgnoreError
    public int getppid();

    @IgnoreError
    public int getpid();

    public NativePasswd getpwent();

    public NativePasswd getpwuid(int var1);

    public NativePasswd getpwnam(CharSequence var1);

    public NativeGroup getgrent();

    public NativeGroup getgrgid(int var1);

    public NativeGroup getgrnam(CharSequence var1);

    public int setpwent();

    public int endpwent();

    public int setgrent();

    public int endgrent();

    @IgnoreError
    public int getuid();

    public int setsid();

    public int setuid(int var1);

    public int getrlimit(int var1, @Out RLimit var2);

    public int getrlimit(int var1, Pointer var2);

    public int setrlimit(int var1, @In RLimit var2);

    public int setrlimit(int var1, Pointer var2);

    public int kill(int var1, int var2);

    public int kill(long var1, int var3);

    public int dup(int var1);

    public int dup2(int var1, int var2);

    @Variadic(fixedCount=2)
    public int fcntl(int var1, int var2, Flock var3);

    @Variadic(fixedCount=2)
    public int fcntl(int var1, int var2, Pointer var3);

    @Variadic(fixedCount=2)
    public int fcntl(int var1, int var2);

    @Variadic(fixedCount=2)
    public int fcntl(int var1, int var2, @u_int64_t int var3);

    @Deprecated
    public int fcntl(int var1, int var2, int ... var3);

    public int access(CharSequence var1, int var2);

    public int getdtablesize();

    @intptr_t
    public long signal(int var1, LibCSignalHandler var2);

    public int raise(int var1);

    public int lchmod(CharSequence var1, int var2);

    public int lchown(CharSequence var1, int var2, int var3);

    public int link(CharSequence var1, CharSequence var2);

    public int lstat(CharSequence var1, @Out @Transient FileStat var2);

    public int lstat64(CharSequence var1, @Out @Transient FileStat var2);

    public int mkdir(CharSequence var1, int var2);

    public int rmdir(CharSequence var1);

    public int stat(CharSequence var1, @Out @Transient FileStat var2);

    public int stat64(CharSequence var1, @Out @Transient FileStat var2);

    public int symlink(CharSequence var1, CharSequence var2);

    public int readlink(CharSequence var1, @Out ByteBuffer var2, int var3);

    public int readlink(CharSequence var1, @Out byte[] var2, int var3);

    public int readlink(CharSequence var1, Pointer var2, int var3);

    public int setenv(CharSequence var1, CharSequence var2, int var3);

    @IgnoreError
    public int umask(int var1);

    public int unsetenv(CharSequence var1);

    public int utimes(CharSequence var1, @In Timeval[] var2);

    public int utimes(String var1, @In Pointer var2);

    public int futimes(int var1, @In Timeval[] var2);

    public int lutimes(CharSequence var1, @In Timeval[] var2);

    public int utimensat(int var1, String var2, Timespec[] var3, int var4);

    public int utimensat(int var1, String var2, @In Pointer var3, int var4);

    public int futimens(int var1, Timespec[] var2);

    public int futimens(int var1, @In Pointer var2);

    public int fork();

    public int waitpid(long var1, @Out int[] var3, int var4);

    public int wait(@Out int[] var1);

    public int getpriority(int var1, int var2);

    public int setpriority(int var1, int var2, int var3);

    @IgnoreError
    public int isatty(int var1);

    @ssize_t
    public long read(int var1, @Out byte[] var2, @size_t long var3);

    @ssize_t
    public long write(int var1, @In byte[] var2, @size_t long var3);

    @ssize_t
    public long read(int var1, @Out ByteBuffer var2, @size_t long var3);

    @ssize_t
    public long write(int var1, @In ByteBuffer var2, @size_t long var3);

    @ssize_t
    public long pread(int var1, @Out byte[] var2, @size_t long var3, @off_t long var5);

    @ssize_t
    public long pwrite(int var1, @In byte[] var2, @size_t long var3, @off_t long var5);

    @ssize_t
    public long pread(int var1, @Out ByteBuffer var2, @size_t long var3, @off_t long var5);

    @ssize_t
    public long pwrite(int var1, @In ByteBuffer var2, @size_t long var3, @off_t long var5);

    public int read(int var1, @Out byte[] var2, int var3);

    public int write(int var1, @In byte[] var2, int var3);

    public int read(int var1, @Out ByteBuffer var2, int var3);

    public int write(int var1, @In ByteBuffer var2, int var3);

    public int pread(int var1, @Out byte[] var2, int var3, int var4);

    public int pwrite(int var1, @In byte[] var2, int var3, int var4);

    public int pread(int var1, @Out ByteBuffer var2, int var3, int var4);

    public int pwrite(int var1, @In ByteBuffer var2, int var3, int var4);

    public long lseek(int var1, long var2, int var4);

    public int close(int var1);

    public int execv(CharSequence var1, @In CharSequence[] var2);

    public int execve(CharSequence var1, @In CharSequence[] var2, @In CharSequence[] var3);

    public int chdir(CharSequence var1);

    public long sysconf(Sysconf var1);

    public int confstr(Confstr var1, @Out ByteBuffer var2, int var3);

    public int fpathconf(int var1, Pathconf var2);

    @clock_t
    public long times(@Out @Transient NativeTimes var1);

    public int flock(int var1, int var2);

    public int unlink(CharSequence var1);

    @Variadic(fixedCount=2)
    public int open(CharSequence var1, int var2, @u_int32_t int var3);

    public int pipe(@Out int[] var1);

    public int truncate(CharSequence var1, long var2);

    public int ftruncate(int var1, long var2);

    public int rename(CharSequence var1, CharSequence var2);

    public long getcwd(byte[] var1, int var2);

    public int gethostname(@Out ByteBuffer var1, int var2);

    public int fsync(int var1);

    public int fdatasync(int var1);

    public int socketpair(int var1, int var2, int var3, @Out int[] var4);

    public int sendmsg(int var1, @In MsgHdr var2, int var3);

    public int recvmsg(int var1, @Direct MsgHdr var2, int var3);

    public int setsockopt(int var1, int var2, int var3, @In ByteBuffer var4, int var5);

    public int getsockopt(int var1, int var2, int var3, @Out ByteBuffer var4, @In @Out IntByReference var5);

    public Variable<Long> environ();

    public int syscall(int var1);

    public int syscall(int var1, int var2);

    public int syscall(int var1, int var2, int var3);

    public int syscall(int var1, int var2, int var3, int var4);

    public int daemon(int var1, int var2);

    public int getgroups(int var1, int[] var2);

    public String nl_langinfo(int var1);

    public String setlocale(int var1, String var2);

    public String strerror(int var1);

    public int gettimeofday(Timeval var1, long var2);

    public static interface LibCSignalHandler {
        @Delegate
        public void signal(int var1);
    }
}

