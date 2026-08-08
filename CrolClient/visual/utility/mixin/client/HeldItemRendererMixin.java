
package crol.client.utility.mixin.client;

import net.minecraft.Hand;
import net.minecraft.Arm;
import net.minecraft.ItemStack;
import net.minecraft.MatrixStack;
import net.minecraft.VertexConsumerProvider;
import net.minecraft.AbstractClientPlayerEntity;
import net.minecraft.HeldItemRenderer;
import net.minecraft.DataComponentTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import crol.client.modules.impl.render.ShaderHands;
import crol.client.modules.impl.render.SwingAnimation;
import crol.client.modules.impl.render.ViewModel;
import crol.client.utility.interfaces.IMinecraft;
import crol.client.utility.render.item.ShaderHandsRenderState;

@Mixin(value={HeldItemRenderer.class})
public abstract class HeldItemRendererMixin {
    @Shadow
    protected abstract void swingArm(float var1, float var2, MatrixStack var3, int var4, Arm var5);

    @Inject(method={"renderFirstPersonItem"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", ordinal=0)})
    public void injectBeforeRenderCrossBowItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        ViewModel viewModel = ViewModel.INSTANCE;
        if (viewModel.isEnabled()) {
            boolean isMainHand = hand == Hand.MAIN_HAND;
            Arm arm = isMainHand ? player.getMainArm() : player.getMainArm().getOpposite();
            viewModel.applyHandScale(matrices, arm);
        }
    }

    @Inject(method={"renderFirstPersonItem"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", ordinal=1)})
    public void injectBeforeRenderItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        ViewModel viewModel = ViewModel.INSTANCE;
        if (viewModel.isEnabled()) {
            boolean isMainHand = hand == Hand.MAIN_HAND;
            Arm arm = isMainHand ? player.getMainArm() : player.getMainArm().getOpposite();
            viewModel.applyHandScale(matrices, arm);
        }
    }

    @Inject(method={"renderFirstPersonItem"}, at={@At(value="HEAD")})
    private void beginShaderHandsTint(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        boolean applyShaderHands;
        ShaderHands shaderHands = ShaderHands.INSTANCE;
        boolean bl = applyShaderHands = shaderHands.shouldShaderHands() && !item.isEmpty() && !item.contains(DataComponentTypes.MAP_ID);
        if (!applyShaderHands) {
            return;
        }
        float brightness = shaderHands.getHandBrightness();
        float redMul = Math.min(1.0f, (float)shaderHands.getHandColor().getRed() / 255.0f * brightness);
        float greenMul = Math.min(1.0f, (float)shaderHands.getHandColor().getGreen() / 255.0f * brightness);
        float blueMul = Math.min(1.0f, (float)shaderHands.getHandColor().getBlue() / 255.0f * brightness);
        float alphaMul = shaderHands.getHandCombinedAlpha();
        ShaderHandsRenderState.begin(redMul, greenMul, blueMul, alphaMul);
    }

    @Inject(method={"renderFirstPersonItem"}, at={@At(value="RETURN")})
    private void endShaderHandsTint(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        boolean applyShaderHands;
        ShaderHands shaderHands = ShaderHands.INSTANCE;
        boolean bl = applyShaderHands = shaderHands.shouldShaderHands() && !item.isEmpty() && !item.contains(DataComponentTypes.MAP_ID);
        if (applyShaderHands) {
            ShaderHandsRenderState.end();
        }
    }

    @Inject(method={"renderFirstPersonItem"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/util/math/MatrixStack;push()V", shift=At.Shift.AFTER, ordinal=0)})
    public void injectAfterMatrixPushHandPosition(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        ViewModel viewModel = ViewModel.INSTANCE;
        if (viewModel.isEnabled() && !item.isEmpty() && !item.contains(DataComponentTypes.MAP_ID)) {
            boolean isMainHand = hand == Hand.MAIN_HAND;
            Arm arm = isMainHand ? player.getMainArm() : player.getMainArm().getOpposite();
            viewModel.applyHandPosition(matrices, arm);
        }
    }

    @Redirect(method={"renderFirstPersonItem"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/render/item/HeldItemRenderer;swingArm(FFLnet/minecraft/client/util/math/MatrixStack;ILnet/minecraft/util/Arm;)V", ordinal=2))
    public void redirectSwingArmForCustomAnim(HeldItemRenderer instance, float swingProgress, float equipProgress, MatrixStack matrices, int armX, Arm arm) {
        SwingAnimation swingAnimation = SwingAnimation.INSTANCE;
        if (swingAnimation.isEnabled() && IMinecraft.mc.player != null && arm == IMinecraft.mc.player.getMainArm()) {
            swingAnimation.renderSwordAnimation(matrices, swingProgress, equipProgress, arm);
        } else {
            this.swingArm(swingProgress, equipProgress, matrices, armX, arm);
        }
    }
}

