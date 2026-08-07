
package de.jcm.discordgamesdk.impl.channel;

import de.jcm.discordgamesdk.impl.channel.DiscordChannel;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class WindowsDiscordChannel
implements DiscordChannel {
    private final FileChannel channel;
    private boolean blocking = true;
    private String path = System.getenv("DISCORD_IPC_PATH");

    public WindowsDiscordChannel() throws IOException {
        if (this.path == null) {
            String instance = System.getenv("DISCORD_INSTANCE_ID");
            int i = 0;
            if (instance != null) {
                i = Integer.parseInt(instance);
            }
            this.path = "\\\\.\\\\pipe\\\\discord-ipc-" + i;
        }
        RandomAccessFile raf = new RandomAccessFile(this.path, "rw");
        this.channel = raf.getChannel();
    }

    @Override
    public void close() throws IOException {
        this.channel.close();
    }

    @Override
    public void configureBlocking(boolean block) throws IOException {
        this.blocking = block;
    }

    @Override
    public boolean isAvailable() {
        return new File(this.path).exists() && this.channel.isOpen();
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        int res = 0;
        if (this.blocking || this.channel.size() - this.channel.position() >= (long)dst.remaining()) {
            res = this.channel.read(dst);
        }
        return res;
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        long res = 0L;
        long remaining = 0L;
        for (int i = offset; !this.blocking && i < offset + length; ++i) {
            remaining += (long)dsts[i].remaining();
        }
        if (this.blocking || this.channel.size() - this.channel.position() >= remaining) {
            res = this.channel.read(dsts, offset, length);
        }
        return res;
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        int res = this.channel.write(src);
        this.channel.force(false);
        return res;
    }
}

