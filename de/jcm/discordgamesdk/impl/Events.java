
package de.jcm.discordgamesdk.impl;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.impl.Command;
import de.jcm.discordgamesdk.impl.EventHandler;
import de.jcm.discordgamesdk.impl.events.ActivityJoinEvent;
import de.jcm.discordgamesdk.impl.events.CurrentUserUpdateEvent;
import de.jcm.discordgamesdk.impl.events.OverlayUpdateEvent;
import de.jcm.discordgamesdk.impl.events.ReadyEvent;
import de.jcm.discordgamesdk.impl.events.RelationshipUpdateEvent;
import de.jcm.discordgamesdk.impl.events.VoiceSettingsUpdate2Event;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class Events {
    private final Core.CorePrivate core;
    private final Map<Command.Event, EventHandler<?>> handlers = new HashMap();

    public Events(Core.CorePrivate core) {
        this.core = core;
        this.registerEvents();
    }

    private void registerEvents() {
        this.handlers.put(Command.Event.READY, new ReadyEvent.Handler(this.core));
        this.handlers.put(Command.Event.ACTIVITY_JOIN, new ActivityJoinEvent.Handler(this.core));
        this.handlers.put(Command.Event.CURRENT_USER_UPDATE, new CurrentUserUpdateEvent.Handler(this.core));
        this.handlers.put(Command.Event.OVERLAY_UPDATE, new OverlayUpdateEvent.Handler(this.core));
        this.handlers.put(Command.Event.RELATIONSHIP_UPDATE, new RelationshipUpdateEvent.Handler(this.core));
        this.handlers.put(Command.Event.VOICE_SETTINGS_UPDATE_2, new VoiceSettingsUpdate2Event.Handler(this.core));
    }

    public EventHandler<?> forEvent(Command.Event e) {
        return this.handlers.get((Object)e);
    }

    public <T> void register(Command.Event event, final Class<T> clazz, final Consumer<T> consumer) {
        this.handlers.put(event, new EventHandler<T>(this.core){

            @Override
            public void handle(Command command, T t) {
                consumer.accept(t);
            }

            @Override
            public Class<?> getDataClass() {
                return clazz;
            }
        });
    }

    public Set<Map.Entry<Command.Event, EventHandler<?>>> getEventTypes() {
        return this.handlers.entrySet();
    }
}

