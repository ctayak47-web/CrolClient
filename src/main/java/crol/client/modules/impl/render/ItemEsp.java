package crol.client.modules.impl.render;

import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import crol.client.CrolClient;
import crol.client.event.classes.Render2DEvent;
import crol.client.event.classes.Render3DEvent;
import crol.client.event.interfaces.IRenderable2D;
import crol.client.event.interfaces.IRenderable3D;
import crol.client.managers.FontManager;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.BooleanSetting;
import crol.client.modules.settings.impl.MultiBoxSetting;
import crol.client.util.IUtil;
import crol.client.util.render.ProjectionUtil;
import crol.client.util.render.RenderUtil3D;
import crol.client.util.render.builders.Builder;
import crol.client.util.render.builders.states.QuadColorState;
import crol.client.util.render.builders.states.QuadRadiusState;
import crol.client.util.render.builders.states.SizeState;
import crol.client.util.render.msdf.MsdfFont;
import crol.client.util.render.renderers.impl.BuiltRectangle;
import crol.client.util.render.renderers.impl.BuiltText;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class ItemEsp extends Module implements IRenderable3D, IRenderable2D, IUtil {
   private final MultiBoxSetting elements = ((MultiBoxSetting)(new MultiBoxSetting()).name("Elements")).booleanSettings(((BooleanSetting)(new BooleanSetting()).name("NameTag")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Box")).value(false));
   private static final double EXPAND = 0.02;
   private final List<ItemData> visibleItems = new ArrayList();
   private final HashMap<Integer, Float> smoothWidths = new HashMap();

   public ItemEsp() {
      super(new ModuleInfo("ItemEsp", Category.RENDER, "Показывает предметы на земле"));
      this.addSetting(new Setting[]{this.elements});
   }

   public void onRender3D(Render3DEvent event) {
      if (mc.player != null && mc.world != null) {
         boolean showBox = this.elements.getValueByName("Box");
         boolean showNametag = this.elements.getValueByName("NameTag");
         if (showBox || showNametag) {
            float tickDelta = event.getRenderTickCounter().getTickDelta(false);
            RenderUtil3D r3d = RenderUtil3D.getInstance();
            Matrix4f mat = r3d.lastWorldSpaceMatrix.getPositionMatrix();
            this.visibleItems.clear();
            if (!this.smoothWidths.isEmpty()) {
               Set<Integer> alive = new HashSet();

               for(Entity e : mc.world.getEntities()) {
                  if (e instanceof ItemEntity) {
                     alive.add(e.getId());
                  }
               }

               this.smoothWidths.keySet().retainAll(alive);
            }

            int themeColor = CrolClient.INSTANCE.getThemeManager().getTheme().color().getRGB();
            float tr = (float)(themeColor >> 16 & 255) / 255.0F;
            float tg = (float)(themeColor >> 8 & 255) / 255.0F;
            float tb = (float)(themeColor & 255) / 255.0F;

            for(Entity entity : mc.world.getEntities()) {
               if (entity instanceof ItemEntity) {
                  ItemEntity item = (ItemEntity)entity;
                  ItemStack stack = item.getStack();
                  if (!stack.isEmpty()) {
                     double ix = MathHelper.lerp((double)tickDelta, item.prevX, item.getX());
                     double iy = MathHelper.lerp((double)tickDelta, item.prevY, item.getY());
                     double iz = MathHelper.lerp((double)tickDelta, item.prevZ, item.getZ());
                     Box box = item.getBoundingBox().offset(ix - item.getX(), iy - item.getY(), iz - item.getZ()).expand(0.02);
                     if (r3d.canSee(box)) {
                        ItemData d = new ItemData();
                        d.entityId = item.getId();
                        d.x0 = (float)box.minX;
                        d.y0 = (float)box.minY;
                        d.z0 = (float)box.minZ;
                        d.x1 = (float)box.maxX;
                        d.y1 = (float)box.maxY;
                        d.z1 = (float)box.maxZ;
                        d.r = tr;
                        d.g = tg;
                        d.elementCodec = tb;
                        d.worldX = ix;
                        d.worldY = iy + (double)item.getHeight() + (double)0.25F;
                        d.worldZ = iz;
                        String var10001 = stack.getName().getString();
                        d.label = var10001 + " " + stack.getCount() + "x";
                        d.textWidth = ((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).getWidth(d.label, 6.0F) + 8.0F;
                        this.visibleItems.add(d);
                     }
                  }
               }
            }

            if (!this.visibleItems.isEmpty()) {
               if (showBox) {
                  GL11.glEnable(2881);
                  RenderSystem.enableBlend();
                  RenderSystem.disableCull();
                  RenderSystem.disableDepthTest();
                  RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_CONSTANT_ALPHA);
                  RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
                  BufferBuilder fill = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);

                  for(ItemData d : this.visibleItems) {
                     appendBoxFill(fill, mat, d.x0, d.y0, d.z0, d.x1, d.y1, d.z1, d.r, d.g, d.elementCodec, 0.1F);
                  }

                  BufferRenderer.drawWithGlobalProgram(fill.end());
                  RenderSystem.lineWidth(1.0F);
                  RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);
                  BufferBuilder lines = Tessellator.getInstance().begin(DrawMode.LINES, VertexFormats.LINES);

                  for(ItemData d : this.visibleItems) {
                     appendBoxLines(lines, mat, d.x0, d.y0, d.z0, d.x1, d.y1, d.z1, d.r, d.g, d.elementCodec, 1.0F);
                  }

                  BufferRenderer.drawWithGlobalProgram(lines.end());
                  RenderSystem.enableDepthTest();
                  RenderSystem.enableCull();
                  RenderSystem.disableBlend();
                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                  GL11.glDisable(2881);
               }

            }
         }
      }
   }

   public void onRender2D(Render2DEvent event) {
      if (mc.player != null && mc.world != null) {
         if (this.elements.getValueByName("NameTag")) {
            if (!this.visibleItems.isEmpty()) {
               Matrix4f matrix = event.getDrawContext().getMatrices().peek().getPositionMatrix();
               Color themeColor = new Color(CrolClient.INSTANCE.getThemeManager().getTheme().color().getRGB());

               for(ItemData d : this.visibleItems) {
                  Vector2f screen = ProjectionUtil.project(d.worldX, d.worldY, d.worldZ);
                  if (screen.getX() != Float.MAX_VALUE) {
                     float smoothW = (Float)this.smoothWidths.getOrDefault(d.entityId, d.textWidth);
                     smoothW += (d.textWidth - smoothW) * 0.15F;
                     this.smoothWidths.put(d.entityId, smoothW);
                     float boxH = 11.0F;
                     float bx = screen.getX() - smoothW / 2.0F;
                     float by = screen.getY() - 5.0F;
                     BuiltRectangle bg = (BuiltRectangle)Builder.rectangle().size(new SizeState(smoothW, boxH)).color(new QuadColorState(new Color(-1879048192, true))).radius(new QuadRadiusState(2.0F)).smoothness(1.5F).build();
                     bg.render(matrix, bx, by);
                     BuiltRectangle accent = (BuiltRectangle)Builder.rectangle().size(new SizeState(2.0F, boxH)).color(new QuadColorState(themeColor)).radius(new QuadRadiusState(1.0F)).smoothness(1.0F).build();
                     accent.render(matrix, bx, by);
                     BuiltText text = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(d.label).color(Color.WHITE).size(6.0F).thickness(0.05F).build();
                     text.render(matrix, bx + 4.0F, by + 2.0F);
                  }
               }

            }
         }
      }
   }

   private static void appendBoxLines(BufferBuilder buf, Matrix4f mat, float x0, float y0, float z0, float x1, float y1, float z1, float r, float g, float bl, float a) {
      line(buf, mat, x0, y0, z0, x1, y0, z0, r, g, bl, a);
      line(buf, mat, x1, y0, z0, x1, y0, z1, r, g, bl, a);
      line(buf, mat, x1, y0, z1, x0, y0, z1, r, g, bl, a);
      line(buf, mat, x0, y0, z1, x0, y0, z0, r, g, bl, a);
      line(buf, mat, x0, y1, z0, x1, y1, z0, r, g, bl, a);
      line(buf, mat, x1, y1, z0, x1, y1, z1, r, g, bl, a);
      line(buf, mat, x1, y1, z1, x0, y1, z1, r, g, bl, a);
      line(buf, mat, x0, y1, z1, x0, y1, z0, r, g, bl, a);
      line(buf, mat, x0, y0, z0, x0, y1, z0, r, g, bl, a);
      line(buf, mat, x1, y0, z0, x1, y1, z0, r, g, bl, a);
      line(buf, mat, x1, y0, z1, x1, y1, z1, r, g, bl, a);
      line(buf, mat, x0, y0, z1, x0, y1, z1, r, g, bl, a);
   }

   private static void line(BufferBuilder buf, Matrix4f mat, float x0, float y0, float z0, float x1, float y1, float z1, float r, float g, float b, float a) {
      float dx = x1 - x0;
      float dy = y1 - y0;
      float dz = z1 - z0;
      float len = MathHelper.sqrt(dx * dx + dy * dy + dz * dz);
      if (len != 0.0F) {
         buf.vertex(mat, x0, y0, z0).color(r, g, b, a).normal(dx / len, dy / len, dz / len);
         buf.vertex(mat, x1, y1, z1).color(r, g, b, a).normal(dx / len, dy / len, dz / len);
      }
   }

   private static void appendBoxFill(BufferBuilder buf, Matrix4f mat, float x0, float y0, float z0, float x1, float y1, float z1, float r, float g, float bl, float a) {
      quad(buf, mat, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, r, g, bl, a);
      quad(buf, mat, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, r, g, bl, a);
      quad(buf, mat, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0, r, g, bl, a);
      quad(buf, mat, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, r, g, bl, a);
      quad(buf, mat, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, r, g, bl, a);
      quad(buf, mat, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1, r, g, bl, a);
   }

   private static void quad(BufferBuilder buf, Matrix4f mat, float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float r, float g, float b, float a) {
      buf.vertex(mat, x0, y0, z0).color(r, g, b, a);
      buf.vertex(mat, x1, y1, z1).color(r, g, b, a);
      buf.vertex(mat, x2, y2, z2).color(r, g, b, a);
      buf.vertex(mat, x3, y3, z3).color(r, g, b, a);
   }

   @Environment(EnvType.CLIENT)
   private static class ItemData {
      int entityId;
      float x0;
      float y0;
      float z0;
      float x1;
      float y1;
      float z1;
      float r;
      float g;
      float elementCodec;
      double worldX;
      double worldY;
      double worldZ;
      String label;
      float textWidth;
   }
}
