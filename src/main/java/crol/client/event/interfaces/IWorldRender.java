package crol.client.event.interfaces;

import crol.client.event.classes.WorldRenderEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IWorldRender {
   void onWorldRender(WorldRenderEvent var1);
}
