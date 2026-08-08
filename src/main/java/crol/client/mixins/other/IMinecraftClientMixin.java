package crol.client.mixins.other;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.session.Session;

@Environment(EnvType.CLIENT)
public interface IMinecraftClientMixin {
   void mouseClick();

   void setSession(Session var1);

   int getUseCooldown();

   void setUseCooldown(int var1);
}
