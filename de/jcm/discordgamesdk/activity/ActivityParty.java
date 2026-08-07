
package de.jcm.discordgamesdk.activity;

import de.jcm.discordgamesdk.activity.ActivityPartySize;

public class ActivityParty {
    private String id;
    private int[] size = null;

    ActivityParty() {
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getID() {
        return this.id;
    }

    public ActivityPartySize size() {
        if (this.size == null) {
            this.size = new int[2];
        }
        return new ActivityPartySize(this.size);
    }

    public String toString() {
        return "ActivityParty{id='" + this.id + "', size=" + this.size() + "}";
    }
}

