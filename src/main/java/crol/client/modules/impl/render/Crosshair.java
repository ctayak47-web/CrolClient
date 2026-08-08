package crol.client.modules.impl.render;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import crol.client.CrolClient;
import crol.client.event.classes.ShaderHandEvent2D;
import crol.client.event.interfaces.IShaderHandable;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class Crosshair extends Module implements IShaderHandable {
   private float smoothProgress = 0.0F;
   private float prevProgress = 0.0F;
   private float hitAnim = 0.0F;
   private float hitScale = 1.0F;
   private static final float BG_R = 0.15F;
   private static final float BG_G = 0.15F;
   private static final float BG_B = 0.15F;
   private static final float BG_A = 0.6F;
   private static final float BASE_RADIUS = 5.75F;
   private static final float RING_RADIUS = 4.8875003F;
   private static final float RING_WIDTH = 1.4F;
   private static final int SEGMENTS = 128;
   private static final float SOFT = 0.7F;
   private static final float[] LAYER_R = new float[]{3.4875004F, 4.1875005F, 5.5875F, 6.2875F};

   public Crosshair() {
      super(new ModuleInfo("Crosshair", Category.RENDER, "Изменяет прицел в игре"));
   }

   private void appendThickArcSoft(BufferBuilder buf, Matrix4f matrix, float cx, float cy, float startProgress, float endProgress, float r, float g, float b, float a, int segments) {
      if (!(endProgress <= startProgress)) {
         float[] as = new float[]{0.0F, a, a, 0.0F};
         int arcSegs = Math.max(3, (int)((float)segments * (endProgress - startProgress)));
         float span = endProgress - startProgress;
         double startAngle = (-Math.PI / 2D) + (Math.PI * 2D) * (double)startProgress;
         double step = (Math.PI * 2D) * (double)span / (double)arcSegs;
         float prevCos = (float)Math.cos(startAngle);
         float prevSin = (float)Math.sin(startAngle);

         for(int layer = 0; layer < 3; ++layer) {
            float innerR = LAYER_R[layer];
            float outerR = LAYER_R[layer + 1];
            float innerA = as[layer];
            float outerA = as[layer + 1];
            prevCos = (float)Math.cos(startAngle);
            prevSin = (float)Math.sin(startAngle);

            for(int i = 0; i < arcSegs; ++i) {
               double ang1 = startAngle + step * (double)(i + 1);
               float cos1 = (float)Math.cos(ang1);
               float sin1 = (float)Math.sin(ang1);
               float x0i = cx + prevCos * innerR;
               float y0i = cy + prevSin * innerR;
               float x0o = cx + prevCos * outerR;
               float y0o = cy + prevSin * outerR;
               float x1i = cx + cos1 * innerR;
               float y1i = cy + sin1 * innerR;
               float x1o = cx + cos1 * outerR;
               float y1o = cy + sin1 * outerR;
               buf.vertex(matrix, x0o, y0o, 0.0F).color(r, g, b, outerA);
               buf.vertex(matrix, x0i, y0i, 0.0F).color(r, g, b, innerA);
               buf.vertex(matrix, x1o, y1o, 0.0F).color(r, g, b, outerA);
               buf.vertex(matrix, x1o, y1o, 0.0F).color(r, g, b, outerA);
               buf.vertex(matrix, x0i, y0i, 0.0F).color(r, g, b, innerA);
               buf.vertex(matrix, x1i, y1i, 0.0F).color(r, g, b, innerA);
               prevCos = cos1;
               prevSin = sin1;
            }
         }

      }
   }

   private float clamp(float v) {
      return v < 0.0F ? 0.0F : (v > 1.0F ? 1.0F : v);
   }

   public void onHandRender(ShaderHandEvent2D event) {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.player != null && mc.world != null && mc.options.getPerspective() == Perspective.FIRST_PERSON) {
         Matrix4f matrix = event.getDrawContext().getMatrices().peek().getPositionMatrix();
         float cx = (float)mc.getWindow().getScaledWidth() / 2.0F;
         float cy = (float)mc.getWindow().getScaledHeight() / 2.0F;
         int themeRGB = CrolClient.INSTANCE.getThemeManager().getTheme().color().getRGB();
         float themeR = (float)(themeRGB >> 16 & 255) / 255.0F;
         float themeG = (float)(themeRGB >> 8 & 255) / 255.0F;
         float themeB = (float)(themeRGB & 255) / 255.0F;
         float realProgress = mc.player.getAttackCooldownProgress(0.0F);
         if (this.prevProgress > 0.85F && realProgress < 0.2F) {
            this.hitAnim = 1.0F;
         }

         this.prevProgress = realProgress;
         this.smoothProgress += (realProgress - this.smoothProgress) * (realProgress > this.smoothProgress ? 0.15F : 0.08F);
         if (this.smoothProgress < 0.0F) {
            this.smoothProgress = 0.0F;
         } else if (this.smoothProgress > 1.0F) {
            this.smoothProgress = 1.0F;
         }

         if (this.hitAnim > 0.0F) {
            this.hitAnim -= 0.06F;
            if (this.hitAnim < 0.0F) {
               this.hitAnim = 0.0F;
            }

            float t = 1.0F - this.hitAnim;
            if (t < 0.25F) {
               this.hitScale = 1.0F - t / 0.25F * 0.35F;
            } else {
               float recover = (t - 0.25F) / 0.75F;
               this.hitScale = 0.65F + (float)(Math.sin((double)recover * Math.PI * (double)1.3F) * (double)0.08F) + recover * 0.35F;
               if (this.hitScale > 1.0F) {
                  this.hitScale = 1.0F;
               }
            }
         } else {
            this.hitScale = 1.0F;
         }

         float pulse = 0.0F;
         if (this.smoothProgress >= 0.99F && this.hitAnim <= 0.0F) {
            pulse = (float)(Math.sin((double)System.currentTimeMillis() / (double)700.0F) * (double)0.5F + (double)0.5F);
         }

         float arcR = themeR;
         float arcG = themeG;
         float arcB = themeB;
         if (pulse > 0.0F) {
            float pt = pulse * 0.25F;
            arcR = themeR + (1.0F - themeR) * pt;
            arcG = themeG + (1.0F - themeG) * pt;
            arcB = themeB + (1.0F - themeB) * pt;
         }

         if (this.hitAnim > 0.7F) {
            float flash = (this.hitAnim - 0.7F) / 0.3F * 0.6F;
            arcR += (1.0F - arcR) * flash;
            arcG += (1.0F - arcG) * flash;
            arcB += (1.0F - arcB) * flash;
         }

         float arcA = 0.9F + (this.hitAnim > 0.0F ? this.hitAnim * 0.1F : pulse * 0.1F);
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
         RenderSystem.disableCull();
         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         BufferBuilder buf = Tessellator.getInstance().begin(DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
         this.appendThickArcSoft(buf, matrix, cx, cy, 0.0F, 1.0F, 0.15F, 0.15F, 0.15F, 0.6F, 128);
         BufferRenderer.drawWithGlobalProgram(buf.end());
         if (this.smoothProgress > 0.005F) {
            buf = Tessellator.getInstance().begin(DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
            this.appendThickArcSoft(buf, matrix, cx, cy, 0.0F, this.smoothProgress, this.clamp(arcR), this.clamp(arcG), this.clamp(arcB), this.clamp(arcA), 128);
            BufferRenderer.drawWithGlobalProgram(buf.end());
         }

         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }
   }
}
