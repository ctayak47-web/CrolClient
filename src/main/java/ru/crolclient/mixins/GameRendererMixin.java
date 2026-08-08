package ru.crolclient.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.crolclient.api.event.EventManager;
import ru.crolclient.core.Extra;
import ru.crolclient.implement.events.render.WorldRenderEvent;
import ru.crolclient.implement.features.modules.render.AspectRatioModule;
import ru.crolclient.implement.features.modules.render.ClearRenderModule;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow
    @Final
    MinecraftClient client;
    @Shadow
    private float zoom;

    @Shadow
    private float zoomX;
    @Shadow
    @Final
    private Camera camera;
    @Shadow
    private float zoomY;
    @Shadow
    private float viewDistance;

    @Inject(method = "getBasicProjectionMatrix", at = @At("TAIL"), cancellable = true)
    public void getBasicProjectionMatrixHook(float fov, CallbackInfoReturnable<Matrix4f> cir) {
        AspectRatioModule aspectRatioModule = (AspectRatioModule) Extra.getInstance().getModuleProvider().module("AspectRatio");
        if (aspectRatioModule != null && aspectRatioModule.isState()) {
            aspectRatioModule.updateAspectRatio();

            MatrixStack matrixStack = new MatrixStack();
            matrixStack.peek().getPositionMatrix().identity();
            if (zoom != 1.0f) {
                matrixStack.translate(zoomX, -zoomY, 0.0f);
                matrixStack.scale(zoom, zoom, 1.0f);
            }
            matrixStack.peek().getPositionMatrix().mul(new Matrix4f().setPerspective((float) (fov * 0.01745329238474369), aspectRatioModule.getRatio(), 0.05f, viewDistance * 4.0f));
            cir.setReturnValue(matrixStack.peek().getPositionMatrix());
        }
    }

    @Inject(method = "renderWorld", at = @At("RETURN"))
    public void renderWorld(RenderTickCounter tickCounter, CallbackInfo ci) {
        float tickDelta = tickCounter.getTickDelta(false);
        MatrixStack matrices = new MatrixStack();
        EventManager.callEvent(new WorldRenderEvent(matrices, tickDelta));
    }

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    public void bobViewWhenHurt(MatrixStack matrixStack, float float_1, CallbackInfo ci) {
        ClearRenderModule clearRenderModule = (ClearRenderModule) Extra.getInstance().getModuleProvider().module("ClearRender");
        if (clearRenderModule != null && clearRenderModule.isState() && clearRenderModule.getClearRenderSettings().isSelected("HurtCam")) {
            ci.cancel();
        }
    }
}
