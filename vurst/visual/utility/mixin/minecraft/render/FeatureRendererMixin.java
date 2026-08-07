
package vurst.visual.utility.mixin.minecraft.render;

import net.minecraft.LivingEntityRenderState;
import net.minecraft.LivingEntity;
import net.minecraft.Identifier;
import net.minecraft.MinecraftClient;
import net.minecraft.FeatureRenderer;
import net.minecraft.MatrixStack;
import net.minecraft.VertexConsumerProvider;
import net.minecraft.EntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vurst.visual.client.modules.impl.render.HitColor;
import vurst.visual.utility.render.entity.EntityDamageTracker;
import vurst.visual.utility.render.entity.RenderStateEntityCache;

@Mixin(value={FeatureRenderer.class})
public abstract class FeatureRendererMixin {
    @Unique
    private static final long HIT_COLOR_DURATION_MS = 350L;
    @Unique
    private static final ThreadLocal<LivingEntity> vurstvisual$currentFeatureEntity = new ThreadLocal();

    @Inject(method={"renderModel"}, at={@At(value="HEAD")})
    private static void captureFeatureEntity(EntityModel<?> model, Identifier texture, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, LivingEntityRenderState state, int color, CallbackInfo ci) {
        vurstvisual$currentFeatureEntity.set(RenderStateEntityCache.get(state));
    }

    @Inject(method={"renderModel"}, at={@At(value="RETURN")})
    private static void clearFeatureEntity(EntityModel<?> model, Identifier texture, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, LivingEntityRenderState state, int color, CallbackInfo ci) {
        vurstvisual$currentFeatureEntity.remove();
    }

    @ModifyArg(method={"renderModel"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"), index=4)
    private static int tintFeatureModelColor(int color) {
        LivingEntity entity = vurstvisual$currentFeatureEntity.get();
        HitColor hitColor = HitColor.INSTANCE;
        if (entity != null && entity != MinecraftClient.getInstance().player && hitColor.isEnabled() && EntityDamageTracker.isRecentlyDamaged(entity, 350L) && hitColor.isFullColor()) {
            return hitColor.getColor().withAlpha(255).getRGB();
        }
        return color;
    }
}

