package crol.client.ui.commands.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import crol.client.CrolClient;
import crol.client.managers.FontManager;
import crol.client.ui.commands.Command;
import crol.client.ui.commands.CommandInfo;
import crol.client.util.IUtil;
import crol.client.util.player.ChatUtil;
import crol.client.util.render.builders.Builder;
import crol.client.util.render.msdf.MsdfFont;
import crol.client.util.render.renderers.impl.BuiltText;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class GpsCommand extends Command implements IUtil {
   public static boolean isEnabled;
   public static Vector3d vector3d;

   public GpsCommand() {
      super(new CommandInfo(".gps off | .gps 10 10", "gps"));
   }

   @Compile
   public void execute(String[] args) {
      if (args.length == 1) {
         if (args[0].equalsIgnoreCase("off")) {
            isEnabled = false;
            vector3d = null;
            ChatUtil.addMessage("gps disabled!");
         } else {
            String[] w = new String[]{".gps 10 10 | .gps off"};
            ChatUtil.addMessage(w[0]);
         }
      } else {
         if (args.length == 2) {
            int x = Integer.parseInt(args[0].replaceAll(" ", ""));
            int y = Integer.parseInt(args[1].replaceAll(" ", ""));
            isEnabled = true;
            vector3d = new Vector3d((double)x, (double)0.0F, (double)y);
            ChatUtil.addMessage("gps enabled!");
         }

      }
   }

   public static void drawArrow(DrawContext context) {
      Color clientColor = CrolClient.INSTANCE.getThemeManager().getTheme().color();
      Camera camera = mc.gameRenderer.getCamera();
      Vec3d camPos = camera.getPos();
      double x = vector3d.x - camPos.x;
      double z = vector3d.z - camPos.z;
      float yawRad = (float)Math.toRadians((double)mc.player.getYaw());
      double cos = Math.cos((double)yawRad);
      double sin = Math.sin((double)yawRad);
      double rotY = -(z * cos - x * sin);
      double rotX = -(x * cos + z * sin);
      double dst = Math.sqrt(Math.pow(vector3d.x - mc.player.getX(), (double)2.0F) + Math.pow(vector3d.z - mc.player.getZ(), (double)2.0F));
      float angle = (float)(Math.atan2(rotY, rotX) * (double)180.0F / Math.PI);
      Window window = mc.getWindow();
      float cx = (float)window.getScaledWidth() / 2.0F;
      float cy = (float)window.getScaledHeight() / 2.0F;
      float radius = 75.0F;
      double x2 = (double)cx + (double)radius * Math.cos(Math.toRadians((double)angle));
      double y2 = (double)cy + (double)radius * Math.sin(Math.toRadians((double)angle));
      MatrixStack matrices = context.getMatrices();
      matrices.push();
      matrices.translate(x2, y2, (double)0.0F);
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle));
      drawArrowHead(matrices, 7.0F, clientColor);
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-angle));
      Matrix4f matrix4f = matrices.peek().getPositionMatrix();
      BuiltText themeText = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text((int)dst + "m").color(Color.white).size(7.5F).thickness(0.05F).build();
      themeText.render(matrix4f, -8.0F, 15.0F);
      matrices.pop();
   }

   private static void drawArrowHead(MatrixStack matrices, float size, Color color) {
      matrices.push();
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F));
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      int r = color.getRed();
      int g = color.getGreen();
      int b = color.getBlue();
      int a = color.getAlpha();
      float tipX = -size * 2.0F;
      float wingY = size * 1.1F;
      float backX = size * 0.8F;
      float notchX = size * 0.2F;
      float gs = 1.12F;
      BufferBuilder glow = Tessellator.getInstance().begin(DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
      glow.vertex(matrix, tipX * gs, 0.0F, 0.0F).color(r, g, b, 25);
      glow.vertex(matrix, backX * gs, wingY * gs, 0.0F).color(r, g, b, 0);
      glow.vertex(matrix, notchX * gs, 0.0F, 0.0F).color(r, g, b, 12);
      glow.vertex(matrix, tipX * gs, 0.0F, 0.0F).color(r, g, b, 25);
      glow.vertex(matrix, notchX * gs, 0.0F, 0.0F).color(r, g, b, 12);
      glow.vertex(matrix, backX * gs, -wingY * gs, 0.0F).color(r, g, b, 0);
      BufferRenderer.drawWithGlobalProgram(glow.end());
      BufferBuilder fill = Tessellator.getInstance().begin(DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
      fill.vertex(matrix, tipX, 0.0F, 0.0F).color(r, g, b, a);
      fill.vertex(matrix, backX, wingY, 0.0F).color(r / 5, g / 5, b / 5, 170);
      fill.vertex(matrix, notchX, 0.0F, 0.0F).color(r * 3 / 5, g * 3 / 5, b * 3 / 5, 200);
      fill.vertex(matrix, tipX, 0.0F, 0.0F).color(r, g, b, a);
      fill.vertex(matrix, notchX, 0.0F, 0.0F).color(r * 3 / 5, g * 3 / 5, b * 3 / 5, 200);
      fill.vertex(matrix, backX, -wingY, 0.0F).color(r / 5, g / 5, b / 5, 170);
      BufferRenderer.drawWithGlobalProgram(fill.end());
      float spark = size * 0.3F;
      BufferBuilder sparkBuf = Tessellator.getInstance().begin(DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
      sparkBuf.vertex(matrix, tipX, 0.0F, 0.0F).color(255, 255, 255, 90);
      sparkBuf.vertex(matrix, tipX + spark, spark, 0.0F).color(255, 255, 255, 0);
      sparkBuf.vertex(matrix, tipX + spark, -spark, 0.0F).color(255, 255, 255, 0);
      BufferRenderer.drawWithGlobalProgram(sparkBuf.end());
      float nx = 0.0F;
      float ny = 0.0F;
      float nz = 1.0F;
      RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);
      RenderSystem.lineWidth(2.5F);
      BufferBuilder glowOutline = Tessellator.getInstance().begin(DrawMode.LINES, VertexFormats.LINES);
      glowOutline.vertex(matrix, tipX, 0.0F, 0.0F).color(r, g, b, 90).normal(nx, ny, nz);
      glowOutline.vertex(matrix, backX, -wingY, 0.0F).color(r, g, b, 60).normal(nx, ny, nz);
      glowOutline.vertex(matrix, backX, -wingY, 0.0F).color(r, g, b, 60).normal(nx, ny, nz);
      glowOutline.vertex(matrix, notchX, 0.0F, 0.0F).color(r, g, b, 75).normal(nx, ny, nz);
      glowOutline.vertex(matrix, notchX, 0.0F, 0.0F).color(r, g, b, 75).normal(nx, ny, nz);
      glowOutline.vertex(matrix, backX, wingY, 0.0F).color(r, g, b, 60).normal(nx, ny, nz);
      glowOutline.vertex(matrix, backX, wingY, 0.0F).color(r, g, b, 60).normal(nx, ny, nz);
      glowOutline.vertex(matrix, tipX, 0.0F, 0.0F).color(r, g, b, 90).normal(nx, ny, nz);
      BufferRenderer.drawWithGlobalProgram(glowOutline.end());
      RenderSystem.lineWidth(1.0F);
      BufferBuilder outline = Tessellator.getInstance().begin(DrawMode.LINES, VertexFormats.LINES);
      outline.vertex(matrix, tipX, 0.0F, 0.0F).color(255, 255, 255, 240).normal(nx, ny, nz);
      outline.vertex(matrix, backX, -wingY, 0.0F).color(r, g, b, 200).normal(nx, ny, nz);
      outline.vertex(matrix, backX, -wingY, 0.0F).color(r, g, b, 200).normal(nx, ny, nz);
      outline.vertex(matrix, notchX, 0.0F, 0.0F).color(r, g, b, 210).normal(nx, ny, nz);
      outline.vertex(matrix, notchX, 0.0F, 0.0F).color(r, g, b, 210).normal(nx, ny, nz);
      outline.vertex(matrix, backX, wingY, 0.0F).color(r, g, b, 200).normal(nx, ny, nz);
      outline.vertex(matrix, backX, wingY, 0.0F).color(r, g, b, 200).normal(nx, ny, nz);
      outline.vertex(matrix, tipX, 0.0F, 0.0F).color(255, 255, 255, 240).normal(nx, ny, nz);
      BufferRenderer.drawWithGlobalProgram(outline.end());
      RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
      float hw = size * 0.03F;
      BufferBuilder highlight = Tessellator.getInstance().begin(DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
      highlight.vertex(matrix, tipX, -hw, 0.0F).color(255, 255, 255, 140);
      highlight.vertex(matrix, tipX, hw, 0.0F).color(255, 255, 255, 140);
      highlight.vertex(matrix, notchX, 0.0F, 0.0F).color(255, 255, 255, 0);
      BufferRenderer.drawWithGlobalProgram(highlight.end());
      RenderSystem.disableBlend();
      matrices.pop();
   }
}
