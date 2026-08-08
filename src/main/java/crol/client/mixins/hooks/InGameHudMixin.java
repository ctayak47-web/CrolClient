package crol.client.mixins.hooks;

import crol.client.CrolClient;
import crol.client.modules.impl.render.Crosshair;
import crol.client.modules.impl.render.Interface;
import crol.client.util.color.ColorUtil;
import crol.client.util.math.MathUtil;
import crol.client.util.render.AnimationUtil;
import crol.client.util.render.builders.Builder;
import crol.client.util.render.builders.states.QuadColorState;
import crol.client.util.render.builders.states.QuadRadiusState;
import crol.client.util.render.builders.states.SizeState;
import crol.client.util.render.renderers.impl.BuiltBlur;
import crol.client.util.render.renderers.impl.BuiltBorder;
import crol.client.util.render.renderers.impl.BuiltRectangle;
import java.awt.Color;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({InGameHud.class})
public abstract class InGameHudMixin {
   @Shadow
   @Final
   private static Identifier HOTBAR_TEXTURE;
   @Shadow
   @Final
   private static Identifier HOTBAR_SELECTION_TEXTURE;
   @Shadow
   @Final
   private static Identifier HOTBAR_OFFHAND_LEFT_TEXTURE;
   @Shadow
   @Final
   private static Identifier HOTBAR_OFFHAND_RIGHT_TEXTURE;
   @Shadow
   @Final
   private static Identifier HOTBAR_ATTACK_INDICATOR_BACKGROUND_TEXTURE;
   @Shadow
   @Final
   private static Identifier HOTBAR_ATTACK_INDICATOR_PROGRESS_TEXTURE;
   @Unique
   private double mainSlot;

   @Shadow
   protected abstract void renderHotbarItem(DrawContext var1, int var2, int var3, RenderTickCounter var4, PlayerEntity var5, ItemStack var6, int var7);

   @Inject(
      method = {"renderMainHud"},
      at = {@At("HEAD")}
   )
   private void renderMainHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      CrolClient.INSTANCE.getEventManager().getShaderHandEvent2D().update(context, tickCounter);
      CrolClient.INSTANCE.getEventManager().hookEvent(CrolClient.INSTANCE.getEventManager().getShaderHandEvent2D());
   }

   @Inject(
      method = {"renderCrosshair"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void renderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      if (CrolClient.INSTANCE.getModuleManager().getByClass(Crosshair.class).isEnabled()) {
         ci.cancel();
      }

   }

   @Inject(
      method = {"renderHotbar"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void renderHotbar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
      MinecraftClient mc = MinecraftClient.getInstance();
      PlayerEntity playerEntity = mc.player;
      if (playerEntity != null) {
         Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
         ItemStack itemStack = playerEntity.getOffHandStack();
         Arm arm = playerEntity.getMainArm().getOpposite();
         int i = context.getScaledWindowWidth() / 2;
         int j = 182;
         int k = 91;
         context.getMatrices().push();
         context.getMatrices().translate(0.0F, 0.0F, -90.0F);
         Interface hud = (Interface)CrolClient.INSTANCE.getModuleManager().getByClass(Interface.class);
         boolean enabled = hud.isEnabled() && hud.getElements().getValueByName("Hotbar");
         if (enabled) {
            BuiltBlur blur = (BuiltBlur)Builder.blur().size(new SizeState(202.0F, 26.0F)).radius(new QuadRadiusState(6.0F)).blurRadius(12.0F).smoothness(1.0F).color(new QuadColorState(Color.white)).build();
            blur.render(matrix4f, (float)(i - 102), (float)(context.getScaledWindowHeight() - 27));
            BuiltRectangle rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(202.0F, 26.0F)).color(new QuadColorState(new Color(2048202266, true))).radius(new QuadRadiusState(6.0F)).smoothness(1.15F).build();
            rectangle.render(matrix4f, (float)(i - 102), (float)(context.getScaledWindowHeight() - 27));
            double[] round = new double[]{0 == playerEntity.getInventory().selectedSlot ? (double)6.0F : (double)2.0F, 8 == playerEntity.getInventory().selectedSlot ? (double)6.0F : (double)2.0F};
            this.mainSlot = (double)MathUtil.fast((float)this.mainSlot, (float)(i - 91 - 1 + playerEntity.getInventory().selectedSlot * 22), 10.0F);
            ((BuiltRectangle)Builder.rectangle().size(new SizeState(20.0F, 20.0F)).color(new QuadColorState(ColorUtil.setAlpha((double)0.3F, CrolClient.INSTANCE.getThemeManager().getTheme().color()))).radius(new QuadRadiusState(round[0], round[0], round[1], round[1])).smoothness(1.15F).build()).render(matrix4f, this.mainSlot - (double)7.0F, (double)(context.getScaledWindowHeight() - 24));
            ((BuiltBorder)Builder.border().size(new SizeState(20.0F, 20.0F)).color(new QuadColorState(new Color(184549375, true))).radius(new QuadRadiusState(round[0], round[0], round[1], round[1])).thickness(0.01F).smoothness(0.6F, 0.6F).build()).render(matrix4f, this.mainSlot - (double)7.0F, (double)(context.getScaledWindowHeight() - 24));
         } else {
            context.drawGuiTexture(RenderLayer::getGuiTextured, HOTBAR_TEXTURE, i - 91, context.getScaledWindowHeight() - 22, 182, 22);
            context.drawGuiTexture(RenderLayer::getGuiTextured, HOTBAR_SELECTION_TEXTURE, i - 91 - 1 + playerEntity.getInventory().selectedSlot * 20, context.getScaledWindowHeight() - 22 - 1, 24, 23);
         }

         if (!itemStack.isEmpty()) {
            if (arm == Arm.LEFT) {
               if (enabled) {
                  double x = (double)(i - 91 - 29 - 12);
                  BuiltBlur blur = (BuiltBlur)Builder.blur().size(new SizeState(26.0F, 26.0F)).radius(new QuadRadiusState(6.0F)).blurRadius(12.0F).smoothness(1.0F).color(new QuadColorState(Color.white)).build();
                  blur.render(matrix4f, x, (double)(context.getScaledWindowHeight() - 27));
                  BuiltRectangle rectangle = (BuiltRectangle)Builder.rectangle().size(new SizeState(26.0F, 26.0F)).color(new QuadColorState(new Color(2048202266, true))).radius(new QuadRadiusState(6.0F)).smoothness(1.15F).build();
                  rectangle.render(matrix4f, x, (double)(context.getScaledWindowHeight() - 27));
                  ((BuiltRectangle)Builder.rectangle().size(new SizeState(20.0F, 20.0F)).color(new QuadColorState(new Color(100663295, true))).radius(new QuadRadiusState(5.0F)).smoothness(1.15F).build()).render(matrix4f, x + (double)3.0F, (double)(context.getScaledWindowHeight() - 27 + 3));
                  ((BuiltBorder)Builder.border().size(new SizeState(20.0F, 20.0F)).color(new QuadColorState(new Color(184549375, true))).radius(new QuadRadiusState(5.0F)).thickness(0.01F).smoothness(0.6F, 0.6F).build()).render(matrix4f, x + (double)3.0F, (double)(context.getScaledWindowHeight() - 27 + 3));
               } else {
                  context.drawGuiTexture(RenderLayer::getGuiTextured, HOTBAR_OFFHAND_LEFT_TEXTURE, i - 91 - 29, context.getScaledWindowHeight() - 23, 29, 24);
               }
            } else {
               context.drawGuiTexture(RenderLayer::getGuiTextured, HOTBAR_OFFHAND_RIGHT_TEXTURE, i + 91, context.getScaledWindowHeight() - 23, 29, 24);
            }
         }

         context.getMatrices().pop();
         int l = 1;

         for(int m = 0; m < 9; ++m) {
            int n = i - 90 + m * 20 + 2;
            int o = context.getScaledWindowHeight() - 16 - 3;
            if (enabled) {
               n = i - 100 + m * 22 + 2;
               double[] round = new double[]{0 == m ? (double)6.0F : (double)2.0F, 8 == m ? (double)6.0F : (double)2.0F};
               double x = (double)(n - 1);
               double y = (double)(o - 5);
               ((BuiltRectangle)Builder.rectangle().size(new SizeState(20.0F, 20.0F)).color(new QuadColorState(new Color(100663295, true))).radius(new QuadRadiusState(round[0], round[0], round[1], round[1])).smoothness(1.15F).build()).render(matrix4f, x, y);
               ((BuiltBorder)Builder.border().size(new SizeState(20.0F, 20.0F)).color(new QuadColorState(new Color(184549375, true))).radius(new QuadRadiusState(round[0], round[0], round[1], round[1])).thickness(0.01F).smoothness(0.6F, 0.6F).build()).render(matrix4f, x, y);
               ++n;
               o -= 2;
            }

            if (enabled) {
               context.getMatrices().push();
               AnimationUtil.sizeAnimation(context, (double)((float)n + 1.92F), (double)((float)o + 1.0F), 0.83);
            }

            this.renderHotbarItem(context, enabled ? (int)((float)n + 1.92F) : n, enabled ? (int)((float)o + 1.0F) : o, tickCounter, playerEntity, (ItemStack)playerEntity.getInventory().main.get(m), l++);
            if (enabled) {
               context.getMatrices().pop();
            }
         }

         if (!itemStack.isEmpty()) {
            int m = context.getScaledWindowHeight() - 16 - (enabled ? 4 : 3);
            if (arm == Arm.LEFT) {
               if (enabled) {
                  context.getMatrices().push();
                  AnimationUtil.sizeAnimation(context, (double)(i - 91 - 26 - 9), (double)m, 0.83);
               }

               this.renderHotbarItem(context, i - 91 - 26 - (enabled ? 9 : 0), m, tickCounter, playerEntity, itemStack, l++);
               if (enabled) {
                  context.getMatrices().pop();
               }
            } else {
               this.renderHotbarItem(context, i + 91 + 10, m, tickCounter, playerEntity, itemStack, l++);
            }
         }

         if (mc.options.getAttackIndicator().getValue() == AttackIndicator.HOTBAR) {
            float f = mc.player.getAttackCooldownProgress(0.0F);
            if (f < 1.0F) {
               int n = context.getScaledWindowHeight() - 20;
               int o = i + 91 + 6;
               if (arm == Arm.RIGHT) {
                  o = i - 91 - 22;
               }

               int p = (int)(f * 19.0F);
               context.drawGuiTexture(RenderLayer::getGuiTextured, HOTBAR_ATTACK_INDICATOR_BACKGROUND_TEXTURE, o, n, 18, 18);
               context.drawGuiTexture(RenderLayer::getGuiTextured, HOTBAR_ATTACK_INDICATOR_PROGRESS_TEXTURE, 18, 18, 0, 18 - p, o, n + 18 - p, 18, p);
            }
         }
      }

      ci.cancel();
   }
}
