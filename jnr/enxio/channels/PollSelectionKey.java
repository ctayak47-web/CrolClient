
package jnr.enxio.channels;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectionKey;
import jnr.enxio.channels.NativeSelectableChannel;
import jnr.enxio.channels.PollSelector;

class PollSelectionKey
extends AbstractSelectionKey {
    private final PollSelector selector;
    private final NativeSelectableChannel channel;
    private int interestOps = 0;
    private int readyOps = 0;
    private int index = -1;

    public PollSelectionKey(PollSelector selector, NativeSelectableChannel channel) {
        this.selector = selector;
        this.channel = channel;
    }

    void setIndex(int index) {
        this.index = index;
    }

    int getIndex() {
        return this.index;
    }

    int getFD() {
        return this.channel.getFD();
    }

    @Override
    public SelectableChannel channel() {
        return (SelectableChannel)((Object)this.channel);
    }

    @Override
    public Selector selector() {
        return this.selector;
    }

    @Override
    public int interestOps() {
        return this.interestOps;
    }

    @Override
    public SelectionKey interestOps(int ops) {
        this.interestOps = ops;
        this.selector.interestOps(this, ops);
        return this;
    }

    @Override
    public int readyOps() {
        return this.readyOps;
    }

    void readyOps(int readyOps) {
        this.readyOps = readyOps;
    }
}

