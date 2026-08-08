
package crol.client.utility.mixin.client.render;

import net.minecraft.ItemEntityRenderState;
import net.minecraft.ItemEntity;
import net.minecraft.MatrixStack;
import net.minecraft.VertexConsumerProvider;
import net.minecraft.RotationAxis;
import net.minecraft.ItemEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import crol.client.modules.impl.utility.ItemPhysics;
import crol.client.utility.ext.ItemEntityRenderStateExt;

@Mixin(value={ItemEntityRenderer.class})
public abstract class ItemEntityRendererMixin {
    @Inject(method={"updateRenderState(Lnet/minecraft/entity/ItemEntity;Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;F)V"}, at={@At(value="TAIL")})
    private void vv$updatePhysicsState(ItemEntity entity, ItemEntityRenderState state, float tickDelta, CallbackInfo ci) {
        ItemEntityRenderStateExt ext = (ItemEntityRenderStateExt)state;
        if (!ItemPhysics.INSTANCE.isEnabled() || entity == null) {
            ext.vv$setGrounded(false);
            return;
        }
        boolean grounded = !entity.isTouchingWater() && !entity.isSubmergedInWater() && !entity.isInLava();
        ext.vv$setGrounded(grounded);
        ext.vv$setGroundRoll((float)entity.getId() * 31.0f % 360.0f);
        if (grounded) {
            state.age = 0.0f;
            state.uniqueOffset = 0.0f;
        }
    }

    @Inject(method={"render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/ItemEntityRenderer;renderStack(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/ItemStackEntityRenderState;Lnet/minecraft/util/math/random/Random;)V", shift=At.Shift.BEFORE)})
    private void vv$applyGroundTransform(ItemEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (!ItemPhysics.INSTANCE.isEnabled()) {
            return;
        }
        ItemEntityRenderStateExt ext = (ItemEntityRenderStateExt)state;
        if (!ext.vv$isGrounded()) {
            return;
        }
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(ext.vv$getGroundRoll()));
    }
}

