
package de.jcm.discordgamesdk.impl.channel;

import de.jcm.discordgamesdk.impl.channel.DiscordChannel;
import de.jcm.discordgamesdk.impl.channel.NoSuchInstanceException;
import java.io.File;
import java.io.IOException;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class UnixDiscordChannel
implements DiscordChannel {
    private final SocketChannel channel;
    private String path = System.getenv("DISCORD_IPC_PATH");

    private static String[] getSockets() {
        ArrayList<String> candidates = new ArrayList<String>();
        candidates.add(System.getenv("XDG_RUNTIME_DIR"));
        candidates.add(System.getenv("TMPDIR"));
        candidates.add(System.getenv("TMP"));
        candidates.add(System.getenv("TEMP"));
        candidates.removeIf(Objects::isNull);
        candidates.removeIf(p -> !new File((String)p).exists());
        List<String> flatpakCandidates = candidates.stream().map(p -> p + "/app/com.discordapp.Discord").toList();
        List<String> snapCandidates = candidates.stream().map(p -> p + "/snap.discord").toList();
        List additionalSnapCandidates = candidates.stream().flatMap(p -> Arrays.stream(Objects.requireNonNull(new File((String)p).listFiles((file, name) -> name.startsWith("snap.discord_")))).map(File::getAbsolutePath)).toList();
        candidates.addAll(flatpakCandidates);
        candidates.addAll(snapCandidates);
        candidates.addAll(additionalSnapCandidates);
        candidates.removeIf(Objects::isNull);
        candidates.removeIf(p -> !new File((String)p).exists());
        return (String[])candidates.stream().flatMap(p -> IntStream.iterate(0, x -> x + 1).mapToObj(i -> p + "/discord-ipc-" + i).takeWhile(pp -> new File((String)pp).exists())).toArray(String[]::new);
    }

    public UnixDiscordChannel() throws IOException {
        if (this.path == null) {
            String instance = System.getenv("DISCORD_INSTANCE_ID");
            int i = 0;
            if (instance != null) {
                i = Integer.parseInt(instance);
            }
            String[] sockets = UnixDiscordChannel.getSockets();
            if (i < 0 || i >= sockets.length) {
                throw new NoSuchInstanceException(i);
            }
            this.path = sockets[i];
        }
        this.channel = SocketChannel.open(UnixDomainSocketAddress.of(this.path));
    }

    @Override
    public void close() throws IOException {
        this.channel.close();
    }

    @Override
    public void configureBlocking(boolean block) throws IOException {
        this.channel.configureBlocking(block);
    }

    @Override
    public boolean isAvailable() {
        return new File(this.path).exists() && this.channel.isConnected();
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        return this.channel.read(dst);
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        return this.channel.read(dsts, offset, length);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        return this.channel.write(src);
    }
}

