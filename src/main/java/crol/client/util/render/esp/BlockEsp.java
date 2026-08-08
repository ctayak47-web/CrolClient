package crol.client.util.render.esp;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import crol.client.CrolClient;
import crol.client.util.IUtil;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class BlockEsp implements IUtil {
   public static void render(MatrixStack matrices, BlockPos blockPos, boolean fill) {
      Camera camera = mc.gameRenderer.getCamera();
      double tPosX = (double)blockPos.getX() - camera.getPos().x;
      double tPosY = (double)blockPos.getY() - camera.getPos().y;
      double tPosZ = (double)blockPos.getZ() - camera.getPos().z;
      matrices.push();
      matrices.translate(tPosX, tPosY, tPosZ);
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE);
      RenderSystem.disableCull();

      assert mc.player != null;

      RenderSystem.disableDepthTest();
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      Color themeColor = CrolClient.INSTANCE.getThemeManager().getTheme().color();
      int bloomColor = themeColor.getRGB();
      int fillColor = (new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 50)).getRGB();
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      VoxelShape shape = VoxelShapes.fullCube();
      Vec3d camPos = camera.getPos();
      if (fill) {
         buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);

         for(Box box : shape.getBoundingBoxes()) {
            double minX = box.minX + (double)blockPos.getX() - camPos.x;
            double minY = box.minY + (double)blockPos.getY() - camPos.y;
            double minZ = box.minZ + (double)blockPos.getZ() - camPos.z;
            double maxX = box.maxX + (double)blockPos.getX() - camPos.x + (double)1.0F;
            double maxY = box.maxY + (double)blockPos.getY() - camPos.y + (double)1.0F;
            double maxZ = box.maxZ + (double)blockPos.getZ() - camPos.z + (double)1.0F;
            buffer.vertex(matrix, (float)minX, (float)minY, (float)minZ).color(fillColor);
            buffer.vertex(matrix, (float)maxX, (float)minY, (float)minZ).color(fillColor);
            buffer.vertex(matrix, (float)maxX, (float)minY, (float)maxZ).color(fillColor);
            buffer.vertex(matrix, (float)minX, (float)minY, (float)maxZ).color(fillColor);
            buffer.vertex(matrix, (float)minX, (float)maxY, (float)minZ).color(fillColor);
            buffer.vertex(matrix, (float)maxX, (float)maxY, (float)minZ).color(fillColor);
            buffer.vertex(matrix, (float)maxX, (float)maxY, (float)maxZ).color(fillColor);
            buffer.vertex(matrix, (float)minX, (float)maxY, (float)maxZ).color(fillColor);
            buffer.vertex(matrix, (float)minX, (float)minY, (float)minZ).color(fillColor);
            buffer.vertex(matrix, (float)maxX, (float)minY, (float)minZ).color(fillColor);
            buffer.vertex(matrix, (float)maxX, (float)maxY, (float)minZ).color(fillColor);
            buffer.vertex(matrix, (float)minX, (float)maxY, (float)minZ).color(fillColor);
            buffer.vertex(matrix, (float)minX, (float)minY, (float)maxZ).color(fillColor);
            buffer.vertex(matrix, (float)maxX, (float)minY, (float)maxZ).color(fillColor);
            buffer.vertex(matrix, (float)maxX, (float)maxY, (float)maxZ).color(fillColor);
            buffer.vertex(matrix, (float)minX, (float)maxY, (float)maxZ).color(fillColor);
            buffer.vertex(matrix, (float)minX, (float)minY, (float)minZ).color(fillColor);
            buffer.vertex(matrix, (float)minX, (float)minY, (float)maxZ).color(fillColor);
            buffer.vertex(matrix, (float)minX, (float)maxY, (float)maxZ).color(fillColor);
            buffer.vertex(matrix, (float)minX, (float)maxY, (float)minZ).color(fillColor);
            buffer.vertex(matrix, (float)maxX, (float)minY, (float)minZ).color(fillColor);
            buffer.vertex(matrix, (float)maxX, (float)minY, (float)maxZ).color(fillColor);
            buffer.vertex(matrix, (float)maxX, (float)maxY, (float)maxZ).color(fillColor);
            buffer.vertex(matrix, (float)maxX, (float)maxY, (float)minZ).color(fillColor);
         }

         BufferRenderer.drawWithGlobalProgram(buffer.end());
      }

      buffer = Tessellator.getInstance().begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

      for(Box box : shape.getBoundingBoxes()) {
         double minX = box.minX + (double)blockPos.getX() - camPos.x;
         double minY = box.minY + (double)blockPos.getY() - camPos.y;
         double minZ = box.minZ + (double)blockPos.getZ() - camPos.z;
         double maxX = box.maxX + (double)blockPos.getX() - camPos.x + (double)1.0F;
         double maxY = box.maxY + (double)blockPos.getY() - camPos.y + (double)1.0F;
         double maxZ = box.maxZ + (double)blockPos.getZ() - camPos.z + (double)1.0F;
         buffer.vertex(matrix, (float)minX, (float)minY, (float)minZ).color(bloomColor);
         buffer.vertex(matrix, (float)maxX, (float)minY, (float)minZ).color(bloomColor);
         buffer.vertex(matrix, (float)maxX, (float)minY, (float)minZ).color(bloomColor);
         buffer.vertex(matrix, (float)maxX, (float)minY, (float)maxZ).color(bloomColor);
         buffer.vertex(matrix, (float)maxX, (float)minY, (float)maxZ).color(bloomColor);
         buffer.vertex(matrix, (float)minX, (float)minY, (float)maxZ).color(bloomColor);
         buffer.vertex(matrix, (float)minX, (float)minY, (float)maxZ).color(bloomColor);
         buffer.vertex(matrix, (float)minX, (float)minY, (float)minZ).color(bloomColor);
         buffer.vertex(matrix, (float)minX, (float)maxY, (float)minZ).color(bloomColor);
         buffer.vertex(matrix, (float)maxX, (float)maxY, (float)minZ).color(bloomColor);
         buffer.vertex(matrix, (float)maxX, (float)maxY, (float)minZ).color(bloomColor);
         buffer.vertex(matrix, (float)maxX, (float)maxY, (float)maxZ).color(bloomColor);
         buffer.vertex(matrix, (float)maxX, (float)maxY, (float)maxZ).color(bloomColor);
         buffer.vertex(matrix, (float)minX, (float)maxY, (float)maxZ).color(bloomColor);
         buffer.vertex(matrix, (float)minX, (float)maxY, (float)maxZ).color(bloomColor);
         buffer.vertex(matrix, (float)minX, (float)maxY, (float)minZ).color(bloomColor);
         buffer.vertex(matrix, (float)minX, (float)minY, (float)minZ).color(bloomColor);
         buffer.vertex(matrix, (float)minX, (float)maxY, (float)minZ).color(bloomColor);
         buffer.vertex(matrix, (float)maxX, (float)minY, (float)minZ).color(bloomColor);
         buffer.vertex(matrix, (float)maxX, (float)maxY, (float)minZ).color(bloomColor);
         buffer.vertex(matrix, (float)minX, (float)minY, (float)maxZ).color(bloomColor);
         buffer.vertex(matrix, (float)minX, (float)maxY, (float)maxZ).color(bloomColor);
         buffer.vertex(matrix, (float)maxX, (float)minY, (float)maxZ).color(bloomColor);
         buffer.vertex(matrix, (float)maxX, (float)maxY, (float)maxZ).color(bloomColor);
      }

      BufferRenderer.drawWithGlobalProgram(buffer.end());
      GL11.glDisable(2848);
      RenderSystem.enableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.disableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      matrices.pop();
   }
}
