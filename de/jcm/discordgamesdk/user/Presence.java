
package de.jcm.discordgamesdk.user;

import de.jcm.discordgamesdk.activity.Activity;
import de.jcm.discordgamesdk.user.OnlineStatus;

public class Presence {
    private final OnlineStatus status;
    private final Activity activity;

    public Presence(OnlineStatus status, Activity activity) {
        this.status = status;
        this.activity = activity;
    }

    public OnlineStatus getStatus() {
        return this.status;
    }

    public Activity getActivity() {
        return this.activity;
    }

    public String toString() {
        return "Presence{status=" + this.status + ", activity=" + this.activity + "}";
    }
}

