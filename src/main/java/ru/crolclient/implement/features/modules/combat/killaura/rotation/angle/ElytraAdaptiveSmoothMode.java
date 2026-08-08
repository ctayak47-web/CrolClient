package ru.crolclient.implement.features.modules.combat.killaura.rotation.angle;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.crolclient.implement.features.modules.combat.killaura.rotation.Angle;
import ru.crolclient.implement.features.modules.combat.killaura.rotation.AngleUtil;

public class ElytraAdaptiveSmoothMode extends AngleSmoothMode {
    public ElytraAdaptiveSmoothMode() {
        super("ElytraAdaptive");
    }

    @Override
    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, Vec3d vec3d, Entity entity) {
        Angle angleDelta = AngleUtil.calculateDelta(currentAngle, targetAngle);
        float yawDelta = angleDelta.getYaw();
        float pitchDelta = angleDelta.getPitch();

        float baseYawSpeed = 45.0f;
        float basePitchSpeed = 35.0f;

        double currentTime = System.currentTimeMillis();

        boolean shouldBoost = Math.sin(currentTime / 300.0) > 0.8;
        float speedMultiplier = shouldBoost ? 2.0f : 1.2f;

        float smoothBoost = shouldBoost ?
                (float) (Math.sin((currentTime % 300) / 300.0 * Math.PI) * 0.8 + 1.2) : 1.2f;

        float rotationDifference = (float) Math.hypot(yawDelta, pitchDelta);
        boolean isTargetBehind = Math.abs(yawDelta) > 90.0f;

        float backTargetMultiplier = isTargetBehind ? 2.2f : 1.2f;

        if (isTargetBehind) {
            float smoothBackTurn = (float) (Math.sin(currentTime / 150.0) * 0.2 + 1.0);
            backTargetMultiplier *= smoothBackTurn;
        }

        float finalYawSpeed = baseYawSpeed * speedMultiplier * smoothBoost * backTargetMultiplier;
        float finalPitchSpeed = basePitchSpeed * speedMultiplier * smoothBoost;

        float microAdjustment = (float) (
                Math.sin(currentTime / 80.0) * 0.08 +
                        Math.cos(currentTime / 120.0) * 0.05
        );

        float moveYaw = MathHelper.clamp(yawDelta, -finalYawSpeed, finalYawSpeed);
        float movePitch = MathHelper.clamp(pitchDelta, -finalPitchSpeed, finalPitchSpeed);

        if (rotationDifference < 5.0f) {
            moveYaw += microAdjustment * 0.2f;
            movePitch += microAdjustment * 0.1f;
        }

        float newYaw = currentAngle.getYaw() + moveYaw;
        float newPitch = MathHelper.clamp(
                currentAngle.getPitch() + movePitch,
                -90.0f,
                90.0f
        );

        return new Angle(newYaw, newPitch);
    }

    @Override
    public Vec3d randomValue() {
        double time = System.currentTimeMillis() / 1000.0;
        return new Vec3d(
                Math.sin(time * 1.8) * 0.04 + (Math.random() - 0.5) * 0.02,
                Math.sin(time * 2.2) * 0.03 + (Math.random() - 0.5) * 0.015,
                Math.cos(time * 1.8) * 0.04 + (Math.random() - 0.5) * 0.02
        );
    }
}