package ru.crolclient.implement.features.modules.movement;

import ru.crolclient.api.event.EventHandler;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.common.util.coroutine.CoroutineContext;
import ru.crolclient.implement.events.player.TickEvent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlightModule extends Module {

    public FlightModule() {
        super("Flight", "Flight", ModuleCategory.MOVEMENT);
    }

    CoroutineContext context = new CoroutineContext();

    @EventHandler
    public void onTick(TickEvent tickEvent) {
        context.launch(() -> {
            System.out.println("Coroutine 1 started");
            suspend(10000);
            System.out.println("Coroutine 1 resumed after 1 second");
        });
    }
    private static void suspend(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void deactivate() {
        context.shutdown();
        super.deactivate();
    }
}