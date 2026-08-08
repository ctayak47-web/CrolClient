package ru.crolclient.implement.events.keyboard;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.crolclient.api.event.events.Event;

@Getter
@RequiredArgsConstructor
public class KeyEvent implements Event {
    private final int key;
    private final int action;
    private final boolean isMouse;

    public KeyEvent(int key, int action) {
        this(key, action, false);
    }

    public boolean isKeyDown(int key) {
        return this.key == key && ((isMouse && action == 1) || (!isMouse && action == 0));
    }
}