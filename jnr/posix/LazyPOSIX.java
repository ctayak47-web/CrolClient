
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
import jnr.posix.FileStat;
import jnr.posix.Group;
import jnr.posix.LibC;
import jnr.posix.MsgHdr;
import jnr.posix.POSIX;
import jnr.posix.POSIXFactory;
import jnr.posix.POSIXHandler;
import jnr.posix.Passwd;
import jnr.posix.RLimit;
import jnr.posix.SignalHandler;
import jnr.posix.SpawnAttribute;
import jnr.posix.SpawnFileAction;
import jnr.posix.Times;
import jnr.posix.Timeval;
import jnr.posix.util.ProcessMaker;

final class LazyPOSIX
implements POSIX {
    private final POSIXHandler handler;
    private final boolean useNativePosix;
    private volatile POSIX posix;

    LazyPOSIX(POSIXHandler handler, boolean useNativePosix) {
        this.handler = handler;
        this.useNativePosix = useNativePosix;
    }

    private final POSIX posix() {
        return this.posix != null ? this.posix : this.loadPOSIX();
    }

    private final synchronized POSIX loadPOSIX() {
        return this.posix != null ? this.posix : (this.posix = POSIXFactory.loadPOSIX(this.handler, this.useNativePosix));
    }

    @Override
    public ProcessMaker newProcessMaker(String ... command) {
        return this.posix().newProcessMaker(command);
    }

    @Override
    public ProcessMaker newProcessMaker() {
        return this.posix().newProcessMaker();
    }

    @Override
    public FileStat allocateStat() {
        return this.posix().allocateStat();
    }

    @Override
    public MsgHdr allocateMsgHdr() {
        return this.posix().allocateMsgHdr();
    }

    @Override
    public int chdir(String path) {
        return this.posix().chdir(path);
    }

    @Override
    public int chmod(String filename, int mode) {
        return this.posix().chmod(filename, mode);
    }

    @Override
    public int fchmod(int fd, int mode) {
        return this.posix().fchmod(fd, mode);
    }

    @Override
    public int chown(String filename, int user, int group) {
        return this.posix().chown(filename, user, group);
    }

    @Override
    public CharSequence crypt(CharSequence key, CharSequence salt) {
        return this.posix().crypt(key, salt);
    }

    @Override
    public byte[] crypt(byte[] key, byte[] salt) {
        return this.posix().crypt(key, salt);
    }

    @Override
    public int fchown(int fd, int user, int group) {
        return this.posix().fchown(fd, user, group);
    }

    @Override
    public int endgrent() {
        return this.posix().endgrent();
    }

    @Override
    public int endpwent() {
        return this.posix().endpwent();
    }

    @Override
    public int errno() {
        return this.posix().errno();
    }

    @Override
    public void errno(int value) {
        this.posix().errno(value);
    }

    @Override
    public int exec(String path, String ... args) {
        return this.posix().exec(path, args);
    }

    @Override
    public int exec(String path, String[] args, String[] envp) {
        return this.posix().exec(path, args, envp);
    }

    @Override
    public int execv(String path, String[] argv) {
        return this.posix().execv(path, argv);
    }

    @Override
    public int execve(String path, String[] argv, String[] envp) {
        return this.posix().execve(path, argv, envp);
    }

    @Override
    public int fork() {
        return this.posix().fork();
    }

    @Override
    public FileStat fstat(int fd) {
        return this.posix().fstat(fd);
    }

    @Override
    public int fstat(int fd, FileStat stat) {
        return this.posix().fstat(fd, stat);
    }

    @Override
    public FileStat fstat(FileDescriptor descriptor2) {
        return this.posix().fstat(descriptor2);
    }

    @Override
    public int fstat(FileDescriptor descriptor2, FileStat stat) {
        return this.posix().fstat(descriptor2, stat);
    }

    @Override
    public int getegid() {
        return this.posix().getegid();
    }

    @Override
    public int geteuid() {
        return this.posix().geteuid();
    }

    @Override
    public int getgid() {
        return this.posix().getgid();
    }

    @Override
    public int getdtablesize() {
        return this.posix().getdtablesize();
    }

    @Override
    public Group getgrent() {
        return this.posix().getgrent();
    }

    @Override
    public Group getgrgid(int which) {
        return this.posix().getgrgid(which);
    }

    @Override
    public Group getgrnam(String which) {
        return this.posix().getgrnam(which);
    }

    @Override
    public String getlogin() {
        return this.posix().getlogin();
    }

    @Override
    public int getpgid() {
        return this.posix().getpgid();
    }

    @Override
    public int getpgid(int pid) {
        return this.posix().getpgid(pid);
    }

    @Override
    public int getpgrp() {
        return this.posix().getpgrp();
    }

    @Override
    public int getpid() {
        return this.posix().getpid();
    }

    @Override
    public int getppid() {
        return this.posix().getppid();
    }

    @Override
    public int getpriority(int which, int who) {
        return this.posix().getpriority(which, who);
    }

    @Override
    public Passwd getpwent() {
        return this.posix().getpwent();
    }

    @Override
    public Passwd getpwnam(String which) {
        return this.posix().getpwnam(which);
    }

    @Override
    public Passwd getpwuid(int which) {
        return this.posix().getpwuid(which);
    }

    @Override
    public int getuid() {
        return this.posix().getuid();
    }

    @Override
    public int getrlimit(int resource, RLimit rlim) {
        return this.posix().getrlimit(resource, rlim);
    }

    @Override
    public int getrlimit(int resource, Pointer rlim) {
        return this.posix().getrlimit(resource, rlim);
    }

    @Override
    public RLimit getrlimit(int resource) {
        return this.posix().getrlimit(resource);
    }

    @Override
    public int setrlimit(int resource, RLimit rlim) {
        return this.posix().setrlimit(resource, rlim);
    }

    @Override
    public int setrlimit(int resource, Pointer rlim) {
        return this.posix().setrlimit(resource, rlim);
    }

    @Override
    public int setrlimit(int resource, long rlimCur, long rlimMax) {
        return this.posix().setrlimit(resource, rlimCur, rlimMax);
    }

    @Override
    public boolean isatty(FileDescriptor descriptor2) {
        return this.posix().isatty(descriptor2);
    }

    @Override
    public int isatty(int descriptor2) {
        return this.posix().isatty(descriptor2);
    }

    @Override
    public int kill(int pid, int signal) {
        return this.kill((long)pid, signal);
    }

    @Override
    public int kill(long pid, int signal) {
        return this.posix().kill(pid, signal);
    }

    @Override
    public SignalHandler signal(Signal sig, SignalHandler handler) {
        return this.posix().signal(sig, handler);
    }

    @Override
    public int raise(int sig) {
        return this.posix().raise(sig);
    }

    @Override
    public int lchmod(String filename, int mode) {
        return this.posix().lchmod(filename, mode);
    }

    @Override
    public int lchown(String filename, int user, int group) {
        return this.posix().lchown(filename, user, group);
    }

    @Override
    public int link(String oldpath, String newpath) {
        return this.posix().link(oldpath, newpath);
    }

    @Override
    public FileStat lstat(String path) {
        return this.posix().lstat(path);
    }

    @Override
    public int lstat(String path, FileStat stat) {
        return this.posix().lstat(path, stat);
    }

    @Override
    public int mkdir(String path, int mode) {
        return this.posix().mkdir(path, mode);
    }

    @Override
    public String readlink(String path) throws IOException {
        return this.posix().readlink(path);
    }

    @Override
    public int readlink(CharSequence path, byte[] buf, int bufsize) {
        return this.posix().readlink(path, buf, bufsize);
    }

    @Override
    public int readlink(CharSequence path, ByteBuffer buf, int bufsize) {
        return this.posix().readlink(path, buf, bufsize);
    }

    @Override
    public int readlink(CharSequence path, Pointer bufPtr, int bufsize) {
        return this.posix().readlink(path, bufPtr, bufsize);
    }

    @Override
    public int rmdir(String path) {
        return this.posix().rmdir(path);
    }

    @Override
    public int setegid(int egid) {
        return this.posix().setegid(egid);
    }

    @Override
    public int seteuid(int euid) {
        return this.posix().seteuid(euid);
    }

    @Override
    public int setgid(int gid) {
        return this.posix().setgid(gid);
    }

    @Override
    public int setgrent() {
        return this.posix().setgrent();
    }

    @Override
    public int setpgid(int pid, int pgid) {
        return this.posix().setpgid(pid, pgid);
    }

    @Override
    public int setpgrp(int pid, int pgrp) {
        return this.posix().setpgrp(pid, pgrp);
    }

    @Override
    public int setpriority(int which, int who, int prio) {
        return this.posix().setpriority(which, who, prio);
    }

    @Override
    public int setpwent() {
        return this.posix().setpwent();
    }

    @Override
    public int setsid() {
        return this.posix().setsid();
    }

    @Override
    public int setuid(int uid) {
        return this.posix().setuid(uid);
    }

    @Override
    public FileStat stat(String path) {
        return this.posix().stat(path);
    }

    @Override
    public int stat(String path, FileStat stat) {
        return this.posix().stat(path, stat);
    }

    @Override
    public int symlink(String oldpath, String newpath) {
        return this.posix().symlink(oldpath, newpath);
    }

    @Override
    public int umask(int mask) {
        return this.posix().umask(mask);
    }

    @Override
    public int utimes(String path, long[] atimeval, long[] mtimeval) {
        return this.posix().utimes(path, atimeval, mtimeval);
    }

    @Override
    public int utimes(String path, Pointer times) {
        return this.posix().utimes(path, times);
    }

    @Override
    public int futimes(int fd, long[] atimeval, long[] mtimeval) {
        return this.posix().futimes(fd, atimeval, mtimeval);
    }

    @Override
    public int lutimes(String path, long[] atimeval, long[] mtimeval) {
        return this.posix().lutimes(path, atimeval, mtimeval);
    }

    @Override
    public int utimensat(int dirfd, String path, long[] atimespec, long[] mtimespec, int flag) {
        return this.posix().utimensat(dirfd, path, atimespec, mtimespec, flag);
    }

    @Override
    public int utimensat(int dirfd, String path, Pointer times, int flag) {
        return this.posix().utimensat(dirfd, path, times, flag);
    }

    @Override
    public int futimens(int fd, long[] atimespec, long[] mtimespec) {
        return this.posix().futimens(fd, atimespec, mtimespec);
    }

    @Override
    public int futimens(int fd, Pointer times) {
        return this.posix().futimens(fd, times);
    }

    @Override
    public int wait(int[] status) {
        return this.posix().wait(status);
    }

    @Override
    public int waitpid(int pid, int[] status, int flags) {
        return this.waitpid((long)pid, status, flags);
    }

    @Override
    public int waitpid(long pid, int[] status, int flags) {
        return this.posix().waitpid(pid, status, flags);
    }

    @Override
    public boolean isNative() {
        return this.posix().isNative();
    }

    @Override
    public LibC libc() {
        return this.posix().libc();
    }

    @Override
    public Pointer environ() {
        return this.posix().environ();
    }

    @Override
    public String getenv(String envName) {
        return this.posix().getenv(envName);
    }

    @Override
    public int setenv(String envName, String envValue, int overwrite) {
        return this.posix().setenv(envName, envValue, overwrite);
    }

    @Override
    public int unsetenv(String envName) {
        return this.posix().unsetenv(envName);
    }

    @Override
    public long posix_spawnp(String path, Collection<? extends SpawnFileAction> fileActions, Collection<? extends CharSequence> argv, Collection<? extends CharSequence> envp) {
        return this.posix().posix_spawnp(path, fileActions, argv, envp);
    }

    @Override
    public long posix_spawnp(String path, Collection<? extends SpawnFileAction> fileActions, Collection<? extends SpawnAttribute> spawnAttributes, Collection<? extends CharSequence> argv, Collection<? extends CharSequence> envp) {
        return this.posix().posix_spawnp(path, fileActions, spawnAttributes, argv, envp);
    }

    @Override
    public long sysconf(Sysconf name) {
        return this.posix().sysconf(name);
    }

    @Override
    public int confstr(Confstr name, ByteBuffer buf, int len) {
        return this.posix().confstr(name, buf, len);
    }

    @Override
    public int fpathconf(int fd, Pathconf name) {
        return this.posix().fpathconf(fd, name);
    }

    @Override
    public Times times() {
        return this.posix().times();
    }

    @Override
    public int flock(int fd, int mode) {
        return this.posix().flock(fd, mode);
    }

    @Override
    public int dup(int fd) {
        return this.posix().dup(fd);
    }

    @Override
    public int dup2(int oldFd, int newFd) {
        return this.posix().dup2(oldFd, newFd);
    }

    @Override
    public int fcntlInt(int fd, Fcntl fcntlConst, int arg) {
        return this.posix().fcntlInt(fd, fcntlConst, arg);
    }

    @Override
    public int fcntl(int fd, Fcntl fcntlConst) {
        return this.posix().fcntl(fd, fcntlConst);
    }

    @Override
    public int fcntl(int fd, Fcntl fcntlConst, int arg) {
        return this.posix().fcntl(fd, fcntlConst, arg);
    }

    @Override
    @Deprecated
    public int fcntl(int fd, Fcntl fcntlConst, int ... arg) {
        return this.posix().fcntl(fd, fcntlConst);
    }

    @Override
    public int access(CharSequence path, int amode) {
        return this.posix().access(path, amode);
    }

    @Override
    public int close(int fd) {
        return this.posix().close(fd);
    }

    @Override
    public int unlink(CharSequence path) {
        return this.posix().unlink(path);
    }

    @Override
    public int open(CharSequence path, int flags, int perm) {
        return this.posix().open(path, flags, perm);
    }

    @Override
    public long read(int fd, byte[] buf, long n) {
        return this.posix().read(fd, buf, n);
    }

    @Override
    public long write(int fd, byte[] buf, long n) {
        return this.posix().write(fd, buf, n);
    }

    @Override
    public long read(int fd, ByteBuffer buf, long n) {
        return this.posix().read(fd, buf, n);
    }

    @Override
    public long write(int fd, ByteBuffer buf, long n) {
        return this.posix().write(fd, buf, n);
    }

    @Override
    public long pread(int fd, byte[] buf, long n, long offset) {
        return this.posix().pread(fd, buf, n, offset);
    }

    @Override
    public long pwrite(int fd, byte[] buf, long n, long offset) {
        return this.posix().pwrite(fd, buf, n, offset);
    }

    @Override
    public long pread(int fd, ByteBuffer buf, long n, long offset) {
        return this.posix().pread(fd, buf, n, offset);
    }

    @Override
    public long pwrite(int fd, ByteBuffer buf, long n, long offset) {
        return this.posix().pwrite(fd, buf, n, offset);
    }

    @Override
    public int read(int fd, byte[] buf, int n) {
        return this.posix().read(fd, buf, n);
    }

    @Override
    public int write(int fd, byte[] buf, int n) {
        return this.posix().write(fd, buf, n);
    }

    @Override
    public int read(int fd, ByteBuffer buf, int n) {
        return this.posix().read(fd, buf, n);
    }

    @Override
    public int write(int fd, ByteBuffer buf, int n) {
        return this.posix().write(fd, buf, n);
    }

    @Override
    public int pread(int fd, byte[] buf, int n, int offset) {
        return this.posix().pread(fd, buf, n, offset);
    }

    @Override
    public int pwrite(int fd, byte[] buf, int n, int offset) {
        return this.posix().pwrite(fd, buf, n, offset);
    }

    @Override
    public int pread(int fd, ByteBuffer buf, int n, int offset) {
        return this.posix().pread(fd, buf, n, offset);
    }

    @Override
    public int pwrite(int fd, ByteBuffer buf, int n, int offset) {
        return this.posix().pwrite(fd, buf, n, offset);
    }

    @Override
    public int lseek(int fd, long offset, int whence) {
        return this.posix().lseek(fd, offset, whence);
    }

    @Override
    public long lseekLong(int fd, long offset, int whence) {
        return this.posix().lseekLong(fd, offset, whence);
    }

    @Override
    public int pipe(int[] fds) {
        return this.posix().pipe(fds);
    }

    @Override
    public int socketpair(int domain, int type, int protocol, int[] fds) {
        return this.posix().socketpair(domain, type, protocol, fds);
    }

    @Override
    public int sendmsg(int socket, MsgHdr message, int flags) {
        return this.posix().sendmsg(socket, message, flags);
    }

    @Override
    public int recvmsg(int socket, MsgHdr message, int flags) {
        return this.posix().recvmsg(socket, message, flags);
    }

    @Override
    public int truncate(CharSequence path, long length) {
        return this.posix().truncate(path, length);
    }

    @Override
    public int ftruncate(int fd, long offset) {
        return this.posix().ftruncate(fd, offset);
    }

    @Override
    public int rename(CharSequence oldName, CharSequence newName) {
        return this.posix().rename(oldName, newName);
    }

    @Override
    public String getcwd() {
        return this.posix().getcwd();
    }

    @Override
    public int fsync(int fd) {
        return this.posix().fsync(fd);
    }

    @Override
    public int fdatasync(int fd) {
        return this.posix().fdatasync(fd);
    }

    @Override
    public int mkfifo(String path, int mode) {
        return this.posix().mkfifo(path, mode);
    }

    @Override
    public String gethostname() {
        return this.posix().gethostname();
    }

    @Override
    public int daemon(int nochdir, int noclose) {
        return this.posix().daemon(nochdir, noclose);
    }

    @Override
    public long[] getgroups() {
        return this.posix().getgroups();
    }

    @Override
    public int getgroups(int size, int[] groups2) {
        return this.posix().getgroups(size, groups2);
    }

    @Override
    public String nl_langinfo(int item) {
        return this.posix().nl_langinfo(item);
    }

    @Override
    public String setlocale(int category, String locale) {
        return this.posix().setlocale(category, locale);
    }

    @Override
    public String strerror(int code) {
        return this.posix().strerror(code);
    }

    @Override
    public Timeval allocateTimeval() {
        return this.posix().allocateTimeval();
    }

    @Override
    public int gettimeofday(Timeval tv) {
        return this.posix().gettimeofday(tv);
    }
}

