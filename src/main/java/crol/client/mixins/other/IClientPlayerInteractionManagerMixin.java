package crol.client.mixins.other;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.world.ClientWorld;

@Environment(EnvType.CLIENT)
public interface IClientPlayerInteractionManagerMixin {
   float getBreakingProgress();

   void invokeSendSequencedPacket(ClientWorld var1, SequencedPacketCreator var2);
}
