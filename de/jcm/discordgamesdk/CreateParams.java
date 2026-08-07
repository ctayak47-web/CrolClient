
package de.jcm.discordgamesdk;

import de.jcm.discordgamesdk.DiscordEventAdapter;
import java.util.stream.Stream;

public class CreateParams
implements AutoCloseable {
    long flags;
    long clientID;
    DiscordEventAdapter eventAdapter;

    public void setClientID(long id) {
        this.clientID = id;
    }

    public long getClientID() {
        return this.clientID;
    }

    public void setFlags(Flags ... flags) {
        this.setFlags(Flags.toLong(flags));
    }

    public void setFlags(long flags) {
        this.flags = flags;
    }

    public long getFlags() {
        return this.flags;
    }

    public void registerEventHandler(DiscordEventAdapter eventHandler) {
        this.eventAdapter = eventHandler;
    }

    public static long getDefaultFlags() {
        return Flags.DEFAULT.value;
    }

    @Override
    @Deprecated
    public void close() {
    }

    public static enum Flags {
        DEFAULT(0L),
        NO_REQUIRE_DISCORD(1L),
        SUPPRESS_EXCEPTIONS(3L);

        private final long value;

        private Flags(long value) {
            this.value = value;
        }

        public static long toLong(Flags ... flags) {
            long l = 0L;
            for (Flags f : flags) {
                l |= f.value;
            }
            return l;
        }

        public static Flags[] fromLong(long l) {
            return (Flags[])Stream.of(Flags.values()).filter(f -> (l & f.value) != 0L).toArray(Flags[]::new);
        }
    }
}

