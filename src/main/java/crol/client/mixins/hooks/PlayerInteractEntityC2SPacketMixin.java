package crol.client.mixins.hooks;

import crol.client.mixins.other.IPlayerInteractEntityC2SPacketMixin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin({PlayerInteractEntityC2SPacket.class})
public class PlayerInteractEntityC2SPacketMixin implements IPlayerInteractEntityC2SPacketMixin {
   @Final
   @Shadow
   private int entityId;

   public int getId() {
      return this.entityId;
   }
}
