package crol.client.event.classes;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.Framebuffer;

@Environment(EnvType.CLIENT)
public class ShaderChamsEvent {
   private final Framebuffer beforeHandFbo;
   private final Framebuffer withHandFbo;

   public ShaderChamsEvent(Framebuffer beforeHandFbo, Framebuffer withHandFbo) {
      this.beforeHandFbo = beforeHandFbo;
      this.withHandFbo = withHandFbo;
   }

   public Framebuffer getBeforeHandFbo() {
      return this.beforeHandFbo;
   }

   public Framebuffer getWithHandFbo() {
      return this.withHandFbo;
   }
}
