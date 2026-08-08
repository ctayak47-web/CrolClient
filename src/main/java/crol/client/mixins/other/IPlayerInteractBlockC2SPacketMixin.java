package crol.client.mixins.other;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Hand;

@Environment(EnvType.CLIENT)
public interface IPlayerInteractBlockC2SPacketMixin {
   void setHand(Hand var1);
}
