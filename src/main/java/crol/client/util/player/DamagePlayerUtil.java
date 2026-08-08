package crol.client.util.player;

import crol.client.event.classes.DamageEvent;
import crol.client.event.classes.ReceivePacketEvent;
import crol.client.util.IUtil;
import crol.client.util.math.TimerUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;

@Environment(EnvType.CLIENT)
public class DamagePlayerUtil implements IUtil {
   private final TimerUtil timeTracker = new TimerUtil();
   private boolean normalDamage;
   private boolean fallDamage;
   private boolean explosionDamage;
   private boolean arrowDamage;
   private boolean pearlDamage;

   public void onPacketEvent(ReceivePacketEvent eventPacket) {
      if (mc.player != null) {
         boolean isDamage = this.fallDamage || this.arrowDamage || this.explosionDamage || this.pearlDamage;
         if (!this.isBadEffects()) {
            if (eventPacket.getPacket() instanceof ExplosionS2CPacket) {
               this.explosionDamage = true;
            }

            if (!isDamage) {
               Packet var4 = eventPacket.getPacket();
               if (var4 instanceof EntityStatusS2CPacket) {
                  EntityStatusS2CPacket statusPacket = (EntityStatusS2CPacket)var4;
                  if (statusPacket.getStatus() == 2 && statusPacket.getEntity(mc.world) == mc.player) {
                     this.normalDamage = true;
                  }
               }
            } else if (mc.player.hurtTime > 0) {
               this.normalDamage = false;
               this.reset();
            }

         }
      }
   }

   public boolean time(long time) {
      if (this.normalDamage) {
         if (this.timeTracker.isReached(time)) {
            this.normalDamage = false;
            this.timeTracker.reset();
            return true;
         }
      } else {
         this.timeTracker.reset();
      }

      return false;
   }

   public TimerUtil getTimeTracker() {
      return this.timeTracker;
   }

   public void processDamage(DamageEvent damageEvent) {
      switch (damageEvent.getDamageType()) {
         case FALL -> this.fallDamage = true;
         case ARROW -> this.arrowDamage = true;
         case ENDER_PEARL -> this.pearlDamage = true;
      }

      this.normalDamage = false;
   }

   public void resetDamages() {
      this.normalDamage = false;
      this.reset();
      this.timeTracker.reset();
   }

   private void reset() {
      this.fallDamage = false;
      this.explosionDamage = false;
      this.arrowDamage = false;
      this.pearlDamage = false;
   }

   private boolean isBadEffects() {
      if (mc.player == null) {
         return false;
      } else {
         return mc.player.hasStatusEffect(StatusEffects.POISON) || mc.player.hasStatusEffect(StatusEffects.WITHER) || mc.player.hasStatusEffect(StatusEffects.INSTANT_DAMAGE);
      }
   }
}
