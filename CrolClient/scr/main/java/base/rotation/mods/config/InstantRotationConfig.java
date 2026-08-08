
package crol.client.base.rotation.mods.config;

import crol.client.base.rotation.mods.config.api.RotationConfig;
import crol.client.base.rotation.mods.config.api.RotationModeType;

public class InstantRotationConfig
extends RotationConfig {
    @Override
    public RotationModeType getType() {
        return RotationModeType.INSTANT;
    }
}

