package crol.client.mixins.hooks;

import crol.client.mixins.other.IPlayerMoveC2SPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin({PlayerMoveC2SPacket.class})
public class PlayerMoveC2SPacketMixin implements IPlayerMoveC2SPacket {
   @Mutable
   @Shadow
   @Final
   protected float yaw;
   @Mutable
   @Shadow
   @Final
   protected float pitch;

   public float getYaw() {
      return this.yaw;
   }

   public float getPitch() {
      return this.pitch;
   }

   public void setPitch(float pitch) {
      this.pitch = pitch;
   }

   public void setYaw(float yaw) {
      this.yaw = yaw;
   }
}
