
package vurst.visual.base.rotation.mods;

import java.util.Random;
import kotlin.Pair;
import net.minecraft.MathHelper;
import vurst.visual.VurstVisual;
import vurst.visual.base.rotation.mods.api.RotationMode;
import vurst.visual.base.rotation.mods.config.InterpolationRotationConfig;
import vurst.visual.utility.game.player.rotation.Rotation;
import vurst.visual.utility.game.player.rotation.RotationDelta;
import vurst.visual.utility.math.IntRange;

public class InterpolationRotationMode
extends RotationMode {
    private final Sigmoid sigmoid = new Sigmoid();
    private final Bezier bezier = new Bezier();
    private final Random random = new Random();

    public Rotation process(InterpolationRotationConfig config, Rotation modelOut, Rotation targetRotation) {
        Pair<Float, Float> pair = this.calculateFactors(VurstVisual.getInstance().getRotationManager().getCurrentRotation(), targetRotation, config.getHorizontalSpeedSetting(), config.getVerticalSpeedSetting(), config.getDirectionChangeFactor(), config.getMidPoint());
        return modelOut.towardsLinear(targetRotation, pair.getFirst().floatValue(), pair.getSecond().floatValue());
    }

    public Pair<Float, Float> calculateFactors(Rotation currentRotation, Rotation targetRotation, IntRange horizontalSpeedSetting, IntRange verticalSpeedSetting, IntRange directionChangeFactor, float midpoint) {
        RotationDelta diff = currentRotation.rotationDeltaTo(targetRotation);
        float yawDiff = diff.getDeltaYaw();
        float pitchDiff = diff.getDeltaPitch();
        float directionChange = 0.0f;
        if (targetRotation != null && VurstVisual.getInstance().getRotationManager().getPreviousRotationTarget() != null) {
            directionChange = VurstVisual.getInstance().getRotationManager().getPreviousRotationTarget().targetRotation().angleTo(targetRotation);
            directionChange = MathHelper.clamp((float)directionChange, (float)0.0f, (float)1.0f);
            directionChange *= (float)directionChangeFactor.random() / 100.0f;
        }
        float horizontalSpeed = (float)(targetRotation != null ? horizontalSpeedSetting.random() : horizontalSpeedSetting.getStart()) / 100.0f;
        float verticalSpeed = (float)(targetRotation != null ? verticalSpeedSetting.random() : verticalSpeedSetting.getStart()) / 100.0f;
        float horizontalFactor = this.calculateFactor("Yaw", Math.abs(yawDiff), MathHelper.clamp((float)horizontalSpeed, (float)0.0f, (float)1.0f), directionChange, midpoint);
        float verticalFactor = this.calculateFactor("Pitch", Math.abs(pitchDiff), MathHelper.clamp((float)verticalSpeed, (float)0.0f, (float)1.0f), directionChange, midpoint);
        return new Pair<Float, Float>(Float.valueOf(horizontalFactor * Math.abs(yawDiff)), Float.valueOf(verticalFactor * Math.abs(pitchDiff)));
    }

    private float calculateFactor(String name, float rotationDifference, float turnSpeed, float directionChange, float midpoint) {
        float t = MathHelper.clamp((float)(rotationDifference / 180.0f), (float)0.0f, (float)1.0f);
        float bezierSpeed = this.bezier.transform(0.05f, 1.0f, 1.0f - t);
        float sigmoidSpeed = this.sigmoid.transform(t);
        if (t > midpoint) {
            return bezierSpeed * turnSpeed;
        }
        return sigmoidSpeed * MathHelper.clamp((float)(turnSpeed + directionChange), (float)0.0f, (float)1.0f);
    }

    private static class Sigmoid {
        private Sigmoid() {
        }

        public float transform(float t) {
            return (float)(1.0 / (1.0 + Math.exp(-0.5 * ((double)t - 0.3))));
        }
    }

    private static class Bezier {
        private Bezier() {
        }

        public float transform(float start, float end, float t) {
            return (1.0f - t) * (1.0f - t) * start + 2.0f * (1.0f - t) * t * 1.0f + t * t * end;
        }
    }
}

