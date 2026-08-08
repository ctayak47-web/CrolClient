package crol.client.util.render.renderers.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class OutlineBlurRenderer {
   private static final ShaderProgramKey OUTLINE_BLUR_KEY;
   private Framebuffer outlineFbo;
   private Framebuffer mainFbo;
   private final float blurRadius;
   private float[] tintColor;
   private final int screenW;
   private final int screenH;

   public OutlineBlurRenderer(Framebuffer outlineFbo, Framebuffer mainFbo, float blurRadius, float[] tintColor, int screenW, int screenH) {
      this.outlineFbo = outlineFbo;
      this.mainFbo = mainFbo;
      this.blurRadius = blurRadius;
      this.tintColor = tintColor;
      this.screenW = screenW;
      this.screenH = screenH;
   }

   public void updateFramebuffers(Framebuffer outline, Framebuffer main) {
      this.outlineFbo = outline;
      this.mainFbo = main;
   }

   public void updateColor(float r, float g, float b, float a) {
      this.tintColor = new float[]{r, g, b, a};
   }

   public void render(Matrix4f matrix) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.setShaderTexture(0, this.outlineFbo.getColorAttachment());
      RenderSystem.setShaderTexture(1, this.mainFbo.getColorAttachment());
      ShaderProgram shader = RenderSystem.setShader(OUTLINE_BLUR_KEY);
      if (shader == null) {
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
      } else {
         GlUniform blurU = shader.getUniform("BlurRadius");
         GlUniform tintU = shader.getUniform("TintColor");
         GlUniform sizeU = shader.getUniform("InSize");
         if (blurU != null) {
            blurU.set(this.blurRadius);
         }

         if (tintU != null) {
            tintU.set(this.tintColor[0], this.tintColor[1], this.tintColor[2], this.tintColor[3]);
         }

         if (sizeU != null) {
            sizeU.set((float)this.outlineFbo.textureWidth, (float)this.outlineFbo.textureHeight);
         }

         int white = -1;
         BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
         builder.vertex(matrix, 0.0F, (float)this.screenH, 0.0F).color(white);
         builder.vertex(matrix, 0.0F, 0.0F, 0.0F).color(white);
         builder.vertex(matrix, (float)this.screenW, 0.0F, 0.0F).color(white);
         builder.vertex(matrix, (float)this.screenW, (float)this.screenH, 0.0F).color(white);
         BufferRenderer.drawWithGlobalProgram(builder.end());
         RenderSystem.setShaderTexture(0, 0);
         RenderSystem.setShaderTexture(1, 0);
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
      }
   }

   static {
      OUTLINE_BLUR_KEY = new ShaderProgramKey(Identifier.of("crol", "core/outline_blur"), VertexFormats.POSITION_COLOR, Defines.EMPTY);
   }
}
