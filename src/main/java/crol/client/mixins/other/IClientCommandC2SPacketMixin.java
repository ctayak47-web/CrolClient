package crol.client.mixins.other;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;

@Environment(EnvType.CLIENT)
public interface IClientCommandC2SPacketMixin {
   void setMode(ClientCommandC2SPacket.Mode var1);
}
