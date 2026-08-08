package crol.client.ui.hud.impl;

import crol.client.CrolClient;
import crol.client.managers.FontManager;
import crol.client.ui.draggable.impl.PotionsDraggable;
import crol.client.ui.hud.HudElement;
import crol.client.ui.hud.Potion;
import crol.client.util.animations.Direction;
import crol.client.util.color.ColorUtil;
import crol.client.util.render.ParsedText;
import crol.client.util.render.RomanToArabic;
import crol.client.util.render.builders.Builder;
import crol.client.util.render.builders.states.QuadColorState;
import crol.client.util.render.builders.states.QuadRadiusState;
import crol.client.util.render.builders.states.SizeState;
import crol.client.util.render.msdf.MsdfFont;
import crol.client.util.render.renderers.impl.BuiltBlur;
import crol.client.util.render.renderers.impl.BuiltBorder;
import crol.client.util.render.renderers.impl.BuiltRectangle;
import crol.client.util.render.renderers.impl.BuiltText;
import java.awt.Color;
import java.util.ArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class PotionsElement extends HudElement {
   private final ArrayList<Potion> potions = new ArrayList();
   private float offset = 0.0F;

   public PotionsElement() {
      super(new PotionsDraggable(), "Potions");
   }

   public void render(DrawContext drawContext) {
      for(StatusEffectInstance effect : MinecraftClient.getInstance().player.getStatusEffects()) {
         if (!this.isInList(effect)) {
            this.potions.add(new Potion(effect));
         }
      }

      Matrix4f matrix = drawContext.getMatrices().peek().getPositionMatrix();
      this.offset = 0.0F;
      float size = 7.5F;

      for(Potion potion : this.potions) {
         if (!potion.getAnimation().finished(Direction.BACKWARDS)) {
            if (!MinecraftClient.getInstance().player.getStatusEffects().contains(potion.getEffect())) {
               potion.getAnimation().setDirection(Direction.BACKWARDS);
            }

            String time = StatusEffectUtil.getDurationText(potion.getEffect(), 1.0F, MinecraftClient.getInstance().world.getTickManager().getTickRate()).getString().toCharArray().length <= 5 && StatusEffectUtil.getDurationText(potion.getEffect(), 1.0F, MinecraftClient.getInstance().world.getTickManager().getTickRate()).getString().toCharArray().length >= 2 ? StatusEffectUtil.getDurationText(potion.getEffect(), 1.0F, MinecraftClient.getInstance().world.getTickManager().getTickRate()).getString() : "++:++";
            float widthTime = ((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).getWidth(time, size) + 13.0F;
            ParsedText result = RomanToArabic.parseText(this.getStatusEffectDescription(potion.getEffect()).getString());
            String name = result.getText();
            String level = "level " + result.getNumber();
            Color white = ColorUtil.setAlpha(potion.getAnimation().getOutput(), Color.white);
            Color gray = ColorUtil.setAlpha(potion.getAnimation().getOutput() * 0.48, Color.white);
            float widthName = ((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).getWidth(level, size) + ((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).getWidth(name, size) + 13.0F + 13.0F;
            float width = 31.0F + widthName + widthTime;
            Color clientColor = ColorUtil.setAlpha(potion.getAnimation().getOutput(), CrolClient.INSTANCE.getThemeManager().getTheme().color());
            BuiltBlur blur = (BuiltBlur)Builder.blur().size(new SizeState(width, 26.0F)).radius(new QuadRadiusState(8.0F)).blurRadius((float)((double)12.0F * potion.getAnimation().getOutput())).smoothness((float)((double)1.0F * potion.getAnimation().getOutput())).color(new QuadColorState(Color.white)).build();
            blur.render(matrix, (float)this.draggable.x, (float)this.draggable.y + this.offset);
            BuiltRectangle rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(width, 26.0F)).color(new QuadColorState(ColorUtil.setAlpha(potion.getAnimation().getOutput(), new Color(2048202266, true)))).radius(new QuadRadiusState(8.0F)).smoothness(1.15F).build();
            rectangle.render(matrix, (float)this.draggable.x, (float)this.draggable.y + this.offset);
            rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(18.0F, 18.0F)).color(new QuadColorState(ColorUtil.setAlpha(potion.getAnimation().getOutput(), new Color(100663295, true)))).radius(new QuadRadiusState(5.0F, 5.0F, 2.0F, 2.0F)).smoothness(1.15F).build();
            rectangle.render(matrix, (float)(this.draggable.x + 4), (float)this.draggable.y + this.offset + 4.0F);
            BuiltBorder border = (BuiltBorder)Builder.border().size(new SizeState(18.0F, 18.0F)).color(new QuadColorState(ColorUtil.colorAlpha(new Color(184549375, true), (int)((double)4.0F * potion.getAnimation().getOutput())))).radius(new QuadRadiusState(5.0F, 5.0F, 2.0F, 2.0F)).thickness(0.01F).smoothness(0.65F, 0.65F).build();
            border.render(matrix, (float)(this.draggable.x + 4), (float)this.draggable.y + this.offset + 4.0F);
            StatusEffectSpriteManager statusEffectSpriteManager = MinecraftClient.getInstance().getStatusEffectSpriteManager();
            RegistryEntry<StatusEffect> registryEntry = potion.getEffect().getEffectType();
            Sprite sprite = statusEffectSpriteManager.getSprite(registryEntry);
            drawContext.drawSpriteStretched(RenderLayer::getGuiTextured, sprite, this.draggable.x + 8, (int)((float)this.draggable.y + this.offset + 8.0F), 10, 10, white.getRGB());
            Color color = ColorUtil.setAlpha(potion.getAnimation().getOutput(), new Color(-1206577638, true));
            rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(widthName, 18.0F)).color(new QuadColorState(color)).radius(new QuadRadiusState(3.0F)).smoothness(1.15F).build();
            rectangle.render(matrix, (float)(this.draggable.x + 25), (float)(this.draggable.y + 4) + this.offset);
            BuiltText themeText = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(name).color(white).size(7.5F).thickness(0.05F).build();
            themeText.render(matrix, (double)(this.draggable.x + 30), (double)this.draggable.y + (double)8.5F + (double)this.offset);
            themeText = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(level).color(gray).size(7.5F).thickness(0.05F).build();
            themeText.render(matrix, (double)((float)(this.draggable.x + 44) + ((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).getWidth(name, size)), (double)this.draggable.y + (double)8.5F + (double)this.offset);
            rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(widthTime, 18.0F)).color(new QuadColorState(color)).radius(new QuadRadiusState(3.0F, 3.0F, 5.0F, 5.0F)).smoothness(1.15F).build();
            rectangle.render(matrix, (float)(this.draggable.x + 27) + widthName, (float)(this.draggable.y + 4) + this.offset);
            themeText = (BuiltText)Builder.text().font((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).text(time).color(clientColor).size(7.5F).thickness(0.05F).build();
            themeText.render(matrix, (double)((float)(this.draggable.x + 32) + widthName), (double)this.draggable.y + (double)8.5F + (double)this.offset);
            this.offset = (float)((double)this.offset + (double)28.0F * potion.getAnimation().getOutput());
         } else if (potion.getAnimation().finished(Direction.BACKWARDS)) {
            this.potions.remove(potion);
            return;
         }
      }

   }

   private Text getStatusEffectDescription(StatusEffectInstance statusEffect) {
      MutableText mutableText = ((StatusEffect)statusEffect.getEffectType().value()).getName().copy();
      if (statusEffect.getAmplifier() >= 1 && statusEffect.getAmplifier() <= 9) {
         MutableText var10000 = mutableText.append(ScreenTexts.SPACE);
         int var10001 = statusEffect.getAmplifier();
         var10000.append(Text.translatable("enchantment.level." + (var10001 + 1)));
      }

      return mutableText;
   }

   private boolean isInList(StatusEffectInstance effectInstance) {
      for(Potion potion : this.potions) {
         RegistryEntry<StatusEffect> registryEntry = potion.getEffect().getEffectType();
         if (potion.getEffect() == effectInstance) {
            return true;
         }

         if (registryEntry == effectInstance.getEffectType()) {
            potion.setEffect(effectInstance);
            return true;
         }
      }

      return false;
   }
}
