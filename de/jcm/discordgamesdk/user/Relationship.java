
package de.jcm.discordgamesdk.user;

import de.jcm.discordgamesdk.user.DiscordUser;
import de.jcm.discordgamesdk.user.Presence;
import de.jcm.discordgamesdk.user.RelationshipType;

public class Relationship {
    private final RelationshipType type;
    private final DiscordUser user;
    private final Presence presence;

    public Relationship(RelationshipType type, DiscordUser user, Presence presence) {
        this.type = type;
        this.user = user;
        this.presence = presence;
    }

    public RelationshipType getType() {
        return this.type;
    }

    public DiscordUser getUser() {
        return this.user;
    }

    public Presence getPresence() {
        return this.presence;
    }

    public String toString() {
        return "Relationship{type=" + this.type + ", user=" + this.user + ", presence=" + this.presence + "}";
    }
}

