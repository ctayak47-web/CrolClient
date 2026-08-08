package crol.client.event.classes;

import crol.client.event.CancellableEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class DamageEvent extends CancellableEvent {
   private final DamageType damageType;

   public DamageEvent(DamageType damageType) {
      this.damageType = damageType;
   }

   public DamageType getDamageType() {
      return this.damageType;
   }

   @Environment(EnvType.CLIENT)
   public static enum DamageType {
      ENDER_PEARL,
      ARROW,
      FALL;

      // $FF: synthetic method
      private static DamageType[] $values() {
         return new DamageType[]{ENDER_PEARL, ARROW, FALL};
      }
   }
}
