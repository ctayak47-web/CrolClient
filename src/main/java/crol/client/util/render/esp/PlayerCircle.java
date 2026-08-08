package crol.client.util.render.esp;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import crol.client.CrolClient;
import crol.client.event.classes.Render3DEvent;
import crol.client.util.IUtil;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class PlayerCircle implements IUtil {
   private static final int segments = 360;

   public void render(Render3DEvent render3DEvent, PlayerEntity target) {
      MatrixStack matrices = render3DEvent.getMatrixStack();
      Camera camera = mc.gameRenderer.getCamera();
      Vec3d targetPos = target.getLerpedPos(MathUtil.getTickDelta()).subtract(camera.getPos());
      double tPosX = targetPos.x;
      double tPosY = targetPos.y;
      double tPosZ = targetPos.z;
      float height = target.getHeight() + 0.2F;
      double progress = (double)0.0F;
      progress = progress < (double)0.5F ? (double)2.0F * progress * progress : (double)1.0F - Math.pow((double)-2.0F * progress + (double)2.0F, (double)2.0F) / (double)2.0F;
      double eased = (double)2.0F;
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
      int coreColor = (new Color(themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue(), 1)).getRGB();
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
      double width = (double)target.getWidth() * 0.9;
      Matrix4f matrix = matrices.peek().getPositionMatrix();

      for(int i = 0; i <= 360; ++i) {
         double angle = Math.toRadians((double)i);
         float x = (float)(Math.cos(angle) * width);
         float z = (float)(Math.sin(angle) * width);
         buffer.vertex(matrix, x, (float)((double)height * progress + eased), z).color(coreColor);
         buffer.vertex(matrix, x, (float)((double)height * progress), z).color(bloomColor);
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
