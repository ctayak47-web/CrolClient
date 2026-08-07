
package vurst.visual.utility.game.other.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ShaderProgramKeys;
import net.minecraft.ShaderProgramKey;
import net.minecraft.Text;
import net.minecraft.MinecraftClient;
import net.minecraft.DrawContext;
import net.minecraft.Screen;
import vurst.visual.utility.game.other.MouseButton;
import vurst.visual.utility.interfaces.IMinecraft;
import vurst.visual.utility.render.display.base.UIContext;

public abstract class CustomScreen
extends Screen
implements IMinecraft {
    protected CustomScreen() {
        super((Text)Text.empty());
    }

    public abstract void render(UIContext var1, float var2, float var3);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        UIContext uiContext = UIContext.of(context, mouseX, mouseY, delta);
        CustomScreen.prepareCustomGuiState();
        try {
            this.render(uiContext, mouseX, mouseY);
            super.render(context, mouseX, mouseY, delta);
        }
        finally {
            CustomScreen.restoreRenderState();
        }
    }

    public final boolean mouseClicked(double mouseX, double mouseY, int button) {
        MouseButton mouseButton = MouseButton.fromButtonIndex(button);
        this.onMouseClicked(mouseX, mouseY, mouseButton);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void tick() {
    }

    public final boolean mouseReleased(double mouseX, double mouseY, int button) {
        MouseButton mouseButton = MouseButton.fromButtonIndex(button);
        this.onMouseReleased(mouseX, mouseY, mouseButton);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public final boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        MouseButton mouseButton = MouseButton.fromButtonIndex(button);
        this.onMouseDragged(mouseX, mouseY, mouseButton, deltaX, deltaY);
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    public void onMouseClicked(double mouseX, double mouseY, MouseButton button) {
    }

    public void onMouseReleased(double mouseX, double mouseY, MouseButton button) {
    }

    public void onMouseDragged(double mouseX, double mouseY, MouseButton button, double deltaX, double deltaY) {
    }

    private static void prepareCustomGuiState() {
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
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.lineWidth((float)1.0f);
    }

    private static void restoreRenderState() {
        MinecraftClient client = MinecraftClient.getInstance();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.colorMask((boolean)true, (boolean)true, (boolean)true, (boolean)true);
        RenderSystem.disableScissor();
        if (client != null && client.getWindow() != null) {
            RenderSystem.viewport((int)0, (int)0, (int)client.getWindow().getFramebufferWidth(), (int)client.getWindow().getFramebufferHeight());
        }
        RenderSystem.lineWidth((float)1.0f);
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }
}

