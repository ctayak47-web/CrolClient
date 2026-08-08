
package crol.client.base.rotation;

import java.util.function.Supplier;
import crol.client.base.rotation.mods.config.api.RotationConfig;
import crol.client.utility.game.player.rotation.Rotation;

public record RotationTarget(Rotation targetRotation, Supplier<Rotation> rotation, RotationConfig rotationConfigBack) {
}

