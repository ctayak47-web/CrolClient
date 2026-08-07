
package jnr.posix;

public interface FileStat {
    public static final int S_IFIFO = 4096;
    public static final int S_IFCHR = 8192;
    public static final int S_IFDIR = 16384;
    public static final int S_IFBLK = 24576;
    public static final int S_IFREG = 32768;
    public static final int S_IFLNK = 40960;
    public static final int S_IFSOCK = 49152;
    public static final int S_IFMT = 61440;
    public static final int S_ISUID = 2048;
    public static final int S_ISGID = 1024;
    public static final int S_ISVTX = 512;
    public static final int S_IRUSR = 256;
    public static final int S_IWUSR = 128;
    public static final int S_IXUSR = 64;
    public static final int S_IRGRP = 32;
    public static final int S_IWGRP = 16;
    public static final int S_IXGRP = 8;
    public static final int S_IROTH = 4;
    public static final int S_IWOTH = 2;
    public static final int S_IXOTH = 1;
    public static final int ALL_READ = 292;
    public static final int ALL_WRITE = 146;
    public static final int S_IXUGO = 73;

    public long atime();

    public long blocks();

    public long blockSize();

    public long ctime();

    public long dev();

    public String ftype();

    public int gid();

    public boolean groupMember(int var1);

    public long ino();

    public boolean isBlockDev();

    public boolean isCharDev();

    public boolean isDirectory();

    public boolean isEmpty();

    public boolean isExecutable();

    public boolean isExecutableReal();

    public boolean isFifo();

    public boolean isFile();

    public boolean isGroupOwned();

    public boolean isIdentical(FileStat var1);

    public boolean isNamedPipe();

    public boolean isOwned();

    public boolean isROwned();

    public boolean isReadable();

    public boolean isReadableReal();

    public boolean isWritable();

    public boolean isWritableReal();

    public boolean isSetgid();

    public boolean isSetuid();

    public boolean isSocket();

    public boolean isSticky();

    public boolean isSymlink();

    public int major(long var1);

    public int minor(long var1);

    public int mode();

    public long mtime();

    public int nlink();

    public long rdev();

    public long st_size();

    public int uid();
}

