package crol.client.ui.hud.impl;

import crol.client.CrolClient;
import crol.client.managers.FontManager;
import crol.client.modules.impl.combat.Aura;
import crol.client.modules.impl.combat.TriggerBot;
import crol.client.modules.impl.player.NameProtect;
import crol.client.ui.draggable.impl.TargetHudDraggable;
import crol.client.ui.hud.HudElement;
import crol.client.util.animations.Animation;
import crol.client.util.animations.Direction;
import crol.client.util.animations.impl.EaseBackIn;
import crol.client.util.color.ColorUtil;
import crol.client.util.math.MathUtil;
import crol.client.util.render.AnimationUtil;
import crol.client.util.render.ItemRenderUtil;
import crol.client.util.render.LastTarget;
import crol.client.util.render.builders.Builder;
import crol.client.util.render.builders.impl.TextBuilder;
import crol.client.util.render.builders.states.QuadColorState;
import crol.client.util.render.builders.states.QuadRadiusState;
import crol.client.util.render.builders.states.SizeState;
import crol.client.util.render.msdf.MsdfFont;
import crol.client.util.render.renderers.impl.BuiltBlur;
import crol.client.util.render.renderers.impl.BuiltBorder;
import crol.client.util.render.renderers.impl.BuiltRectangle;
import crol.client.util.render.renderers.impl.BuiltText;
import crol.client.util.render.renderers.impl.BuiltTexture;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class TargetHudElement extends HudElement {
   private final Animation animation;
   private LastTarget lastTarget = new LastTarget();
   float health;
   private NameProtect nameProtect;

   public TargetHudElement() {
      super(new TargetHudDraggable(), "TargetHUD");
      this.animation = new EaseBackIn(300, (double)1.0F, 0.1F, Direction.BACKWARDS);
      this.health = 0.0F;
   }

   public void render(DrawContext drawContext) {
      float width = 130.0F;
      float widthBlack = 76.0F;
      if (this.nameProtect == null) {
         this.nameProtect = (NameProtect)CrolClient.INSTANCE.getModuleManager().getByClass(NameProtect.class);
      }

      Matrix4f matrix = drawContext.getMatrices().peek().getPositionMatrix();
      Color clientColor = ColorUtil.setAlpha(this.animation.getOutput(), CrolClient.INSTANCE.getThemeManager().getTheme().color());
      Color white = ColorUtil.setAlpha(this.animation.getOutput(), Color.WHITE);
      Color gray = ColorUtil.setAlpha(this.animation.getOutput(), new Color(2063597567, true));
      Color black = ColorUtil.setAlpha(this.animation.getOutput(), new Color(-1206577638, true));
      if (!this.animation.finished(Direction.BACKWARDS)) {
         BuiltBlur blur = (BuiltBlur)Builder.blur().size(new SizeState(width, 50.0F)).radius(new QuadRadiusState(11.0F)).blurRadius((float)((double)12.0F * this.animation.getOutput())).smoothness((float)((double)1.0F * this.animation.getOutput())).color(new QuadColorState(Color.white)).build();
         blur.render(matrix, (float)this.draggable.x, (float)this.draggable.y);
         BuiltRectangle rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(width, 50.0F)).color(new QuadColorState(ColorUtil.setAlpha(this.animation.getOutput(), new Color(2048202266, true)))).radius(new QuadRadiusState(11.0F)).smoothness(1.15F).build();
         rectangle.render(matrix, (float)this.draggable.x, (float)this.draggable.y);
         rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(42.0F, 42.0F)).color(new QuadColorState(black)).radius(new QuadRadiusState(8.0F, 8.0F, 5.0F, 5.0F)).smoothness(1.15F).build();
         rectangle.render(matrix, (float)(this.draggable.x + 4), (float)(this.draggable.y + 4));
         AbstractTexture abstractTexture = MinecraftClient.getInstance().getTextureManager().getTexture(this.lastTarget.getAvatar());
         float hurtPercent = ((float)this.lastTarget.getHurtTime() - (this.lastTarget.getHurtTime() != 0 ? MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false) : 0.0F)) / 10.0F;
         Color imageColor = white;
         if (hurtPercent > 0.0F) {
            int f = (int)(hurtPercent * 90.0F);
            if (f > 200) {
               f = 200;
            }

            imageColor = new Color(255, 255 - f, 255 - f, white.getAlpha());
         }

         if (this.lastTarget.isPlayer()) {
            BuiltTexture texture = (BuiltTexture)Builder.texture().size(new SizeState(32.0F, 32.0F)).radius(new QuadRadiusState(6.0F)).texture(0.125F, 0.125F, 0.125F, 0.125F, abstractTexture).color(new QuadColorState(imageColor)).build();
            texture.render(matrix, (float)(this.draggable.x + 9), (float)(this.draggable.y + 9));
         } else {
            rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(32.0F, 32.0F)).color(new QuadColorState(black)).radius(new QuadRadiusState(6.0F)).smoothness(1.15F).build();
            rectangle.render(matrix, (float)(this.draggable.x + 9), (float)(this.draggable.y + 9));
            ((BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text("?").color(white).size(25.0F).thickness(0.05F).build()).render(matrix, (float)(this.draggable.x + 17), (float)(this.draggable.y + 10));
         }

         rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState((double)widthBlack, (double)19.5F)).color(new QuadColorState(black)).radius(new QuadRadiusState(3.0F, 3.0F, 3.0F, 7.0F)).smoothness(1.15F).build();
         rectangle.render(matrix, (float)(this.draggable.x + 48), (float)(this.draggable.y + 4));
         BuiltText themeText = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(this.nameProtect.replace(this.lastTarget.getName())).color(white).size(7.5F).thickness(0.05F).build();
         themeText.render(matrix, (float)(this.draggable.x + 54), (float)(this.draggable.y + 9));
         rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState((double)widthBlack, (double)19.5F)).color(new QuadColorState(black)).radius(new QuadRadiusState(3.0F, 3.0F, 7.0F, 3.0F)).smoothness(1.15F).build();
         rectangle.render(matrix, (double)(this.draggable.x + 48), (double)this.draggable.y + (double)25.5F);
         rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState((double)(widthBlack - 10.0F), (double)3.25F)).color(new QuadColorState(ColorUtil.setAlpha(this.animation.getOutput(), new Color(184549375, true)))).radius(new QuadRadiusState(0.5F)).smoothness(1.15F).build();
         rectangle.render(matrix, (float)(this.draggable.x + 52), (float)(this.draggable.y + 38));
         rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState((double)((widthBlack - 10.0F) * (this.health > 1.0F ? 1.0F : this.health)), (double)3.25F)).color(new QuadColorState(clientColor)).radius(new QuadRadiusState(0.5F)).smoothness(1.15F).build();
         rectangle.render(matrix, (float)(this.draggable.x + 52), (float)(this.draggable.y + 38));
         TextBuilder var10000 = Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get());
         double var10001 = (double)this.health * this.lastTarget.getMaxHealth();
         themeText = (BuiltText)var10000.text("" + (int)MathUtil.round(var10001, (double)0.5F)).color(white).size(6.0F).thickness(0.05F).build();
         themeText.render(matrix, (float)this.draggable.x + widthBlack + 25.0F, (float)(this.draggable.y + 29));
         themeText = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text("hp").color(gray).size(6.0F).thickness(0.05F).build();
         themeText.render(matrix, (float)this.draggable.x + widthBlack + 35.0F, (float)(this.draggable.y + 29));

         for(int i = 0; i < 4; ++i) {
            rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(8.0F, 8.0F)).color(new QuadColorState(ColorUtil.setAlpha(this.animation.getOutput(), new Color(67108863, true)))).radius(new QuadRadiusState(0.5F)).smoothness(1.15F).build();
            rectangle.render(matrix, (double)(this.draggable.x + 52 + i * 10), (double)this.draggable.y + (double)28.5F);
            BuiltBorder border = (BuiltBorder)Builder.border().size(new SizeState(8.0F, 8.0F)).color(new QuadColorState(ColorUtil.colorAlpha(new Color(184549375, true), 3))).radius(new QuadRadiusState(0.5F)).thickness(0.01F).smoothness(0.65F, 0.65F).build();
            border.render(matrix, (double)(this.draggable.x + 52 + i * 10), (double)this.draggable.y + (double)28.5F);
         }

         if (this.lastTarget.getEntity() != null) {
            Entity var32 = this.lastTarget.getEntity();
            if (var32 instanceof PlayerEntity) {
               PlayerEntity player = (PlayerEntity)var32;
               this.renderItems(drawContext, this.draggable.x + 53, (int)((float)this.draggable.y + 30.5F), player, (float)this.animation.getOutput());
            }
         }

         this.health = MathUtil.fast(this.health, (float)(this.lastTarget.getHealth() / this.lastTarget.getMaxHealth()), 5.0F);
      }

      if (this.getTarget() != null) {
         Entity target = this.getTarget();
         this.animation.setDirection(Direction.FORWARDS);
         this.lastTarget.setEntity(target);
         this.lastTarget.update(target);
         if (target instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)target;
            this.lastTarget.setName(player.getName().getString());
         } else {
            this.lastTarget.setName(target.getName().getString());
         }

         if (target instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)target;
            this.lastTarget.setHealth((double)livingEntity.getHealth());
            this.lastTarget.setMaxHealth(this.lastTarget.getMaxHealth());
         }
      } else {
         this.animation.setDirection(Direction.BACKWARDS);
      }

   }

   void renderItems(DrawContext drawContext, int x, int y, PlayerEntity player, float alpha) {
      MatrixStack matrices = drawContext.getMatrices();
      int xoffset = 0;
      matrices.push();
      AnimationUtil.sizeAnimation(drawContext, (double)x, (double)y, 0.38);
      ArrayList<ItemStack> armorItems = new ArrayList((Collection)player.getArmorItems());
      Collections.reverse(armorItems);

      for(ItemStack item : armorItems) {
         if (item.getItem() != Items.AIR) {
            ItemRenderUtil.drawItemAlpha(matrices, item, x + xoffset, y - 2, alpha);
            xoffset += 26;
         }
      }

      matrices.pop();
   }

   private Entity getTarget() {
      Aura aura = (Aura)CrolClient.INSTANCE.getModuleManager().getByClass(Aura.class);
      TriggerBot triggerBot = (TriggerBot)CrolClient.INSTANCE.getModuleManager().getByClass(TriggerBot.class);
      if (MinecraftClient.getInstance().currentScreen instanceof ChatScreen) {
         return MinecraftClient.getInstance().player;
      } else if (aura.isEnabled() && aura.getTarget() != null) {
         return aura.getTarget();
      } else {
         return triggerBot.isEnabled() && triggerBot.getTarget() != null ? triggerBot.getTarget() : null;
      }
   }
}
