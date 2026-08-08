
package crol.client.base.rotation.mods;

import crol.client.base.rotation.mods.api.RotationMode;
import crol.client.utility.game.player.rotation.Rotation;

public class InstantRotationMode
extends RotationMode {
    public Rotation process(Rotation target) {
        return rotationManager.getCurrentRotation().add(rotationManager.getCurrentRotation().rotationDeltaTo(target));
    }
}

