
package jnr.constants.platform.windows;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum Errno implements Constant
{
    EPERM(1L),
    ENOENT(2L),
    ESRCH(3L),
    EINTR(4L),
    EIO(5L),
    ENXIO(6L),
    E2BIG(7L),
    ENOEXEC(8L),
    EBADF(9L),
    ECHILD(10L),
    EDEADLK(36L),
    ENOMEM(12L),
    EACCES(13L),
    EFAULT(14L),
    EBUSY(16L),
    EEXIST(17L),
    EXDEV(18L),
    ENODEV(19L),
    ENOTDIR(20L),
    EISDIR(21L),
    EINVAL(22L),
    ENFILE(23L),
    EMFILE(24L),
    ENOTTY(25L),
    ETXTBSY(139L),
    EFBIG(27L),
    ENOSPC(28L),
    ESPIPE(29L),
    EROFS(30L),
    EMLINK(31L),
    EPIPE(32L),
    EDOM(33L),
    ERANGE(34L),
    EWOULDBLOCK(140L),
    EAGAIN(11L),
    EINPROGRESS(112L),
    EALREADY(103L),
    ENOTSOCK(128L),
    EDESTADDRREQ(109L),
    EMSGSIZE(115L),
    EPROTOTYPE(136L),
    ENOPROTOOPT(123L),
    EPROTONOSUPPORT(135L),
    EOPNOTSUPP(130L),
    EAFNOSUPPORT(102L),
    EADDRINUSE(100L),
    EADDRNOTAVAIL(101L),
    ENETDOWN(116L),
    ENETUNREACH(118L),
    ENETRESET(117L),
    ECONNABORTED(106L),
    ECONNRESET(108L),
    ENOBUFS(119L),
    EISCONN(113L),
    ENOTCONN(126L),
    ETIMEDOUT(138L),
    ECONNREFUSED(107L),
    ELOOP(114L),
    ENAMETOOLONG(38L),
    EHOSTUNREACH(110L),
    ENOTEMPTY(41L),
    ENOLCK(39L),
    ENOSYS(40L),
    EOVERFLOW(132L),
    EIDRM(111L),
    ENOMSG(122L),
    EILSEQ(42L),
    EBADMSG(104L),
    ENODATA(120L),
    ENOLINK(121L),
    ENOSR(124L),
    ENOSTR(125L),
    EPROTO(134L),
    ETIME(137L),
    EDEADLOCK(36L),
    ECANCELED(105L),
    ENOTRECOVERABLE(127L),
    EOWNERDEAD(133L),
    ENOTSUP(129L);

    private final long value;
    public static final long MIN_VALUE = 1L;
    public static final long MAX_VALUE = 140L;

    private Errno(long value) {
        this.value = value;
    }

    public final String toString() {
        return StringTable.descriptions.get(this);
    }

    public final int value() {
        return (int)this.value;
    }

    @Override
    public final int intValue() {
        return (int)this.value;
    }

    @Override
    public final long longValue() {
        return this.value;
    }

    @Override
    public final boolean defined() {
        return true;
    }

    static final class StringTable {
        public static final Map<Errno, String> descriptions = StringTable.generateTable();

        StringTable() {
        }

        public static final Map<Errno, String> generateTable() {
            EnumMap<Errno, String> map = new EnumMap<Errno, String>(Errno.class);
            map.put(EPERM, "Operation not permitted");
            map.put(ENOENT, "No such file or directory");
            map.put(ESRCH, "No such process");
            map.put(EINTR, "Interrupted function call");
            map.put(EIO, "Input/output error");
            map.put(ENXIO, "No such device or address");
            map.put(E2BIG, "Arg list too long");
            map.put(ENOEXEC, "Exec format error");
            map.put(EBADF, "Bad file descriptor");
            map.put(ECHILD, "No child processes");
            map.put(EDEADLK, "Resource deadlock avoided");
            map.put(ENOMEM, "Not enough space");
            map.put(EACCES, "Permission denied");
            map.put(EFAULT, "Bad address");
            map.put(EBUSY, "Resource device");
            map.put(EEXIST, "File exists");
            map.put(EXDEV, "Improper link");
            map.put(ENODEV, "No such device");
            map.put(ENOTDIR, "Not a directory");
            map.put(EISDIR, "Is a directory");
            map.put(EINVAL, "Invalid argument");
            map.put(ENFILE, "Too many open files in system");
            map.put(EMFILE, "Too many open files");
            map.put(ENOTTY, "Inappropriate I/O control operation");
            map.put(ETXTBSY, "Unknown error");
            map.put(EFBIG, "File too large");
            map.put(ENOSPC, "No space left on device");
            map.put(ESPIPE, "Invalid seek");
            map.put(EROFS, "Read-only file system");
            map.put(EMLINK, "Too many links");
            map.put(EPIPE, "Broken pipe");
            map.put(EDOM, "Domain error");
            map.put(ERANGE, "Result too large");
            map.put(EWOULDBLOCK, "Unknown error");
            map.put(EAGAIN, "Resource temporarily unavailable");
            map.put(EINPROGRESS, "Unknown error");
            map.put(EALREADY, "Unknown error");
            map.put(ENOTSOCK, "Unknown error");
            map.put(EDESTADDRREQ, "Unknown error");
            map.put(EMSGSIZE, "Unknown error");
            map.put(EPROTOTYPE, "Unknown error");
            map.put(ENOPROTOOPT, "Unknown error");
            map.put(EPROTONOSUPPORT, "Unknown error");
            map.put(EOPNOTSUPP, "Unknown error");
            map.put(EAFNOSUPPORT, "Unknown error");
            map.put(EADDRINUSE, "Unknown error");
            map.put(EADDRNOTAVAIL, "Unknown error");
            map.put(ENETDOWN, "Unknown error");
            map.put(ENETUNREACH, "Unknown error");
            map.put(ENETRESET, "Unknown error");
            map.put(ECONNABORTED, "Unknown error");
            map.put(ECONNRESET, "Unknown error");
            map.put(ENOBUFS, "Unknown error");
            map.put(EISCONN, "Unknown error");
            map.put(ENOTCONN, "Unknown error");
            map.put(ETIMEDOUT, "Unknown error");
            map.put(ECONNREFUSED, "Unknown error");
            map.put(ELOOP, "Unknown error");
            map.put(ENAMETOOLONG, "Filename too long");
            map.put(EHOSTUNREACH, "Unknown error");
            map.put(ENOTEMPTY, "Directory not empty");
            map.put(ENOLCK, "No locks available");
            map.put(ENOSYS, "Function not implemented");
            map.put(EOVERFLOW, "Unknown error");
            map.put(EIDRM, "Unknown error");
            map.put(ENOMSG, "Unknown error");
            map.put(EILSEQ, "Illegal byte sequence");
            map.put(EBADMSG, "Unknown error");
            map.put(ENODATA, "Unknown error");
            map.put(ENOLINK, "Unknown error");
            map.put(ENOSR, "Unknown error");
            map.put(ENOSTR, "Unknown error");
            map.put(EPROTO, "Unknown error");
            map.put(ETIME, "Unknown error");
            map.put(EDEADLOCK, "Resource deadlock avoided");
            map.put(ECANCELED, "Unknown error");
            map.put(ENOTRECOVERABLE, "Unknown error");
            map.put(EOWNERDEAD, "Unknown error");
            map.put(ENOTSUP, "Unknown error");
            return map;
        }
    }
}

