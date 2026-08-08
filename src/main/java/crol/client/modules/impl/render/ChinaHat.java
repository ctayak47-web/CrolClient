package crol.client.modules.impl.render;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import crol.client.CrolClient;
import crol.client.event.classes.WorldRenderEvent;
import crol.client.event.interfaces.IWorldRender;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.util.IUtil;
import crol.client.util.color.ColorUtil;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class ChinaHat extends Module implements IWorldRender, IUtil {
   public ChinaHat() {
      super(new ModuleInfo("ChinaHat", Category.RENDER, "Добавляет игроку шляпу на голове"));
   }

   public void onWorldRender(WorldRenderEvent event) {
      if (mc.player != null && !mc.options.getPerspective().isFirstPerson()) {
         MatrixStack stack = event.getMatrixStack();
         float partialTicks = event.getRenderTickCounter().getTickDelta(false);
         Vec3d playerPos = mc.player.getLerpedPos(partialTicks);
         float yOffset = (float)(playerPos.y + (double)this.getYOffset(mc.player)) + 1.7F;
         stack.push();
         RenderSystem.enableBlend();
         RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA, SrcFactor.ONE, DstFactor.ZERO);
         RenderSystem.enableDepthTest();
         RenderSystem.depthFunc(515);
         RenderSystem.depthMask(false);
         RenderSystem.disableCull();
         stack.translate(playerPos.x, (double)yOffset, playerPos.z);
         stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(System.currentTimeMillis() % 36000L) / 100.0F));
         float radiusValue = 0.6F;
         float heightValue = 0.3F;
         float steps = 64.0F;
         double angleStep = (Math.PI * 2D) / (double)steps;
         Color themeColor = CrolClient.INSTANCE.getThemeManager().getTheme().color();
         int tipColor = ColorUtil.setAlpha(0.95, themeColor).getRGB();
         int baseColor = ColorUtil.setAlpha((double)0.5F, themeColor).getRGB();
         int outlineColor = themeColor.getRGB();
         RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
         BufferBuilder cone = Tessellator.getInstance().begin(DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
         cone.vertex(stack.peek().getPositionMatrix(), 0.0F, heightValue, 0.0F).color(tipColor);

         for(int i = 0; i <= (int)steps; ++i) {
            float x = (float)(Math.cos((double)i * angleStep) * (double)radiusValue);
            float z = (float)(Math.sin((double)i * angleStep) * (double)radiusValue);
            cone.vertex(stack.peek().getPositionMatrix(), x, 0.0F, z).color(baseColor);
         }

         BufferRenderer.drawWithGlobalProgram(cone.end());
         RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);
         RenderSystem.lineWidth(2.0F);
         GL11.glEnable(2848);
         GL11.glHint(3154, 4354);
         BufferBuilder ring = Tessellator.getInstance().begin(DrawMode.LINES, VertexFormats.LINES);

         for(int i = 0; i < (int)steps; ++i) {
            float x1 = (float)(Math.cos((double)i * angleStep) * (double)radiusValue);
            float z1 = (float)(Math.sin((double)i * angleStep) * (double)radiusValue);
            float x2 = (float)(Math.cos((double)(i + 1) * angleStep) * (double)radiusValue);
            float z2 = (float)(Math.sin((double)(i + 1) * angleStep) * (double)radiusValue);
            Vector3f normal = (new Vector3f(x2 - x1, 0.0F, z2 - z1)).normalize();
            ring.vertex(stack.peek(), x1, 0.0F, z1).color(outlineColor).normal(stack.peek(), normal.x, normal.y, normal.z);
            ring.vertex(stack.peek(), x2, 0.0F, z2).color(outlineColor).normal(stack.peek(), normal.x, normal.y, normal.z);
         }

         BufferRenderer.drawWithGlobalProgram(ring.end());
         GL11.glDisable(2848);
         RenderSystem.lineWidth(1.0F);
         RenderSystem.depthMask(true);
         RenderSystem.enableCull();
         RenderSystem.defaultBlendFunc();
         RenderSystem.depthFunc(513);
         stack.pop();
      }
   }

   private float getYOffset(Entity entity) {
      float offset = 0.15F;
      if (entity instanceof LivingEntity livingEntity) {
         if (livingEntity.getEquippedStack(EquipmentSlot.HEAD).getItem() instanceof ArmorItem) {
            offset -= 0.065F;
         }
      }

      return offset;
   }
}
