package crol.client.modules.impl.render;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import crol.client.CrolClient;
import crol.client.event.classes.Render2DEvent;
import crol.client.event.classes.Render3DEvent;
import crol.client.event.interfaces.IRenderable2D;
import crol.client.event.interfaces.IRenderable3D;
import crol.client.managers.FontManager;
import crol.client.modules.Category;
import crol.client.modules.IDisableable;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.util.IUtil;
import crol.client.util.render.ProjectionUtil;
import crol.client.util.render.builders.Builder;
import crol.client.util.render.msdf.MsdfFont;
import crol.client.util.render.renderers.impl.BuiltText;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class Prediction extends Module implements IRenderable3D, IUtil, IDisableable, IRenderable2D {
   private final List<PredictionPearl> pearls = new ArrayList();

   public Prediction() {
      super(new ModuleInfo("Prediction", Category.RENDER, "Показывает траекторию и место падения летящим предметам"));
   }

   public void onRender3D(Render3DEvent event) {
      MatrixStack matrices = event.getMatrixStack();
      Camera camera = mc.gameRenderer.getCamera();
      Vec3d cameraPos = camera.getPos();
      matrices.push();
      matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
      Color themeColor = CrolClient.INSTANCE.getThemeManager().getTheme().color();
      int color = themeColor.getRGB();
      float r = (float)(color >> 16 & 255) / 255.0F;
      float g = (float)(color >> 8 & 255) / 255.0F;
      float b = (float)(color & 255) / 255.0F;
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      this.pearls.clear();
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
      RenderSystem.disableCull();
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);

      for(Entity entity : mc.world.getEntities()) {
         if (entity instanceof EnderPearlEntity enderPearl) {
            RenderSystem.disableDepthTest();
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            BufferBuilder bufBehind = Tessellator.getInstance().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            Vec3d simPos = enderPearl.getPos();
            Vec3d simMotion = enderPearl.getVelocity();
            boolean behindEmpty = true;

            for(int i = 0; i < 150; ++i) {
               Vec3d simPrev = simPos;
               simPos = simPos.add(simMotion);
               simMotion = this.getNextMotion(enderPearl, simMotion);
               bufBehind.vertex(matrix, (float)simPrev.x, (float)simPrev.y, (float)simPrev.z).color(r, g, b, 0.2F);
               BlockHitResult hit = mc.world.raycast(new RaycastContext(simPrev, simPos, ShapeType.COLLIDER, FluidHandling.NONE, enderPearl));
               boolean isLast = hit.getType() == Type.BLOCK;
               if (isLast) {
                  simPos = hit.getPos();
               }

               bufBehind.vertex(matrix, (float)simPos.x, (float)simPos.y, (float)simPos.z).color(r, g, b, 0.2F);
               behindEmpty = false;
               if (isLast || simPos.y < (double)0.0F) {
                  break;
               }
            }

            if (!behindEmpty) {
               BufferRenderer.drawWithGlobalProgram(bufBehind.end());
            }

            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(515);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
            Vec3d pos = enderPearl.getPos();
            Vec3d motion = enderPearl.getVelocity();
            boolean empty = true;
            int totalTicks = 0;
            Vec3d landPos = null;

            for(int i = 0; i < 150; ++i) {
               Vec3d prevPos = pos;
               pos = pos.add(motion);
               motion = this.getNextMotion(enderPearl, motion);
               float fade = 1.0F - (float)i / 150.0F * 0.5F;
               buffer.vertex(matrix, (float)prevPos.x, (float)prevPos.y, (float)prevPos.z).color(r * fade, g * fade, b * fade, 1.0F);
               BlockHitResult blockHit = mc.world.raycast(new RaycastContext(prevPos, pos, ShapeType.COLLIDER, FluidHandling.NONE, enderPearl));
               boolean isLast = blockHit.getType() == Type.BLOCK;
               if (isLast) {
                  pos = blockHit.getPos();
                  totalTicks = i + 1;
                  landPos = pos;
               }

               buffer.vertex(matrix, (float)pos.x, (float)pos.y, (float)pos.z).color(r * fade, g * fade, b * fade, 1.0F);
               empty = false;
               if (isLast || pos.y < (double)0.0F) {
                  break;
               }
            }

            if (!empty) {
               BufferRenderer.drawWithGlobalProgram(buffer.end());
            }

            if (landPos != null) {
               RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
               BufferBuilder crossBuf = Tessellator.getInstance().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
               float cx = (float)landPos.x;
               float cy = (float)landPos.y;
               float cz = (float)landPos.z;
               float s = 0.2F;
               crossBuf.vertex(matrix, cx - s, cy, cz).color(r, g, b, 1.0F);
               crossBuf.vertex(matrix, cx + s, cy, cz).color(r, g, b, 1.0F);
               crossBuf.vertex(matrix, cx, cy, cz - s).color(r, g, b, 1.0F);
               crossBuf.vertex(matrix, cx, cy, cz + s).color(r, g, b, 1.0F);
               BufferRenderer.drawWithGlobalProgram(crossBuf.end());
               float seconds = (float)totalTicks / 20.0F;
               float progress = Math.min(1.0F, seconds / 7.5F);
               this.pearls.add(new PredictionPearl(ProjectionUtil.project(landPos.x, landPos.y, landPos.z), seconds, progress));
            }
         }
      }

      GL11.glDisable(2848);
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.depthFunc(515);
      matrices.pop();
   }

   public void onRender2D(Render2DEvent event) {
      if (!this.pearls.isEmpty()) {
         Matrix4f matrix = event.getDrawContext().getMatrices().peek().getPositionMatrix();
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
         RenderSystem.disableCull();
         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         int segments = 64;
         BufferBuilder buf = Tessellator.getInstance().begin(DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

         for(PredictionPearl pearl : this.pearls) {
            float cx = pearl.vector2f.getX();
            float cy = pearl.vector2f.getY();
            this.appendFilledCircle(buf, matrix, cx, cy, 8.1F, new Color(-871559923, true), segments);
         }

         BufferRenderer.drawWithGlobalProgram(buf.end());
         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         buf = Tessellator.getInstance().begin(DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

         for(PredictionPearl pearl : this.pearls) {
            float cx = pearl.vector2f.getX();
            float cy = pearl.vector2f.getY();
            this.appendThickArc(buf, matrix, cx, cy, 8.1F, 1.75F, 0.0F, 1.0F, (new Color(-871559923, true)).brighter().brighter(), segments);
         }

         BufferRenderer.drawWithGlobalProgram(buf.end());
         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         buf = Tessellator.getInstance().begin(DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);

         for(PredictionPearl pearl : this.pearls) {
            if (!(pearl.progress <= 0.0F)) {
               float cx = pearl.vector2f.getX();
               float cy = pearl.vector2f.getY();
               Color arcColor = CrolClient.INSTANCE.getThemeManager().getTheme().color();
               this.appendThickArc(buf, matrix, cx, cy, 8.1F, 1.75F, 0.0F, pearl.progress, arcColor, segments);
            }
         }

         BufferRenderer.drawWithGlobalProgram(buf.end());

         for(PredictionPearl pearl : this.pearls) {
            float cx = pearl.vector2f.getX();
            float cy = pearl.vector2f.getY();
            String timeStr = String.format("%.1fs", pearl.seconds);
            float textWidth = ((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).getWidth(timeStr, 5.5F);
            Color textColor = Color.WHITE;
            Matrix4f textMatrix = event.getDrawContext().getMatrices().peek().getPositionMatrix();
            BuiltText text = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(timeStr).color(textColor).size(5.5F).thickness(0.05F).build();
            text.render(textMatrix, cx - textWidth / 2.0F, cy - 3.0F, 5.0F);
         }

         RenderSystem.enableCull();
         RenderSystem.disableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         this.pearls.clear();
      }
   }

   private void appendFilledCircle(BufferBuilder buf, Matrix4f matrix, float cx, float cy, float radius, Color color, int segments) {
      float r = (float)color.getRed() / 255.0F;
      float g = (float)color.getGreen() / 255.0F;
      float b = (float)color.getBlue() / 255.0F;
      float a = (float)color.getAlpha() / 255.0F;
      float softEdge = 1.2F;
      float hardR = radius - softEdge;

      for(int i = 0; i < segments; ++i) {
         float a0 = (float)((Math.PI * 2D) * (double)i / (double)segments);
         float a1 = (float)((Math.PI * 2D) * (double)(i + 1) / (double)segments);
         float cos0 = (float)Math.cos((double)a0);
         float sin0 = (float)Math.sin((double)a0);
         float cos1 = (float)Math.cos((double)a1);
         float sin1 = (float)Math.sin((double)a1);
         buf.vertex(matrix, cx, cy, 0.0F).color(r, g, b, a);
         buf.vertex(matrix, cx + cos0 * hardR, cy + sin0 * hardR, 0.0F).color(r, g, b, a);
         buf.vertex(matrix, cx + cos1 * hardR, cy + sin1 * hardR, 0.0F).color(r, g, b, a);
         buf.vertex(matrix, cx + cos0 * hardR, cy + sin0 * hardR, 0.0F).color(r, g, b, a);
         buf.vertex(matrix, cx + cos0 * radius, cy + sin0 * radius, 0.0F).color(r, g, b, 0.0F);
         buf.vertex(matrix, cx + cos1 * hardR, cy + sin1 * hardR, 0.0F).color(r, g, b, a);
         buf.vertex(matrix, cx + cos1 * hardR, cy + sin1 * hardR, 0.0F).color(r, g, b, a);
         buf.vertex(matrix, cx + cos0 * radius, cy + sin0 * radius, 0.0F).color(r, g, b, 0.0F);
         buf.vertex(matrix, cx + cos1 * radius, cy + sin1 * radius, 0.0F).color(r, g, b, 0.0F);
      }

   }

   private void appendThickArc(BufferBuilder buf, Matrix4f matrix, float cx, float cy, float radius, float thickness, float startProgress, float endProgress, Color color, int segments) {
      if (!(endProgress <= startProgress)) {
         float r = (float)color.getRed() / 255.0F;
         float g = (float)color.getGreen() / 255.0F;
         float b = (float)color.getBlue() / 255.0F;
         float a = (float)color.getAlpha() / 255.0F;
         float soft = 0.8F;
         float innerH = radius - thickness / 2.0F;
         float outerH = radius + thickness / 2.0F;
         float innerS = innerH - soft;
         float outerS = outerH + soft;
         float[] rs = new float[]{innerS, innerH, outerH, outerS};
         float[] as = new float[]{0.0F, a, a, 0.0F};
         int arcSegs = Math.max(3, (int)((float)segments * (endProgress - startProgress)));

         for(int layer = 0; layer < 3; ++layer) {
            float rIn = rs[layer];
            float rOut = rs[layer + 1];
            float aIn = as[layer];
            float aOut = as[layer + 1];

            for(int i = 0; i < arcSegs; ++i) {
               float t0 = startProgress + (endProgress - startProgress) * ((float)i / (float)arcSegs);
               float t1 = startProgress + (endProgress - startProgress) * ((float)(i + 1) / (float)arcSegs);
               float ang0 = (float)((-Math.PI / 2D) + (Math.PI * 2D) * (double)t0);
               float ang1 = (float)((-Math.PI / 2D) + (Math.PI * 2D) * (double)t1);
               float cos0 = (float)Math.cos((double)ang0);
               float sin0 = (float)Math.sin((double)ang0);
               float cos1 = (float)Math.cos((double)ang1);
               float sin1 = (float)Math.sin((double)ang1);
               buf.vertex(matrix, cx + cos0 * rOut, cy + sin0 * rOut, 0.0F).color(r, g, b, aOut);
               buf.vertex(matrix, cx + cos0 * rIn, cy + sin0 * rIn, 0.0F).color(r, g, b, aIn);
               buf.vertex(matrix, cx + cos1 * rOut, cy + sin1 * rOut, 0.0F).color(r, g, b, aOut);
               buf.vertex(matrix, cx + cos1 * rOut, cy + sin1 * rOut, 0.0F).color(r, g, b, aOut);
               buf.vertex(matrix, cx + cos0 * rIn, cy + sin0 * rIn, 0.0F).color(r, g, b, aIn);
               buf.vertex(matrix, cx + cos1 * rIn, cy + sin1 * rIn, 0.0F).color(r, g, b, aIn);
            }
         }

      }
   }

   private Vec3d getNextMotion(EnderPearlEntity enderPearl, Vec3d motion) {
      if (enderPearl.isTouchingWater()) {
         motion = motion.multiply(0.8);
      } else {
         motion = motion.multiply(0.99);
      }

      if (!enderPearl.hasNoGravity()) {
         motion = motion.add((double)0.0F, -0.03, (double)0.0F);
      }

      return motion;
   }

   @Compile
   public void onDisable() {
      this.pearls.clear();
   }

   @Environment(EnvType.CLIENT)
   private static class PredictionPearl {
      final Vector2f vector2f;
      final float seconds;
      final float progress;

      PredictionPearl(Vector2f v, float s, float p) {
         this.vector2f = v;
         this.seconds = s;
         this.progress = p;
      }
   }
}
