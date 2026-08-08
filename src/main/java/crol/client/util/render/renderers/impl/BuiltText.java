package crol.client.util.render.renderers.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import crol.client.util.color.ColorUtil;
import crol.client.util.render.msdf.MsdfFont;
import crol.client.util.render.providers.ResourceProvider;
import crol.client.util.render.renderers.IRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public record BuiltText(MsdfFont font, String text, float size, float thickness, int color, float smoothness, float spacing, int outlineColor, float outlineThickness, boolean gradient, boolean gradientAlpha, double alpha) implements IRenderer {
   private static final ShaderProgramKey MSDF_FONT_SHADER_KEY;

   public void render(Matrix4f matrix, float x, float y, float z) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.setShaderTexture(0, this.font.getTextureId());
      boolean outlineEnabled = this.outlineThickness > 0.0F;
      ShaderProgram shader = RenderSystem.setShader(MSDF_FONT_SHADER_KEY);
      shader.getUniform("Range").set(this.font.getAtlas().range());
      shader.getUniform("Thickness").set(this.thickness);
      shader.getUniform("Smoothness").set(this.smoothness);
      shader.getUniform("Outline").set(outlineEnabled ? 1 : 0);
      if (outlineEnabled) {
         shader.getUniform("OutlineThickness").set(this.outlineThickness);
         float[] outlineComponents = ColorUtil.normalize(this.outlineColor);
         shader.getUniform("OutlineColor").set(outlineComponents[0], outlineComponents[1], outlineComponents[2], outlineComponents[3]);
      }

      BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      this.font.applyGlyphs(matrix, builder, this.text, this.size, (this.thickness + this.outlineThickness * 0.5F) * 0.5F * this.size, this.spacing, x, y + this.font.getMetrics().baselineHeight() * this.size, z, this.color, this.gradient, this.gradientAlpha, this.alpha);
      BuiltBuffer builtBuffer = builder.endNullable();
      if (builtBuffer != null) {
         BufferRenderer.drawWithGlobalProgram(builtBuffer);
      }

      RenderSystem.setShaderTexture(0, 0);
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   }

   static {
      MSDF_FONT_SHADER_KEY = new ShaderProgramKey(ResourceProvider.getShaderIdentifier("msdf_font"), VertexFormats.POSITION_TEXTURE_COLOR, Defines.EMPTY);
   }
}
