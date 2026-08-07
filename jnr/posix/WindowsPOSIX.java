
package jnr.posix;

import java.io.FileDescriptor;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import jnr.constants.platform.Errno;
import jnr.constants.platform.Fcntl;
import jnr.constants.platform.OpenFlags;
import jnr.constants.platform.WaitFlags;
import jnr.constants.platform.windows.LastError;
import jnr.ffi.Pointer;
import jnr.ffi.byref.IntByReference;
import jnr.ffi.mapper.FromNativeContext;
import jnr.posix.BaseNativePOSIX;
import jnr.posix.DefaultNativeTimeval;
import jnr.posix.FileStat;
import jnr.posix.FileTime;
import jnr.posix.Group;
import jnr.posix.HANDLE;
import jnr.posix.JavaLibCHelper;
import jnr.posix.LibCProvider;
import jnr.posix.MsgHdr;
import jnr.posix.POSIXHandler;
import jnr.posix.Passwd;
import jnr.posix.SocketMacros;
import jnr.posix.Timeval;
import jnr.posix.WString;
import jnr.posix.WindowsChildRecord;
import jnr.posix.WindowsFileStat;
import jnr.posix.WindowsLibC;
import jnr.posix.WindowsProcessInformation;
import jnr.posix.WindowsRawFileStat;
import jnr.posix.WindowsSecurityAttributes;
import jnr.posix.WindowsStartupInfo;
import jnr.posix.util.MethodName;
import jnr.posix.util.WindowsHelpers;
import jnr.posix.windows.WindowsByHandleFileInformation;
import jnr.posix.windows.WindowsFileInformation;
import jnr.posix.windows.WindowsFindData;

public final class WindowsPOSIX
extends BaseNativePOSIX {
    private static final int FILE_TYPE_CHAR = 2;
    private static final Map<Integer, Errno> errorToErrnoMapper = new HashMap<Integer, Errno>();
    private final FileStat checkFdStat = new WindowsFileStat(this);
    private static final int GENERIC_ALL = 0x10000000;
    private static final int GENERIC_READ = Integer.MIN_VALUE;
    private static final int GENERIC_WRITE = 0x40000000;
    private static final int GENERIC_EXECUTE = 0x2000000;
    private static final int FILE_SHARE_DELETE = 4;
    private static final int FILE_SHARE_READ = 1;
    private static final int FILE_SHARE_WRITE = 2;
    private static final int CREATE_ALWAYS = 2;
    private static final int CREATE_NEW = 1;
    private static final int OPEN_ALWAYS = 4;
    private static final int OPEN_EXISTING = 3;
    private static final int TRUNCATE_EXISTING = 5;
    public static final int FILE_FLAG_BACKUP_SEMANTICS = 0x2000000;
    static final int FILE_ATTRIBUTE_READONLY = 1;
    static final int INVALID_FILE_ATTRIBUTES = -1;
    private static final int STARTF_USESTDHANDLES = 256;
    public static final BaseNativePOSIX.PointerConverter PASSWD;

    WindowsPOSIX(LibCProvider libc, POSIXHandler handler) {
        super(libc, handler);
    }

    @Override
    public FileStat allocateStat() {
        return new WindowsRawFileStat(this, this.handler);
    }

    @Override
    public MsgHdr allocateMsgHdr() {
        this.handler.unimplementedError(MethodName.getCallerMethodName());
        return null;
    }

    @Override
    public SocketMacros socketMacros() {
        this.handler.unimplementedError(MethodName.getCallerMethodName());
        return null;
    }

    @Override
    public int kill(int pid, int signal) {
        this.handler.unimplementedError("kill");
        return -1;
    }

    @Override
    public int kill(long pid, int signal) {
        this.handler.unimplementedError("kill");
        return -1;
    }

    @Override
    public int chmod(String filename, int mode) {
        return this.wlibc()._wchmod(WString.path(filename), mode);
    }

    @Override
    public int chdir(String path) {
        return this.wlibc()._wchdir(WString.path(path));
    }

    @Override
    public int chown(String filename, int user, int group) {
        return 0;
    }

    @Override
    public int exec(String path, String[] argv) {
        if (argv.length == 1) {
            return this.spawn(true, argv[0], null, path, null);
        }
        return this.aspawn(true, null, argv, path, null);
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
    public int exec(String path, String[] argv, String[] envp) {
        if (argv.length == 1) {
            return this.spawn(true, argv[0], null, path, envp);
        }
        return this.aspawn(true, null, argv, path, envp);
    }

    @Override
    public int execv(String path, String[] argv) {
        this.handler.unimplementedError("egid");
        return -1;
    }

    @Override
    public int getegid() {
        this.handler.unimplementedError("egid");
        return -1;
    }

    @Override
    public int setegid(int egid) {
        this.handler.unimplementedError("setegid");
        return -1;
    }

    @Override
    public int geteuid() {
        return 0;
    }

    @Override
    public int seteuid(int euid) {
        this.handler.unimplementedError("seteuid");
        return -1;
    }

    @Override
    public int getuid() {
        return 0;
    }

    @Override
    public int setuid(int uid) {
        this.handler.unimplementedError("setuid");
        return -1;
    }

    @Override
    public int getgid() {
        return 0;
    }

    @Override
    public int setgid(int gid) {
        this.handler.unimplementedError("setgid");
        return -1;
    }

    @Override
    public int getpgid(int pid) {
        this.handler.unimplementedError("getpgid");
        return -1;
    }

    @Override
    public int getpgid() {
        this.handler.unimplementedError("getpgid");
        return -1;
    }

    @Override
    public int setpgid(int pid, int pgid) {
        this.handler.unimplementedError("setpgid");
        return -1;
    }

    @Override
    public int getpriority(int which, int who) {
        this.handler.unimplementedError("getpriority");
        return -1;
    }

    @Override
    public int setpriority(int which, int who, int prio) {
        this.handler.unimplementedError("setpriority");
        return -1;
    }

    @Override
    public int getpid() {
        return this.wlibc()._getpid();
    }

    @Override
    public int getppid() {
        return 0;
    }

    @Override
    public int lchmod(String filename, int mode) {
        this.handler.unimplementedError("lchmod");
        return -1;
    }

    @Override
    public int lchown(String filename, int user, int group) {
        this.handler.unimplementedError("lchown");
        return -1;
    }

    @Override
    public String gethostname() {
        ByteBuffer buffer = ByteBuffer.allocate(64);
        IntByReference len = new IntByReference(buffer.capacity() - 1);
        if (!this.wlibc().GetComputerNameW(buffer, len)) {
            return this.helper.gethostname();
        }
        buffer.limit(len.intValue() * 2);
        return Charset.forName("UTF-16LE").decode(buffer).toString();
    }

    @Override
    public FileStat fstat(int fd) {
        WindowsFileStat stat = new WindowsFileStat(this);
        if (this.fstat(fd, (FileStat)stat) < 0) {
            this.handler.error(Errno.valueOf(this.errno()), "fstat", "" + fd);
        }
        return stat;
    }

    @Override
    public int fstat(FileDescriptor fileDescriptor, FileStat stat) {
        WindowsByHandleFileInformation info = new WindowsByHandleFileInformation(this.getRuntime());
        if (this.wlibc().GetFileInformationByHandle(JavaLibCHelper.gethandle(fileDescriptor), info) == 0) {
            return -1;
        }
        ((WindowsRawFileStat)stat).setup(info);
        return 0;
    }

    @Override
    public FileStat lstat(String path) {
        return this.stat(path);
    }

    @Override
    public int lstat(String path, FileStat stat) {
        return this.stat(path, stat);
    }

    @Override
    public int stat(String path, FileStat stat) {
        WindowsFileInformation info = new WindowsFileInformation(this.getRuntime());
        byte[] wpath = WString.path(path, true);
        if (this.wlibc().GetFileAttributesExW(wpath, 0, info) == 0) {
            int e = this.errno();
            if (e == LastError.ERROR_FILE_NOT_FOUND.intValue() || e == LastError.ERROR_PATH_NOT_FOUND.intValue() || e == LastError.ERROR_BAD_NETPATH.intValue()) {
                return -1;
            }
            return this.findFirstFile(path, stat);
        }
        ((WindowsRawFileStat)stat).setup(path, info);
        return 0;
    }

    public int findFirstFile(String path, FileStat stat) {
        byte[] wpath = WString.path(path, true);
        WindowsFindData findData = new WindowsFindData(this.getRuntime());
        HANDLE handle = this.wlibc().FindFirstFileW(wpath, findData);
        if (!handle.isValid()) {
            return -1;
        }
        this.wlibc().FindClose(handle);
        ((WindowsRawFileStat)stat).setup(path, findData);
        return 0;
    }

    @Override
    public String readlink(String oldpath) {
        this.handler.unimplementedError("readlink");
        return null;
    }

    @Override
    public Pointer environ() {
        return this.getRuntime().getMemoryManager().newPointer(this.wlibc()._environ().get());
    }

    @Override
    public int setenv(String envName, String envValue, int overwrite) {
        if (envName.contains("=")) {
            this.handler.error(Errno.EINVAL, "setenv", envName);
            return -1;
        }
        if (!this.wlibc().SetEnvironmentVariableW(new WString(envName), new WString(envValue))) {
            this.handler.error(Errno.EINVAL, "setenv", envName);
            return -1;
        }
        return 0;
    }

    @Override
    public int umask(int mask) {
        return this.wlibc()._umask(mask);
    }

    @Override
    public int unsetenv(String envName) {
        if (!this.wlibc().SetEnvironmentVariableW(new WString(envName), null)) {
            this.handler.error(Errno.EINVAL, "unsetenv", envName);
            return -1;
        }
        return 0;
    }

    @Override
    public int utimes(String path, long[] atimeval, long[] mtimeval) {
        FileTime aTime = this.timevalToFileTime(atimeval);
        FileTime mTime = this.timevalToFileTime(mtimeval);
        return this.setFileTime(path, aTime, mTime);
    }

    @Override
    public int utimensat(int dirfd, String path, long[] atimespec, long[] mtimespec, int flag) {
        FileTime aTime = this.timespecToFileTime(atimespec);
        FileTime mTime = this.timespecToFileTime(mtimespec);
        return this.setFileTime(path, aTime, mTime);
    }

    private FileTime timevalToFileTime(long[] timeval) {
        if (timeval == null) {
            return this.currentFileTime();
        }
        long unixEpochIn100ns = timeval[0] * 10000000L + timeval[1] * 10L;
        return this.unixTimeToFileTime(unixEpochIn100ns);
    }

    private FileTime timespecToFileTime(long[] timespec) {
        if (timespec == null) {
            return this.currentFileTime();
        }
        long unixEpochIn100ns = timespec[0] * 10000000L + timespec[1] / 100L;
        return this.unixTimeToFileTime(unixEpochIn100ns);
    }

    private int setFileTime(String path, FileTime aTime, FileTime mTime) {
        byte[] wpath = WindowsHelpers.toWPath(path);
        HANDLE handle = this.wlibc().CreateFileW(wpath, 0x40000000, 3, null, 3, 0x2000000, 0);
        if (!handle.isValid()) {
            return -1;
        }
        boolean timeSet = this.wlibc().SetFileTime(handle, null, aTime, mTime);
        this.wlibc().CloseHandle(handle);
        return timeSet ? 0 : -1;
    }

    private FileTime unixTimeToFileTime(long unixEpochIn100ns) {
        long ft = 116444736000000000L + unixEpochIn100ns;
        FileTime fileTime = new FileTime(this.getRuntime());
        fileTime.dwLowDateTime.set(ft & 0xFFFFFFFFL);
        fileTime.dwHighDateTime.set(ft >> 32 & 0xFFFFFFFFL);
        return fileTime;
    }

    private FileTime nullFileTime() {
        FileTime fileTime = new FileTime(this.getRuntime());
        fileTime.dwLowDateTime.set(0L);
        fileTime.dwHighDateTime.set(0L);
        return fileTime;
    }

    private FileTime currentFileTime() {
        return this.unixTimeToFileTime(System.currentTimeMillis() * 10000L);
    }

    @Override
    public int wait(int[] status) {
        this.handler.unimplementedError("wait");
        return -1;
    }

    @Override
    public int waitpid(int pid, int[] status, int flags) {
        HANDLE h;
        if (pid <= 0) {
            this.handler.unimplementedError("waitpid");
        }
        if ((h = this.wlibc().OpenProcess(1024, 0, pid)) == null) {
            return -1;
        }
        if ((flags & WaitFlags.WNOHANG.intValue()) != 0) {
            this.wlibc().WaitForSingleObject(h, -1);
        }
        IntByReference exitCode = new IntByReference();
        this.wlibc().GetExitCodeProcess(h, exitCode);
        this.wlibc().CloseHandle(h);
        int code = (Integer)exitCode.getValue();
        if (code == 259) {
            return 0;
        }
        status[0] = code;
        return pid;
    }

    @Override
    public int waitpid(long pid, int[] status, int flags) {
        if (pid > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("waitpid");
        }
        return this.waitpid((int)pid, status, flags);
    }

    @Override
    public String getlogin() {
        return this.helper.getlogin();
    }

    @Override
    public int endgrent() {
        return 0;
    }

    @Override
    public int endpwent() {
        return this.helper.endpwent();
    }

    @Override
    public Group getgrent() {
        return null;
    }

    @Override
    public Passwd getpwent() {
        return null;
    }

    @Override
    public Group getgrgid(int which) {
        return null;
    }

    @Override
    public Passwd getpwnam(String which) {
        return null;
    }

    @Override
    public Group getgrnam(String which) {
        return null;
    }

    @Override
    public int setgrent() {
        return 0;
    }

    @Override
    public int setpwent() {
        return this.helper.setpwent();
    }

    @Override
    public Passwd getpwuid(int which) {
        return null;
    }

    @Override
    public boolean isatty(FileDescriptor fd) {
        HANDLE handle = JavaLibCHelper.gethandle(fd);
        int type = this.wlibc().GetFileType(handle);
        return type == 2;
    }

    @Override
    public int isatty(int fd) {
        HANDLE handle = JavaLibCHelper.gethandle(fd);
        int type = this.wlibc().GetFileType(handle);
        return type == 2 ? 1 : 0;
    }

    @Override
    public int mkdir(String path, int mode) {
        WString widePath = WString.path(path);
        int res = -1;
        if (this.wlibc()._wmkdir(widePath) == 0) {
            res = this.wlibc()._wchmod(widePath, mode);
        }
        if (res < 0) {
            int errno = this.errno();
            this.handler.error(Errno.valueOf(errno), "mkdir", path);
        }
        return res;
    }

    @Override
    public int rmdir(String path) {
        boolean isReadOnly;
        WString pathW = WString.path(path);
        int attr = this.wlibc().GetFileAttributesW(pathW);
        boolean bl = isReadOnly = attr != -1 && (attr & 1) != 0;
        if (isReadOnly) {
            this.wlibc().SetFileAttributesW(pathW, attr & 0xFFFFFFFE);
        }
        if (!this.wlibc().RemoveDirectoryW(pathW)) {
            int errno = this.errno();
            if (isReadOnly) {
                this.wlibc().SetFileAttributesW(pathW, attr & 1);
            }
            this.handler.error(WindowsPOSIX.mapErrorToErrno(errno), "rmdir", path);
            return -1;
        }
        return 0;
    }

    @Override
    public int link(String oldpath, String newpath) {
        boolean linkCreated = this.wlibc().CreateHardLinkW(WString.path(newpath), WString.path(oldpath), null);
        if (!linkCreated) {
            int error = this.errno();
            this.handler.error(WindowsPOSIX.mapErrorToErrno(error), "link", oldpath + " or " + newpath);
            return error;
        }
        return 0;
    }

    public int aspawn(boolean overlay, String program, String[] argv, String path, String[] envp) {
        try {
            if (argv.length == 0) {
                return -1;
            }
            String[] cmds = WindowsHelpers.processCommandArgs(this, program, argv, path);
            return this.childResult(this.createProcess("aspawn", cmds[0], cmds[1], null, null, null, null, envp), overlay);
        }
        catch (Exception e) {
            return -1;
        }
    }

    @Override
    public int pipe(int[] fds) {
        return ((WindowsLibC)this.libc())._pipe(fds, 512, 0);
    }

    @Override
    public int truncate(CharSequence path, long length) {
        int fd = this.libc().open(path, OpenFlags.O_WRONLY.intValue(), 0);
        if (fd == -1) {
            return -1;
        }
        if (this.libc().ftruncate(fd, length) == -1) {
            return -1;
        }
        if (this.libc().close(fd) == -1) {
            return -1;
        }
        return 0;
    }

    @Override
    public int fcntlInt(int fd, Fcntl fcntl, int arg) {
        switch (fcntl) {
            case F_GETFD: {
                if (this.checkFd(fd) == -1) {
                    return -1;
                }
                return 0;
            }
            case F_SETFD: {
                if (this.checkFd(fd) == -1) {
                    return -1;
                }
                return 0;
            }
            case F_GETFL: {
                if (this.checkFd(fd) == -1) {
                    return -1;
                }
                return OpenFlags.O_RDWR.intValue();
            }
        }
        this.handler.unimplementedError("fcntl");
        return -1;
    }

    private WindowsLibC wlibc() {
        return (WindowsLibC)this.libc();
    }

    public int spawn(boolean overlay, String command, String program, String path, String[] envp) {
        if (command == null) {
            return -1;
        }
        String[] cmds = WindowsHelpers.processCommandLine(this, command, program, path);
        return this.childResult(this.createProcess("spawn", cmds[0], cmds[1], null, null, null, null, envp), overlay);
    }

    private int childResult(WindowsChildRecord child, boolean overlay) {
        if (child == null) {
            return -1;
        }
        if (overlay) {
            IntByReference exitCode = new IntByReference();
            WindowsLibC libc = (WindowsLibC)this.libc();
            HANDLE handle = child.getProcess();
            libc.WaitForSingleObject(handle, -1);
            libc.GetExitCodeProcess(handle, exitCode);
            libc.CloseHandle(handle);
            System.exit((Integer)exitCode.getValue());
        }
        return child.getPid();
    }

    private static Errno mapErrorToErrno(int error) {
        Errno errno = errorToErrnoMapper.get(error);
        if (errno == null) {
            errno = Errno.__UNKNOWN_CONSTANT__;
        }
        return errno;
    }

    private WindowsChildRecord createProcess(String callingMethodName, String command, String program, WindowsSecurityAttributes securityAttributes, HANDLE input, HANDLE output, HANDLE error, String[] envp) {
        if (command == null && program == null) {
            this.handler.error(Errno.EFAULT, callingMethodName, "no command or program specified");
            return null;
        }
        if (securityAttributes == null) {
            securityAttributes = new WindowsSecurityAttributes(this.getRuntime());
        }
        WindowsStartupInfo startupInfo = new WindowsStartupInfo(this.getRuntime());
        startupInfo.setFlags(256);
        startupInfo.setStandardInput(input != null ? input : this.wlibc().GetStdHandle(-10));
        startupInfo.setStandardOutput(output != null ? output : this.wlibc().GetStdHandle(-11));
        startupInfo.setStandardError(error != null ? input : this.wlibc().GetStdHandle(-12));
        int creationFlags = 1056;
        WindowsProcessInformation processInformation = new WindowsProcessInformation(this.getRuntime());
        Pointer wideEnv = null;
        byte[] programW = WindowsHelpers.toWString(program);
        byte[] cwd = WindowsHelpers.toWString(WindowsHelpers.escapePath(this.handler.getCurrentWorkingDirectory().toString()) + "\\");
        ByteBuffer commandW = ByteBuffer.wrap(WindowsHelpers.toWString(command));
        boolean returnValue = this.wlibc().CreateProcessW(programW, commandW, securityAttributes, securityAttributes, securityAttributes.getInheritHandle() ? 1 : 0, creationFlags, wideEnv, cwd, startupInfo, processInformation);
        if (!returnValue) {
            return null;
        }
        this.wlibc().CloseHandle(processInformation.getThread());
        return new WindowsChildRecord(processInformation.getProcess(), processInformation.getPid());
    }

    private int checkFd(int fd) {
        return this.libc().fstat(fd, this.checkFdStat);
    }

    @Override
    public int mkfifo(String filename, int mode) {
        this.handler.unimplementedError("mkfifo");
        return -1;
    }

    @Override
    public Timeval allocateTimeval() {
        return new DefaultNativeTimeval(this.getRuntime());
    }

    @Override
    public int gettimeofday(Timeval tv) {
        long currentMillis = System.currentTimeMillis();
        tv.sec(currentMillis / 1000L);
        tv.usec(currentMillis % 1000L * 1000L);
        return 0;
    }

    static {
        errorToErrnoMapper.put(LastError.ERROR_INVALID_FUNCTION.value(), Errno.EINVAL);
        errorToErrnoMapper.put(LastError.ERROR_FILE_NOT_FOUND.value(), Errno.ENOENT);
        errorToErrnoMapper.put(LastError.ERROR_PATH_NOT_FOUND.value(), Errno.ENOENT);
        errorToErrnoMapper.put(LastError.ERROR_TOO_MANY_OPEN_FILES.value(), Errno.EMFILE);
        errorToErrnoMapper.put(LastError.ERROR_ACCESS_DENIED.value(), Errno.EACCES);
        errorToErrnoMapper.put(LastError.ERROR_INVALID_HANDLE.value(), Errno.EBADF);
        errorToErrnoMapper.put(LastError.ERROR_ARENA_TRASHED.value(), Errno.ENOMEM);
        errorToErrnoMapper.put(LastError.ERROR_NOT_ENOUGH_MEMORY.value(), Errno.ENOMEM);
        errorToErrnoMapper.put(LastError.ERROR_INVALID_BLOCK.value(), Errno.ENOMEM);
        errorToErrnoMapper.put(LastError.ERROR_BAD_ENVIRONMENT.value(), Errno.E2BIG);
        errorToErrnoMapper.put(LastError.ERROR_BAD_FORMAT.value(), Errno.ENOEXEC);
        errorToErrnoMapper.put(LastError.ERROR_INVALID_ACCESS.value(), Errno.EINVAL);
        errorToErrnoMapper.put(LastError.ERROR_INVALID_DATA.value(), Errno.EINVAL);
        errorToErrnoMapper.put(LastError.ERROR_INVALID_DRIVE.value(), Errno.ENOENT);
        errorToErrnoMapper.put(LastError.ERROR_CURRENT_DIRECTORY.value(), Errno.EACCES);
        errorToErrnoMapper.put(LastError.ERROR_NOT_SAME_DEVICE.value(), Errno.EXDEV);
        errorToErrnoMapper.put(LastError.ERROR_NO_MORE_FILES.value(), Errno.ENOENT);
        errorToErrnoMapper.put(LastError.ERROR_WRITE_PROTECT.value(), Errno.EROFS);
        errorToErrnoMapper.put(LastError.ERROR_BAD_UNIT.value(), Errno.ENODEV);
        errorToErrnoMapper.put(LastError.ERROR_NOT_READY.value(), Errno.ENXIO);
        errorToErrnoMapper.put(LastError.ERROR_BAD_COMMAND.value(), Errno.EACCES);
        errorToErrnoMapper.put(LastError.ERROR_CRC.value(), Errno.EACCES);
        errorToErrnoMapper.put(LastError.ERROR_BAD_LENGTH.value(), Errno.EACCES);
        errorToErrnoMapper.put(LastError.ERROR_SEEK.value(), Errno.EIO);
        errorToErrnoMapper.put(LastError.ERROR_NOT_DOS_DISK.value(), Errno.EACCES);
        errorToErrnoMapper.put(LastError.ERROR_SECTOR_NOT_FOUND.value(), Errno.EACCES);
        errorToErrnoMapper.put(LastError.ERROR_OUT_OF_PAPER.value(), Errno.EACCES);
        errorToErrnoMapper.put(LastError.ERROR_WRITE_FAULT.value(), Errno.EIO);
        errorToErrnoMapper.put(LastError.ERROR_READ_FAULT.value(), Errno.EIO);
        errorToErrnoMapper.put(LastError.ERROR_GEN_FAILURE.value(), Errno.EACCES);
        errorToErrnoMapper.put(LastError.ERROR_LOCK_VIOLATION.value(), Errno.EACCES);
        errorToErrnoMapper.put(LastError.ERROR_SHARING_VIOLATION.value(), Errno.EACCES);
        errorToErrnoMapper.put(LastError.ERROR_WRONG_DISK.value(), Errno.EACCES);
        errorToErrnoMapper.put(LastError.ERROR_SHARING_BUFFER_EXCEEDED.value(), Errno.EACCES);
        errorToErrnoMapper.put(LastError.ERROR_BAD_NETPATH.value(), Errno.ENOENT);
        errorToErrnoMapper.put(LastError.ERROR_NETWORK_ACCESS_DENIED.value(), Errno.EACCES);
        errorToErrnoMapper.put(LastError.ERROR_BAD_NET_NAME.value(), Errno.ENOENT);
        errorToErrnoMapper.put(LastError.ERROR_FILE_EXISTS.value(), Errno.EEXIST);
        errorToErrnoMapper.put(LastError.ERROR_CANNOT_MAKE.value(), Errno.EACCES);
        errorToErrnoMapper.put(LastError.ERROR_FAIL_I24.value(), Errno.EACCES);
        errorToErrnoMapper.put(LastError.ERROR_INVALID_PARAMETER.value(), Errno.EINVAL);
        errorToErrnoMapper.put(LastError.ERROR_NO_PROC_SLOTS.value(), Errno.EAGAIN);
        errorToErrnoMapper.put(LastError.ERROR_DRIVE_LOCKED.value(), Errno.EACCES);
        errorToErrnoMapper.put(LastError.ERROR_BROKEN_PIPE.value(), Errno.EPIPE);
        errorToErrnoMapper.put(LastError.ERROR_DISK_FULL.value(), Errno.ENOSPC);
        errorToErrnoMapper.put(LastError.ERROR_INVALID_TARGET_HANDLE.value(), Errno.EBADF);
        errorToErrnoMapper.put(LastError.ERROR_INVALID_HANDLE.value(), Errno.EINVAL);
        errorToErrnoMapper.put(LastError.ERROR_WAIT_NO_CHILDREN.value(), Errno.ECHILD);
        errorToErrnoMapper.put(LastError.ERROR_CHILD_NOT_COMPLETE.value(), Errno.ECHILD);
        errorToErrnoMapper.put(LastError.ERROR_DIRECT_ACCESS_HANDLE.value(), Errno.EBADF);
        errorToErrnoMapper.put(LastError.ERROR_NEGATIVE_SEEK.value(), Errno.EINVAL);
        errorToErrnoMapper.put(LastError.ERROR_SEEK_ON_DEVICE.value(), Errno.EACCES);
        errorToErrnoMapper.put(LastError.ERROR_DIR_NOT_EMPTY.value(), Errno.ENOTEMPTY);
        errorToErrnoMapper.put(LastError.ERROR_DIRECTORY.value(), Errno.ENOTDIR);
        errorToErrnoMapper.put(LastError.ERROR_NOT_LOCKED.value(), Errno.EACCES);
        errorToErrnoMapper.put(LastError.ERROR_BAD_PATHNAME.value(), Errno.ENOENT);
        errorToErrnoMapper.put(LastError.ERROR_MAX_THRDS_REACHED.value(), Errno.EAGAIN);
        errorToErrnoMapper.put(LastError.ERROR_LOCK_FAILED.value(), Errno.EACCES);
        errorToErrnoMapper.put(LastError.ERROR_ALREADY_EXISTS.value(), Errno.EEXIST);
        errorToErrnoMapper.put(LastError.ERROR_INVALID_STARTING_CODESEG.value(), Errno.ENOEXEC);
        errorToErrnoMapper.put(LastError.ERROR_INVALID_STACKSEG.value(), Errno.ENOEXEC);
        errorToErrnoMapper.put(LastError.ERROR_INVALID_MODULETYPE.value(), Errno.ENOEXEC);
        errorToErrnoMapper.put(LastError.ERROR_INVALID_EXE_SIGNATURE.value(), Errno.ENOEXEC);
        errorToErrnoMapper.put(LastError.ERROR_EXE_MARKED_INVALID.value(), Errno.ENOEXEC);
        errorToErrnoMapper.put(LastError.ERROR_BAD_EXE_FORMAT.value(), Errno.ENOEXEC);
        errorToErrnoMapper.put(LastError.ERROR_ITERATED_DATA_EXCEEDS_64k.value(), Errno.ENOEXEC);
        errorToErrnoMapper.put(LastError.ERROR_INVALID_MINALLOCSIZE.value(), Errno.ENOEXEC);
        errorToErrnoMapper.put(LastError.ERROR_DYNLINK_FROM_INVALID_RING.value(), Errno.ENOEXEC);
        errorToErrnoMapper.put(LastError.ERROR_IOPL_NOT_ENABLED.value(), Errno.ENOEXEC);
        errorToErrnoMapper.put(LastError.ERROR_INVALID_SEGDPL.value(), Errno.ENOEXEC);
        errorToErrnoMapper.put(LastError.ERROR_AUTODATASEG_EXCEEDS_64k.value(), Errno.ENOEXEC);
        errorToErrnoMapper.put(LastError.ERROR_RING2SEG_MUST_BE_MOVABLE.value(), Errno.ENOEXEC);
        errorToErrnoMapper.put(LastError.ERROR_RELOC_CHAIN_XEEDS_SEGLIM.value(), Errno.ENOEXEC);
        errorToErrnoMapper.put(LastError.ERROR_INFLOOP_IN_RELOC_CHAIN.value(), Errno.ENOEXEC);
        errorToErrnoMapper.put(LastError.ERROR_FILENAME_EXCED_RANGE.value(), Errno.ENOENT);
        errorToErrnoMapper.put(LastError.ERROR_NESTING_NOT_ALLOWED.value(), Errno.EAGAIN);
        errorToErrnoMapper.put(229, Errno.EPIPE);
        errorToErrnoMapper.put(LastError.ERROR_BAD_PIPE.value(), Errno.EPIPE);
        errorToErrnoMapper.put(LastError.ERROR_PIPE_BUSY.value(), Errno.EAGAIN);
        errorToErrnoMapper.put(LastError.ERROR_NO_DATA.value(), Errno.EPIPE);
        errorToErrnoMapper.put(LastError.ERROR_PIPE_NOT_CONNECTED.value(), Errno.EPIPE);
        errorToErrnoMapper.put(LastError.ERROR_OPERATION_ABORTED.value(), Errno.EINTR);
        errorToErrnoMapper.put(LastError.ERROR_NOT_ENOUGH_QUOTA.value(), Errno.ENOMEM);
        errorToErrnoMapper.put(LastError.ERROR_MOD_NOT_FOUND.value(), Errno.ENOENT);
        errorToErrnoMapper.put(LastError.WSAENAMETOOLONG.value(), Errno.ENAMETOOLONG);
        errorToErrnoMapper.put(LastError.WSAENOTEMPTY.value(), Errno.ENOTEMPTY);
        errorToErrnoMapper.put(LastError.WSAEINTR.value(), Errno.EINTR);
        errorToErrnoMapper.put(LastError.WSAEBADF.value(), Errno.EBADF);
        errorToErrnoMapper.put(LastError.WSAEACCES.value(), Errno.EACCES);
        errorToErrnoMapper.put(LastError.WSAEFAULT.value(), Errno.EFAULT);
        errorToErrnoMapper.put(LastError.WSAEINVAL.value(), Errno.EINVAL);
        errorToErrnoMapper.put(LastError.WSAEMFILE.value(), Errno.EMFILE);
        PASSWD = new BaseNativePOSIX.PointerConverter(){

            public Object fromNative(Object arg, FromNativeContext ctx) {
                throw new RuntimeException("no support for native passwd");
            }
        };
    }
}

