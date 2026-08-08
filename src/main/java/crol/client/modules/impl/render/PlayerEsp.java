package crol.client.modules.impl.render;

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
import crol.client.modules.settings.impl.ModeSetting;
import crol.client.modules.settings.impl.MultiBoxSetting;
import crol.client.util.IUtil;
import crol.client.util.math.MathUtil;
import crol.client.util.render.AnimationUtil;
import crol.client.util.render.ProjectionUtil;
import crol.client.util.render.RenderUtil3D;
import crol.client.util.render.builders.Builder;
import crol.client.util.render.builders.states.QuadColorState;
import crol.client.util.render.builders.states.QuadRadiusState;
import crol.client.util.render.builders.states.SizeState;
import crol.client.util.render.esp.EspMatrixHolder;
import crol.client.util.render.msdf.MsdfFont;
import crol.client.util.render.renderers.impl.BuiltRectangle;
import crol.client.util.render.renderers.impl.BuiltText;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LimbAnimator;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;

@Environment(EnvType.CLIENT)
public class PlayerEsp extends Module implements IRenderable3D, IUtil, IRenderable2D {
   public final MultiBoxSetting options = ((MultiBoxSetting)(new MultiBoxSetting()).name("Options")).booleanSettings(((BooleanSetting)(new BooleanSetting()).name("Chams")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Nametag")).value(false), ((BooleanSetting)(new BooleanSetting()).name("3DBox")).value(false));
   public final ModeSetting chamsMode = ((ModeSetting)(new ModeSetting() {
      public boolean isVisible() {
         return PlayerEsp.this.options.getValueByName("Chams");
      }
   }).name("Mode")).value("New").modes("Glow", "New", "Flat", "Glass", "Skeleton");
   public final MultiBoxSetting nametagOptions = ((MultiBoxSetting)(new MultiBoxSetting() {
      public boolean isVisible() {
         return PlayerEsp.this.options.getValueByName("Nametag");
      }
   }).name("NameTag Options")).booleanSettings(((BooleanSetting)(new BooleanSetting() {
   }).name("HpBar")).value(false), ((BooleanSetting)(new BooleanSetting() {
   }).name("Box")).value(false), ((BooleanSetting)(new BooleanSetting() {
   }).name("Enchants")).value(false));

   public PlayerEsp() {
      super(new ModuleInfo("Esp", Category.RENDER, "Показывает информацию о противниках в мире"));
      this.addSetting(new Setting[]{this.options, this.chamsMode, this.nametagOptions});
   }

   public void onRender3D(Render3DEvent event) {
      if (this.options.getValueByName("3DBox")) {
         float tickDelta = event.getRenderTickCounter().getTickDelta(false);

         for(PlayerEntity player : mc.world.getPlayers()) {
            if (player != mc.player) {
               Box current = player.getBoundingBox();
               Box prev = player.getBoundingBox().offset(player.prevX - player.getX(), player.prevY - player.getY(), player.prevZ - player.getZ());
               double minX = MathHelper.lerp((double)tickDelta, prev.minX, current.minX);
               double minY = MathHelper.lerp((double)tickDelta, prev.minY, current.minY);
               double minZ = MathHelper.lerp((double)tickDelta, prev.minZ, current.minZ);
               double maxX = MathHelper.lerp((double)tickDelta, prev.maxX, current.maxX);
               double maxY = MathHelper.lerp((double)tickDelta, prev.maxY, current.maxY);
               double maxZ = MathHelper.lerp((double)tickDelta, prev.maxZ, current.maxZ);
               Box smoothBox = new Box(minX, minY, minZ, maxX, maxY, maxZ);
               RenderUtil3D.getInstance().drawBox(smoothBox, CrolClient.INSTANCE.getThemeManager().getTheme().color().getRGB(), 1.24F);
            }
         }
      }

      if (this.options.getValueByName("Chams") && this.chamsMode.is("Skeleton")) {
         float tickDelta = event.getRenderTickCounter().getTickDelta(true);

         for(PlayerEntity player : mc.world.getPlayers()) {
            if (player != mc.player) {
               this.renderSkeleton(player, tickDelta, event.getMatrixStack());
            }
         }
      }

   }

   private void renderSkeleton(PlayerEntity player, float partialTicks, MatrixStack matrices) {
      Camera camera = mc.gameRenderer.getCamera();
      Vec3d camPos = camera.getPos();
      Vec3d pos = player.getLerpedPos(partialTicks);
      double x = pos.x - camPos.x;
      double y = pos.y - camPos.y;
      double z = pos.z - camPos.z;
      float scale = player.isBaby() ? 0.5F : 1.05F;
      double headY = 1.6 * (double)scale;
      double torsoTopY = 1.2 * (double)scale;
      double torsoBottomY = 0.6 * (double)scale;
      double armOffset = 0.33;
      double handY = 0.8 * (double)scale;
      double legOffset = 0.15 * (double)scale;
      double footY = (double)0.0F;
      if (player.isSwimming()) {
         headY = (double)1.5F * (double)scale;
         torsoTopY = 1.2 * (double)scale;
         torsoBottomY = 0.6 * (double)scale;
         handY = 0.8 * (double)scale;
         footY = 0.1 * (double)scale;
         armOffset = 0.33;
         legOffset = (double)0.25F * (double)scale;
      } else if (player.isInSneakingPose()) {
         headY = (double)1.25F * (double)scale;
         torsoTopY = 0.95 * (double)scale;
         torsoBottomY = 0.45 * (double)scale;
         handY = 0.55 * (double)scale;
         footY = -0.1 * (double)scale;
      } else if (player.isGliding()) {
         headY = (double)1.5F * (double)scale;
         torsoTopY = 1.2 * (double)scale;
         torsoBottomY = 0.6 * (double)scale;
         handY = 0.8 * (double)scale;
         footY = 0.1 * (double)scale;
         armOffset = 0.33;
      }

      int lineColor = (CrolClient.INSTANCE.getFriendManager().isFriend(player.getNameForScoreboard()) ? Color.green : CrolClient.INSTANCE.getThemeManager().getTheme().color()).getRGB();
      float lineWidth = 1.0F;
      boolean depth = false;
      float bodyYaw = MathHelper.lerpAngleDegrees(partialTicks, player.prevBodyYaw, player.bodyYaw);
      float headYaw = MathHelper.lerpAngleDegrees(partialTicks, player.prevHeadYaw, player.headYaw);
      float headPitch = MathHelper.lerp(partialTicks, player.prevPitch, player.getPitch());
      float netHeadYaw = headYaw - bodyYaw;
      headPitch = MathHelper.clamp(headPitch, -60.0F, 60.0F);
      LimbAnimator limb = player.limbAnimator;
      float limbAngle = limb.getPos(partialTicks);
      float limbDistance = limb.getSpeed(partialTicks);
      if (player.isBaby()) {
         limbAngle *= 3.0F;
         limbDistance *= 0.8F;
      }

      if (limbDistance > 1.0F) {
         limbDistance = 1.0F;
      }

      float rightArmRX = MathHelper.cos(limbAngle * 0.6662F + (float)Math.PI) * 2.0F * limbDistance * 0.5F;
      float leftArmRX = MathHelper.cos(limbAngle * 0.6662F) * 2.0F * limbDistance * 0.5F;
      float rightLegRX = MathHelper.cos(limbAngle * 0.6662F) * 1.4F * limbDistance;
      float leftLegRX = MathHelper.cos(limbAngle * 0.6662F + (float)Math.PI) * 1.4F * limbDistance;
      float swingProgress = player.getHandSwingProgress(partialTicks);
      if (swingProgress > 0.0F) {
         leftArmRX += -MathHelper.sin(swingProgress * (float)Math.PI) * 1.5F;
      }

      matrices.push();
      matrices.translate(x, y, z);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-bodyYaw));
      if (!player.isSwimming() && !player.isGliding()) {
         if (player.isInSneakingPose()) {
            matrices.translate(0.0F, -0.1F * scale, 0.0F);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(4.0F));
         }
      } else {
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(headPitch + 90.0F));
      }

      MatrixStack.Entry bodyEntry = matrices.peek();
      matrices.push();
      matrices.translate(0.0F, (float)torsoTopY, 0.0F);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-netHeadYaw));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(headPitch * 0.5F));
      RenderUtil3D.getInstance().drawLineSafe(matrices.peek(), (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, headY - torsoTopY, (double)0.0F, lineColor, lineWidth, depth);
      matrices.pop();
      RenderUtil3D.getInstance().drawLineSafe(bodyEntry, (double)0.0F, torsoTopY, (double)0.0F, (double)0.0F, torsoBottomY, (double)0.0F, lineColor, lineWidth, depth);
      RenderUtil3D.getInstance().drawLineSafe(bodyEntry, (double)0.0F, torsoTopY, (double)0.0F, -armOffset, torsoTopY, (double)0.0F, lineColor, lineWidth, depth);
      matrices.push();
      matrices.translate(-armOffset, torsoTopY, (double)0.0F);
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(leftArmRX * (180F / (float)Math.PI)));
      RenderUtil3D.getInstance().drawLineSafe(matrices.peek(), (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, handY - torsoTopY, (double)0.0F, lineColor, lineWidth, depth);
      matrices.pop();
      RenderUtil3D.getInstance().drawLineSafe(bodyEntry, (double)0.0F, torsoTopY, (double)0.0F, armOffset, torsoTopY, (double)0.0F, lineColor, lineWidth, depth);
      matrices.push();
      matrices.translate(armOffset, torsoTopY, (double)0.0F);
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rightArmRX * (180F / (float)Math.PI)));
      RenderUtil3D.getInstance().drawLineSafe(matrices.peek(), (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, handY - torsoTopY, (double)0.0F, lineColor, lineWidth, depth);
      matrices.pop();
      RenderUtil3D.getInstance().drawLineSafe(bodyEntry, (double)0.0F, torsoBottomY, (double)0.0F, -legOffset, torsoBottomY, (double)0.0F, lineColor, lineWidth, depth);
      matrices.push();
      matrices.translate(-legOffset, torsoBottomY, (double)0.0F);
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(leftLegRX * (180F / (float)Math.PI)));
      RenderUtil3D.getInstance().drawLineSafe(matrices.peek(), (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, footY - torsoBottomY, (double)0.0F, lineColor, lineWidth, depth);
      matrices.pop();
      RenderUtil3D.getInstance().drawLineSafe(bodyEntry, (double)0.0F, torsoBottomY, (double)0.0F, legOffset, torsoBottomY, (double)0.0F, lineColor, lineWidth, depth);
      matrices.push();
      matrices.translate(legOffset, torsoBottomY, (double)0.0F);
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rightLegRX * (180F / (float)Math.PI)));
      RenderUtil3D.getInstance().drawLineSafe(matrices.peek(), (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, footY - torsoBottomY, (double)0.0F, lineColor, lineWidth, depth);
      matrices.pop();
      matrices.pop();
   }

   public void onRender2D(Render2DEvent event) {
      if (this.options.getValueByName("Nametag")) {
         Iterator var2 = mc.world.getPlayers().iterator();

         while(true) {
            PlayerEntity player;
            while(true) {
               if (!var2.hasNext()) {
                  return;
               }

               player = (PlayerEntity)var2.next();
               if (player != mc.player) {
                  if (!this.nametagOptions.getValueByName("Box") && !this.nametagOptions.getValueByName("HpBar")) {
                     break;
                  }

                  double scale = mc.getWindow().getScaleFactor();
                  int fbW = mc.getFramebuffer().textureWidth;
                  int fbH = mc.getFramebuffer().textureHeight;
                  float td = mc.getRenderTickCounter().getTickDelta(false);
                  Vec3d cam = EspMatrixHolder.camera.getPos();
                  double px = MathHelper.lerp((double)td, player.prevX, player.getX()) - cam.x;
                  double py = MathHelper.lerp((double)td, player.prevY, player.getY()) - cam.y;
                  double pz = MathHelper.lerp((double)td, player.prevZ, player.getZ()) - cam.z;
                  float h = player.getHeight();
                  float hw = player.getWidth() / 2.0F;
                  Vec3d[] corners = new Vec3d[]{new Vec3d(px - (double)hw, py, pz - (double)hw), new Vec3d(px + (double)hw, py, pz - (double)hw), new Vec3d(px - (double)hw, py, pz + (double)hw), new Vec3d(px + (double)hw, py, pz + (double)hw), new Vec3d(px - (double)hw, py + (double)h, pz - (double)hw), new Vec3d(px + (double)hw, py + (double)h, pz - (double)hw), new Vec3d(px - (double)hw, py + (double)h, pz + (double)hw), new Vec3d(px + (double)hw, py + (double)h, pz + (double)hw)};
                  float minX = Float.MAX_VALUE;
                  float minY = Float.MAX_VALUE;
                  float maxX = -Float.MAX_VALUE;
                  float maxY = -Float.MAX_VALUE;
                  boolean anyVisible = false;

                  for(Vec3d c : corners) {
                     float[] p = project(c, EspMatrixHolder.projView, fbW, fbH);
                     if (p != null) {
                        float sx = (float)((double)p[0] / scale);
                        float sy = (float)((double)p[1] / scale);
                        if (sx < minX) {
                           minX = sx;
                        }

                        if (sx > maxX) {
                           maxX = sx;
                        }

                        if (sy < minY) {
                           minY = sy;
                        }

                        if (sy > maxY) {
                           maxY = sy;
                        }

                        anyVisible = true;
                     }
                  }

                  if (anyVisible) {
                     float PAD = 2.0F;
                     minX -= 2.0F;
                     minY -= 2.0F;
                     maxX += 2.0F;
                     maxY += 2.0F;
                     float boxW = maxX - minX;
                     float boxH = maxY - minY;
                     if (!(boxW < 2.0F) && !(boxH < 2.0F)) {
                        if (this.nametagOptions.getValueByName("Box")) {
                           Matrix4f matrix = event.getDrawContext().getMatrices().peek().getPositionMatrix();
                           Color theme = CrolClient.INSTANCE.getThemeManager().getTheme().color();
                           float w = maxX - minX;
                           float hh = maxY - minY;
                           float len = 8.0F;
                           float thick = 1.5F;
                           float l = Math.min(len, Math.min(w / 2.0F, hh / 2.0F));
                           rect(matrix, minX, minY, l, thick, theme, 0.0F);
                           rect(matrix, minX, minY, thick, l, theme, 0.0F);
                           rect(matrix, maxX - l, minY, l, thick, theme, 0.0F);
                           rect(matrix, maxX - thick, minY, thick, l, theme, 0.0F);
                           rect(matrix, minX, maxY - thick, l, thick, theme, 0.0F);
                           rect(matrix, minX, maxY - l, thick, l, theme, 0.0F);
                           rect(matrix, maxX - l, maxY - thick, l, thick, theme, 0.0F);
                           rect(matrix, maxX - thick, maxY - l, thick, l, theme, 0.0F);
                        }

                        if (this.nametagOptions.getValueByName("HpBar")) {
                           Matrix4f matrix = event.getDrawContext().getMatrices().peek().getPositionMatrix();
                           float hp = player.getHealth();
                           float maxHp = player.getMaxHealth();
                           float hpFrac = MathHelper.clamp(hp / maxHp, 0.0F, 1.0F);
                           Color hpCol = new Color(lerpColor(16720278, 4521796, hpFrac));
                           float barX = minX - 4.0F;
                           rect(matrix, barX, minY, 2.0F, boxH, new Color(-2147483647, true), 1.0F);
                           float fillH = boxH * hpFrac;
                           rect(matrix, barX, maxY - fillH, 2.0F, fillH, hpCol, 1.0F);
                        }
                        break;
                     }
                  }
               }
            }

            this.renderNametags(event, player);
         }
      }
   }

   public static float[] project(Vec3d pos, Matrix4f projView, int fbW, int fbH) {
      Vector4f v = new Vector4f((float)pos.getX(), (float)pos.getY(), (float)pos.getZ(), 1.0F);
      v.mul(projView);
      if (v.w <= 0.0F) {
         return null;
      } else {
         float nx = v.x / v.w;
         float ny = -v.y / v.w;
         return new float[]{(nx + 1.0F) / 2.0F * (float)fbW, (ny + 1.0F) / 2.0F * (float)fbH};
      }
   }

   private static int lerpColor(int from, int to, float t) {
      t = MathHelper.clamp(t, 0.0F, 1.0F);
      int r = (int)((float)(from >> 16 & 255) * (1.0F - t) + (float)(to >> 16 & 255) * t);
      int g = (int)((float)(from >> 8 & 255) * (1.0F - t) + (float)(to >> 8 & 255) * t);
      int b = (int)((float)(from & 255) * (1.0F - t) + (float)(to & 255) * t);
      return r << 16 | g << 8 | b;
   }

   private void renderNametags(Render2DEvent event, PlayerEntity player) {
      double x = MathUtil.interpolate(player.getX(), player.lastRenderX, (double)event.getTickCounter().getTickDelta(false));
      double y = MathUtil.interpolate(player.getY(), player.lastRenderY, (double)event.getTickCounter().getTickDelta(false));
      double z = MathUtil.interpolate(player.getZ(), player.lastRenderZ, (double)event.getTickCounter().getTickDelta(false));
      Matrix4f matrix = event.getDrawContext().getMatrices().peek().getPositionMatrix();
      String itemName = player.getNameForScoreboard();
      if (itemName.isEmpty() || !itemName.matches(".*[a-zA-Zа-яА-Я0-9].*")) {
         itemName = "unknown";
      }

      boolean isFriend = CrolClient.INSTANCE.getFriendManager().isFriend(itemName);
      String text = (isFriend ? "[F]" : "") + itemName + "[" + (int)player.getHealth() + "]";
      Vector2f project = ProjectionUtil.project(x, y + (double)player.getHeight() + 0.24, z);
      float textWidth = ((MsdfFont)FontManager.BOLD.get()).getWidth(text, 6.0F) + 9.0F + (isFriend ? 1.5F : 0.0F);
      float centr = textWidth / 2.0F;
      float height = 10.0F;
      this.drawItems(event, player, (int)project.getX(), (int)(project.getY() - 15.0F));
      BuiltRectangle rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(textWidth, height)).color(new QuadColorState(new Color(-2147483647, true))).radius(new QuadRadiusState(1.0F)).smoothness(1.75F).build();
      rectangle.render(matrix, (float)((int)(project.getX() - 1.5F - centr)), (float)((int)(project.getY() - 4.0F)));
      double x2 = (double)0.0F;
      if (CrolClient.INSTANCE.getFriendManager().isFriend(itemName)) {
         BuiltText textRender = (BuiltText)Builder.text().font((MsdfFont)FontManager.BOLD.get()).text("[").color(Color.white).size(6.0F).thickness(0.05F).build();
         textRender.render(matrix, (double)(project.getX() - centr) + (double)1.25F + x2, (double)(project.getY() - 3.0F), (double)5.0F);
         x2 += (double)((MsdfFont)FontManager.BOLD.get()).getWidth("[", 6.0F);
         textRender = (BuiltText)Builder.text().font((MsdfFont)FontManager.BOLD.get()).text("F").color(Color.green).size(6.0F).thickness(0.05F).build();
         textRender.render(matrix, (double)(project.getX() - centr) + (double)1.25F + x2, (double)(project.getY() - 3.0F), (double)5.0F);
         x2 += (double)((MsdfFont)FontManager.BOLD.get()).getWidth("F", 6.0F);
         textRender = (BuiltText)Builder.text().font((MsdfFont)FontManager.BOLD.get()).text("]").color(Color.white).size(6.0F).thickness(0.05F).build();
         textRender.render(matrix, (double)(project.getX() - centr) + (double)1.25F + x2, (double)(project.getY() - 3.0F), (double)5.0F);
         x2 += (double)((MsdfFont)FontManager.BOLD.get()).getWidth("]", 6.0F) + (double)1.5F;
      }

      BuiltText textRender = (BuiltText)Builder.text().font((MsdfFont)FontManager.BOLD.get()).text(itemName).color(Color.white).size(6.0F).thickness(0.05F).build();
      textRender.render(matrix, (double)(project.getX() - centr) + (double)1.25F + x2, (double)(project.getY() - 3.0F), (double)5.0F);
      x2 += (double)(((MsdfFont)FontManager.BOLD.get()).getWidth(itemName, 6.0F) + 3.0F);
      textRender = (BuiltText)Builder.text().font((MsdfFont)FontManager.BOLD.get()).text("[").color(Color.white).size(6.0F).thickness(0.05F).build();
      textRender.render(matrix, (double)(project.getX() - centr) + (double)1.25F + x2, (double)(project.getY() - 3.0F), (double)5.0F);
      x2 += (double)((MsdfFont)FontManager.BOLD.get()).getWidth("[", 6.0F);
      textRender = (BuiltText)Builder.text().font((MsdfFont)FontManager.BOLD.get()).text("" + (int)player.getHealth()).color(this.get(player)).size(6.0F).thickness(0.05F).build();
      textRender.render(matrix, (double)(project.getX() - centr) + (double)1.25F + x2, (double)(project.getY() - 3.0F), (double)5.0F);
      x2 += (double)((MsdfFont)FontManager.BOLD.get()).getWidth("" + (int)player.getHealth(), 6.0F);
      textRender = (BuiltText)Builder.text().font((MsdfFont)FontManager.BOLD.get()).text("]").color(Color.white).size(6.0F).thickness(0.05F).build();
      textRender.render(matrix, (double)(project.getX() - centr) + (double)1.25F + x2, (double)(project.getY() - 3.0F), (double)5.0F);
   }

   private void drawItems(Render2DEvent render2DEvent, LivingEntity entity, int posX, int posY) {
      int size = 13;
      int padding = 2;
      float fontHeight = 10.0F;
      List<ItemStack> items = new ArrayList();
      ItemStack mainStack = entity.getMainHandStack();
      if (!mainStack.isEmpty()) {
         items.add(mainStack);
      }

      for(ItemStack itemStack : entity.getArmorItems()) {
         if (!itemStack.isEmpty()) {
            items.add(itemStack);
         }
      }

      ItemStack offStack = entity.getOffHandStack();
      if (!offStack.isEmpty()) {
         items.add(offStack);
      }

      float totalWidth = 0.0F;

      for(ItemStack itemStack : items) {
         totalWidth += (float)(size + padding);
      }

      totalWidth -= (float)padding;
      posX -= (int)(totalWidth / 2.0F);

      for(ItemStack itemStack : items) {
         if (!itemStack.isEmpty()) {
            BuiltRectangle rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(12.0F, 12.0F)).color(new QuadColorState(new Color(-2063597568, true))).radius(new QuadRadiusState(0.5F)).smoothness(1.75F).build();
            int iX = Math.round((float)posX);
            int iY = Math.round((float)posY) - 1;
            rectangle.render(render2DEvent.getDrawContext().getMatrices().peek().getPositionMatrix(), (float)(iX - 1), (float)iY);
            render2DEvent.getDrawContext().getMatrices().push();
            AnimationUtil.sizeAnimation(render2DEvent.getDrawContext(), (double)((float)iX + (float)size / 2.0F), (double)((float)iY + (float)size / 2.0F), (double)0.5F);
            render2DEvent.getDrawContext().drawItem(itemStack, iX - 4, iY - 2);
            render2DEvent.getDrawContext().getMatrices().pop();
            if (!itemStack.getEnchantments().isEmpty() && this.nametagOptions.getValueByName("Enchants")) {
               int ePosY = (int)((float)posY - fontHeight);

               for(RegistryEntry<Enchantment> enchantment : EnchantmentHelper.getEnchantments(itemStack).getEnchantments()) {
                  int level = EnchantmentHelper.getLevel(enchantment, itemStack);
                  if (level >= 1) {
                     String text = ((Enchantment)enchantment.value()).description().asTruncatedString(2);
                     BuiltText textRender = (BuiltText)Builder.text().font((MsdfFont)FontManager.BOLD.get()).text(text + " " + level).color(Color.white).size(4.05F).thickness(0.05F).build();
                     textRender.render(render2DEvent.getDrawContext().getMatrices().peek().getPositionMatrix(), (double)posX, (double)ePosY + (double)3.5F);
                     ePosY -= 6;
                  }
               }
            }

            posX += size + padding;
         }
      }

   }

   private Color get(PlayerEntity player) {
      float health = player.getHealth();
      if (health > 18.0F) {
         return Color.green;
      } else if (health > 12.0F) {
         return Color.yellow;
      } else {
         return health > 5.0F ? new Color(16755200) : new Color(16733525);
      }
   }

   private static void rect(Matrix4f m, float x, float y, float w, float h, Color color, float radius) {
      if (!(w <= 0.0F) && !(h <= 0.0F)) {
         ((BuiltRectangle)Builder.rectangle().size(new SizeState(w, h)).color(new QuadColorState(color)).radius(new QuadRadiusState(radius)).smoothness(1.0F).build()).render(m, x, y);
      }

   }
}
