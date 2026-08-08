package crol.client.mixins.hooks;

import crol.client.mixins.other.IPlayerPosition;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerPosition;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin({PlayerPosition.class})
public abstract class PlayerPositionMixin implements IPlayerPosition {
   @Mutable
   @Shadow
   @Final
   private float yaw;
   @Mutable
   @Shadow
   @Final
   private float pitch;

   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   public void setPitch(float pitch) {
      this.pitch = pitch;
   }
}
