
package de.jcm.discordgamesdk;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.GameSDKException;
import de.jcm.discordgamesdk.Result;
import de.jcm.discordgamesdk.activity.ActivityType;
import de.jcm.discordgamesdk.impl.Command;
import de.jcm.discordgamesdk.impl.DataProxies;
import de.jcm.discordgamesdk.impl.commands.GetRelationships;
import de.jcm.discordgamesdk.user.OnlineStatus;
import de.jcm.discordgamesdk.user.Relationship;
import de.jcm.discordgamesdk.user.RelationshipType;
import java.util.List;
import java.util.function.Predicate;

public class RelationshipManager {
    public static final Predicate<Relationship> NO_FILTER = r -> true;
    public static final Predicate<Relationship> FRIEND_FILTER = r -> r.getType() == RelationshipType.FRIEND;
    public static final Predicate<Relationship> ONLINE_FILTER = r -> r.getPresence().getStatus() == OnlineStatus.ONLINE;
    public static final Predicate<Relationship> OFFLINE_FILTER = r -> r.getPresence().getStatus() == OnlineStatus.OFFLINE;
    public static final Predicate<Relationship> SPECIAL_FILTER = r -> r.getPresence().getActivity().getType() != ActivityType.PLAYING || r.getPresence().getActivity().getApplicationId() != 0L;
    private final Core.CorePrivate core;
    private List<Relationship> relationships;

    RelationshipManager(Core.CorePrivate core) {
        this.core = core;
        this.core.sendCommand(Command.Type.GET_RELATIONSHIPS, new Object(), o -> {
            if (o.isError()) {
                return;
            }
            GetRelationships.Response r = core.getGson().fromJson(o.getData(), GetRelationships.Response.class);
            for (DataProxies.RelationshipImpl rel : r.getRelationships()) {
                core.relationships.put(rel.user.getUserId(), rel.toRelationship());
            }
            core.getEventAdapter().onRelationshipRefresh();
        });
    }

    public Relationship getWith(long userId) {
        if (!this.core.relationships.containsKey(userId)) {
            throw new GameSDKException(Result.NOT_FOUND);
        }
        return this.core.relationships.get(userId);
    }

    public void filter(Predicate<Relationship> filter) {
        this.relationships = this.core.relationships.values().stream().filter(filter).toList();
    }

    public int count() {
        return this.relationships.size();
    }

    public Relationship getAt(int index) {
        return this.relationships.get(index);
    }

    public List<Relationship> asList() {
        return this.relationships;
    }
}

