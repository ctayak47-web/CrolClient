package crol.client.mixins.other;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IPlayerMoveC2SPacket {
   default float getYaw() {
      return 0.0F;
   }

   default float getPitch() {
      return 0.0F;
   }

   void setPitch(float var1);

   void setYaw(float var1);
}
