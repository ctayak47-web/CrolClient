package crol.client.event.interfaces;

import crol.client.event.classes.ShaderHandEvent2D;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IShaderHandable {
   void onHandRender(ShaderHandEvent2D var1);
}
