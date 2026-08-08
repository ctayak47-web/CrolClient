package crol.client.mixins.hooks;

import crol.client.CrolClient;
import crol.client.modules.impl.render.ItemPhysics;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ItemEntityRenderer.class)
public class ItemEntityRendererMixin {

    // ОПТИМИЗАЦИЯ: Кэшируем модуль, чтобы не искать его в списке каждый кадр для каждого предмета
    @Unique
    private ItemPhysics itemPhysicsModule;

    @Unique
    private ItemPhysics getItemPhysics() {
        if (this.itemPhysicsModule == null) {
            this.itemPhysicsModule = (ItemPhysics) CrolClient.INSTANCE.getModuleManager().getByClass(ItemPhysics.class);
        }
        return this.itemPhysicsModule;
    }

    /**
     * Отменяет ванильное вертикальное вращение предмета (originalRotation обнуляется).
     */
    @ModifyVariable(
            method = "render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/math/MatrixStack;multiply(Lorg/joml/Quaternionf;)V",
                    shift = At.Shift.AFTER
            ),
            ordinal = 0
    )
    private float modifyRotation(float originalRotation, ItemEntityRenderState state) {
        ItemPhysics physics = getItemPhysics();
        return (physics != null && physics.isEnabled()) ? 0.0F : originalRotation;
    }

    /**
     * Применяет кастомное вращение: кладет предмет плашмя и заставляет его крутиться по оси Z.
     */
    @Inject(
            method = "render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/item/ItemRenderer;renderItem(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/item/ItemRenderState;Lnet/minecraft/util/math/random/Random;)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void applyCustomRotation(ItemEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        ItemPhysics physics = getItemPhysics();
        if (physics != null && physics.isEnabled()) {
            float rotation = ItemEntity.getRotation(state.age, state.uniqueOffset);

            // Наклоняем матрицу на 85 градусов, чтобы предмет лежал "плашмя" на земле
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(85.0F));

            // Применяем замедленное (0.3F) вращение вокруг своей оси
            matrices.multiply(RotationAxis.POSITIVE_Z.rotation(rotation * 0.3F));
        }
    }
}