package ru.crolclient.implement.features.modules.movement;

import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.feature.module.setting.implement.*;
import ru.crolclient.api.event.EventHandler;
import ru.crolclient.implement.events.player.TickEvent;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.scoreboard.Team;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class DragonFlyModule extends Module {

    final SelectSetting modeSetting = new SelectSetting("Mode", "Режим ускорения")
            .value(
                "Smooth",
                "Motion"
            );

    final ValueSetting speedSetting = new ValueSetting("Speed", "Множитель скорости")
            .setValue(1.5f).range(1.0f, 3.0f).increment(0.1f);

    final ValueSetting smoothStrength = new ValueSetting("Smooth Strength", "Сила плавного ускорения")
            .setValue(0.1f).range(0.05f, 0.5f).increment(0.05f)
            .visible(() -> modeSetting.isSelected("Smooth"));

    final BooleanSetting omnidirectionalSprint = new BooleanSetting("Omnidirectional Sprint", 
            "Позволяет спринтить во всех направлениях во время полета")
            .setValue(true);

    int tickCounter = 0;
    double lastSpeed = 0;

    public DragonFlyModule() {
        super("DragonFly", ModuleCategory.MOVEMENT);
        setup(modeSetting, speedSetting, smoothStrength, omnidirectionalSprint);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null || !mc.player.getAbilities().allowFlying) {
            return;
        }

        tickCounter++;

        switch (modeSetting.getSelected()) {
            case "Smooth":
                handleSmoothMode();
                break;
            case "Motion":
                handleMotionMode();
                break;
        }
    }

    private void handleSmoothMode() {
        if (isMoving()) {
            Vec3d motion = getMovementVector();
            double strength = smoothStrength.getValue();
            
            mc.player.addVelocity(
                motion.x * strength,
                0,
                motion.z * strength
            );

            Vec3d currentVel = mc.player.getVelocity();
            double currentSpeed = Math.sqrt(currentVel.x * currentVel.x + currentVel.z * currentVel.z);
            double maxSpeed = speedSetting.getValue() * 0.5;
            
            if (currentSpeed > maxSpeed) {
                double scale = maxSpeed / currentSpeed;
                mc.player.setVelocity(
                    currentVel.x * scale,
                    currentVel.y,
                    currentVel.z * scale
                );
            }
        }
    }

    private void handleMotionMode() {
        if (isMoving()) {
            mc.player.getAbilities().setFlySpeed(0.05f * speedSetting.getValue());
        } else {
            mc.player.getAbilities().setFlySpeed(0.05f);
        }
    }

    private Vec3d getMovementVector() {
        float yaw = mc.player.getYaw();
        float forward = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;

        if (omnidirectionalSprint.isValue()) {
            if (forward != 0) forward = forward > 0 ? 1 : -1;
            if (side != 0) side = side > 0 ? 1 : -1;
        }

        double rad = Math.toRadians(yaw);
        double sin = Math.sin(rad);
        double cos = Math.cos(rad);

        Vec3d movement = new Vec3d(
            (side * cos - forward * sin),
            0,
            (forward * cos + side * sin)
        );

        if (movement.lengthSquared() > 0.01) {
            movement = movement.normalize();

            if (omnidirectionalSprint.isValue()) {
                movement = movement.multiply(1.0);
            }
        }

        return movement;
    }

    private boolean isMoving() {
        if (omnidirectionalSprint.isValue()) {
            return mc.player.input.movementForward != 0 || 
                   mc.player.input.movementSideways != 0 || 
                   mc.options.jumpKey.isPressed() || 
                   mc.options.sneakKey.isPressed();
        }
        return mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0;
    }

    @Override
    public void deactivate() {
        if (mc.player != null) {
            mc.player.getAbilities().setFlySpeed(0.05f);
        }
        tickCounter = 0;
        lastSpeed = 0;
        super.deactivate();
    }
}