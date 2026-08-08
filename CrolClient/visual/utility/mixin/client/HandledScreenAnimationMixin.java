
package crol.client.utility.mixin.client;

import net.minecraft.Text;
import net.minecraft.DrawContext;
import net.minecraft.MathHelper;
import net.minecraft.Screen;
import net.minecraft.HandledScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import crol.client.modules.impl.render.Animation;

@Mixin(value={HandledScreen.class})
public abstract class HandledScreenAnimationMixin
extends Screen {
    @Unique
    private static final float VURSTVISUAL_ANIMATION_SPEED = 10.0f;
    @Unique
    private float CrolClient$progress = 0.0f;
    @Unique
    private boolean CrolClient$closing = false;
    @Unique
    private boolean CrolClient$bypassClose = false;
    @Unique
    private boolean CrolClient$scheduledClose = false;
    @Unique
    private boolean CrolClient$pushed = false;
    @Unique
    private boolean CrolClient$cursorHidden = false;

    protected HandledScreenAnimationMixin(Text title) {
        super(title);
    }

    @Unique
    private boolean CrolClient$isActive() {
        if (!Animation.INSTANCE.isEnabled()) {
            this.CrolClient$restoreCursor();
            return false;
        }
        return Animation.INSTANCE.shouldAnimate(this);
    }

    @Unique
    private void CrolClient$restoreCursor() {
        if (!this.CrolClient$cursorHidden || this.client == null) {
            return;
        }
        GLFW.glfwSetInputMode((long)this.client.getWindow().getHandle(), (int)208897, (int)212993);
        this.CrolClient$cursorHidden = false;
    }

    @Unique
    private float CrolClient$easeOutCubic(float value) {
        float clamped = MathHelper.clamp((float)value, (float)0.0f, (float)1.0f);
        return 1.0f - (float)Math.pow(1.0f - clamped, 3.0);
    }

    @Unique
    private float CrolClient$easeOutBack(float value) {
        float clamped = MathHelper.clamp((float)value, (float)0.0f, (float)1.0f);
        float c1 = 1.70158f;
        float c3 = c1 + 1.0f;
        return 1.0f + c3 * (float)Math.pow(clamped - 1.0f, 3.0) + c1 * (float)Math.pow(clamped - 1.0f, 2.0);
    }

    @Unique
    private float CrolClient$applyEasing(float raw) {
        if (this.CrolClient$closing) {
            return this.CrolClient$easeOutCubic(raw);
        }
        return Math.max(0.001f, this.CrolClient$easeOutBack(raw));
    }

    @Unique
    private float CrolClient$animate(float current, float target, float speed) {
        float factor = MathHelper.clamp((float)(speed / 90.0f), (float)0.02f, (float)0.85f);
        return MathHelper.lerp((float)factor, (float)current, (float)target);
    }

    @Unique
    private void CrolClient$applyTransform(DrawContext context, float value) {
        float centerX = (float)this.width / 2.0f;
        float centerY = (float)this.height / 2.0f;
        float safeValue = Math.max(0.001f, value);
        context.getMatrices().translate(centerX, centerY, 0.0f);
        context.getMatrices().scale(safeValue, safeValue, 1.0f);
        context.getMatrices().translate(-centerX, -centerY, 0.0f);
    }

    @Inject(method={"render"}, at={@At(value="HEAD")}, cancellable=true)
    private void CrolClient$onRenderPre(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        this.CrolClient$pushed = false;
        if (this.CrolClient$scheduledClose) {
            ci.cancel();
            return;
        }
        if (!this.CrolClient$isActive()) {
            return;
        }
        float animationSpeed = 10.0f;
        if (this.CrolClient$closing) {
            this.CrolClient$progress = this.CrolClient$animate(this.CrolClient$progress, 0.0f, animationSpeed * 3.0f);
            if (this.CrolClient$progress < 0.05f && !this.CrolClient$cursorHidden && this.client != null) {
                this.CrolClient$cursorHidden = true;
                GLFW.glfwSetInputMode((long)this.client.getWindow().getHandle(), (int)208897, (int)212994);
            }
            if (this.CrolClient$progress < 0.005f && this.client != null) {
                this.CrolClient$scheduledClose = true;
                this.CrolClient$bypassClose = true;
                this.CrolClient$restoreCursor();
                this.client.execute(() -> ((Screen)this).close());
                ci.cancel();
                return;
            }
        } else {
            this.CrolClient$progress = Math.min(1.0f, this.CrolClient$animate(this.CrolClient$progress, 1.0f, animationSpeed));
            if (this.CrolClient$progress >= 0.999f) {
                return;
            }
        }
        context.getMatrices().push();
        this.CrolClient$pushed = true;
        this.CrolClient$applyTransform(context, this.CrolClient$applyEasing(this.CrolClient$progress));
    }

    @Inject(method={"render"}, at={@At(value="TAIL")})
    private void CrolClient$onRenderPost(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.CrolClient$pushed) {
            context.getMatrices().pop();
            this.CrolClient$pushed = false;
        }
    }

    @Inject(method={"close"}, at={@At(value="HEAD")}, cancellable=true)
    private void CrolClient$onClose(CallbackInfo ci) {
        if (this.CrolClient$bypassClose) {
            return;
        }
        if (!this.CrolClient$isActive()) {
            return;
        }
        if (this.CrolClient$closing || this.CrolClient$scheduledClose) {
            ci.cancel();
            return;
        }
        if (this.CrolClient$progress > 0.05f) {
            this.CrolClient$closing = true;
            ci.cancel();
        }
    }
}

