package crol.client.modules.impl.render;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import crol.client.CrolClient;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.BooleanSetting;
import crol.client.util.IUtil;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
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
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class BlockOutline extends Module implements IUtil {
   public final BooleanSetting fill = ((BooleanSetting)(new BooleanSetting()).name("Fill")).value(false);

   public BlockOutline() {
      super(new ModuleInfo("BlockOutline", Category.RENDER, "Изменяет очертание выбранного блока"));
      this.addSetting(new Setting[]{this.fill});
   }

   public static void render(MatrixStack matrices, BlockPos blockPos, boolean fill) {
      Camera camera = mc.gameRenderer.getCamera();
      Vec3d cameraPos = camera.getPos();
      BlockState state = mc.world.getBlockState(blockPos);
      VoxelShape shape = state.getOutlineShape(mc.world, blockPos);
      if (shape.isEmpty()) {
         shape = VoxelShapes.fullCube();
      }

      matrices.push();
      matrices.translate((double)blockPos.getX() - cameraPos.x, (double)blockPos.getY() - cameraPos.y, (double)blockPos.getZ() - cameraPos.z);
      Color themeColor = CrolClient.INSTANCE.getThemeManager().getTheme().color();
      int lineColor = themeColor.getRGB();
      int fillColor = (new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 75)).getRGB();
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA);
      RenderSystem.disableCull();
      RenderSystem.enableDepthTest();
      RenderSystem.depthMask(false);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      RenderSystem.lineWidth(2.25F);

      for(Box box : shape.getBoundingBoxes()) {
         double minX = box.minX - 0.001;
         double minY = box.minY - 0.001;
         double minZ = box.minZ - 0.001;
         double maxX = box.maxX + 0.001;
         double maxY = box.maxY + 0.001;
         double maxZ = box.maxZ + 0.001;
         if (fill) {
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            MatrixStack.Entry entry = matrices.peek();
            buffer.vertex(entry, (float)minX, (float)minY, (float)minZ).color(fillColor);
            buffer.vertex(entry, (float)maxX, (float)minY, (float)minZ).color(fillColor);
            buffer.vertex(entry, (float)maxX, (float)minY, (float)maxZ).color(fillColor);
            buffer.vertex(entry, (float)minX, (float)minY, (float)maxZ).color(fillColor);
            buffer.vertex(entry, (float)minX, (float)maxY, (float)minZ).color(fillColor);
            buffer.vertex(entry, (float)maxX, (float)maxY, (float)minZ).color(fillColor);
            buffer.vertex(entry, (float)maxX, (float)maxY, (float)maxZ).color(fillColor);
            buffer.vertex(entry, (float)minX, (float)maxY, (float)maxZ).color(fillColor);
            buffer.vertex(entry, (float)minX, (float)minY, (float)minZ).color(fillColor);
            buffer.vertex(entry, (float)maxX, (float)minY, (float)minZ).color(fillColor);
            buffer.vertex(entry, (float)maxX, (float)maxY, (float)minZ).color(fillColor);
            buffer.vertex(entry, (float)minX, (float)maxY, (float)minZ).color(fillColor);
            buffer.vertex(entry, (float)minX, (float)minY, (float)maxZ).color(fillColor);
            buffer.vertex(entry, (float)maxX, (float)minY, (float)maxZ).color(fillColor);
            buffer.vertex(entry, (float)maxX, (float)maxY, (float)maxZ).color(fillColor);
            buffer.vertex(entry, (float)minX, (float)maxY, (float)maxZ).color(fillColor);
            buffer.vertex(entry, (float)minX, (float)minY, (float)minZ).color(fillColor);
            buffer.vertex(entry, (float)minX, (float)minY, (float)maxZ).color(fillColor);
            buffer.vertex(entry, (float)minX, (float)maxY, (float)maxZ).color(fillColor);
            buffer.vertex(entry, (float)minX, (float)maxY, (float)minZ).color(fillColor);
            buffer.vertex(entry, (float)maxX, (float)minY, (float)minZ).color(fillColor);
            buffer.vertex(entry, (float)maxX, (float)minY, (float)maxZ).color(fillColor);
            buffer.vertex(entry, (float)maxX, (float)maxY, (float)maxZ).color(fillColor);
            buffer.vertex(entry, (float)maxX, (float)maxY, (float)minZ).color(fillColor);
            BufferRenderer.drawWithGlobalProgram(buffer.end());
         }

         RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);
         BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.LINES, VertexFormats.LINES);
         MatrixStack.Entry entry = matrices.peek();
         drawLine(buffer, entry, minX, minY, minZ, maxX, minY, minZ, lineColor);
         drawLine(buffer, entry, maxX, minY, minZ, maxX, minY, maxZ, lineColor);
         drawLine(buffer, entry, maxX, minY, maxZ, minX, minY, maxZ, lineColor);
         drawLine(buffer, entry, minX, minY, maxZ, minX, minY, minZ, lineColor);
         drawLine(buffer, entry, minX, maxY, minZ, maxX, maxY, minZ, lineColor);
         drawLine(buffer, entry, maxX, maxY, minZ, maxX, maxY, maxZ, lineColor);
         drawLine(buffer, entry, maxX, maxY, maxZ, minX, maxY, maxZ, lineColor);
         drawLine(buffer, entry, minX, maxY, maxZ, minX, maxY, minZ, lineColor);
         drawLine(buffer, entry, minX, minY, minZ, minX, maxY, minZ, lineColor);
         drawLine(buffer, entry, maxX, minY, minZ, maxX, maxY, minZ, lineColor);
         drawLine(buffer, entry, maxX, minY, maxZ, maxX, maxY, maxZ, lineColor);
         drawLine(buffer, entry, minX, minY, maxZ, minX, maxY, maxZ, lineColor);
         BufferRenderer.drawWithGlobalProgram(buffer.end());
      }

      GL11.glDisable(2848);
      RenderSystem.depthMask(true);
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.lineWidth(1.0F);
      matrices.pop();
   }

   private static void drawLine(BufferBuilder buffer, MatrixStack.Entry entry, double x1, double y1, double z1, double x2, double y2, double z2, int color) {
      Vector3f start = new Vector3f((float)x1, (float)y1, (float)z1);
      Vector3f end = new Vector3f((float)x2, (float)y2, (float)z2);
      Vector3f normal = (new Vector3f(end)).sub(start).normalize();
      buffer.vertex(entry, start.x, start.y, start.z).color(color).normal(entry, normal.x, normal.y, normal.z);
      buffer.vertex(entry, end.x, end.y, end.z).color(color).normal(entry, normal.x, normal.y, normal.z);
   }
}
