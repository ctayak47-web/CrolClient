package ru.crolclient.implement.events.chat;

import ru.crolclient.api.event.events.Event;
import ru.crolclient.api.event.events.callables.EventCancellable;

public class TabCompleteEvent extends EventCancellable {
    public final String prefix;
    public String[] completions;

    public TabCompleteEvent(String prefix) {
        this.prefix = prefix;
        this.completions = null;
    }
}
