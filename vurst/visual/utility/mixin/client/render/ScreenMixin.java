
package vurst.visual.utility.mixin.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ShaderProgramKeys;
import net.minecraft.ShaderProgramKey;
import net.minecraft.MinecraftClient;
import net.minecraft.DrawContext;
import net.minecraft.ChatScreen;
import net.minecraft.GameMenuScreen;
import net.minecraft.Screen;
import net.minecraft.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Screen.class})
public class ScreenMixin {
    @Inject(method={"renderBackground(Lnet/minecraft/client/gui/DrawContext;IIF)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void vurstvisual$resetRenderStateBeforeBackground(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.shouldSkipDarkening()) {
            ScreenMixin.applyGuiState();
            ci.cancel();
        }
    }

    @Inject(method={"renderInGameBackground(Lnet/minecraft/client/gui/DrawContext;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void vurstvisual$resetRenderStateBeforeInGameBackground(DrawContext context, CallbackInfo ci) {
        if (this.shouldSkipDarkening()) {
            ScreenMixin.applyGuiState();
            ci.cancel();
        }
    }

    @Inject(method={"render(Lnet/minecraft/client/gui/DrawContext;IIF)V"}, at={@At(value="HEAD")})
    private void vurstvisual$resetRenderStateForGui(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ScreenMixin.applyGuiState();
    }

    @Inject(method={"render(Lnet/minecraft/client/gui/DrawContext;IIF)V"}, at={@At(value="TAIL")})
    private void vurstvisual$restoreRenderStateAfterGui(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.colorMask((boolean)true, (boolean)true, (boolean)true, (boolean)true);
        RenderSystem.disableScissor();
        RenderSystem.lineWidth((float)1.0f);
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.getWindow() != null) {
            RenderSystem.viewport((int)0, (int)0, (int)client.getWindow().getFramebufferWidth(), (int)client.getWindow().getFramebufferHeight());
        }
    }

    private static void applyGuiState() {
        boolean handledScreen;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.getFramebuffer() != null) {
            client.getFramebuffer().beginWrite(false);
        }
        RenderSystem.disableScissor();
        if (client != null && client.getWindow() != null) {
            RenderSystem.viewport((int)0, (int)0, (int)client.getWindow().getFramebufferWidth(), (int)client.getWindow().getFramebufferHeight());
        }
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.colorMask((boolean)true, (boolean)true, (boolean)true, (boolean)true);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        boolean bl = handledScreen = client != null && client.currentScreen instanceof HandledScreen;
        if (handledScreen) {
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask((boolean)true);
        } else {
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask((boolean)false);
        }
    }

    private boolean shouldSkipDarkening() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) {
            return false;
        }
        Screen self = (Screen)this;
        if (self instanceof ChatScreen) {
            return false;
        }
        return self instanceof HandledScreen || self instanceof GameMenuScreen || client.currentScreen != null;
    }
}

