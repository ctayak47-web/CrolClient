package crol.client.mixins.hooks;

import crol.client.mixins.other.IPlayerInteractBlockC2SPacketMixin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin({PlayerInteractBlockC2SPacket.class})
public class PlayerInteractBlockC2SPacketMixin implements IPlayerInteractBlockC2SPacketMixin {
   @Mutable
   @Shadow
   @Final
   private Hand hand;

   public void setHand(Hand hand) {
      this.hand = hand;
   }
}
