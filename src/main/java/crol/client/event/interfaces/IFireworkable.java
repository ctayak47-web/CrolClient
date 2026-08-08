package crol.client.event.interfaces;

import crol.client.event.classes.FireworkEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IFireworkable {
   void onFirework(FireworkEvent var1);
}
