package ru.crolclient.implement.features.modules.movement;

import net.minecraft.util.Hand;
import ru.crolclient.api.event.EventHandler;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.feature.module.setting.implement.BooleanSetting;
import ru.crolclient.common.util.player.MoveUtil;
import ru.crolclient.implement.events.player.MotionEvent;

public class SpeedEatingSykaBlyat extends Module {

    private final BooleanSetting enabled = new BooleanSetting("Enabled", "Prevent slowdown while eating")
            .setValue(true);

    public SpeedEatingSykaBlyat() {
        super("SpeedEatingSykaBlyat", ModuleCategory.MOVEMENT);
        setup(enabled);
    }

    @EventHandler
    public void onMotionEvent(MotionEvent event) {
        if (!enabled.isValue() || mc.player == null || !mc.player.isUsingItem()) return;

        simulateNormalMovement();
    }

    private void simulateNormalMovement() {
        boolean wasUsingItem = mc.player.isUsingItem();

        try {
            mc.player.stopUsingItem();
            double[] motion = MoveUtil.forward(1.0);
            mc.player.addVelocity(motion[0], 0.0, motion[1]);
        } finally {
            if (wasUsingItem) {
                mc.player.setCurrentHand(Hand.MAIN_HAND);
            }
        }
    }
}