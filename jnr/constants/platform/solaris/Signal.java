
package jnr.constants.platform.solaris;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum Signal implements Constant
{
    SIGHUP(1L),
    SIGINT(2L),
    SIGQUIT(3L),
    SIGILL(4L),
    SIGTRAP(5L),
    SIGABRT(6L),
    SIGIOT(6L),
    SIGBUS(10L),
    SIGFPE(8L),
    SIGKILL(9L),
    SIGUSR1(16L),
    SIGSEGV(11L),
    SIGUSR2(17L),
    SIGPIPE(13L),
    SIGALRM(14L),
    SIGTERM(15L),
    SIGCLD(18L),
    SIGCHLD(18L),
    SIGCONT(25L),
    SIGSTOP(23L),
    SIGTSTP(24L),
    SIGTTIN(26L),
    SIGTTOU(27L),
    SIGURG(21L),
    SIGXCPU(30L),
    SIGXFSZ(31L),
    SIGVTALRM(28L),
    SIGPROF(29L),
    SIGWINCH(20L),
    SIGPOLL(22L),
    SIGIO(22L),
    SIGPWR(19L),
    SIGSYS(12L),
    SIGRTMIN(41L),
    SIGRTMAX(72L),
    NSIG(73L);

    private final long value;
    public static final long MIN_VALUE = 1L;
    public static final long MAX_VALUE = 73L;

    private Signal(long value) {
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
        public static final Map<Signal, String> descriptions = StringTable.generateTable();

        StringTable() {
        }

        public static final Map<Signal, String> generateTable() {
            EnumMap<Signal, String> map = new EnumMap<Signal, String>(Signal.class);
            map.put(SIGHUP, "SIGHUP");
            map.put(SIGINT, "SIGINT");
            map.put(SIGQUIT, "SIGQUIT");
            map.put(SIGILL, "SIGILL");
            map.put(SIGTRAP, "SIGTRAP");
            map.put(SIGABRT, "SIGABRT");
            map.put(SIGIOT, "SIGIOT");
            map.put(SIGBUS, "SIGBUS");
            map.put(SIGFPE, "SIGFPE");
            map.put(SIGKILL, "SIGKILL");
            map.put(SIGUSR1, "SIGUSR1");
            map.put(SIGSEGV, "SIGSEGV");
            map.put(SIGUSR2, "SIGUSR2");
            map.put(SIGPIPE, "SIGPIPE");
            map.put(SIGALRM, "SIGALRM");
            map.put(SIGTERM, "SIGTERM");
            map.put(SIGCLD, "SIGCLD");
            map.put(SIGCHLD, "SIGCHLD");
            map.put(SIGCONT, "SIGCONT");
            map.put(SIGSTOP, "SIGSTOP");
            map.put(SIGTSTP, "SIGTSTP");
            map.put(SIGTTIN, "SIGTTIN");
            map.put(SIGTTOU, "SIGTTOU");
            map.put(SIGURG, "SIGURG");
            map.put(SIGXCPU, "SIGXCPU");
            map.put(SIGXFSZ, "SIGXFSZ");
            map.put(SIGVTALRM, "SIGVTALRM");
            map.put(SIGPROF, "SIGPROF");
            map.put(SIGWINCH, "SIGWINCH");
            map.put(SIGPOLL, "SIGPOLL");
            map.put(SIGIO, "SIGIO");
            map.put(SIGPWR, "SIGPWR");
            map.put(SIGSYS, "SIGSYS");
            map.put(SIGRTMIN, "SIGRTMIN");
            map.put(SIGRTMAX, "SIGRTMAX");
            map.put(NSIG, "NSIG");
            return map;
        }
    }
}

