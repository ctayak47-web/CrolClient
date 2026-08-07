
package de.jcm.discordgamesdk.activity;

public class ActivityPartySize {
    private final int[] size;

    ActivityPartySize(int[] size) {
        this.size = size;
    }

    public void setCurrentSize(int size) {
        this.size[0] = size;
    }

    public int getCurrentSize() {
        return this.size[0];
    }

    public void setMaxSize(int size) {
        this.size[1] = size;
    }

    public int getMaxSize() {
        return this.size[1];
    }

    public String toString() {
        return "ActivityPartySize{currentSize=" + this.size[0] + ", maxSize=" + this.size[1] + "}";
    }
}

