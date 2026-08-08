package crol.client.event.interfaces;

import crol.client.event.classes.ShaderChamsEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IShaderChams {
   void onShaderChams(ShaderChamsEvent var1);
}
