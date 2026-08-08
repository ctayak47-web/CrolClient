package ru.crolclient.implement.features.modules.combat.killaura.rotation.angle;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.crolclient.common.util.math.MathUtil;
import ru.crolclient.implement.features.modules.combat.killaura.rotation.Angle;
import ru.crolclient.implement.features.modules.combat.killaura.rotation.AngleUtil;

public class AdaptiveSmoothMode extends AngleSmoothMode {
    public AdaptiveSmoothMode() {
        super("Adaptive");
    }

    @Override
    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, Vec3d vec3d, Entity entity) {
        Angle angleDelta = AngleUtil.calculateDelta(currentAngle, targetAngle);
        float yawDelta = angleDelta.getYaw();
        float pitchDelta = angleDelta.getPitch();

        float baseYawSpeed = 18.0f;
        float basePitchSpeed = 14.0f;

        double currentTime = System.currentTimeMillis();

        boolean shouldBoost = Math.sin(currentTime / 400.0) > 0.85;
        float speedMultiplier = shouldBoost ? 1.6f : 1.0f;

        float smoothBoost = shouldBoost ? 
                (float) (Math.sin((currentTime % 400) / 400.0 * Math.PI) * 0.6 + 1.0) : 1.0f;

        float rotationDifference = (float) Math.hypot(yawDelta, pitchDelta);
        boolean isTargetBehind = Math.abs(yawDelta) > 90.0f;

        float backTargetMultiplier = isTargetBehind ? 1.8f : 1.0f;

        if (isTargetBehind) {
            float smoothBackTurn = (float) (Math.sin(currentTime / 200.0) * 0.1 + 0.9);
            backTargetMultiplier *= smoothBackTurn;
        }

        float finalYawSpeed = baseYawSpeed * speedMultiplier * smoothBoost * backTargetMultiplier;
        float finalPitchSpeed = basePitchSpeed * speedMultiplier * smoothBoost;

        float microAdjustment = (float) (
                Math.sin(currentTime / 100.0) * 0.15 +
                        Math.cos(currentTime / 150.0) * 0.1
        );

        float moveYaw = MathHelper.clamp(yawDelta, -finalYawSpeed, finalYawSpeed);
        float movePitch = MathHelper.clamp(pitchDelta, -finalPitchSpeed, finalPitchSpeed);

        if (rotationDifference < 10.0f) {
            moveYaw += microAdjustment * 0.3f;
            movePitch += microAdjustment * 0.2f;
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
                Math.sin(time * 1.5) * 0.08 + (Math.random() - 0.5) * 0.04,
                Math.sin(time * 2.0) * 0.06 + (Math.random() - 0.5) * 0.03,
                Math.cos(time * 1.5) * 0.08 + (Math.random() - 0.5) * 0.04
        );
    }
}
