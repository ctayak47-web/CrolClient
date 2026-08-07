
package jnr.constants.platform.linux.aarch64;

import java.util.EnumMap;
import java.util.Map;
import jnr.constants.Constant;

public enum SocketOption implements Constant
{
    SO_DEBUG(1L),
    SO_ACCEPTCONN(30L),
    SO_REUSEADDR(2L),
    SO_KEEPALIVE(9L),
    SO_DONTROUTE(5L),
    SO_BROADCAST(6L),
    SO_LINGER(13L),
    SO_OOBINLINE(10L),
    SO_REUSEPORT(15L),
    SO_TIMESTAMP(29L),
    SO_SNDBUF(7L),
    SO_RCVBUF(8L),
    SO_SNDLOWAT(19L),
    SO_RCVLOWAT(18L),
    SO_SNDTIMEO(21L),
    SO_RCVTIMEO(20L),
    SO_ERROR(4L),
    SO_TYPE(3L),
    SO_ATTACH_FILTER(26L),
    SO_BINDTODEVICE(25L),
    SO_DETACH_FILTER(27L),
    SO_NO_CHECK(11L),
    SO_PASSCRED(16L),
    SO_PEERCRED(17L),
    SO_PEERNAME(28L),
    SO_PRIORITY(12L),
    SO_SNDBUFFORCE(32L),
    SO_RCVBUFFORCE(33L),
    SO_GET_FILTER(26L),
    SO_TIMESTAMPNS(35L),
    SO_PEERSEC(31L),
    SO_PASSSEC(34L),
    SO_MARK(36L),
    SO_TIMESTAMPING(37L),
    SO_PROTOCOL(38L),
    SO_DOMAIN(39L),
    SO_RXQ_OVFL(40L),
    SO_WIFI_STATUS(41L),
    SO_PEEK_OFF(42L),
    SO_NOFCS(43L),
    SO_LOCK_FILTER(44L),
    SO_SELECT_ERR_QUEUE(45L),
    SO_BUSY_POLL(46L),
    SO_MAX_PACING_RATE(47L),
    SO_BPF_EXTENSIONS(48L),
    SO_SECURITY_AUTHENTICATION(22L),
    SO_SECURITY_ENCRYPTION_NETWORK(24L),
    SO_SECURITY_ENCRYPTION_TRANSPORT(23L);

    private final long value;
    public static final long MIN_VALUE = 1L;
    public static final long MAX_VALUE = 48L;

    private SocketOption(long value) {
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
        public static final Map<SocketOption, String> descriptions = StringTable.generateTable();

        StringTable() {
        }

        public static final Map<SocketOption, String> generateTable() {
            EnumMap<SocketOption, String> map = new EnumMap<SocketOption, String>(SocketOption.class);
            map.put(SO_DEBUG, "SO_DEBUG");
            map.put(SO_ACCEPTCONN, "SO_ACCEPTCONN");
            map.put(SO_REUSEADDR, "SO_REUSEADDR");
            map.put(SO_KEEPALIVE, "SO_KEEPALIVE");
            map.put(SO_DONTROUTE, "SO_DONTROUTE");
            map.put(SO_BROADCAST, "SO_BROADCAST");
            map.put(SO_LINGER, "SO_LINGER");
            map.put(SO_OOBINLINE, "SO_OOBINLINE");
            map.put(SO_REUSEPORT, "SO_REUSEPORT");
            map.put(SO_TIMESTAMP, "SO_TIMESTAMP");
            map.put(SO_SNDBUF, "SO_SNDBUF");
            map.put(SO_RCVBUF, "SO_RCVBUF");
            map.put(SO_SNDLOWAT, "SO_SNDLOWAT");
            map.put(SO_RCVLOWAT, "SO_RCVLOWAT");
            map.put(SO_SNDTIMEO, "SO_SNDTIMEO");
            map.put(SO_RCVTIMEO, "SO_RCVTIMEO");
            map.put(SO_ERROR, "SO_ERROR");
            map.put(SO_TYPE, "SO_TYPE");
            map.put(SO_ATTACH_FILTER, "SO_ATTACH_FILTER");
            map.put(SO_BINDTODEVICE, "SO_BINDTODEVICE");
            map.put(SO_DETACH_FILTER, "SO_DETACH_FILTER");
            map.put(SO_NO_CHECK, "SO_NO_CHECK");
            map.put(SO_PASSCRED, "SO_PASSCRED");
            map.put(SO_PEERCRED, "SO_PEERCRED");
            map.put(SO_PEERNAME, "SO_PEERNAME");
            map.put(SO_PRIORITY, "SO_PRIORITY");
            map.put(SO_SNDBUFFORCE, "SO_SNDBUFFORCE");
            map.put(SO_RCVBUFFORCE, "SO_RCVBUFFORCE");
            map.put(SO_GET_FILTER, "SO_GET_FILTER");
            map.put(SO_TIMESTAMPNS, "SO_TIMESTAMPNS");
            map.put(SO_PEERSEC, "SO_PEERSEC");
            map.put(SO_PASSSEC, "SO_PASSSEC");
            map.put(SO_MARK, "SO_MARK");
            map.put(SO_TIMESTAMPING, "SO_TIMESTAMPING");
            map.put(SO_PROTOCOL, "SO_PROTOCOL");
            map.put(SO_DOMAIN, "SO_DOMAIN");
            map.put(SO_RXQ_OVFL, "SO_RXQ_OVFL");
            map.put(SO_WIFI_STATUS, "SO_WIFI_STATUS");
            map.put(SO_PEEK_OFF, "SO_PEEK_OFF");
            map.put(SO_NOFCS, "SO_NOFCS");
            map.put(SO_LOCK_FILTER, "SO_LOCK_FILTER");
            map.put(SO_SELECT_ERR_QUEUE, "SO_SELECT_ERR_QUEUE");
            map.put(SO_BUSY_POLL, "SO_BUSY_POLL");
            map.put(SO_MAX_PACING_RATE, "SO_MAX_PACING_RATE");
            map.put(SO_BPF_EXTENSIONS, "SO_BPF_EXTENSIONS");
            map.put(SO_SECURITY_AUTHENTICATION, "SO_SECURITY_AUTHENTICATION");
            map.put(SO_SECURITY_ENCRYPTION_NETWORK, "SO_SECURITY_ENCRYPTION_NETWORK");
            map.put(SO_SECURITY_ENCRYPTION_TRANSPORT, "SO_SECURITY_ENCRYPTION_TRANSPORT");
            return map;
        }
    }
}

