package ru.crolclient.core.listener.impl;

import ru.crolclient.api.event.EventHandler;
import ru.crolclient.core.Extra;
import ru.crolclient.core.listener.Listener;
import ru.crolclient.implement.events.player.PostTickEvent;
import ru.crolclient.implement.events.player.TickEvent;
import ru.crolclient.implement.features.modules.combat.AuraModule;

public class TickEventListener implements Listener {
    @EventHandler
    public void onTick(TickEvent tickEvent) {
        Extra.getInstance().getAttackPerpetrator().tick();

    }
}
