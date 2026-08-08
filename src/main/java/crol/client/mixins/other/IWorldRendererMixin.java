package crol.client.mixins.other;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Frustum;

@Environment(EnvType.CLIENT)
public interface IWorldRendererMixin {
   Frustum getFrustum();
}
