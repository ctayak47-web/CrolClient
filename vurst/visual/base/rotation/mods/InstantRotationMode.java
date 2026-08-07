
package vurst.visual.base.rotation.mods;

import vurst.visual.base.rotation.mods.api.RotationMode;
import vurst.visual.utility.game.player.rotation.Rotation;

public class InstantRotationMode
extends RotationMode {
    public Rotation process(Rotation target) {
        return rotationManager.getCurrentRotation().add(rotationManager.getCurrentRotation().rotationDeltaTo(target));
    }
}

