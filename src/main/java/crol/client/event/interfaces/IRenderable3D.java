package crol.client.event.interfaces;

import crol.client.event.classes.Render3DEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IRenderable3D {
   void onRender3D(Render3DEvent var1);
}
