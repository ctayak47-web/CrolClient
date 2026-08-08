package crol.client.mixins.other;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;

@Environment(EnvType.CLIENT)
public interface IMixinGameRenderer {
   float getFov2(Camera var1, float var2, boolean var3);
}
