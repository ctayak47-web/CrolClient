
package jnr.posix;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import jnr.constants.platform.Confstr;
import jnr.constants.platform.Fcntl;
import jnr.constants.platform.Pathconf;
import jnr.constants.platform.Signal;
import jnr.constants.platform.Sysconf;
import jnr.ffi.Pointer;
import jnr.ffi.annotations.Out;
import jnr.posix.FileStat;
import jnr.posix.Group;
import jnr.posix.LibC;
import jnr.posix.MsgHdr;
import jnr.posix.Passwd;
import jnr.posix.RLimit;
import jnr.posix.SignalHandler;
import jnr.posix.SpawnAttribute;
import jnr.posix.SpawnFileAction;
import jnr.posix.Times;
import jnr.posix.Timeval;
import jnr.posix.util.ProcessMaker;

public interface POSIX {
    public CharSequence crypt(CharSequence var1, CharSequence var2);

    public byte[] crypt(byte[] var1, byte[] var2);

    public FileStat allocateStat();

    public int chmod(String var1, int var2);

    public int fchmod(int var1, int var2);

    public int chown(String var1, int var2, int var3);

    public int fchown(int var1, int var2, int var3);

    public int exec(String var1, String ... var2);

    public int exec(String var1, String[] var2, String[] var3);

    public int execv(String var1, String[] var2);

    public int execve(String var1, String[] var2, String[] var3);

    public int fork();

    public FileStat fstat(FileDescriptor var1);

    public FileStat fstat(int var1);

    public int fstat(FileDescriptor var1, FileStat var2);

    public int fstat(int var1, FileStat var2);

    public Pointer environ();

    public String getenv(String var1);

    public int getegid();

    public int geteuid();

    public int seteuid(int var1);

    public int getgid();

    public int getdtablesize();

    public String getlogin();

    public int getpgid();

    public int getpgid(int var1);

    public int getpgrp();

    public int getpid();

    public int getppid();

    public int getpriority(int var1, int var2);

    public Passwd getpwent();

    public Passwd getpwuid(int var1);

    public Passwd getpwnam(String var1);

    public Group getgrgid(int var1);

    public Group getgrnam(String var1);

    public Group getgrent();

    public int endgrent();

    public int setgrent();

    public int endpwent();

    public int setpwent();

    public int getuid();

    public int getrlimit(int var1, RLimit var2);

    public int getrlimit(int var1, Pointer var2);

    public RLimit getrlimit(int var1);

    public int setrlimit(int var1, RLimit var2);

    public int setrlimit(int var1, Pointer var2);

    public int setrlimit(int var1, long var2, long var4);

    public boolean isatty(FileDescriptor var1);

    public int isatty(int var1);

    public int kill(int var1, int var2);

    public int kill(long var1, int var3);

    public SignalHandler signal(Signal var1, SignalHandler var2);

    public int raise(int var1);

    public int lchmod(String var1, int var2);

    public int lchown(String var1, int var2, int var3);

    public int link(String var1, String var2);

    public FileStat lstat(String var1);

    public int lstat(String var1, FileStat var2);

    public int mkdir(String var1, int var2);

    public String readlink(String var1) throws IOException;

    public int readlink(CharSequence var1, byte[] var2, int var3);

    public int readlink(CharSequence var1, ByteBuffer var2, int var3);

    public int readlink(CharSequence var1, Pointer var2, int var3);

    public int rmdir(String var1);

    public int setenv(String var1, String var2, int var3);

    public int setsid();

    public int setgid(int var1);

    public int setegid(int var1);

    public int setpgid(int var1, int var2);

    public int setpgrp(int var1, int var2);

    public int setpriority(int var1, int var2, int var3);

    public int setuid(int var1);

    public FileStat stat(String var1);

    public int stat(String var1, FileStat var2);

    public int symlink(String var1, String var2);

    public int umask(int var1);

    public int unsetenv(String var1);

    public int utimes(String var1, long[] var2, long[] var3);

    public int utimes(String var1, Pointer var2);

    public int futimes(int var1, long[] var2, long[] var3);

    public int lutimes(String var1, long[] var2, long[] var3);

    public int utimensat(int var1, String var2, long[] var3, long[] var4, int var5);

    public int utimensat(int var1, String var2, Pointer var3, int var4);

    public int futimens(int var1, long[] var2, long[] var3);

    public int futimens(int var1, Pointer var2);

    public int waitpid(int var1, int[] var2, int var3);

    public int waitpid(long var1, int[] var3, int var4);

    public int wait(int[] var1);

    public int errno();

    public void errno(int var1);

    public String strerror(int var1);

    public int chdir(String var1);

    public boolean isNative();

    public LibC libc();

    public ProcessMaker newProcessMaker(String ... var1);

    public ProcessMaker newProcessMaker();

    public long sysconf(Sysconf var1);

    public int confstr(Confstr var1, @Out ByteBuffer var2, int var3);

    public int fpathconf(int var1, Pathconf var2);

    public Times times();

    public long posix_spawnp(String var1, Collection<? extends SpawnFileAction> var2, Collection<? extends CharSequence> var3, Collection<? extends CharSequence> var4);

    public long posix_spawnp(String var1, Collection<? extends SpawnFileAction> var2, Collection<? extends SpawnAttribute> var3, Collection<? extends CharSequence> var4, Collection<? extends CharSequence> var5);

    public int flock(int var1, int var2);

    public int dup(int var1);

    public int dup2(int var1, int var2);

    public int fcntlInt(int var1, Fcntl var2, int var3);

    public int fcntl(int var1, Fcntl var2, int var3);

    public int fcntl(int var1, Fcntl var2);

    public int access(CharSequence var1, int var2);

    public int close(int var1);

    public int unlink(CharSequence var1);

    public int open(CharSequence var1, int var2, int var3);

    public long read(int var1, byte[] var2, long var3);

    public long write(int var1, byte[] var2, long var3);

    public long read(int var1, ByteBuffer var2, long var3);

    public long write(int var1, ByteBuffer var2, long var3);

    public long pread(int var1, byte[] var2, long var3, long var5);

    public long pwrite(int var1, byte[] var2, long var3, long var5);

    public long pread(int var1, ByteBuffer var2, long var3, long var5);

    public long pwrite(int var1, ByteBuffer var2, long var3, long var5);

    public int read(int var1, byte[] var2, int var3);

    public int write(int var1, byte[] var2, int var3);

    public int read(int var1, ByteBuffer var2, int var3);

    public int write(int var1, ByteBuffer var2, int var3);

    public int pread(int var1, byte[] var2, int var3, int var4);

    public int pwrite(int var1, byte[] var2, int var3, int var4);

    public int pread(int var1, ByteBuffer var2, int var3, int var4);

    public int pwrite(int var1, ByteBuffer var2, int var3, int var4);

    public int lseek(int var1, long var2, int var4);

    public long lseekLong(int var1, long var2, int var4);

    public int pipe(int[] var1);

    public int truncate(CharSequence var1, long var2);

    public int ftruncate(int var1, long var2);

    public int rename(CharSequence var1, CharSequence var2);

    public String getcwd();

    public String gethostname();

    public int socketpair(int var1, int var2, int var3, int[] var4);

    public int sendmsg(int var1, MsgHdr var2, int var3);

    public int recvmsg(int var1, MsgHdr var2, int var3);

    public MsgHdr allocateMsgHdr();

    @Deprecated
    public int fcntl(int var1, Fcntl var2, int ... var3);

    public int fsync(int var1);

    public int fdatasync(int var1);

    public int mkfifo(String var1, int var2);

    public int daemon(int var1, int var2);

    public long[] getgroups();

    public int getgroups(int var1, int[] var2);

    public String nl_langinfo(int var1);

    public String setlocale(int var1, String var2);

    public Timeval allocateTimeval();

    public int gettimeofday(Timeval var1);
}

