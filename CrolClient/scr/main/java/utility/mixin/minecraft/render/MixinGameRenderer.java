
package crol.client.utility.mixin.minecraft.render;

import com.darkmagician6.eventapi.EventManager;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ShaderProgramKeys;
import net.minecraft.ShaderProgramKey;
import net.minecraft.Window;
import net.minecraft.LivingEntity;
import net.minecraft.ItemStack;
import net.minecraft.DrawContext;
import net.minecraft.Profiler;
import net.minecraft.MatrixStack;
import net.minecraft.GameRenderer;
import net.minecraft.RenderTickCounter;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import crol.client.base.events.impl.render.EventAspectRatio;
import crol.client.base.events.impl.render.EventFov;
import crol.client.base.events.impl.render.EventRender3D;
import crol.client.base.events.impl.render.EventRenderScreen;
import crol.client.modules.impl.render.FullBright;
import crol.client.modules.impl.render.NoRender;
import crol.client.utility.interfaces.IMinecraft;
import crol.client.utility.render.display.base.UIContext;
import crol.client.utility.render.level.Render3DUtil;

@Mixin(value={GameRenderer.class})
public abstract class MixinGameRenderer {
    @Shadow
    private float zoom;
    @Shadow
    private float zoomX;
    @Shadow
    private float zoomY;

    @Shadow
    public abstract float getFarPlaneDistance();

    @Inject(method={"getBasicProjectionMatrix"}, at={@At(value="TAIL")}, cancellable=true)
    public void getBasicProjectionMatrixHook(float fovDegrees, CallbackInfoReturnable<Matrix4f> cir) {
        EventAspectRatio eventAspectRatio = new EventAspectRatio();
        EventManager.call(eventAspectRatio);
        if (eventAspectRatio.isCancelled()) {
            Matrix4f original = (Matrix4f)cir.getReturnValue();
            float targetRatio = eventAspectRatio.getRatio();
            if (original == null || targetRatio <= 0.0f || IMinecraft.mc == null || IMinecraft.mc.getWindow() == null) {
                return;
            }
            float actualRatio = (float)IMinecraft.mc.getWindow().getFramebufferWidth() / (float)Math.max(1, IMinecraft.mc.getWindow().getFramebufferHeight());
            if (actualRatio <= 0.0f) {
                return;
            }
            Matrix4f adjusted = new Matrix4f((Matrix4fc)original);
            adjusted.m00(adjusted.m00() * (actualRatio / targetRatio));
            cir.setReturnValue((Object)adjusted);
        }
    }

    @Inject(method={"tiltViewWhenHurt"}, at={@At(value="HEAD")}, cancellable=true)
    private void removeHurtCamera(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (NoRender.INSTANCE.isRemoveHurtCamera()) {
            ci.cancel();
        }
    }

    @Inject(method={"showFloatingItem"}, at={@At(value="HEAD")}, cancellable=true)
    private void removeTotemAnimation(ItemStack floatingItem, CallbackInfo ci) {
        if (NoRender.INSTANCE.isRemoveTotemAnimation()) {
            ci.cancel();
        }
    }

    @Inject(method={"getNightVisionStrength"}, at={@At(value="HEAD")}, cancellable=true)
    private static void CrolClient$fullBrightAsNightVision(LivingEntity entity, float tickDelta, CallbackInfoReturnable<Float> cir) {
        if (entity == IMinecraft.mc.player && FullBright.INSTANCE.shouldForceNightVisionStrength()) {
            cir.setReturnValue((Object)Float.valueOf(1.0f));
        }
    }

    @ModifyExpressionValue(method={"getFov"}, at={@At(value="INVOKE", target="Ljava/lang/Integer;intValue()I", remap=false)})
    private int hookGetFov(int original) {
        EventFov event = new EventFov();
        event.setFov(original);
        EventManager.call(event);
        if (event.isCancelled()) {
            return event.getFov();
        }
        return original;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Inject(method={"renderWorld"}, at={@At(value="FIELD", target="Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode=180, ordinal=0)})
    public void hookWorldRender(RenderTickCounter tickCounter, CallbackInfo ci, @Local(ordinal=2) Matrix4f matrix4f) {
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.multiplyPositionMatrix(matrix4f);
        Render3DUtil.setLastProjMat(RenderSystem.getProjectionMatrix());
        Render3DUtil.setLastModMat(RenderSystem.getModelViewMatrix());
        Render3DUtil.setLastWorldSpaceMatrix(matrix4f);
        try {
            EventRender3D event = new EventRender3D(matrixStack, tickCounter.getTickDelta(false));
            EventManager.call(event);
            Render3DUtil.onEventRender3D(event.getMatrix());
        }
        finally {
            MixinGameRenderer.resetRenderState();
        }
    }

    @Inject(method={"renderWorld"}, at={@At(value="HEAD")})
    private void hookRenderWorldHead(RenderTickCounter tickCounter, CallbackInfo ci) {
        MixinGameRenderer.resetRenderState();
    }

    @Inject(method={"render"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/screen/Screen;renderWithTooltip(Lnet/minecraft/client/gui/DrawContext;IIF)V", shift=At.Shift.BEFORE)})
    private void hookBeforeAnyScreenRender(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        MixinGameRenderer.prepareGuiRenderState();
    }

    @Inject(method={"render"}, at={@At(value="TAIL")})
    private void hookRenderTail(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        MixinGameRenderer.resetRenderState();
    }

    private static void resetRenderState() {
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.colorMask((boolean)true, (boolean)true, (boolean)true, (boolean)true);
        RenderSystem.disableScissor();
        if (IMinecraft.mc != null && IMinecraft.mc.getWindow() != null) {
            RenderSystem.viewport((int)0, (int)0, (int)IMinecraft.mc.getWindow().getFramebufferWidth(), (int)IMinecraft.mc.getWindow().getFramebufferHeight());
        }
        RenderSystem.lineWidth((float)1.0f);
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private static void prepareGuiRenderState() {
        if (IMinecraft.mc != null && IMinecraft.mc.getFramebuffer() != null) {
            IMinecraft.mc.getFramebuffer().beginWrite(false);
        }
        RenderSystem.disableScissor();
        if (IMinecraft.mc != null && IMinecraft.mc.getWindow() != null) {
            RenderSystem.viewport((int)0, (int)0, (int)IMinecraft.mc.getWindow().getFramebufferWidth(), (int)IMinecraft.mc.getWindow().getFramebufferHeight());
        }
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.colorMask((boolean)true, (boolean)true, (boolean)true, (boolean)true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask((boolean)false);
    }

    @Inject(method={"render"}, at={@At(value="FIELD", target="Lnet/minecraft/client/MinecraftClient;world:Lnet/minecraft/client/world/ClientWorld;", opcode=180, ordinal=2)}, locals=LocalCapture.CAPTURE_FAILHARD)
    private void renderScreenHook(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci, Profiler profiler, boolean bl, int i, int j, Window window, Matrix4f matrix4f, Matrix4fStack matrix4fStack, DrawContext drawContext) {
        EventManager.call(new EventRenderScreen(UIContext.of(drawContext, i, j, IMinecraft.mc.getRenderTickCounter().getTickDelta(false))));
    }
}

