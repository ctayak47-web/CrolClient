
package vurst.visual.utility.mixin.client;

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
import vurst.visual.client.modules.impl.render.Animation;

@Mixin(value={HandledScreen.class})
public abstract class HandledScreenAnimationMixin
extends Screen {
    @Unique
    private static final float VURSTVISUAL_ANIMATION_SPEED = 10.0f;
    @Unique
    private float vurstvisual$progress = 0.0f;
    @Unique
    private boolean vurstvisual$closing = false;
    @Unique
    private boolean vurstvisual$bypassClose = false;
    @Unique
    private boolean vurstvisual$scheduledClose = false;
    @Unique
    private boolean vurstvisual$pushed = false;
    @Unique
    private boolean vurstvisual$cursorHidden = false;

    protected HandledScreenAnimationMixin(Text title) {
        super(title);
    }

    @Unique
    private boolean vurstvisual$isActive() {
        if (!Animation.INSTANCE.isEnabled()) {
            this.vurstvisual$restoreCursor();
            return false;
        }
        return Animation.INSTANCE.shouldAnimate(this);
    }

    @Unique
    private void vurstvisual$restoreCursor() {
        if (!this.vurstvisual$cursorHidden || this.client == null) {
            return;
        }
        GLFW.glfwSetInputMode((long)this.client.getWindow().getHandle(), (int)208897, (int)212993);
        this.vurstvisual$cursorHidden = false;
    }

    @Unique
    private float vurstvisual$easeOutCubic(float value) {
        float clamped = MathHelper.clamp((float)value, (float)0.0f, (float)1.0f);
        return 1.0f - (float)Math.pow(1.0f - clamped, 3.0);
    }

    @Unique
    private float vurstvisual$easeOutBack(float value) {
        float clamped = MathHelper.clamp((float)value, (float)0.0f, (float)1.0f);
        float c1 = 1.70158f;
        float c3 = c1 + 1.0f;
        return 1.0f + c3 * (float)Math.pow(clamped - 1.0f, 3.0) + c1 * (float)Math.pow(clamped - 1.0f, 2.0);
    }

    @Unique
    private float vurstvisual$applyEasing(float raw) {
        if (this.vurstvisual$closing) {
            return this.vurstvisual$easeOutCubic(raw);
        }
        return Math.max(0.001f, this.vurstvisual$easeOutBack(raw));
    }

    @Unique
    private float vurstvisual$animate(float current, float target, float speed) {
        float factor = MathHelper.clamp((float)(speed / 90.0f), (float)0.02f, (float)0.85f);
        return MathHelper.lerp((float)factor, (float)current, (float)target);
    }

    @Unique
    private void vurstvisual$applyTransform(DrawContext context, float value) {
        float centerX = (float)this.width / 2.0f;
        float centerY = (float)this.height / 2.0f;
        float safeValue = Math.max(0.001f, value);
        context.getMatrices().translate(centerX, centerY, 0.0f);
        context.getMatrices().scale(safeValue, safeValue, 1.0f);
        context.getMatrices().translate(-centerX, -centerY, 0.0f);
    }

    @Inject(method={"render"}, at={@At(value="HEAD")}, cancellable=true)
    private void vurstvisual$onRenderPre(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        this.vurstvisual$pushed = false;
        if (this.vurstvisual$scheduledClose) {
            ci.cancel();
            return;
        }
        if (!this.vurstvisual$isActive()) {
            return;
        }
        float animationSpeed = 10.0f;
        if (this.vurstvisual$closing) {
            this.vurstvisual$progress = this.vurstvisual$animate(this.vurstvisual$progress, 0.0f, animationSpeed * 3.0f);
            if (this.vurstvisual$progress < 0.05f && !this.vurstvisual$cursorHidden && this.client != null) {
                this.vurstvisual$cursorHidden = true;
                GLFW.glfwSetInputMode((long)this.client.getWindow().getHandle(), (int)208897, (int)212994);
            }
            if (this.vurstvisual$progress < 0.005f && this.client != null) {
                this.vurstvisual$scheduledClose = true;
                this.vurstvisual$bypassClose = true;
                this.vurstvisual$restoreCursor();
                this.client.execute(() -> ((Screen)this).close());
                ci.cancel();
                return;
            }
        } else {
            this.vurstvisual$progress = Math.min(1.0f, this.vurstvisual$animate(this.vurstvisual$progress, 1.0f, animationSpeed));
            if (this.vurstvisual$progress >= 0.999f) {
                return;
            }
        }
        context.getMatrices().push();
        this.vurstvisual$pushed = true;
        this.vurstvisual$applyTransform(context, this.vurstvisual$applyEasing(this.vurstvisual$progress));
    }

    @Inject(method={"render"}, at={@At(value="TAIL")})
    private void vurstvisual$onRenderPost(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.vurstvisual$pushed) {
            context.getMatrices().pop();
            this.vurstvisual$pushed = false;
        }
    }

    @Inject(method={"close"}, at={@At(value="HEAD")}, cancellable=true)
    private void vurstvisual$onClose(CallbackInfo ci) {
        if (this.vurstvisual$bypassClose) {
            return;
        }
        if (!this.vurstvisual$isActive()) {
            return;
        }
        if (this.vurstvisual$closing || this.vurstvisual$scheduledClose) {
            ci.cancel();
            return;
        }
        if (this.vurstvisual$progress > 0.05f) {
            this.vurstvisual$closing = true;
            ci.cancel();
        }
    }
}

