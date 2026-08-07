
package vurst.visual.base.rotation.mods;

import vurst.visual.VurstVisual;
import vurst.visual.base.rotation.deeplearnig.MinaraiModel;
import vurst.visual.base.rotation.mods.api.RotationMode;
import vurst.visual.base.rotation.mods.config.AiRotationConfig;
import vurst.visual.utility.game.player.rotation.Rotation;
import vurst.visual.utility.game.player.rotation.RotationDelta;

public class AIRotationMode
extends RotationMode {
    private Rotation lerpTargetRotation = Rotation.ZERO;

    public Rotation process(AiRotationConfig config, Rotation targetRotation) {
        RotationDelta prevDelta = VurstVisual.getInstance().getRotationManager().getPreviousRotation().rotationDeltaTo(VurstVisual.getInstance().getRotationManager().getCurrentRotation());
        Rotation currentRotation = VurstVisual.getInstance().getRotationManager().getCurrentRotation();
        if (Math.abs(targetRotation.rotationDeltaTo(this.lerpTargetRotation).getDeltaYaw()) > 80.0f) {
            this.lerpTargetRotation = targetRotation;
        }
        for (int i = 0; i < 3; ++i) {
            Rotation newOut = this.process(config, currentRotation, targetRotation, prevDelta, i == config.getTick() - 1);
            prevDelta = currentRotation.rotationDeltaTo(newOut);
            currentRotation = newOut;
        }
        if (currentRotation.rotationDeltaTo(this.lerpTargetRotation).isInRange(10.0f)) {
            this.lerpTargetRotation = targetRotation;
        }
        return currentRotation;
    }

    private Rotation process(AiRotationConfig config, Rotation currentRotation, Rotation targetRotation, RotationDelta prevDelta, boolean tickUpdate) {
        MinaraiModel model = VurstVisual.getInstance().getDeepLearningManager().getSlowModel();
        try {
            RotationDelta deltaLerpTarget = currentRotation.rotationDeltaTo(this.lerpTargetRotation);
            if (Math.copySign(1.0f, prevDelta.getDeltaYaw()) != Math.copySign(1.0f, deltaLerpTarget.getDeltaYaw())) {
                
            }
            float[] input = new float[]{prevDelta.getDeltaYaw(), prevDelta.getDeltaPitch(), deltaLerpTarget.getDeltaYaw(), deltaLerpTarget.getDeltaPitch()};
            float[] result = (float[])model.predict(input);
            float diffYaw = result[0];
            float diffPitch = result[1];
            RotationDelta newDelta = new RotationDelta(diffYaw, diffPitch);
            return currentRotation.add(newDelta);
        }
        catch (Exception e) {
            e.printStackTrace();
            return currentRotation;
        }
    }

    public void resetLerp(Rotation targetRotation) {
        this.lerpTargetRotation = targetRotation;
    }
}

