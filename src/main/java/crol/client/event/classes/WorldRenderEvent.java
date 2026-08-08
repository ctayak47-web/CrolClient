package crol.client.event.classes;

import crol.client.event.CancellableEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public class WorldRenderEvent extends CancellableEvent {
   private RenderTickCounter renderTickCounter;
   private MatrixStack matrixStack;

   public void update(RenderTickCounter renderTickCounter, MatrixStack matrixStack) {
      this.renderTickCounter = renderTickCounter;
      this.matrixStack = matrixStack;
   }

   public RenderTickCounter getRenderTickCounter() {
      return this.renderTickCounter;
   }

   public MatrixStack getMatrixStack() {
      return this.matrixStack;
   }
}
