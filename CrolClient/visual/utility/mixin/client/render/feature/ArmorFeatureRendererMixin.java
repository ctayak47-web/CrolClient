package crol.client.utility.mixin.client.render.feature;

import net.minecraft.BipedEntityRenderState;
import net.minecraft.LivingEntityRenderState;
import net.minecraft.LivingEntity;
import net.minecraft.MatrixStack;
import net.minecraft.VertexConsumerProvider;
import net.minecraft.ArmorFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import crol.client.modules.impl.render.CustomModels;
import crol.client.modules.impl.render.NoRender;
import crol.client.utility.render.entity.ArmorTintContext;
import crol.client.utility.render.entity.RenderStateEntityCache;

@Mixin(value={ArmorFeatureRenderer.class})
public class ArmorFeatureRendererMixin {
    @Inject(method={"render"}, at={@At(value="HEAD")}, cancellable=true)
    private void removeArmor(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, BipedEntityRenderState state, float limbAngle, float limbDistance, CallbackInfo ci) {
        LivingEntity entity = RenderStateEntityCache.get((LivingEntityRenderState)state);
        ArmorTintContext.set(entity);
        if (NoRender.INSTANCE.isHideArmor() || entity != null && CustomModels.INSTANCE.shouldApplyTo(entity)) {
            ArmorTintContext.clear();
            ci.cancel();
        }
    }

    @Inject(method={"render"}, at={@At(value="TAIL")})
    private void clearArmorContext(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, BipedEntityRenderState state, float limbAngle, float limbDistance, CallbackInfo ci) {
        ArmorTintContext.clear();
    }
}
