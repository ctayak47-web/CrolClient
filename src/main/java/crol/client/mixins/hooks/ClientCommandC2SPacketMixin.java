package crol.client.mixins.hooks;

import crol.client.mixins.other.IClientCommandC2SPacketMixin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin({ClientCommandC2SPacket.class})
public class ClientCommandC2SPacketMixin implements IClientCommandC2SPacketMixin {
   @Mutable
   @Shadow
   @Final
   private ClientCommandC2SPacket.Mode mode;

   public void setMode(ClientCommandC2SPacket.Mode mode) {
      this.mode = mode;
   }
}
