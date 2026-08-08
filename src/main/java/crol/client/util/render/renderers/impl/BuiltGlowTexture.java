package crol.client.util.render.renderers.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import crol.client.util.render.builders.states.QuadColorState;
import crol.client.util.render.builders.states.QuadRadiusState;
import crol.client.util.render.builders.states.SizeState;
import crol.client.util.render.providers.ResourceProvider;
import crol.client.util.render.renderers.IRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public record BuiltGlowTexture(SizeState size, QuadRadiusState radius, QuadColorState color, float smoothness, float u, float v, float texWidth, float texHeight, int textureId) implements IRenderer {
   private static final ShaderProgramKey TEXTURE_SHADER_KEY;
   private static final float GLOW_RADIUS = 8.0F;
   private static final int GLOW_PASSES = 6;

   public void render(Matrix4f matrix, float x, float y, float z) {
      float width = this.size.width();
      float height = this.size.height();
      float cx = x + width / 2.0F;
      float cy = y + height / 2.0F;
      float hw = width / 2.0F;
      float hh = height / 2.0F;
      float[] vx = new float[]{cx + hh, cx - hh, cx - hh, cx + hh};
      float[] vy = new float[]{cy - hw, cy - hw, cy + hw, cy + hw};
      float[][] uvs = new float[][]{{this.u, this.v}, {this.u, this.v + this.texHeight}, {this.u + this.texWidth, this.v + this.texHeight}, {this.u + this.texWidth, this.v}};
      int[] colors = new int[]{this.color.color1(), this.color.color2(), this.color.color3(), this.color.color4()};
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.setShaderTexture(0, this.textureId);
      ShaderProgram shader = RenderSystem.setShader(TEXTURE_SHADER_KEY);
      shader.getUniform("Size").set(width, height);
      shader.getUniform("Radius").set(this.radius.radius1(), this.radius.radius2(), this.radius.radius3(), this.radius.radius4());
      shader.getUniform("Smoothness").set(this.smoothness);

      for(int pass = 6; pass >= 1; --pass) {
         float offset = (float)pass * 1.3333334F;
         float alpha = (1.0F - (float)pass / 7.0F) * 0.35F;
         int glowAlpha = (int)(alpha * 255.0F);
         int glowColor = glowAlpha << 24 | 16777215;
         BufferBuilder glowBuilder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

         for(int i = 0; i < 4; ++i) {
            float dx = vx[i] - cx;
            float dy = vy[i] - cy;
            float len = (float)Math.sqrt((double)(dx * dx + dy * dy));
            float nx = len > 0.0F ? dx / len : 0.0F;
            float ny = len > 0.0F ? dy / len : 0.0F;
            glowBuilder.vertex(matrix, vx[i] + nx * offset, vy[i] + ny * offset, z).texture(uvs[i][0], uvs[i][1]).color(glowColor);
         }

         BufferRenderer.drawWithGlobalProgram(glowBuilder.end());
      }

      BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

      for(int i = 0; i < 4; ++i) {
         builder.vertex(matrix, vx[i], vy[i], z).texture(uvs[i][0], uvs[i][1]).color(colors[i]);
      }

      BufferRenderer.drawWithGlobalProgram(builder.end());
      RenderSystem.setShaderTexture(0, 0);
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   }

   static {
      TEXTURE_SHADER_KEY = new ShaderProgramKey(ResourceProvider.getShaderIdentifier("texture"), VertexFormats.POSITION_TEXTURE_COLOR, Defines.EMPTY);
   }
}
