package ru.crolclient.implement.features.modules.movement;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import ru.crolclient.api.event.EventHandler;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.feature.module.setting.implement.BooleanSetting;
import ru.crolclient.api.feature.module.setting.implement.ValueSetting;
import ru.crolclient.common.util.player.MoveUtil;
import ru.crolclient.implement.events.player.MotionEvent;

public class SpeedModule extends Module {

    private final BooleanSetting onlyPlayers = new BooleanSetting("Only Players", "Speed up only from players")
            .setValue(false);
    private final ValueSetting speed = new ValueSetting("Speed", "Set speed")
            .setValue(8).range(1.0F, 50F);
    private final ValueSetting radius = new ValueSetting("Radius", "Set radius")
            .setValue(1).range(0.5F, 5F);

    public SpeedModule() {
        super("Speed", ModuleCategory.MOVEMENT);
        setup(onlyPlayers, speed, radius);
    }

    @EventHandler
    public void onMotionEvent(MotionEvent event) {
        if (MoveUtil.isMoving()) {
            int collisionCount = 0;
            for (Entity entity : mc.world.getEntities()) {
                if (!(entity instanceof PlayerEntity) && onlyPlayers.isValue()) continue;
                if (entity != mc.player && (entity instanceof LivingEntity || entity instanceof BoatEntity) &&
                        mc.player.getBoundingBox().expand(radius.getValue()).intersects(entity.getBoundingBox())) {
                    collisionCount++;
                }
            }

            double[] motion = MoveUtil.forward((speed.getValue() * 0.01) * collisionCount);
            mc.player.addVelocity(motion[0], 0.0, motion[1]);
        }
    }
}
