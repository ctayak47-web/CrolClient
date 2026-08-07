
package vurst.visual.utility.mixin.client.render;

import com.darkmagician6.eventapi.EventManager;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.LivingEntityRenderState;
import net.minecraft.Entity;
import net.minecraft.LivingEntity;
import net.minecraft.RenderLayer;
import net.minecraft.Identifier;
import net.minecraft.MathHelper;
import net.minecraft.MatrixStack;
import net.minecraft.VertexConsumer;
import net.minecraft.VertexConsumerProvider;
import net.minecraft.InventoryScreen;
import net.minecraft.EntityModel;
import net.minecraft.LivingEntityRenderer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vurst.visual.VurstVisual;
import vurst.visual.base.events.impl.entity.EventEntityColor;
import vurst.visual.client.modules.impl.render.BabyModel;
import vurst.visual.client.modules.impl.render.CustomGlow;
import vurst.visual.client.modules.impl.render.CustomModelType;
import vurst.visual.client.modules.impl.render.CustomModels;
import vurst.visual.client.modules.impl.render.HitColor;
import vurst.visual.client.modules.impl.render.NameF5;
import vurst.visual.client.modules.impl.utility.EggMan;
import vurst.visual.client.modules.impl.utility.FreeLook;
import vurst.visual.utility.interfaces.IMinecraft;
import vurst.visual.utility.render.entity.CustomModelsRenderer;
import vurst.visual.utility.render.entity.EntityDamageTracker;
import vurst.visual.utility.render.entity.RenderStateEntityCache;
import vurst.visual.utility.render.level.Render3DUtil;

@Mixin(value={LivingEntityRenderer.class})
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
implements IMinecraft {
    private static final long HIT_COLOR_DURATION_MS = 350L;

    @Inject(method={"updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V"}, at={@At(value="TAIL")})
    private void storeRenderStateEntity(LivingEntity entity, LivingEntityRenderState state, float tickDelta, CallbackInfo ci) {
        RenderStateEntityCache.put(state, entity);
    }

    @Inject(method={"render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at={@At(value="HEAD")})
    private void applyEntityScale(S state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci, @Share(value="entityScaled") LocalBooleanRef entityScaled) {
        entityScaled.set(false);
        LivingEntity entity = RenderStateEntityCache.get(state);
        if (entity == null) {
            return;
        }
        boolean scaled = false;
        if (!this.isInventoryPreviewPlayer(entity) && EggMan.INSTANCE.shouldWobble(entity)) {
            matrices.push();
            EggMan.INSTANCE.applyWobbleScale(entity, matrices);
            scaled = true;
        }
        if (BabyModel.INSTANCE.shouldApplyTo((Entity)entity)) {
            if (!scaled) {
                matrices.push();
                scaled = true;
            }
            BabyModel.INSTANCE.applyModelScale(matrices);
        }
        entityScaled.set(scaled);
    }

    @Inject(method={"render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at={@At(value="RETURN")})
    private void restoreEntityScale(S state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci, @Share(value="entityScaled") LocalBooleanRef entityScaled) {
        if (entityScaled.get()) {
            matrices.pop();
        }
    }

    @ModifyExpressionValue(method={"updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/LivingEntityRenderer;clampBodyYaw(Lnet/minecraft/entity/LivingEntity;FF)F")})
    public float changeYaw(float oldValue, LivingEntity entity) {
        if (this.isInventoryPreviewPlayer(entity)) {
            return oldValue;
        }
        if (entity.equals((Object)LivingEntityRendererMixin.mc.player) && FreeLook.isActive()) {
            return FreeLook.getLockedYaw();
        }
        if (entity.equals((Object)LivingEntityRendererMixin.mc.player) && !VurstVisual.getInstance().getRotationManager().isSetRotation()) {
            return MathHelper.lerpAngleDegrees((float)Render3DUtil.getTickDelta(), (float)VurstVisual.getInstance().getRotationManager().getPreviousRotation().getYaw(), (float)VurstVisual.getInstance().getRotationManager().getCurrentRotation().getYaw());
        }
        return oldValue;
    }

    @ModifyExpressionValue(method={"updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/util/math/MathHelper;lerpAngleDegrees(FFF)F")})
    public float changeHeadYaw(float oldValue, LivingEntity entity) {
        if (this.isInventoryPreviewPlayer(entity)) {
            return oldValue;
        }
        if (entity.equals((Object)LivingEntityRendererMixin.mc.player) && FreeLook.isActive()) {
            return FreeLook.getLockedYaw();
        }
        if (entity.equals((Object)LivingEntityRendererMixin.mc.player) && !VurstVisual.getInstance().getRotationManager().isSetRotation()) {
            return MathHelper.lerpAngleDegrees((float)Render3DUtil.getTickDelta(), (float)VurstVisual.getInstance().getRotationManager().getPreviousRotation().getYaw(), (float)VurstVisual.getInstance().getRotationManager().getCurrentRotation().getYaw());
        }
        return oldValue;
    }

    @ModifyExpressionValue(method={"updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/entity/LivingEntity;getLerpedPitch(F)F")})
    public float changePitch(float oldValue, LivingEntity entity) {
        if (this.isInventoryPreviewPlayer(entity)) {
            return oldValue;
        }
        if (entity.equals((Object)LivingEntityRendererMixin.mc.player) && FreeLook.isActive()) {
            return FreeLook.getLockedPitch();
        }
        if (entity.equals((Object)LivingEntityRendererMixin.mc.player) && !VurstVisual.getInstance().getRotationManager().isSetRotation()) {
            return MathHelper.lerpAngleDegrees((float)Render3DUtil.getTickDelta(), (float)VurstVisual.getInstance().getRotationManager().getPreviousRotation().getPitch(), (float)VurstVisual.getInstance().getRotationManager().getCurrentRotation().getPitch());
        }
        return oldValue;
    }

    @Shadow
    @Nullable
    protected abstract RenderLayer getRenderLayer(LivingEntityRenderState var1, boolean var2, boolean var3, boolean var4);

    @Redirect(method={"render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/LivingEntityRenderer;getRenderLayer(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;ZZZ)Lnet/minecraft/client/render/RenderLayer;"))
    private RenderLayer renderHook(LivingEntityRenderer instance, LivingEntityRenderState state, boolean showBody, boolean translucent, boolean showOutline) {
        CustomModelType type;
        LivingEntity entity = RenderStateEntityCache.get(state);
        if (entity != null && showOutline && CustomGlow.INSTANCE.shouldApplyTo((Entity)entity)) {
            return RenderLayer.getOutline((Identifier)CustomGlow.INSTANCE.getSafeOutlineTexture());
        }
        CustomModels customModels = CustomModels.INSTANCE;
        if (entity != null && customModels.shouldApplyTo(entity) && (type = customModels.getSelectedType()) != null) {
            return showOutline ? RenderLayer.getOutline((Identifier)type.getTexture()) : RenderLayer.getEntityTranslucent((Identifier)type.getTexture());
        }
        if (!translucent && state.width == 0.6f) {
            EventEntityColor event = new EventEntityColor(-1);
            EventManager.call(event);
            if (event.isCancelled()) {
                translucent = true;
            }
        }
        return this.getRenderLayer(state, showBody, translucent, showOutline);
    }

    @Redirect(method={"render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V"))
    private void renderModelHook(EntityModel<?> instance, MatrixStack matrixStack, VertexConsumer vertexConsumer, int i, int j, int l, @Local(ordinal=0, argsOnly=true) LivingEntityRenderState renderState) {
        CustomModelType type;
        int color = l;
        LivingEntity entity = RenderStateEntityCache.get(renderState);
        HitColor hitColor = HitColor.INSTANCE;
        if (entity != null && entity != LivingEntityRendererMixin.mc.player && hitColor.isEnabled() && EntityDamageTracker.isRecentlyDamaged(entity, 350L)) {
            color = hitColor.getColor().withAlpha(255).getRGB();
        }
        EventEntityColor event = new EventEntityColor(color);
        EventManager.call(event);
        CustomModels customModels = CustomModels.INSTANCE;
        if (entity != null && customModels.shouldApplyTo(entity) && (type = customModels.getSelectedType()) != null && CustomModelsRenderer.render(type, instance, matrixStack, vertexConsumer, i, j, event.getColor())) {
            return;
        }
        instance.render(matrixStack, vertexConsumer, i, j, event.getColor());
    }

    @Inject(method={"hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z"}, at={@At(value="HEAD")}, cancellable=true)
    private void forceSelfLabel(LivingEntity entity, double distance, CallbackInfoReturnable<Boolean> cir) {
        if (entity == LivingEntityRendererMixin.mc.player && NameF5.INSTANCE.shouldShowName()) {
            cir.setReturnValue((Object)true);
        }
    }

    private boolean isInventoryPreviewPlayer(LivingEntity entity) {
        return entity == LivingEntityRendererMixin.mc.player && LivingEntityRendererMixin.mc.currentScreen instanceof InventoryScreen;
    }
}

