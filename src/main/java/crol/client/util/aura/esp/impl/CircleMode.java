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
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class CircleMode extends EspMode implements IUtil {
   private static final int segments = 360;

   public void render(Entity target, MatrixStack matrices) {
      if (target != null && target != mc.player && !(target instanceof ArmorStandEntity)) {
         Camera camera = mc.gameRenderer.getCamera();
         Vec3d targetPos = target.getLerpedPos(MathUtil.getTickDelta()).subtract(camera.getPos());
         double tPosX = targetPos.x;
         double tPosY = targetPos.y;
         double tPosZ = targetPos.z;
         float height = target.getHeight() + 0.1F;
         double duration = (double)2500.0F;
         double elapsed = (double)System.currentTimeMillis() % duration;
         boolean side = elapsed > duration / (double)2.0F;
         double progress = elapsed / (duration / (double)2.0F);
         if (side) {
            --progress;
         } else {
            progress = (double)1.0F - progress;
         }

         progress = progress < (double)0.5F ? (double)2.0F * progress * progress : (double)1.0F - Math.pow((double)-2.0F * progress + (double)2.0F, (double)2.0F) / (double)2.0F;
         double eased = (double)(height / 1.5F) * (progress > (double)0.5F ? (double)1.0F - progress : progress) * (double)(side ? -1 : 1);
         matrices.push();
         matrices.translate(tPosX, tPosY, tPosZ);
         RenderSystem.enableBlend();
         RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
         RenderSystem.disableCull();

         assert mc.player != null;

         if (mc.player.canSee(target)) {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
         } else {
            RenderSystem.disableDepthTest();
         }

         GL11.glEnable(2848);
         GL11.glHint(3154, 4354);
         Color themeColor = CrolClient.INSTANCE.getThemeManager().getTheme().color();
         int bloomColor = themeColor.getRGB();
         int coreColor = (new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 1)).getRGB();
         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
         Matrix4f matrix = matrices.peek().getPositionMatrix();

         for(int i = 0; i <= 360; ++i) {
            double angle = Math.toRadians((double)i);
            float x = (float)(Math.cos(angle) * (double)target.getWidth());
            float z = (float)(Math.sin(angle) * (double)target.getWidth());
            buffer.vertex(matrix, x, (float)((double)height * progress + eased), z).color(coreColor);
            buffer.vertex(matrix, x, (float)((double)height * progress), z).color(bloomColor);
         }

         BufferRenderer.drawWithGlobalProgram(buffer.end());
         buffer = Tessellator.getInstance().begin(DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);

         for(int i = 0; i <= 360; ++i) {
            double angle = Math.toRadians((double)i);
            float x = (float)(Math.cos(angle) * (double)target.getWidth());
            float z = (float)(Math.sin(angle) * (double)target.getWidth());
            buffer.vertex(matrix, x, (float)((double)height * progress), z).color(bloomColor);
         }

         BufferRenderer.drawWithGlobalProgram(buffer.end());
         GL11.glDisable(2848);
         RenderSystem.enableCull();
         if (mc.player.canSee(target)) {
            RenderSystem.depthMask(true);
         }

         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         matrices.pop();
      }
   }
}
