
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
import jnr.posix.POSIXHandler;
import jnr.posix.Passwd;
import jnr.posix.RLimit;
import jnr.posix.SignalHandler;
import jnr.posix.SpawnAttribute;
import jnr.posix.SpawnFileAction;
import jnr.posix.Times;
import jnr.posix.Timeval;
import jnr.posix.util.MethodName;
import jnr.posix.util.ProcessMaker;

final class CheckedPOSIX
implements POSIX {
    private final POSIX posix;
    private final POSIXHandler handler;

    CheckedPOSIX(POSIX posix, POSIXHandler handler) {
        this.posix = posix;
        this.handler = handler;
    }

    private <T> T unimplementedNull() {
        this.handler.unimplementedError(MethodName.getCallerMethodName());
        return null;
    }

    private int unimplementedInt() {
        this.handler.unimplementedError(MethodName.getCallerMethodName());
        return -1;
    }

    private boolean unimplementedBool() {
        this.handler.unimplementedError(MethodName.getCallerMethodName());
        return false;
    }

    private String unimplementedString() {
        this.handler.unimplementedError(MethodName.getCallerMethodName());
        return null;
    }

    @Override
    public ProcessMaker newProcessMaker(String ... command) {
        try {
            return this.posix.newProcessMaker(command);
        }
        catch (UnsatisfiedLinkError ule) {
            return (ProcessMaker)this.unimplementedNull();
        }
    }

    @Override
    public ProcessMaker newProcessMaker() {
        try {
            return this.posix.newProcessMaker();
        }
        catch (UnsatisfiedLinkError ule) {
            return (ProcessMaker)this.unimplementedNull();
        }
    }

    @Override
    public FileStat allocateStat() {
        try {
            return this.posix.allocateStat();
        }
        catch (UnsatisfiedLinkError ule) {
            return (FileStat)this.unimplementedNull();
        }
    }

    @Override
    public MsgHdr allocateMsgHdr() {
        try {
            return this.posix.allocateMsgHdr();
        }
        catch (UnsatisfiedLinkError ule) {
            return (MsgHdr)this.unimplementedNull();
        }
    }

    @Override
    public int chdir(String path) {
        try {
            return this.posix.chdir(path);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int chmod(String filename, int mode) {
        try {
            return this.posix.chmod(filename, mode);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int fchmod(int fd, int mode) {
        try {
            return this.posix.fchmod(fd, mode);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int chown(String filename, int user, int group) {
        try {
            return this.posix.chown(filename, user, group);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public CharSequence crypt(CharSequence key, CharSequence salt) {
        try {
            return this.posix.crypt(key, salt);
        }
        catch (UnsatisfiedLinkError ule) {
            return (CharSequence)this.unimplementedNull();
        }
    }

    @Override
    public byte[] crypt(byte[] key, byte[] salt) {
        try {
            return this.posix.crypt(key, salt);
        }
        catch (UnsatisfiedLinkError ule) {
            return (byte[])this.unimplementedNull();
        }
    }

    @Override
    public int fchown(int fd, int user, int group) {
        try {
            return this.posix.fchown(fd, user, group);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int endgrent() {
        try {
            return this.posix.endgrent();
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int endpwent() {
        try {
            return this.posix.endpwent();
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int errno() {
        return this.posix.errno();
    }

    @Override
    public void errno(int value) {
        this.posix.errno(value);
    }

    @Override
    public int exec(String path, String ... args) {
        try {
            return this.posix.exec(path, args);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int exec(String path, String[] args, String[] envp) {
        try {
            return this.posix.exec(path, args, envp);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int execv(String path, String[] argv) {
        try {
            return this.posix.execv(path, argv);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int execve(String path, String[] argv, String[] envp) {
        try {
            return this.posix.execve(path, argv, envp);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int fork() {
        try {
            return this.posix.fork();
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public FileStat fstat(int fd) {
        try {
            return this.posix.fstat(fd);
        }
        catch (UnsatisfiedLinkError ule) {
            return (FileStat)this.unimplementedNull();
        }
    }

    @Override
    public int fstat(int fd, FileStat stat) {
        try {
            return this.posix.fstat(fd, stat);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public FileStat fstat(FileDescriptor descriptor2) {
        try {
            return this.posix.fstat(descriptor2);
        }
        catch (UnsatisfiedLinkError ule) {
            return (FileStat)this.unimplementedNull();
        }
    }

    @Override
    public int fstat(FileDescriptor descriptor2, FileStat stat) {
        try {
            return this.posix.fstat(descriptor2, stat);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int getegid() {
        try {
            return this.posix.getegid();
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int geteuid() {
        try {
            return this.posix.geteuid();
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int getgid() {
        try {
            return this.posix.getgid();
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int getdtablesize() {
        try {
            return this.posix.getdtablesize();
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public Group getgrent() {
        try {
            return this.posix.getgrent();
        }
        catch (UnsatisfiedLinkError ule) {
            return (Group)this.unimplementedNull();
        }
    }

    @Override
    public Group getgrgid(int which) {
        try {
            return this.posix.getgrgid(which);
        }
        catch (UnsatisfiedLinkError ule) {
            return (Group)this.unimplementedNull();
        }
    }

    @Override
    public Group getgrnam(String which) {
        try {
            return this.posix.getgrnam(which);
        }
        catch (UnsatisfiedLinkError ule) {
            return (Group)this.unimplementedNull();
        }
    }

    @Override
    public String getlogin() {
        try {
            return this.posix.getlogin();
        }
        catch (UnsatisfiedLinkError ule) {
            return (String)this.unimplementedNull();
        }
    }

    @Override
    public int getpgid() {
        try {
            return this.posix.getpgid();
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int getpgid(int pid) {
        try {
            return this.posix.getpgid(pid);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int getpgrp() {
        try {
            return this.posix.getpgrp();
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int getpid() {
        try {
            return this.posix.getpid();
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int getppid() {
        try {
            return this.posix.getppid();
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int getpriority(int which, int who) {
        try {
            return this.posix.getpriority(which, who);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public Passwd getpwent() {
        try {
            return this.posix.getpwent();
        }
        catch (UnsatisfiedLinkError ule) {
            return (Passwd)this.unimplementedNull();
        }
    }

    @Override
    public Passwd getpwnam(String which) {
        try {
            return this.posix.getpwnam(which);
        }
        catch (UnsatisfiedLinkError ule) {
            return (Passwd)this.unimplementedNull();
        }
    }

    @Override
    public Passwd getpwuid(int which) {
        try {
            return this.posix.getpwuid(which);
        }
        catch (UnsatisfiedLinkError ule) {
            return (Passwd)this.unimplementedNull();
        }
    }

    @Override
    public int getuid() {
        try {
            return this.posix.getuid();
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int getrlimit(int resource, RLimit rlim) {
        try {
            return this.posix.getrlimit(resource, rlim);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int getrlimit(int resource, Pointer rlim) {
        try {
            return this.posix.getrlimit(resource, rlim);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public RLimit getrlimit(int resource) {
        try {
            return this.posix.getrlimit(resource);
        }
        catch (UnsatisfiedLinkError ule) {
            return (RLimit)this.unimplementedNull();
        }
    }

    @Override
    public int setrlimit(int resource, RLimit rlim) {
        try {
            return this.posix.setrlimit(resource, rlim);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int setrlimit(int resource, Pointer rlim) {
        try {
            return this.posix.setrlimit(resource, rlim);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int setrlimit(int resource, long rlimCur, long rlimMax) {
        try {
            return this.posix.setrlimit(resource, rlimCur, rlimMax);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public boolean isatty(FileDescriptor descriptor2) {
        try {
            return this.posix.isatty(descriptor2);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedBool();
        }
    }

    @Override
    public int isatty(int descriptor2) {
        try {
            return this.posix.isatty(descriptor2);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int kill(int pid, int signal) {
        return this.kill((long)pid, signal);
    }

    @Override
    public int kill(long pid, int signal) {
        try {
            return this.posix.kill(pid, signal);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public SignalHandler signal(Signal sig, SignalHandler handler) {
        try {
            return this.posix.signal(sig, handler);
        }
        catch (UnsatisfiedLinkError ule) {
            return (SignalHandler)this.unimplementedNull();
        }
    }

    @Override
    public int raise(int sig) {
        try {
            return this.posix.raise(sig);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int lchmod(String filename, int mode) {
        try {
            return this.posix.lchmod(filename, mode);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int lchown(String filename, int user, int group) {
        try {
            return this.posix.lchown(filename, user, group);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int link(String oldpath, String newpath) {
        try {
            return this.posix.link(oldpath, newpath);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public FileStat lstat(String path) {
        try {
            return this.posix.lstat(path);
        }
        catch (UnsatisfiedLinkError ule) {
            return (FileStat)this.unimplementedNull();
        }
    }

    @Override
    public int lstat(String path, FileStat stat) {
        try {
            return this.posix.lstat(path, stat);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int mkdir(String path, int mode) {
        try {
            return this.posix.mkdir(path, mode);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public String readlink(String path) throws IOException {
        try {
            return this.posix.readlink(path);
        }
        catch (UnsatisfiedLinkError ule) {
            return (String)this.unimplementedNull();
        }
    }

    @Override
    public int readlink(CharSequence path, byte[] buf, int bufsize) {
        try {
            return this.posix.readlink(path, buf, bufsize);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int readlink(CharSequence path, ByteBuffer buf, int bufsize) {
        try {
            return this.posix.readlink(path, buf, bufsize);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int readlink(CharSequence path, Pointer bufPtr, int bufsize) {
        try {
            return this.posix.readlink(path, bufPtr, bufsize);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int rmdir(String path) {
        try {
            return this.posix.rmdir(path);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int setegid(int egid) {
        try {
            return this.posix.setegid(egid);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int seteuid(int euid) {
        try {
            return this.posix.seteuid(euid);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int setgid(int gid) {
        try {
            return this.posix.setgid(gid);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int setgrent() {
        try {
            return this.posix.setgrent();
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int setpgid(int pid, int pgid) {
        try {
            return this.posix.setpgid(pid, pgid);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int setpgrp(int pid, int pgrp) {
        try {
            return this.posix.setpgrp(pid, pgrp);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int setpriority(int which, int who, int prio) {
        try {
            return this.posix.setpriority(which, who, prio);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int setpwent() {
        try {
            return this.posix.setpwent();
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int setsid() {
        try {
            return this.posix.setsid();
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int setuid(int uid) {
        try {
            return this.posix.setuid(uid);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public FileStat stat(String path) {
        try {
            return this.posix.stat(path);
        }
        catch (UnsatisfiedLinkError ule) {
            return (FileStat)this.unimplementedNull();
        }
    }

    @Override
    public int stat(String path, FileStat stat) {
        try {
            return this.posix.stat(path, stat);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int symlink(String oldpath, String newpath) {
        try {
            return this.posix.symlink(oldpath, newpath);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int umask(int mask) {
        try {
            return this.posix.umask(mask);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int utimes(String path, long[] atimeval, long[] mtimeval) {
        try {
            return this.posix.utimes(path, atimeval, mtimeval);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int utimes(String path, Pointer times) {
        try {
            return this.posix.utimes(path, times);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int futimes(int fd, long[] atimeval, long[] mtimeval) {
        try {
            return this.posix.futimes(fd, atimeval, mtimeval);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int lutimes(String path, long[] atimeval, long[] mtimeval) {
        try {
            return this.posix.lutimes(path, atimeval, mtimeval);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int utimensat(int dirfd, String path, long[] atimespec, long[] mtimespec, int flag) {
        try {
            return this.posix.utimensat(dirfd, path, atimespec, mtimespec, flag);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int utimensat(int dirfd, String path, Pointer times, int flag) {
        try {
            return this.posix.utimensat(dirfd, path, times, flag);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int futimens(int fd, long[] atimespec, long[] mtimespec) {
        try {
            return this.posix.futimens(fd, atimespec, mtimespec);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int futimens(int fd, Pointer times) {
        try {
            return this.posix.futimens(fd, times);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int wait(int[] status) {
        try {
            return this.posix.wait(status);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int waitpid(int pid, int[] status, int flags) {
        return this.waitpid((long)pid, status, flags);
    }

    @Override
    public int waitpid(long pid, int[] status, int flags) {
        try {
            return this.posix.waitpid(pid, status, flags);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public boolean isNative() {
        return this.posix.isNative();
    }

    @Override
    public LibC libc() {
        return this.posix.libc();
    }

    @Override
    public Pointer environ() {
        try {
            return this.posix.environ();
        }
        catch (UnsatisfiedLinkError ule) {
            return (Pointer)this.unimplementedNull();
        }
    }

    @Override
    public String getenv(String envName) {
        try {
            return this.posix.getenv(envName);
        }
        catch (UnsatisfiedLinkError ule) {
            return (String)this.unimplementedNull();
        }
    }

    @Override
    public int setenv(String envName, String envValue, int overwrite) {
        try {
            return this.posix.setenv(envName, envValue, overwrite);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int unsetenv(String envName) {
        try {
            return this.posix.unsetenv(envName);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public long posix_spawnp(String path, Collection<? extends SpawnFileAction> fileActions, Collection<? extends CharSequence> argv, Collection<? extends CharSequence> envp) {
        try {
            return this.posix.posix_spawnp(path, fileActions, argv, envp);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public long posix_spawnp(String path, Collection<? extends SpawnFileAction> fileActions, Collection<? extends SpawnAttribute> spawnAttributes, Collection<? extends CharSequence> argv, Collection<? extends CharSequence> envp) {
        try {
            return this.posix.posix_spawnp(path, fileActions, spawnAttributes, argv, envp);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public long sysconf(Sysconf name) {
        try {
            return this.posix.sysconf(name);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int confstr(Confstr name, ByteBuffer buf, int len) {
        try {
            return this.posix.confstr(name, buf, len);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int fpathconf(int fd, Pathconf name) {
        try {
            return this.posix.fpathconf(fd, name);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public Times times() {
        try {
            return this.posix.times();
        }
        catch (UnsatisfiedLinkError ule) {
            return (Times)this.unimplementedNull();
        }
    }

    @Override
    public int flock(int fd, int mode) {
        return this.posix.flock(fd, mode);
    }

    @Override
    public int dup(int fd) {
        try {
            return this.posix.dup(fd);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int dup2(int oldFd, int newFd) {
        try {
            return this.posix.dup2(oldFd, newFd);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int fcntlInt(int fd, Fcntl fcntlConst, int arg) {
        try {
            return this.posix.fcntlInt(fd, fcntlConst, arg);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int fcntl(int fd, Fcntl fcntlConst) {
        try {
            return this.posix.fcntl(fd, fcntlConst);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int fcntl(int fd, Fcntl fcntlConst, int arg) {
        try {
            return this.posix.fcntl(fd, fcntlConst, arg);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    @Deprecated
    public int fcntl(int fd, Fcntl fcntlConst, int ... arg) {
        try {
            return this.posix.fcntl(fd, fcntlConst);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int access(CharSequence path, int amode) {
        try {
            return this.posix.access(path, amode);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int close(int fd) {
        try {
            return this.posix.close(fd);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int unlink(CharSequence path) {
        try {
            return this.posix.unlink(path);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int open(CharSequence path, int flags, int perm) {
        try {
            return this.posix.open(path, flags, perm);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public long read(int fd, byte[] buf, long n) {
        try {
            return this.posix.read(fd, buf, n);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public long write(int fd, byte[] buf, long n) {
        try {
            return this.posix.write(fd, buf, n);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public long read(int fd, ByteBuffer buf, long n) {
        try {
            return this.posix.read(fd, buf, n);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public long write(int fd, ByteBuffer buf, long n) {
        try {
            return this.posix.write(fd, buf, n);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public long pread(int fd, byte[] buf, long n, long offset) {
        try {
            return this.posix.pread(fd, buf, n, offset);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public long pwrite(int fd, byte[] buf, long n, long offset) {
        try {
            return this.posix.pwrite(fd, buf, n, offset);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public long pread(int fd, ByteBuffer buf, long n, long offset) {
        try {
            return this.posix.pread(fd, buf, n, offset);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public long pwrite(int fd, ByteBuffer buf, long n, long offset) {
        try {
            return this.posix.pwrite(fd, buf, n, offset);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int read(int fd, byte[] buf, int n) {
        try {
            return this.posix.read(fd, buf, n);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int write(int fd, byte[] buf, int n) {
        try {
            return this.posix.write(fd, buf, n);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int read(int fd, ByteBuffer buf, int n) {
        try {
            return this.posix.read(fd, buf, n);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int write(int fd, ByteBuffer buf, int n) {
        try {
            return this.posix.write(fd, buf, n);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int pread(int fd, byte[] buf, int n, int offset) {
        try {
            return this.posix.pread(fd, buf, n, offset);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int pwrite(int fd, byte[] buf, int n, int offset) {
        try {
            return this.posix.pwrite(fd, buf, n, offset);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int pread(int fd, ByteBuffer buf, int n, int offset) {
        try {
            return this.posix.pread(fd, buf, n, offset);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int pwrite(int fd, ByteBuffer buf, int n, int offset) {
        try {
            return this.posix.pwrite(fd, buf, n, offset);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int lseek(int fd, long offset, int whence) {
        try {
            return this.posix.lseek(fd, offset, whence);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public long lseekLong(int fd, long offset, int whence) {
        try {
            return this.posix.lseekLong(fd, offset, whence);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int pipe(int[] fds) {
        try {
            return this.posix.pipe(fds);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int socketpair(int domain, int type, int protocol, int[] fds) {
        try {
            return this.posix.socketpair(domain, type, protocol, fds);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int sendmsg(int socket, MsgHdr message, int flags) {
        try {
            return this.posix.sendmsg(socket, message, flags);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int recvmsg(int socket, MsgHdr message, int flags) {
        try {
            return this.posix.recvmsg(socket, message, flags);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int truncate(CharSequence path, long length) {
        try {
            return this.posix.truncate(path, length);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int ftruncate(int fd, long offset) {
        try {
            return this.posix.ftruncate(fd, offset);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int rename(CharSequence oldName, CharSequence newName) {
        try {
            return this.posix.rename(oldName, newName);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public String getcwd() {
        try {
            return this.posix.getcwd();
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedString();
        }
    }

    @Override
    public int fsync(int fd) {
        try {
            return this.posix.fsync(fd);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int fdatasync(int fd) {
        try {
            return this.posix.fsync(fd);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int mkfifo(String path, int mode) {
        try {
            return this.posix.mkfifo(path, mode);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public int daemon(int nochdir, int noclose) {
        try {
            return this.posix.daemon(nochdir, noclose);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public long[] getgroups() {
        try {
            return this.posix.getgroups();
        }
        catch (UnsatisfiedLinkError ule) {
            return null;
        }
    }

    @Override
    public int getgroups(int size, int[] groups2) {
        try {
            return this.posix.getgroups(size, groups2);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public String nl_langinfo(int item) {
        try {
            return this.posix.nl_langinfo(item);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedString();
        }
    }

    @Override
    public String setlocale(int category, String locale) {
        try {
            return this.posix.setlocale(category, locale);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedString();
        }
    }

    @Override
    public String strerror(int code) {
        try {
            return this.posix.strerror(code);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedString();
        }
    }

    @Override
    public Timeval allocateTimeval() {
        try {
            return this.posix.allocateTimeval();
        }
        catch (UnsatisfiedLinkError ule) {
            return (Timeval)this.unimplementedNull();
        }
    }

    @Override
    public int gettimeofday(Timeval tv) {
        try {
            return this.posix.gettimeofday(tv);
        }
        catch (UnsatisfiedLinkError ule) {
            return this.unimplementedInt();
        }
    }

    @Override
    public String gethostname() {
        try {
            return this.posix.gethostname();
        }
        catch (UnsatisfiedLinkError ule) {
            return (String)this.unimplementedNull();
        }
    }
}

