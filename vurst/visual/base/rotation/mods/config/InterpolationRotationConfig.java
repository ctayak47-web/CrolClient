
package vurst.visual.base.rotation.mods.config;

import lombok.Generated;
import vurst.visual.base.rotation.mods.config.api.RotationConfig;
import vurst.visual.base.rotation.mods.config.api.RotationModeType;
import vurst.visual.utility.math.IntRange;

public class InterpolationRotationConfig
extends RotationConfig {
    private final IntRange horizontalSpeedSetting;
    private final IntRange verticalSpeedSetting;
    private final IntRange directionChangeFactor;
    private final float midPoint;

    @Override
    public RotationModeType getType() {
        return RotationModeType.INTERPOLATION;
    }

    @Generated
    public IntRange getHorizontalSpeedSetting() {
        return this.horizontalSpeedSetting;
    }

    @Generated
    public IntRange getVerticalSpeedSetting() {
        return this.verticalSpeedSetting;
    }

    @Generated
    public IntRange getDirectionChangeFactor() {
        return this.directionChangeFactor;
    }

    @Generated
    public float getMidPoint() {
        return this.midPoint;
    }

    @Generated
    public InterpolationRotationConfig(IntRange horizontalSpeedSetting, IntRange verticalSpeedSetting, IntRange directionChangeFactor, float midPoint) {
        this.horizontalSpeedSetting = horizontalSpeedSetting;
        this.verticalSpeedSetting = verticalSpeedSetting;
        this.directionChangeFactor = directionChangeFactor;
        this.midPoint = midPoint;
    }
}

