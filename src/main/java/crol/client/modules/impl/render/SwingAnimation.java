package crol.client.modules.impl.render;

import crol.client.CrolClient;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.impl.combat.Aura;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.BooleanSetting;
import crol.client.modules.settings.impl.FloatSetting;
import crol.client.modules.settings.impl.ModeSetting;
import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class SwingAnimation extends Module implements IUtil {
   private final ModeSetting mode = ((ModeSetting)(new ModeSetting()).name("Mode")).value("Default").modes("Default", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight");
   public final FloatSetting slow = ((FloatSetting)(new FloatSetting() {
      public boolean isVisible() {
         return !SwingAnimation.this.mode.is("Six") && !SwingAnimation.this.mode.is("Seven");
      }
   }).name("Slow")).minValue(5.0F).maxValue(25.0F).value(5.0F).incriment(1.0F);
   private final BooleanSetting onlyaura = ((BooleanSetting)(new BooleanSetting()).name("OnlyAura")).value(false);
   private final FloatSetting positionMainX = ((FloatSetting)(new FloatSetting()).name("MainX")).minValue(-2.0F).maxValue(2.0F).incriment(0.01F).value(0.16F);
   private final FloatSetting positionMainY = ((FloatSetting)(new FloatSetting()).name("MainY")).minValue(-2.0F).maxValue(2.0F).incriment(0.01F).value(0.16F);
   private final FloatSetting positionMainZ = ((FloatSetting)(new FloatSetting()).name("MainZ")).minValue(-2.0F).maxValue(2.0F).incriment(0.01F).value(0.16F);
   private final FloatSetting positionOffX = ((FloatSetting)(new FloatSetting()).name("OffX")).minValue(-2.0F).maxValue(2.0F).incriment(0.01F).value(0.16F);
   private final FloatSetting positionOffY = ((FloatSetting)(new FloatSetting()).name("OffY")).minValue(-2.0F).maxValue(2.0F).incriment(0.01F).value(0.16F);
   private final FloatSetting positionOffZ = ((FloatSetting)(new FloatSetting()).name("OffZ")).minValue(-2.0F).maxValue(2.0F).incriment(0.01F).value(0.16F);
   private float r = 0.0F;

   public SwingAnimation() {
      super(new ModuleInfo("SwingAnimation", Category.RENDER, "Изменяет положение рук и их анимацию"));
      this.addSetting(new Setting[]{this.mode, this.slow, this.onlyaura, this.positionMainX, this.positionMainY, this.positionMainZ, this.positionOffX, this.positionOffY, this.positionOffZ});
   }

   public boolean shouldAnimate() {
      return this.isEnabled() && (!this.onlyaura.getValue() || CrolClient.INSTANCE.getModuleManager().getByClass(Aura.class).isEnabled());
   }

   public void renderSwordAnimation(MatrixStack matrices, float f, float swingProgress, float equipProgress, Arm arm) {
      if (this.shouldAnimate() && mc.player.getMainHandStack().getItem() != Items.AIR) {
         switch (this.mode.getValue()) {
            case "Default":
               this.applyEquipOffset(matrices, arm, equipProgress);
               this.translateToViewModelOff(matrices);
               this.applySwingOffset(matrices, arm, swingProgress);
               this.translateBacklOff(matrices);
               break;
            case "One":
               this.applyEquipOffset(matrices, arm, 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * ((float)Math.PI * 2F)));
               break;
            case "Two":
               if (arm == Arm.RIGHT) {
                  int i = arm == Arm.RIGHT ? 1 : -1;
                  float g = MathHelper.sin((float)Math.PI);
                  this.applyEquipOffset(matrices, arm, 0.05F);
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * (45.0F + f * -30.0F)));
                  matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)i * g * -10.0F));
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-75.0F));
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * -105.0F));
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(15.0F));
               } else {
                  this.applyEquipOffset(matrices, arm, 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * ((float)Math.PI * 2F)));
               }
               break;
            case "Three":
               if (arm == Arm.RIGHT) {
                  int i = arm == Arm.RIGHT ? 1 : -1;
                  float g = MathHelper.sin(f * (float)Math.PI);
                  this.applyEquipOffset(matrices, arm, 0.05F);
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-35.0F * f));
               } else {
                  this.applyEquipOffset(matrices, arm, 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * ((float)Math.PI * 2F)));
               }
               break;
            case "Four":
               if (arm == Arm.RIGHT) {
                  int i = arm == Arm.RIGHT ? 1 : -1;
                  this.applyEquipOffset(matrices, arm, 0.0F);
                  this.applySwingOffset(matrices, arm, swingProgress);
               } else {
                  this.applyEquipOffset(matrices, arm, 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * ((float)Math.PI * 2F)));
               }
               break;
            case "Five":
               if (arm == Arm.RIGHT) {
                  int i = arm == Arm.RIGHT ? 1 : -1;
                  float g = MathHelper.sin((float)Math.PI);
                  this.applyEquipOffset(matrices, arm, 0.05F);
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * (45.0F + f * -30.0F)));
                  matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)i * g * -10.0F));
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-75.0F));
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * -105.0F));
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(15.0F));
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-15.0F * f));
                  matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(15.0F * f));
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(30.0F * f));
               } else {
                  this.applyEquipOffset(matrices, arm, 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * ((float)Math.PI * 2F)));
               }
               break;
            case "Six":
               if (arm == Arm.RIGHT) {
                  int i = 1;
                  float ease = 1.0F - (1.0F - f) * (1.0F - f);
                  this.applyEquipOffset(matrices, arm, 0.04F);
                  matrices.translate(0.0F, -0.1F * ease, 0.0F);
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-10.0F + ease * -70.0F));
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * (20.0F - ease * 35.0F)));
                  matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)i * ease * -8.0F));
               } else {
                  this.applyEquipOffset(matrices, arm, 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * ((float)Math.PI * 2F)));
               }
               break;
            case "Seven":
               if (arm == Arm.RIGHT) {
                  float arch = MathHelper.sin(f * (float)Math.PI);
                  this.applyEquipOffset(matrices, arm, 0.02F);
                  matrices.translate(0.0F, arch * 0.04F, -arch * 0.2F);
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-arch * 25.0F));
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(arch * 5.0F));
               } else {
                  this.applyEquipOffset(matrices, arm, 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * ((float)Math.PI * 2F)));
               }
               break;
            case "Eight":
               if (arm == Arm.RIGHT) {
                  float arch = MathHelper.sin(f * (float)Math.PI);
                  this.applyEquipOffset(matrices, arm, 0.05F);
                  this.applySwingOffset(matrices, arm, swingProgress);
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(360.0F * this.r));
                  this.r -= 0.001F;
                  if (this.r <= 0.0F) {
                     this.r = 1.0F;
                  }
               } else {
                  this.applyEquipOffset(matrices, arm, 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * ((float)Math.PI * 2F)));
               }
         }
      } else {
         this.applyEquipOffset(matrices, arm, equipProgress);
         this.translateToViewModelOff(matrices);
         this.applySwingOffset(matrices, arm, swingProgress);
         this.translateBacklOff(matrices);
      }

   }

   private void applyBrushTransformation(MatrixStack matrices, float tickDelta, Arm arm, @NotNull ItemStack stack, float equipProgress) {
      this.applyEquipOffset(matrices, arm, equipProgress);
      float f = (float)mc.player.getItemUseTimeLeft() - tickDelta + 1.0F;
      float g = 1.0F - f / (float)stack.getMaxUseTime(mc.player);
      float m = -15.0F + 75.0F * MathHelper.cos(g * 45.0F * (float)Math.PI);
      if (arm != Arm.RIGHT) {
         matrices.translate(0.1, 0.83, 0.35);
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80.0F));
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90.0F));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(m));
         matrices.translate(-0.3, 0.22, 0.35);
      } else {
         matrices.translate((double)-0.25F, 0.22, 0.35);
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80.0F));
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0F));
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(0.0F));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(m));
      }

   }

   private void applyEquipOffset(@NotNull MatrixStack matrices, Arm arm, float equipProgress) {
      int i = arm == Arm.RIGHT ? 1 : -1;
      matrices.translate((float)i * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
   }

   private void applySwingOffset(@NotNull MatrixStack matrices, Arm arm, float swingProgress) {
      int i = arm == Arm.RIGHT ? 1 : -1;
      float f = MathHelper.sin(swingProgress * swingProgress * (float)Math.PI);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * (45.0F + f * -20.0F)));
      float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float)Math.PI);
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)i * g * -20.0F));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -80.0F));
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * -45.0F));
   }

   public void renderFirstPersonItemCustom(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      if (!player.isUsingSpyglass()) {
         boolean bl = hand == Hand.MAIN_HAND;
         Arm arm = bl ? player.getMainArm() : player.getMainArm().getOpposite();
         matrices.push();
         float f = 0.0F;
         if (item.isOf(Items.CROSSBOW)) {
            boolean bl2 = CrossbowItem.isCharged(item);
            boolean bl3 = arm == Arm.RIGHT;
            int i = bl3 ? 1 : -1;
            if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
               this.applyEquipOffset(matrices, arm, equipProgress);
               matrices.translate((float)i * -0.4785682F, -0.094387F, 0.05731531F);
               matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-11.935F));
               matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * 65.3F));
               matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)i * -9.785F));
               f = (float)item.getMaxUseTime(mc.player) - ((float)mc.player.getItemUseTimeLeft() - tickDelta + 1.0F);
               float g = f / (float)CrossbowItem.getPullTime(item, mc.player);
               if (g > 1.0F) {
                  g = 1.0F;
               }

               if (g > 0.1F) {
                  float h = MathHelper.sin((f - 0.1F) * 1.3F);
                  float j = g - 0.1F;
                  float k = h * j;
                  matrices.translate(k * 0.0F, k * 0.004F, k * 0.0F);
               }

               matrices.translate(g * 0.0F, g * 0.0F, g * 0.04F);
               matrices.scale(1.0F, 1.0F, 1.0F + g * 0.2F);
               matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)i * 45.0F));
            } else {
               f = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float)Math.PI);
               float g = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * ((float)Math.PI * 2F));
               float h = -0.2F * MathHelper.sin(swingProgress * (float)Math.PI);
               matrices.translate((float)i * f, g, h);
               this.applyEquipOffset(matrices, arm, equipProgress);
               this.applySwingOffset(matrices, arm, swingProgress);
               if (bl2 && swingProgress < 0.001F && bl) {
                  matrices.translate((float)i * -0.641864F, 0.0F, 0.0F);
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * 10.0F));
               }
            }

            this.renderItem(player, item, bl3 ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND, !bl3, matrices, vertexConsumers, light);
         } else {
            boolean bl2 = arm == Arm.RIGHT;
            float m = 0.0F;
            if (bl2 && mc.player.getMainHandStack().getItem() != Items.AIR) {
               matrices.translate(this.positionMainX.getValue(), this.positionMainY.getValue(), this.positionMainZ.getValue());
            } else if (mc.player.getOffHandStack().getItem() != Items.AIR) {
               matrices.translate(this.positionOffX.getValue(), this.positionOffY.getValue(), this.positionOffZ.getValue());
            }

            if (player.isUsingItem() && player.getItemUseTimeLeft() > 0 && player.getActiveHand() == hand) {
               int l = bl2 ? 1 : -1;
               switch (item.getUseAction()) {
                  case NONE:
                  case BLOCK:
                     this.applyEquipOffset(matrices, arm, equipProgress);
                     break;
                  case EAT:
                  case DRINK:
                     this.applyEatOrDrinkTransformationCustom(matrices, tickDelta, arm, item);
                     this.applyEquipOffset(matrices, arm, equipProgress);
                     break;
                  case BOW:
                     this.applyEquipOffset(matrices, arm, equipProgress);
                     matrices.translate((float)l * -0.2785682F, 0.18344387F, 0.15731531F);
                     matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-13.935F));
                     matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)l * 35.3F));
                     matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)l * -9.785F));
                     m = (float)item.getMaxUseTime(mc.player) - ((float)mc.player.getItemUseTimeLeft() - tickDelta + 1.0F);
                     f = m / 20.0F;
                     f = (f * f + f * 2.0F) / 3.0F;
                     if (f > 1.0F) {
                        f = 1.0F;
                     }

                     if (f > 0.1F) {
                        float g = MathHelper.sin((m - 0.1F) * 1.3F);
                        float h = f - 0.1F;
                        float j = g * h;
                        matrices.translate(j * 0.0F, j * 0.004F, j * 0.0F);
                     }

                     matrices.translate(f * 0.0F, f * 0.0F, f * 0.04F);
                     matrices.scale(1.0F, 1.0F, 1.0F + f * 0.2F);
                     matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)l * 45.0F));
                     break;
                  case SPEAR:
                     this.applyEquipOffset(matrices, arm, equipProgress);
                     matrices.translate((float)l * -0.5F, 0.7F, 0.1F);
                     matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-55.0F));
                     matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)l * 35.3F));
                     matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)l * -9.785F));
                     m = (float)item.getMaxUseTime(mc.player) - ((float)mc.player.getItemUseTimeLeft() - tickDelta + 1.0F);
                     f = m / 10.0F;
                     if (f > 1.0F) {
                        f = 1.0F;
                     }

                     if (f > 0.1F) {
                        float g = MathHelper.sin((m - 0.1F) * 1.3F);
                        float h = f - 0.1F;
                        float j = g * h;
                        matrices.translate(j * 0.0F, j * 0.004F, j * 0.0F);
                     }

                     matrices.translate(0.0F, 0.0F, f * 0.2F);
                     matrices.scale(1.0F, 1.0F, 1.0F + f * 0.2F);
                     matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees((float)l * 45.0F));
                     break;
                  case BRUSH:
                     this.applyBrushTransformation(matrices, tickDelta, arm, item, equipProgress);
               }
            } else if (player.isUsingRiptide()) {
               this.applyEquipOffset(matrices, arm, equipProgress);
               int l = bl2 ? 1 : -1;
               matrices.translate((float)l * -0.4F, 0.8F, 0.3F);
               matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)l * 65.0F));
               matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)l * -85.0F));
            } else {
               float anim = (float)Math.sin((double)swingProgress * (Math.PI / 2D) * (double)2.0F);
               this.renderSwordAnimation(matrices, anim, swingProgress, equipProgress, arm);
            }

            this.renderItem(player, item, bl2 ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND, !bl2, matrices, vertexConsumers, light);
         }

         matrices.pop();
      }

   }

   private void applyEatOrDrinkTransformationCustom(MatrixStack matrices, float tickDelta, Arm arm, @NotNull ItemStack stack) {
      float f = (float)mc.player.getItemUseTimeLeft() - tickDelta + 1.0F;
      float g = f / (float)stack.getMaxUseTime(mc.player);
      if (g < 0.8F) {
         float h = MathHelper.abs(MathHelper.cos(f / 4.0F * (float)Math.PI) * 0.005F);
         matrices.translate(0.0F, h, 0.0F);
      }

      float h = 1.0F - (float)Math.pow((double)g, (double)27.0F);
      int i = arm == Arm.RIGHT ? 1 : -1;
      matrices.translate(h * 0.6F * (float)i * 1.0F, h * -0.5F * 1.0F, h * 0.0F);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * h * 90.0F));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(h * 10.0F));
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)i * h * 30.0F));
   }

   public void renderItem(LivingEntity entity, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
      if (!stack.isEmpty()) {
         mc.getItemRenderer().renderItem(entity, stack, renderMode, leftHanded, matrices, vertexConsumers, entity.getWorld(), light, OverlayTexture.DEFAULT_UV, entity.getId() + renderMode.ordinal());
      }
   }

   private void translateToViewModel(MatrixStack matrices) {
      matrices.translate(this.positionMainX.getValue(), this.positionMainY.getValue(), this.positionMainZ.getValue());
   }

   private void translateToViewModelOff(MatrixStack matrices) {
      matrices.translate(-this.positionMainX.getValue(), this.positionMainY.getValue(), this.positionMainZ.getValue());
   }

   private void translateBack(MatrixStack matrices) {
      matrices.translate(-this.positionMainX.getValue(), -this.positionMainY.getValue(), -this.positionMainZ.getValue());
   }

   private void translateBacklOff(MatrixStack matrices) {
      matrices.translate(this.positionMainX.getValue(), -this.positionMainY.getValue(), -this.positionMainZ.getValue());
   }
}
