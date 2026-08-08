package crol.client.util.animations.hogoshi;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum AnimationType {
   BEZIER,
   EASING;

   // $FF: synthetic method
   private static AnimationType[] $values() {
      return new AnimationType[]{BEZIER, EASING};
   }
}
