
package vurst.visual.base.rotation.mods.config;

import vurst.visual.base.rotation.mods.config.api.RotationConfig;
import vurst.visual.base.rotation.mods.config.api.RotationModeType;

public class InstantRotationConfig
extends RotationConfig {
    @Override
    public RotationModeType getType() {
        return RotationModeType.INSTANT;
    }
}

