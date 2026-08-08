package crol.client.util.render.renderers.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
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
public class HandBlurRenderer {
   private static final ShaderProgramKey HAND_BLUR_KEY;
   private Framebuffer withHandFbo;
   private Framebuffer beforeHandFbo;
   private final float blurRadius;
   private final float threshold;
   private final float[] tintColor;
   private final int screenW;
   private final int screenH;

   public HandBlurRenderer(Framebuffer withHandFbo, Framebuffer beforeHandFbo, float blurRadius, float threshold, float[] tintColor, int screenW, int screenH) {
      this.withHandFbo = withHandFbo;
      this.beforeHandFbo = beforeHandFbo;
      this.blurRadius = blurRadius;
      this.threshold = threshold;
      this.tintColor = tintColor;
      this.screenW = screenW;
      this.screenH = screenH;
   }

   public void render(Matrix4f matrix) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.setShaderTexture(0, this.withHandFbo.getColorAttachment());
      RenderSystem.setShaderTexture(1, this.beforeHandFbo.getColorAttachment());
      ShaderProgram shader = RenderSystem.setShader(HAND_BLUR_KEY);
      if (shader == null) {
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
      } else {
         GlUniform blurU = shader.getUniform("BlurRadius");
         GlUniform thrU = shader.getUniform("Threshold");
         GlUniform tintU = shader.getUniform("TintColor");
         if (blurU != null) {
            blurU.set(this.blurRadius);
         }

         if (thrU != null) {
            thrU.set(this.threshold);
         }

         if (tintU != null) {
            tintU.set(this.tintColor[0], this.tintColor[1], this.tintColor[2], this.tintColor[3]);
         }

         int white = (new Color(255, 255, 255, 255)).getRGB();
         BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
         builder.vertex(matrix, 0.0F, (float)this.screenH, 0.0F).color(white);
         builder.vertex(matrix, 0.0F, 0.0F, 0.0F).color(white);
         builder.vertex(matrix, (float)this.screenW, 0.0F, 0.0F).color(white);
         builder.vertex(matrix, (float)this.screenW, (float)this.screenH, 0.0F).color(white);
         BufferRenderer.drawWithGlobalProgram(builder.end());
         RenderSystem.setShaderTexture(0, 0);
         RenderSystem.setShaderTexture(1, 0);
         RenderSystem.setShaderTexture(2, 0);
         RenderSystem.enableCull();
         RenderSystem.disableBlend();
      }
   }

   public void updateFramebuffers(Framebuffer withHand, Framebuffer beforeHand) {
      this.withHandFbo = withHand;
      this.beforeHandFbo = beforeHand;
   }

   public void updateColor(float r, float g, float b, float a) {
      this.tintColor[0] = r;
      this.tintColor[1] = g;
      this.tintColor[2] = b;
      this.tintColor[3] = a;
   }

   static {
      HAND_BLUR_KEY = new ShaderProgramKey(Identifier.of("crol", "core/hand_blur"), VertexFormats.POSITION_COLOR, Defines.EMPTY);
   }
}
