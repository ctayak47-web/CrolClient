package crol.client.util.aura.esp.impl;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import crol.client.CrolClient;
import crol.client.util.IUtil;
import crol.client.util.aura.esp.EspMode;
import crol.client.util.math.MathUtil;
import java.awt.Color;
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
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class FireMode extends EspMode implements IUtil {
   public void render(Entity target, MatrixStack matrice2) {
      Camera camera = mc.gameRenderer.getCamera();
      Vec3d targetPos = target.getLerpedPos(MathUtil.getTickDelta()).subtract(camera.getPos());
      double tPosX = targetPos.x;
      double tPosY = targetPos.y;
      double tPosZ = targetPos.z;
      float iAge = (float)target.age + MathUtil.getTickDelta();
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      RenderSystem.setShaderTexture(0, IUtil.fire);
      RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);

      assert mc.player != null;

      if (mc.player.canSee(target)) {
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(false);
      } else {
         RenderSystem.disableDepthTest();
      }

      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      float ageMultiplier = iAge * 2.5F;
      Color themeColor = CrolClient.INSTANCE.getThemeManager().getTheme().color();

      for(int j = 0; j < 3; ++j) {
         float jOffset = (float)(j * 120);
         float jMultiplier = (float)(j + 1);

         for(int i = 0; i <= 14; ++i) {
            float iFloat = (float)i;
            double radians = Math.toRadians((double)(((iFloat / 1.5F + iAge) * 8.0F + jOffset) % 2880.0F));
            double sinQuad = Math.sin(Math.toRadians((double)(ageMultiplier + (float)i * jMultiplier)) * (double)3.0F) / (double)1.8F;
            float offset = iFloat / 14.0F;
            float scale = 0.3F;
            int color = applyOpacity(themeColor.getRGB(), offset);
            double ghostX = tPosX + Math.cos(radians) * (double)target.getWidth();
            double ghostY = tPosY + (double)1.0F + sinQuad;
            double ghostZ = tPosZ + Math.sin(radians) * (double)target.getWidth();
            MatrixStack matrices = new MatrixStack();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
            matrices.translate(ghostX, ghostY, ghostZ);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            Matrix4f matrix = matrices.peek().getPositionMatrix();
            buffer.vertex(matrix, -scale, scale, 0.0F).texture(0.0F, 1.0F).color(color);
            buffer.vertex(matrix, scale, scale, 0.0F).texture(1.0F, 1.0F).color(color);
            buffer.vertex(matrix, scale, -scale, 0.0F).texture(1.0F, 0.0F).color(color);
            buffer.vertex(matrix, -scale, -scale, 0.0F).texture(0.0F, 0.0F).color(color);
         }
      }

      BufferRenderer.drawWithGlobalProgram(buffer.end());
      if (mc.player.canSee(target)) {
         RenderSystem.depthMask(true);
      }

      RenderSystem.enableDepthTest();
      RenderSystem.disableBlend();
   }

   public static int applyOpacity(int color_int, float opacity) {
      opacity = Math.min(1.0F, Math.max(0.0F, opacity));
      Color color = new Color(color_int);
      return (new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)((float)color.getAlpha() * opacity))).getRGB();
   }
}
