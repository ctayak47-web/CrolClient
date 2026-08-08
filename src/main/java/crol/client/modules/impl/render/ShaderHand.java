package crol.client.modules.impl.render;

import crol.client.CrolClient;
import crol.client.event.classes.ShaderEvent;
import crol.client.event.classes.ShaderHandEvent2D;
import crol.client.event.interfaces.IShader;
import crol.client.event.interfaces.IShaderHandable;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.util.color.ColorUtil;
import crol.client.util.player.MovementUtil;
import crol.client.util.render.renderers.impl.HandBlurRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.option.Perspective;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class ShaderHand extends Module implements IShader, IShaderHandable {
   private Framebuffer savedBefore = null;
   private Framebuffer savedAfter = null;

   public ShaderHand() {
      super(new ModuleInfo("ShaderHand", Category.RENDER, "Изменяет отображение руки от первого лица"));
   }

   public void onShader(ShaderEvent event) {
      this.savedBefore = event.getBeforeHandFbo();
      this.savedAfter = event.getWithHandFbo();
   }

   public void onHandRender(ShaderHandEvent2D event) {
      if (!MovementUtil.isCameraInWater()) {
         if (MinecraftClient.getInstance().options.getPerspective() == Perspective.FIRST_PERSON) {
            if (this.savedBefore != null && this.savedAfter != null) {
               if (this.savedBefore.getColorAttachment() != 0 && this.savedAfter.getColorAttachment() != 0) {
                  int w = MinecraftClient.getInstance().getWindow().getScaledWidth();
                  int h = MinecraftClient.getInstance().getWindow().getScaledHeight();
                  if (w > 0 && h > 0) {
                     Matrix4f matrix = event.getDrawContext().getMatrices().peek().getPositionMatrix();
                     float[] color = ColorUtil.rgba(CrolClient.INSTANCE.getThemeManager().getTheme().color().getRGB());
                     (new HandBlurRenderer(this.savedAfter, this.savedBefore, 12.0F, 0.01F, new float[]{color[0], color[1], color[2], 0.6F}, w, h)).render(matrix);
                  }
               }
            }
         }
      }
   }
}
