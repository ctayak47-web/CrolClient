package crol.client.mixins.hooks;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import crol.client.CrolClient;
import crol.client.event.classes.Render3DEvent;
import crol.client.event.classes.ShaderEvent;
import crol.client.mixins.other.IMixinGameRenderer;
import crol.client.modules.impl.util.NoRender;
import crol.client.util.render.RenderUtil3D;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL43;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static crol.client.util.IUtil.tickCounter;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer implements IMixinGameRenderer {
    @Shadow private boolean renderingPanorama;
    @Shadow private float lastFovMultiplier;
    @Shadow private float fovMultiplier;

    @Unique private SimpleFramebuffer wild$beforeHandFbo = null;
    @Unique private SimpleFramebuffer wild$withHandFbo = null;
    @Shadow @Final
    MinecraftClient client;

    public float getFov2(Camera camera, float tickDelta, boolean changingFov) {
        if (this.renderingPanorama) {
            return 90.0F;
        }

        float fov = 70.0F;
        if (changingFov) {
            fov = (float) (int) MinecraftClient.getInstance().options.getFov().getValue();
            fov *= MathHelper.lerp(tickDelta, this.lastFovMultiplier, this.fovMultiplier);
        }

        Entity focused = camera.getFocusedEntity();
        if (focused instanceof LivingEntity living) {
            if (living.isDead()) {
                float deathTime = Math.min((float) living.deathTime + tickDelta, 20.0F);
                fov /= (1.0F - 500.0F / (deathTime + 500.0F)) * 2.0F + 1.0F;
            }
        }

        CameraSubmersionType submersion = camera.getSubmersionType();
        if (submersion == CameraSubmersionType.LAVA || submersion == CameraSubmersionType.WATER) {
            float scale = ((Double) MinecraftClient.getInstance().options.getFovEffectScale().getValue()).floatValue();
            fov *= MathHelper.lerp(scale, 1.0F, 0.85714287F);
        }

        return fov;
    }


    @Inject(method = "renderWorld", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode = Opcodes.GETFIELD, ordinal = 0))
    public void hookWorldRender(RenderTickCounter tickCounter, CallbackInfo ci, @Local(ordinal = 2) Matrix4f matrix4f) {
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.multiplyPositionMatrix(matrix4f);
        matrixStack.translate(client.getEntityRenderDispatcher().camera.getPos().negate());

        CrolClient.INSTANCE.getEventManager().getWorldRenderEvent().update(tickCounter, matrixStack);
        CrolClient.INSTANCE.getEventManager().hookEvent(CrolClient.INSTANCE.getEventManager().getWorldRenderEvent());
        RenderUtil3D.getInstance().lastWorldSpaceMatrix = matrixStack.peek();
        RenderUtil3D.getInstance().onRender3D();

    }

    @Inject(
            method = "renderWorld",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z",
                    opcode = 180,
                    ordinal = 0
            )
    )
    private void render3dHook(RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;

        Camera camera = client.gameRenderer.getCamera();
        MatrixStack matrixStack = new MatrixStack();

        // Настройка матриц для 3D рендеринга (World Space)
        RenderSystem.getModelViewStack().pushMatrix().mul(matrixStack.peek().getPositionMatrix());

        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));

        Render3DEvent event = CrolClient.INSTANCE.getEventManager().getRender3DEvent();
        event.update(tickCounter, matrixStack);
        CrolClient.INSTANCE.getEventManager().hookEvent(event);

        RenderSystem.getModelViewStack().popMatrix();
    }


    @Redirect(
            method = "renderWorld",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F")
    )
    private float renderWorldHook(float delta, float first, float second) {
        NoRender noRender = (NoRender) CrolClient.INSTANCE.getModuleManager().getByClass(NoRender.class);
        if (noRender.isEnabled() && noRender.options.getValueByName("Nausea")) {
            return 0.0F;
        }
        return MathHelper.lerp(delta, first, second);
    }

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void tiltViewWhenHurtHook(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        NoRender noRender = (NoRender) CrolClient.INSTANCE.getModuleManager().getByClass(NoRender.class);
        if (noRender.isEnabled() && noRender.options.getValueByName("NoHurtCam")) {
            ci.cancel();
        }
    }

    @Inject(method = "renderHand", at = @At("HEAD"))
    private void morbid$beforeHand(Camera camera, float tickDelta, Matrix4f matrix4f, CallbackInfo ci) {
        Framebuffer main = MinecraftClient.getInstance().getFramebuffer();
        if (main.getColorAttachment() != 0) {
            this.wild$beforeHandFbo = this.morbid$ensureFbo(this.wild$beforeHandFbo, main);
            this.morbid$copyGL(main, this.wild$beforeHandFbo);
        }
    }

    @Inject(method = "renderHand", at = @At("TAIL"))
    private void morbid$afterHand(Camera camera, float tickDelta, Matrix4f matrix4f, CallbackInfo ci) {
        Framebuffer main = MinecraftClient.getInstance().getFramebuffer();
        if (main.getColorAttachment() != 0 && this.wild$beforeHandFbo != null) {
            this.wild$withHandFbo = this.morbid$ensureFbo(this.wild$withHandFbo, main);
            this.morbid$copyGL(main, this.wild$withHandFbo);
            CrolClient.INSTANCE.getEventManager().hookEvent(new ShaderEvent(this.wild$beforeHandFbo, this.wild$withHandFbo));
        }
    }

    @Unique
    private SimpleFramebuffer morbid$ensureFbo(SimpleFramebuffer fbo, Framebuffer ref) {
        if (fbo == null) {
            return new SimpleFramebuffer(ref.textureWidth, ref.textureHeight, false);
        }
        if (fbo.textureWidth != ref.textureWidth || fbo.textureHeight != ref.textureHeight) {
            fbo.resize(ref.textureWidth, ref.textureHeight);
        }
        return fbo;
    }

    @Unique
    private void morbid$copyGL(Framebuffer src, SimpleFramebuffer dst) {
        if (src.getColorAttachment() != 0 && dst.getColorAttachment() != 0) {
            GL43.glCopyImageSubData(
                    src.getColorAttachment(), 3553, 0, 0, 0, 0,
                    dst.getColorAttachment(), 3553, 0, 0, 0, 0,
                    src.textureWidth, src.textureHeight, 1
            );
        }
    }
}