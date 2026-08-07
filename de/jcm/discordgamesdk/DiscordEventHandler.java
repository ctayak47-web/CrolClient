
package de.jcm.discordgamesdk;

import de.jcm.discordgamesdk.DiscordEventAdapter;
import de.jcm.discordgamesdk.user.DiscordUser;
import de.jcm.discordgamesdk.user.Relationship;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DiscordEventHandler
extends DiscordEventAdapter {
    private final List<DiscordEventAdapter> listeners = new CopyOnWriteArrayList<DiscordEventAdapter>();

    public void addListener(DiscordEventAdapter listener) {
        this.listeners.add(listener);
    }

    public boolean removeListener(DiscordEventAdapter listener) {
        return this.listeners.remove(listener);
    }

    public void removeAllListeners() {
        this.listeners.clear();
    }

    @Override
    public void onActivityJoin(String secret) {
        this.listeners.forEach(l -> l.onActivityJoin(secret));
    }

    @Override
    public void onActivitySpectate(String secret) {
        this.listeners.forEach(l -> l.onActivitySpectate(secret));
    }

    @Override
    public void onActivityJoinRequest(DiscordUser user) {
        this.listeners.forEach(l -> l.onActivityJoinRequest(user));
    }

    @Override
    public void onCurrentUserUpdate() {
        this.listeners.forEach(DiscordEventAdapter::onCurrentUserUpdate);
    }

    @Override
    public void onOverlayToggle(boolean locked) {
        this.listeners.forEach(l -> l.onOverlayToggle(locked));
    }

    @Override
    public void onRelationshipRefresh() {
        this.listeners.forEach(DiscordEventAdapter::onRelationshipRefresh);
    }

    @Override
    public void onRelationshipUpdate(Relationship relationship) {
        this.listeners.forEach(l -> l.onRelationshipUpdate(relationship));
    }
}

