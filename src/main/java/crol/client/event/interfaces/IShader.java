package crol.client.event.interfaces;

import crol.client.event.classes.ShaderEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IShader {
   void onShader(ShaderEvent var1);
}
