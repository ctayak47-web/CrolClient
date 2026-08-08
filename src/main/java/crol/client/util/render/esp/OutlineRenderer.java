package crol.client.util.render.esp;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class OutlineRenderer {
   private static final List<Entry> QUEUE = new ArrayList();

   public static void enqueue(EntityModel<?> model, MatrixStack matrices, int color) {
      MatrixStack copy = new MatrixStack();
      copy.peek().getPositionMatrix().set(matrices.peek().getPositionMatrix());
      copy.peek().getNormalMatrix().set(matrices.peek().getNormalMatrix());
      QUEUE.add(new Entry(model, copy.peek(), color));
   }

   public static boolean hasEntries() {
      return !QUEUE.isEmpty();
   }

   public static void renderAll() {
      if (!QUEUE.isEmpty()) {
         setupShader();

         for(Entry e : QUEUE) {
            MatrixStack ms = new MatrixStack();
            ms.peek().getPositionMatrix().set(e.matrixEntry().getPositionMatrix());
            ms.peek().getNormalMatrix().set(e.matrixEntry().getNormalMatrix());
            GL11.glEnable(2960);
            GL11.glStencilMask(255);
            GL11.glClear(1024);
            GL11.glStencilFunc(519, 1, 255);
            GL11.glStencilOp(7680, 7680, 7681);
            RenderSystem.colorMask(false, false, false, false);
            RenderSystem.depthMask(false);
            RenderSystem.disableBlend();
            drawModel(e.model, ms, -1);
            GL11.glStencilFunc(517, 1, 255);
            GL11.glStencilOp(7680, 7680, 7680);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA, SrcFactor.ONE, DstFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.depthMask(false);
            RenderSystem.depthFunc(519);
            ms.push();
            ms.scale(1.05F, 1.05F, 1.05F);
            drawModel(e.model, ms, e.color);
            ms.pop();
            GL11.glStencilMask(255);
            GL11.glStencilFunc(519, 0, 255);
            GL11.glDisable(2960);
         }

         QUEUE.clear();
         RenderSystem.depthFunc(515);
         RenderSystem.depthMask(true);
         RenderSystem.disableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.colorMask(true, true, true, true);
      }
   }

   private static void setupShader() {
      RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_ENTITY_TRANSLUCENT);
      RenderSystem.setShaderTexture(0, MinecraftClient.getInstance().getTextureManager().getTexture(Identifier.ofVanilla("textures/misc/white.png")).getGlId());
      MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager().enable();
      MinecraftClient.getInstance().gameRenderer.getOverlayTexture().setupOverlayColor();
      RenderSystem.enableCull();
   }

   private static void drawModel(EntityModel<?> model, MatrixStack matrices, int color) {
      BufferBuilder buf = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL);
      model.render(matrices, buf, 15728880, OverlayTexture.DEFAULT_UV, color);
      BuiltBuffer built = buf.endNullable();
      if (built != null) {
         BufferRenderer.drawWithGlobalProgram(built);
         built.close();
      }

   }

   @Environment(EnvType.CLIENT)
   public static record Entry(EntityModel<?> model, MatrixStack.Entry matrixEntry, int color) {
   }
}
