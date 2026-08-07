
package de.jcm.discordgamesdk.activity;

import java.time.Instant;

public class ActivityTimestamps {
    private Long start;
    private Long end;

    public void setStart(Instant start) {
        this.start = start.getEpochSecond();
    }

    public Instant getStart() {
        return Instant.ofEpochSecond(this.start);
    }

    public void setEnd(Instant end) {
        this.end = end.getEpochSecond();
    }

    public Instant getEnd() {
        return Instant.ofEpochSecond(this.end);
    }

    public void setStartAndEnd(Instant start, Instant end) {
        this.start = start.getEpochSecond();
        this.end = end.getEpochSecond();
    }

    public void clearStart() {
        this.start = null;
    }

    public void clearEnd() {
        this.end = null;
    }

    public void clear() {
        this.start = null;
        this.end = null;
    }
}

