package com.crolclient.mixin.render;

import com.crolclient.config.ConfigManager;
import com.crolclient.feature.render.ViewModelFeature;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.util.math.RotationAxis;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    @Inject(
        method = "renderFirstPersonItem",
        at = @At("HEAD")
    )
    private void onRenderFirstPersonItem(
        AbstractClientPlayerEntity player,
        float tickDelta,
        float pitch,
        Hand hand,
        float swingProgress,
        ItemStack item,
        float equipProgress,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        CallbackInfo ci
    ) {
        if (!ConfigManager.getConfig().viewmodelEnabled) return;

        boolean isMainHand = hand == Hand.MAIN_HAND;
        boolean isRightHand = player.getMainArm() == Arm.RIGHT;
        boolean rightHanded = isMainHand == isRightHand;

        float offsetX = rightHanded ? ViewModelFeature.getPosX() : -ViewModelFeature.getPosX();
        float offsetY = ViewModelFeature.getPosY();
        float offsetZ = ViewModelFeature.getPosZ();

        matrices.translate(offsetX, offsetY, offsetZ);

        float scale = ViewModelFeature.getScale();
        if (scale != 1.0f) {
            matrices.scale(scale, scale, scale);
        }

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(ViewModelFeature.getRotX()));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rightHanded ? ViewModelFeature.getRotY() : -ViewModelFeature.getRotY()));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rightHanded ? ViewModelFeature.getRotZ() : -ViewModelFeature.getRotZ()));
    }
}
