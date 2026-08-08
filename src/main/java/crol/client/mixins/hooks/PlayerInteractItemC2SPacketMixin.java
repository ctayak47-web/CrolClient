package crol.client.mixins.hooks;

import crol.client.mixins.other.IPlayerMoveC2SPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Environment(EnvType.CLIENT)
@Mixin({PlayerInteractItemC2SPacket.class})
public class PlayerInteractItemC2SPacketMixin implements IPlayerMoveC2SPacket {
   @Mutable
   @Final
   @Shadow
   private float yaw;
   @Mutable
   @Final
   @Shadow
   private float pitch;

   @Unique
   public float getYaw() {
      return IPlayerMoveC2SPacket.super.getYaw();
   }

   @Unique
   public float getPitch() {
      return IPlayerMoveC2SPacket.super.getPitch();
   }

   public void setPitch(float pitch) {
      this.pitch = pitch;
   }

   public void setYaw(float yaw) {
      this.yaw = yaw;
   }
}
