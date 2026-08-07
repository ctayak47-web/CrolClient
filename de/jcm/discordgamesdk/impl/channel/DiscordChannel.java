
package de.jcm.discordgamesdk.impl.channel;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface DiscordChannel {
    public void close() throws IOException;

    public void configureBlocking(boolean var1) throws IOException;

    public int read(ByteBuffer var1) throws IOException;

    public long read(ByteBuffer[] var1, int var2, int var3) throws IOException;

    public int write(ByteBuffer var1) throws IOException;

    public boolean isAvailable();
}

