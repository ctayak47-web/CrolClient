
package vurst.visual.base.rotation.mods.config;

import lombok.Generated;
import vurst.visual.base.rotation.mods.config.InterpolationRotationConfig;
import vurst.visual.base.rotation.mods.config.api.RotationConfig;
import vurst.visual.base.rotation.mods.config.api.RotationModeType;
import vurst.visual.utility.math.IntRange;

public class AiRotationConfig
extends RotationConfig {
    private int tick;
    private InterpolationRotationConfig interpolationRotationConfig;

    @Override
    public RotationModeType getType() {
        return RotationModeType.AI;
    }

    @Generated
    private static int $default$tick() {
        return 3;
    }

    @Generated
    private static InterpolationRotationConfig $default$interpolationRotationConfig() {
        return new InterpolationRotationConfig(new IntRange(2, 5), new IntRange(5, 8), new IntRange(20, 30), 0.35f);
    }

    @Generated
    AiRotationConfig(int tick, InterpolationRotationConfig interpolationRotationConfig) {
        this.tick = tick;
        this.interpolationRotationConfig = interpolationRotationConfig;
    }

    @Generated
    public static AiRotationConfigBuilder builder() {
        return new AiRotationConfigBuilder();
    }

    @Generated
    public int getTick() {
        return this.tick;
    }

    @Generated
    public InterpolationRotationConfig getInterpolationRotationConfig() {
        return this.interpolationRotationConfig;
    }

    @Generated
    public static class AiRotationConfigBuilder {
        @Generated
        private boolean tick$set;
        @Generated
        private int tick$value;
        @Generated
        private boolean interpolationRotationConfig$set;
        @Generated
        private InterpolationRotationConfig interpolationRotationConfig$value;

        @Generated
        AiRotationConfigBuilder() {
        }

        @Generated
        public AiRotationConfigBuilder tick(int tick) {
            this.tick$value = tick;
            this.tick$set = true;
            return this;
        }

        @Generated
        public AiRotationConfigBuilder interpolationRotationConfig(InterpolationRotationConfig interpolationRotationConfig) {
            this.interpolationRotationConfig$value = interpolationRotationConfig;
            this.interpolationRotationConfig$set = true;
            return this;
        }

        @Generated
        public AiRotationConfig build() {
            int tick$value = this.tick$value;
            if (!this.tick$set) {
                tick$value = AiRotationConfig.$default$tick();
            }
            InterpolationRotationConfig interpolationRotationConfig$value = this.interpolationRotationConfig$value;
            if (!this.interpolationRotationConfig$set) {
                interpolationRotationConfig$value = AiRotationConfig.$default$interpolationRotationConfig();
            }
            return new AiRotationConfig(tick$value, interpolationRotationConfig$value);
        }

        @Generated
        public String toString() {
            return "AiRotationConfig.AiRotationConfigBuilder(tick$value=" + this.tick$value + ", interpolationRotationConfig$value=" + String.valueOf(this.interpolationRotationConfig$value) + ")";
        }
    }
}

