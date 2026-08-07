
package jnr.posix;

import java.nio.ByteBuffer;
import jnr.ffi.Pointer;
import jnr.ffi.Variable;
import jnr.ffi.annotations.In;
import jnr.ffi.annotations.Out;
import jnr.ffi.annotations.StdCall;
import jnr.ffi.annotations.Transient;
import jnr.ffi.byref.IntByReference;
import jnr.posix.FileStat;
import jnr.posix.FileTime;
import jnr.posix.HANDLE;
import jnr.posix.LibC;
import jnr.posix.WString;
import jnr.posix.WindowsProcessInformation;
import jnr.posix.WindowsSecurityAttributes;
import jnr.posix.WindowsStartupInfo;
import jnr.posix.windows.SystemTime;
import jnr.posix.windows.WindowsByHandleFileInformation;
import jnr.posix.windows.WindowsFileInformation;
import jnr.posix.windows.WindowsFindData;

public interface WindowsLibC
extends LibC {
    public static final int STD_INPUT_HANDLE = -10;
    public static final int STD_OUTPUT_HANDLE = -11;
    public static final int STD_ERROR_HANDLE = -12;
    public static final int NORMAL_PRIORITY_CLASS = 32;
    public static final int CREATE_UNICODE_ENVIRONMENT = 1024;
    public static final int INFINITE = -1;
    public static final int FILE_TYPE_DISK = 1;
    public static final int FILE_TYPE_CHAR = 2;
    public static final int FILE_TYPE_PIPE = 3;
    public static final int FILE_TYPE_REMOTE = 32768;
    public static final int FILE_TYPE_UNKNOWN = 0;
    public static final int PROCESS_QUERY_INFORMATION = 1024;

    public int _open_osfhandle(HANDLE var1, int var2);

    public HANDLE _get_osfhandle(int var1);

    public int _close(int var1);

    public int _getpid();

    public int _stat64(CharSequence var1, @Out @Transient FileStat var2);

    public int _umask(int var1);

    public int _wmkdir(@In WString var1);

    public boolean RemoveDirectoryW(@In WString var1);

    public int _wchmod(@In WString var1, int var2);

    public int _wchdir(@In WString var1);

    public int _wstat64(@In WString var1, @Out @Transient FileStat var2);

    public int _wstat64(@In byte[] var1, @Out @Transient FileStat var2);

    public int _pipe(int[] var1, int var2, int var3);

    @StdCall
    public boolean CreateProcessW(byte[] var1, @In @Out ByteBuffer var2, WindowsSecurityAttributes var3, WindowsSecurityAttributes var4, int var5, int var6, @In Pointer var7, @In byte[] var8, WindowsStartupInfo var9, WindowsProcessInformation var10);

    public HANDLE OpenProcess(@In int var1, @In int var2, @In int var3);

    public int FileTimeToSystemTime(@In FileTime var1, @Out @Transient SystemTime var2);

    public int GetFileAttributesW(@In WString var1);

    public int GetFileAttributesExW(@In WString var1, @In int var2, @Out @Transient WindowsFileInformation var3);

    public int GetFileAttributesExW(@In byte[] var1, @In int var2, @Out @Transient WindowsFileInformation var3);

    public int SetFileAttributesW(@In WString var1, int var2);

    public int GetFileInformationByHandle(@In HANDLE var1, @Out @Transient WindowsByHandleFileInformation var2);

    public int FindClose(HANDLE var1);

    public HANDLE FindFirstFileW(@In WString var1, @Out WindowsFindData var2);

    public HANDLE FindFirstFileW(@In byte[] var1, @Out WindowsFindData var2);

    @StdCall
    public boolean GetExitCodeProcess(HANDLE var1, @Out Pointer var2);

    @StdCall
    public boolean GetExitCodeProcess(HANDLE var1, @Out IntByReference var2);

    @StdCall
    public int GetFileType(HANDLE var1);

    @StdCall
    public int GetFileSize(HANDLE var1, @Out IntByReference var2);

    @StdCall
    public HANDLE GetStdHandle(int var1);

    @StdCall
    public boolean CreateHardLinkW(@In WString var1, @In WString var2, @In WString var3);

    @StdCall
    public HANDLE CreateFileW(byte[] var1, int var2, int var3, Pointer var4, int var5, int var6, int var7);

    @StdCall
    public boolean SetEnvironmentVariableW(@In WString var1, @In WString var2);

    @StdCall
    public boolean GetComputerNameW(@Out ByteBuffer var1, IntByReference var2);

    @StdCall
    public boolean SetFileTime(HANDLE var1, FileTime var2, FileTime var3, FileTime var4);

    @StdCall
    public boolean CloseHandle(HANDLE var1);

    @StdCall
    public int WaitForSingleObject(HANDLE var1, int var2);

    public Variable<Long> _environ();
}

