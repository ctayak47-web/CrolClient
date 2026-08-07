
package vurst.visual.base.rotation;

import lombok.Generated;
import net.minecraft.MathHelper;
import vurst.visual.VurstVisual;
import vurst.visual.base.rotation.mods.AIRotationMode;
import vurst.visual.base.rotation.mods.InstantRotationMode;
import vurst.visual.base.rotation.mods.InterpolationRotationMode;
import vurst.visual.base.rotation.mods.config.AiRotationConfig;
import vurst.visual.base.rotation.mods.config.InstantRotationConfig;
import vurst.visual.base.rotation.mods.config.InterpolationRotationConfig;
import vurst.visual.base.rotation.mods.config.api.RotationConfig;
import vurst.visual.base.rotation.mods.config.api.RotationModeType;
import vurst.visual.utility.game.player.rotation.Rotation;
import vurst.visual.utility.game.player.rotation.RotationDelta;
import vurst.visual.utility.interfaces.IClient;

public class AimManager
implements IClient {
    private final InterpolationRotationMode interpolationMod = new InterpolationRotationMode();
    private final InstantRotationMode instantMod = new InstantRotationMode();
    private final AIRotationMode smoothMod = new AIRotationMode();
    private final RotationConfig instantSetup = new InstantRotationConfig();
    private final RotationConfig aiSetup = AiRotationConfig.builder().build();
    int index = 0;
    float[] values = new float[]{0.0123967f, 0.053719f, 0.109504f, 0.17562f, 0.21281f, 0.272727f, 0.11157f, 0.0392562f, 0.0103306f, 0.00206612f};

    public Rotation rotate(RotationConfig config, Rotation targetRotation) {
        if (config.getType() != RotationModeType.INSTANT) {
            RotationDelta deltaToTarget = VurstVisual.getInstance().getRotationManager().getCurrentRotation().rotationDeltaTo(targetRotation);
            float maxInitialDiff = 270.0f;
            float progress = MathHelper.clamp((float)(1.0f - (Math.abs(deltaToTarget.getDeltaYaw()) + Math.abs(deltaToTarget.getDeltaPitch())) / maxInitialDiff), (float)0.0f, (float)1.0f);
            RotationDelta offsets = AimManager.getOffset(deltaToTarget, progress);
            targetRotation = targetRotation.add(offsets);
        }
        Rotation newRotation = switch (config.getType()) {
            case RotationModeType.INSTANT -> this.instantMod.process(targetRotation);
            case RotationModeType.INTERPOLATION -> this.interpolationMod.process((InterpolationRotationConfig)config, VurstVisual.getInstance().getRotationManager().getCurrentRotation(), targetRotation);
            case RotationModeType.AI -> this.interpolationMod.process(((AiRotationConfig)config).getInterpolationRotationConfig(), this.smoothMod.process((AiRotationConfig)config, targetRotation), targetRotation);
            default -> VurstVisual.getInstance().getRotationManager().getCurrentRotation();
        };
        if (config.getType() != RotationModeType.AI) {
            
        }
        if (rotationManager.getCurrentRotation().equals(newRotation)) {
            return newRotation;
        }
        return newRotation.normalize(new Rotation(AimManager.mc.player.lastYaw, AimManager.mc.player.lastPitch));
    }

    public void incrementIndex() {
        ++this.index;
        if (this.index >= this.values.length) {
            this.index = 0;
        }
    }

    public float getDiff(float prevTargetYawDiff, float targetYawDiff) {
        return targetYawDiff * this.values[this.index];
    }

    public static RotationDelta getOffset(RotationDelta deltaToTarget, float progress) {
        boolean verticalAiming;
        float curveStrength = 3.0f;
        float smoothness = 1.0f - (float)Math.cos((double)progress * Math.PI);
        float yawSign = Math.signum(deltaToTarget.getDeltaYaw());
        float pitchSign = Math.signum(deltaToTarget.getDeltaPitch());
        float offsetYaw = 0.0f;
        float offsetPitch = 0.0f;
        boolean bl = verticalAiming = Math.abs(deltaToTarget.getDeltaYaw()) < 1.5f && Math.abs(deltaToTarget.getDeltaPitch()) > 10.0f;
        if (verticalAiming) {
            offsetYaw = pitchSign * curveStrength * (1.0f - progress);
            offsetPitch = 0.0f;
        } else if (pitchSign > 0.0f && yawSign >= 0.0f) {
            offsetYaw = -curveStrength * (1.0f - progress);
            offsetPitch = -curveStrength * smoothness;
        } else if (pitchSign > 0.0f && yawSign < 0.0f) {
            offsetYaw = curveStrength * (1.0f - progress);
            offsetPitch = -curveStrength * smoothness;
        } else if (pitchSign < 0.0f && yawSign >= 0.0f) {
            offsetYaw = -curveStrength * (1.0f - progress);
            offsetPitch = curveStrength * smoothness;
        } else if (pitchSign < 0.0f && yawSign < 0.0f) {
            offsetYaw = curveStrength * (1.0f - progress);
            offsetPitch = curveStrength * smoothness;
        }
        return new RotationDelta(offsetYaw, offsetPitch);
    }

    public void reset() {
        this.index = 0;
    }

    @Generated
    public RotationConfig getInstantSetup() {
        return this.instantSetup;
    }

    @Generated
    public RotationConfig getAiSetup() {
        return this.aiSetup;
    }

    @Generated
    public int getIndex() {
        return this.index;
    }
}

