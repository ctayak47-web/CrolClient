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
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class DefaultMode extends EspMode implements IUtil {
   private static float rotationAngle = 0.0F;
   private static float rotationSpeed = 0.0F;
   private static boolean isReversing = false;
   private static final float maxRotationSpeed = 3.0F;
   private String mode = "1";
   private static long lastUpdateTime = System.currentTimeMillis();
   private static final float TIME_FACTOR = 0.2F;

   public void render(Entity target, MatrixStack matrices) {
      if (target != null && target != mc.player && !(target instanceof ArmorStandEntity)) {
         Camera camera = mc.gameRenderer.getCamera();
         Box hitBox = target.getBoundingBox();
         Vec3d targetPos = target.getLerpedPos(MathUtil.getTickDelta()).subtract(camera.getPos());
         double tPosX = targetPos.x;
         double tPosY = targetPos.y + (hitBox.maxY - hitBox.minY) / (double)2.0F;
         double tPosZ = targetPos.z;
         RenderSystem.disableDepthTest();
         RenderSystem.disableCull();
         matrices.push();
         matrices.translate(tPosX, tPosY, tPosZ);
         matrices.multiply(camera.getRotation());
         matrices.scale(0.6F, 0.6F, 0.6F);
         tick();
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationAngle));
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
         RenderSystem.setShaderTexture(0, Identifier.of("crol", "images/targetesps/" + this.mode));
         matrices.translate((double)-0.75F, (double)-0.75F, -0.01);
         Matrix4f matrix = matrices.peek().getPositionMatrix();
         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
         BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
         Color themeColor = CrolClient.INSTANCE.getThemeManager().getTheme().color();
         buffer.vertex(matrix, 0.0F, 1.5F, 0.0F).texture(0.0F, 1.0F).color(themeColor.getRGB());
         buffer.vertex(matrix, 1.5F, 1.5F, 0.0F).texture(1.0F, 1.0F).color(themeColor.getRGB());
         buffer.vertex(matrix, 1.5F, 0.0F, 0.0F).texture(1.0F, 0.0F).color(themeColor.getRGB());
         buffer.vertex(matrix, 0.0F, 0.0F, 0.0F).texture(0.0F, 0.0F).color(themeColor.getRGB());
         BufferRenderer.drawWithGlobalProgram(buffer.end());
         RenderSystem.enableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
         RenderSystem.disableBlend();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         matrices.pop();
      }
   }

   private static void tick() {
      long currentTime = System.currentTimeMillis();
      long deltaTime = currentTime - lastUpdateTime;
      float deltaSeconds = (float)deltaTime * 0.2F;
      if (!isReversing) {
         rotationSpeed += 0.02F * deltaSeconds;
         if (rotationSpeed > 3.0F) {
            rotationSpeed = 3.0F;
            isReversing = true;
         }
      } else {
         rotationSpeed -= 0.02F * deltaSeconds;
         if (rotationSpeed < -3.0F) {
            rotationSpeed = -3.0F;
            isReversing = false;
         }
      }

      rotationAngle += rotationSpeed * deltaSeconds;
      rotationAngle = (rotationAngle + 360.0F) % 360.0F;
      lastUpdateTime = currentTime;
   }

   public void setMode(String mode) {
      this.mode = mode;
   }
}
