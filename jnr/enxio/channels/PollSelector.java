
package jnr.enxio.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import jnr.constants.platform.Errno;
import jnr.enxio.channels.Native;
import jnr.enxio.channels.NativeSelectableChannel;
import jnr.enxio.channels.PollSelectionKey;

class PollSelector
extends AbstractSelector {
    private static final int POLLFD_SIZE = 8;
    private static final int FD_OFFSET = 0;
    private static final int EVENTS_OFFSET = 4;
    private static final int REVENTS_OFFSET = 6;
    static final int POLLIN = 1;
    static final int POLLOUT = 4;
    static final int POLLERR = 8;
    static final int POLLHUP = 16;
    private PollSelectionKey[] keyArray = new PollSelectionKey[0];
    private ByteBuffer pollData = null;
    private int nfds;
    private final int[] pipefd = new int[]{-1, -1};
    private final Object regLock = new Object();
    private final Map<SelectionKey, Boolean> keys = new ConcurrentHashMap<SelectionKey, Boolean>();
    private final Set<SelectionKey> selected = new HashSet<SelectionKey>();

    public PollSelector(SelectorProvider provider) {
        super(provider);
        Native.libc().pipe(this.pipefd);
        this.pollData = ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder());
        this.putPollFD(0, this.pipefd[0]);
        this.putPollEvents(0, 1);
        this.nfds = 1;
        this.keyArray = new PollSelectionKey[1];
    }

    private void putPollFD(int idx, int fd) {
        this.pollData.putInt(idx * 8 + 0, fd);
    }

    private void putPollEvents(int idx, int events) {
        this.pollData.putShort(idx * 8 + 4, (short)events);
    }

    private int getPollFD(int idx) {
        return this.pollData.getInt(idx * 8 + 0);
    }

    private short getPollEvents(int idx) {
        return this.pollData.getShort(idx * 8 + 4);
    }

    private short getPollRevents(int idx) {
        return this.pollData.getShort(idx * 8 + 6);
    }

    private void putPollRevents(int idx, int events) {
        this.pollData.putShort(idx * 8 + 6, (short)events);
    }

    @Override
    protected void implCloseSelector() throws IOException {
        if (this.pipefd[0] != -1) {
            Native.close(this.pipefd[0]);
        }
        if (this.pipefd[1] != -1) {
            Native.close(this.pipefd[1]);
        }
        for (SelectionKey key : this.keys.keySet()) {
            this.remove((PollSelectionKey)key);
        }
    }

    @Override
    protected SelectionKey register(AbstractSelectableChannel ch, int ops, Object att) {
        PollSelectionKey key = new PollSelectionKey(this, (NativeSelectableChannel)((Object)ch));
        this.add(key);
        key.attach(att);
        key.interestOps(ops);
        return key;
    }

    @Override
    public Set<SelectionKey> keys() {
        return new HashSet<SelectionKey>(Arrays.asList(this.keyArray).subList(1, this.nfds));
    }

    @Override
    public Set<SelectionKey> selectedKeys() {
        return this.selected;
    }

    void interestOps(PollSelectionKey k, int ops) {
        short events = 0;
        if ((ops & 0x11) != 0) {
            events = (short)(events | 1);
        }
        if ((ops & 0xC) != 0) {
            events = (short)(events | 4);
        }
        this.putPollEvents(k.getIndex(), events);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void add(PollSelectionKey k) {
        Object object = this.regLock;
        synchronized (object) {
            ++this.nfds;
            if (this.keyArray.length < this.nfds) {
                PollSelectionKey[] newArray = new PollSelectionKey[this.nfds + this.nfds / 2];
                System.arraycopy(this.keyArray, 0, newArray, 0, this.nfds - 1);
                this.keyArray = newArray;
                ByteBuffer newBuffer = ByteBuffer.allocateDirect(newArray.length * 8);
                if (this.pollData != null) {
                    newBuffer.put(this.pollData);
                }
                newBuffer.position(0);
                this.pollData = newBuffer.order(ByteOrder.nativeOrder());
            }
            k.setIndex(this.nfds - 1);
            this.keyArray[this.nfds - 1] = k;
            this.putPollFD(k.getIndex(), k.getFD());
            this.putPollEvents(k.getIndex(), 0);
            this.keys.put(k, true);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void remove(PollSelectionKey k) {
        int idx = k.getIndex();
        Object object = this.regLock;
        synchronized (object) {
            if (idx < this.nfds - 1) {
                PollSelectionKey last;
                this.keyArray[idx] = last = this.keyArray[this.nfds - 1];
                this.putPollFD(idx, this.getPollFD(last.getIndex()));
                this.putPollEvents(idx, this.getPollEvents(last.getIndex()));
                last.setIndex(idx);
            } else {
                this.putPollFD(idx, -1);
                this.putPollEvents(idx, 0);
            }
            this.keyArray[this.nfds - 1] = null;
            --this.nfds;
            Set<SelectionKey> set = this.selected;
            synchronized (set) {
                this.selected.remove(k);
            }
            this.keys.remove(k);
        }
        this.deregister(k);
    }

    @Override
    public int selectNow() throws IOException {
        return this.poll(0L);
    }

    @Override
    public int select(long timeout) throws IOException {
        return this.poll(timeout > 0L ? timeout : -1L);
    }

    @Override
    public int select() throws IOException {
        return this.poll(-1L);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int poll(long timeout) throws IOException {
        Set<SelectionKey> cancelled;
        Set<SelectionKey> set = cancelled = this.cancelledKeys();
        synchronized (set) {
            for (SelectionKey k : cancelled) {
                this.remove((PollSelectionKey)k);
            }
            cancelled.clear();
        }
        int nready = 0;
        try {
            this.begin();
            while ((nready = Native.libc().poll(this.pollData, this.nfds, (int)timeout)) < 0 && Errno.EINTR.equals(Errno.valueOf(Native.getRuntime().getLastError()))) {
            }
        }
        finally {
            this.end();
        }
        if (nready < 1) {
            return nready;
        }
        if ((this.getPollRevents(0) & 1) != 0) {
            this.wakeupReceived();
        }
        int updatedKeyCount = 0;
        for (SelectionKey k : this.keys.keySet()) {
            PollSelectionKey pk = (PollSelectionKey)k;
            short revents = this.getPollRevents(pk.getIndex());
            if (revents == 0) continue;
            this.putPollRevents(pk.getIndex(), 0);
            int iops = k.interestOps();
            int ops = 0;
            if ((revents & 1) != 0) {
                ops |= iops & 0x11;
            }
            if ((revents & 4) != 0) {
                ops |= iops & 0xC;
            }
            if ((revents & 0x18) != 0) {
                ops = iops;
            }
            ((PollSelectionKey)k).readyOps(ops);
            ++updatedKeyCount;
            if (this.selected.contains(k)) continue;
            this.selected.add(k);
        }
        return updatedKeyCount;
    }

    private void wakeupReceived() throws IOException {
        Native.read(this.pipefd[0], ByteBuffer.allocate(1));
    }

    @Override
    public Selector wakeup() {
        try {
            Native.write(this.pipefd[1], ByteBuffer.allocate(1));
        }
        catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        return this;
    }
}

