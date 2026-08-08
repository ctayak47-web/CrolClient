package crol.client.mixins.hooks;

import crol.client.CrolClient;
import crol.client.modules.impl.render.Wings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({ElytraEntityModel.class})
public abstract class ElytraEntityModelMixin {
   @Shadow
   @Final
   private ModelPart leftWing;
   @Shadow
   @Final
   private ModelPart rightWing;

   @Inject(
      method = {"setAngles(Lnet/minecraft/client/render/entity/state/BipedEntityRenderState;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSetAngles(BipedEntityRenderState bipedEntityRenderState, CallbackInfo ci) {
      if (ItemStack.areEqual(bipedEntityRenderState.equippedChestStack, MinecraftClient.getInstance().player.getEquippedStack(EquipmentSlot.CHEST)) && CrolClient.INSTANCE.getModuleManager().getByClass(Wings.class).isEnabled()) {
         this.leftWing.pivotY = bipedEntityRenderState.isInSneakingPose ? 3.0F : 0.0F;
         this.leftWing.pitch = (float)((double)bipedEntityRenderState.leftWingPitch - ((double)0.2F + 0.6 * Wings.animationWings.getOutput()));
         this.leftWing.roll = bipedEntityRenderState.leftWingRoll - 1.4F;
         this.leftWing.yaw = bipedEntityRenderState.leftWingYaw - 0.1F;
         this.rightWing.yaw = -this.leftWing.yaw;
         this.rightWing.pivotY = this.leftWing.pivotY;
         this.rightWing.pitch = this.leftWing.pitch;
         this.rightWing.roll = -this.leftWing.roll;
         ci.cancel();
      }

   }
}
