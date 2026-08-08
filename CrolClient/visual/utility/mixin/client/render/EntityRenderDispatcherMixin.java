
package crol.client.utility.mixin.client.render;

import net.minecraft.Entity;
import net.minecraft.PlayerEntity;
import net.minecraft.MatrixStack;
import net.minecraft.VertexConsumer;
import net.minecraft.VertexConsumerProvider;
import net.minecraft.Frustum;
import net.minecraft.EntityRenderer;
import net.minecraft.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import crol.client.modules.impl.render.HitBoxCustomizer;
import crol.client.modules.impl.render.NoRender;
import crol.client.utility.interfaces.IMinecraft;

@Mixin(value={EntityRenderDispatcher.class})
public abstract class EntityRenderDispatcherMixin
implements IMinecraft {
    @Shadow
    public abstract <T extends Entity> EntityRenderer getRenderer(T var1);

    @Inject(method={"render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void removePlayers(Entity entity, double x, double y, double z, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (entity == null || this.getRenderer(entity) == null) {
            ci.cancel();
            return;
        }
        if (NoRender.INSTANCE.isHidePlayers() && entity instanceof PlayerEntity && entity != EntityRenderDispatcherMixin.mc.player) {
            ci.cancel();
        }
    }

    @Inject(method={"renderHitbox(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;Lnet/minecraft/entity/Entity;FFFF)V"}, at={@At(value="HEAD")}, cancellable=true)
    private static void cancelVanillaPlayerHitbox(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, float red, float green, float blue, float alpha, CallbackInfo ci) {
        if (HitBoxCustomizer.INSTANCE.isActiveForPlayer()) {
            ci.cancel();
        }
    }

    @Inject(method={"shouldRender(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/Frustum;DDD)Z"}, at={@At(value="HEAD")}, cancellable=true)
    private <T extends Entity> void preventNullRendererCrash(T entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (entity == null || this.getRenderer(entity) == null) {
            cir.setReturnValue((Object)false);
        }
    }
}

