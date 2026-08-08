package crol.client.util.animations;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum Direction {
   FORWARDS,
   BACKWARDS;

   public Direction opposite() {
      return this == FORWARDS ? BACKWARDS : FORWARDS;
   }

   // $FF: synthetic method
   private static Direction[] $values() {
      return new Direction[]{FORWARDS, BACKWARDS};
   }
}
