package crol.client.modules;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IDisableable {
   void onDisable();
}
