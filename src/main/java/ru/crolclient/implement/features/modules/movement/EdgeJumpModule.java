package ru.crolclient.implement.features.modules.movement;

import ru.crolclient.api.event.EventHandler;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.implement.events.player.MotionEvent;

public class EdgeJumpModule extends Module {

    private long lastJumpTime;
    private static final float CHECK_DISTANCE = 0.1F;
    private static final long JUMP_DELAY = 140L;

    public EdgeJumpModule() {
        super("EdgeJump", "EdgeJump", ModuleCategory.MOVEMENT);
    }

    @EventHandler
    public void onMotionEvent(MotionEvent event) {
        if (mc.player == null || mc.world == null) return;

        if (!mc.player.isOnGround()) return;
        if (mc.options.jumpKey.isPressed()) return;

        if (System.currentTimeMillis() - lastJumpTime < JUMP_DELAY) return;

        if (!mc.world.getBlockCollisions(
                        mc.player,
                        mc.player.getBoundingBox()
                                .expand(-CHECK_DISTANCE, 0, -CHECK_DISTANCE)
                                .offset(0, -0.99, 0))
                .iterator().hasNext()) {

            mc.player.jump();
            lastJumpTime = System.currentTimeMillis();
        }
    }
}