package crol.client.mixins.other;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface ILivingEntityMixin {
   void setJumpingCooldown(int var1);
}
