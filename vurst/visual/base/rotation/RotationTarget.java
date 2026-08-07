
package vurst.visual.base.rotation;

import java.util.function.Supplier;
import vurst.visual.base.rotation.mods.config.api.RotationConfig;
import vurst.visual.utility.game.player.rotation.Rotation;

public record RotationTarget(Rotation targetRotation, Supplier<Rotation> rotation, RotationConfig rotationConfigBack) {
}

