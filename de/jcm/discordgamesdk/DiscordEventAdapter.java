
package de.jcm.discordgamesdk;

import de.jcm.discordgamesdk.user.DiscordUser;
import de.jcm.discordgamesdk.user.Relationship;

public abstract class DiscordEventAdapter {
    public void onActivityJoin(String secret) {
    }

    public void onActivitySpectate(String secret) {
    }

    public void onActivityJoinRequest(DiscordUser user) {
    }

    public void onCurrentUserUpdate() {
    }

    public void onOverlayToggle(boolean locked) {
    }

    public void onRelationshipRefresh() {
    }

    public void onRelationshipUpdate(Relationship relationship) {
    }
}

