
package vurst.visual.client.modules.impl.hud;

import net.minecraft.ItemStack;
import net.minecraft.MathHelper;
import net.minecraft.ChatScreen;
import org.lwjgl.glfw.GLFW;
import vurst.visual.VurstVisual;
import vurst.visual.base.events.impl.input.EventMouse;
import vurst.visual.base.theme.Theme;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.setting.impl.BooleanSetting;
import vurst.visual.client.modules.api.setting.impl.ColorSetting;
import vurst.visual.client.modules.api.setting.impl.ModeSetting;
import vurst.visual.client.modules.api.setting.impl.NumberSetting;
import vurst.visual.client.screens.menu.MenuScreen;
import vurst.visual.utility.render.display.base.BorderRadius;
import vurst.visual.utility.render.display.base.CustomDrawContext;
import vurst.visual.utility.render.display.base.Gradient;
import vurst.visual.utility.render.display.base.color.ColorRGBA;
import vurst.visual.utility.render.display.shader.DrawUtil;

public abstract class HudModule
extends Module {
    protected static final float PANEL_RADIUS = 4.0f;
    protected static final float PANEL_BLUR = 12.0f;
    protected static final int MEDIA_BLUR_ALPHA = 200;
    protected static final int MEDIA_PANEL_ALPHA = 120;
    private final boolean draggable;
    private final NumberSetting posX;
    private final NumberSetting posY;
    protected final NumberSetting scaleX;
    protected final NumberSetting scaleY;
    protected final ModeSetting themeMode;
    protected final ColorSetting primaryColor;
    protected final ColorSetting secondaryColor;
    protected final BooleanSetting transparentBackground;
    private float width;
    private float height;
    private boolean dragging;
    private float dragOffsetX;
    private float dragOffsetY;
    private boolean leftMouseWasDown;
    private final double[] cursorXBuffer = new double[1];
    private final double[] cursorYBuffer = new double[1];
    private int lastScreenWidth = -1;
    private int lastScreenHeight = -1;

    protected HudModule(float x, float y, float width, float height, boolean draggable) {
        this.width = width;
        this.height = height;
        this.draggable = draggable;
        this.posX = new NumberSetting("X", x, 0.0f, 10000.0f, 1.0f);
        this.posY = new NumberSetting("Y", y, 0.0f, 10000.0f, 1.0f);
        this.posX.setVisible(() -> false);
        this.posY.setVisible(() -> false);
        this.scaleX = new NumberSetting("Масштаб X", 1.0f, 0.5f, 3.0f, 0.05f);
        this.scaleY = new NumberSetting("Масштаб Y", 1.0f, 0.5f, 3.0f, 0.05f);
        this.themeMode = new ModeSetting("Тема", "Клиентский", "Кастомный");
        this.primaryColor = new ColorSetting("Первый цвет", Theme.DARK.getColor(), () -> this.themeMode.is("Кастомный"), Theme.DARK::getColor);
        this.secondaryColor = new ColorSetting("Второй цвет", Theme.DARK.getSecondColor(), () -> this.themeMode.is("Кастомный"), Theme.DARK::getSecondColor);
        this.transparentBackground = new BooleanSetting("Прозрачный фон", false);
    }

    protected HudModule(float x, float y, float width, float height) {
        this(x, y, width, height, true);
    }

    protected void updateHud() {
        this.onTick();
    }

    protected void renderHud(CustomDrawContext ctx) {
        if (!this.isElementVisible()) {
            return;
        }
        this.updateForResize();
        this.pollDragInputFallback();
        if (this.dragging) {
            this.updateDrag();
        }
        float x = this.getX();
        float y = this.getY();
        float sx = this.getScaleX();
        float sy = this.getScaleY();
        ctx.getMatrices().push();
        ctx.getMatrices().translate(x, y, 0.0f);
        ctx.getMatrices().scale(sx, sy, 1.0f);
        ctx.getMatrices().translate(-x, -y, 0.0f);
        this.draw(ctx);
        ctx.getMatrices().pop();
    }

    protected void handleMouse(EventMouse event) {
        if (!this.draggable) {
            return;
        }
        if (!this.isHudEditContext()) {
            this.dragging = false;
            return;
        }
        if (event.getButton() != 0) {
            return;
        }
        if (event.getAction() == 1) {
            float mouseY;
            float mouseX = this.getMouseX();
            if (this.isHovered(mouseX, mouseY = this.getMouseY())) {
                this.dragging = true;
                this.dragOffsetX = mouseX - this.getX();
                this.dragOffsetY = mouseY - this.getY();
            }
        } else if (event.getAction() == 0) {
            this.dragging = false;
        }
    }

    protected boolean isElementVisible() {
        return true;
    }

    protected void onTick() {
    }

    protected abstract void draw(CustomDrawContext var1);

    protected void drawPanel(CustomDrawContext ctx, float x, float y, float width, float height) {
        BorderRadius radius = BorderRadius.all(4.0f);
        this.drawMediaCard(ctx, x, y, width, height, 4.0f, 1.0f);
        ColorRGBA border = this.getBorderColor().mulAlpha(0.75f);
        ctx.drawRoundedBorder(x, y, width, height, 1.0f, radius, border);
    }

    protected void drawMediaCard(CustomDrawContext ctx, float x, float y, float width, float height, float radius, float alphaMul) {
        if (this.transparentBackground.isEnabled()) {
            return;
        }
        float clamped = MathHelper.clamp((float)alphaMul, (float)0.0f, (float)1.0f);
        int blurAlpha = Math.max(0, Math.min(255, Math.round(200.0f * clamped)));
        int panelAlpha = Math.max(0, Math.min(255, Math.round(120.0f * clamped)));
        BorderRadius borderRadius = BorderRadius.all(radius);
        if (this.getTheme().isBlur()) {
            DrawUtil.drawBlur(ctx.getMatrices(), x, y, width, height, 12.0f, borderRadius, new ColorRGBA(255, 255, 255, blurAlpha));
        }
        ctx.drawRoundedRect(x, y, width, height, borderRadius, new ColorRGBA(0, 0, 0, panelAlpha));
    }

    protected void drawItem(CustomDrawContext ctx, ItemStack stack, float x, float y, float scale) {
        ctx.getMatrices().push();
        ctx.getMatrices().translate(x, y, 0.0f);
        ctx.getMatrices().scale(scale, scale, 1.0f);
        ctx.getMatrices().translate(-x, -y, 0.0f);
        ctx.drawItem(stack, (int)x, (int)y);
        ctx.drawStackOverlay(HudModule.mc.textRenderer, stack, (int)x, (int)y);
        ctx.getMatrices().pop();
    }

    protected Gradient getAccentGradient() {
        if (this.themeMode.is("Кастомный")) {
            ColorRGBA primary = this.primaryColor.getColor();
            return Gradient.of(primary, primary, primary, primary);
        }
        ColorRGBA base = VurstVisual.getInstance().getThemeManager().getCurrentTheme().getColor();
        return Gradient.of(base, base, base, base);
    }

    protected ColorRGBA getAccentColor() {
        if (this.themeMode.is("Кастомный")) {
            return this.primaryColor.getColor();
        }
        return VurstVisual.getInstance().getThemeManager().getCurrentTheme().getColor();
    }

    protected ColorRGBA resolveTextColor(ColorRGBA fallback) {
        if (!this.themeMode.is("Кастомный")) {
            return fallback;
        }
        return this.primaryColor.getColor().mulAlpha((float)fallback.getAlpha() / 255.0f);
    }

    protected ColorRGBA getTextColor() {
        return this.resolveTextColor(this.getTheme().getWhite());
    }

    protected ColorRGBA getHeaderColor() {
        return this.getTheme().getForegroundLight();
    }

    protected ColorRGBA getPanelColor() {
        return this.getTheme().getForegroundColor();
    }

    protected ColorRGBA getBorderColor() {
        return this.getTheme().getForegroundStroke();
    }

    protected Theme getTheme() {
        return VurstVisual.getInstance().getThemeManager().getCurrentTheme();
    }

    protected float getX() {
        return this.posX.getCurrent();
    }

    protected float getY() {
        return this.posY.getCurrent();
    }

    protected void setX(float x) {
        this.posX.setCurrent(x);
    }

    protected void setY(float y) {
        this.posY.setCurrent(y);
    }

    protected float getWidth() {
        return this.width;
    }

    protected float getHeight() {
        return this.height;
    }

    protected void setBounds(float width, float height) {
        this.width = width;
        this.height = height;
        this.clampToScreen();
    }

    protected float getScaleX() {
        return Math.max(0.1f, this.scaleX.getCurrent());
    }

    protected float getScaleY() {
        return Math.max(0.1f, this.scaleY.getCurrent());
    }

    protected boolean clampPositionToScreen() {
        return true;
    }

    private void updateDrag() {
        if (!this.isHudEditContext()) {
            if (this.dragging) {
                this.dragging = false;
                this.saveHudLayout();
            }
            return;
        }
        if (GLFW.glfwGetMouseButton((long)mc.getWindow().getHandle(), (int)0) != 1) {
            if (this.dragging) {
                this.dragging = false;
                this.saveHudLayout();
            }
            return;
        }
        float mouseX = this.getMouseX();
        float mouseY = this.getMouseY();
        this.setX(mouseX - this.dragOffsetX);
        this.setY(mouseY - this.dragOffsetY);
        this.clampToScreen();
    }

    private void pollDragInputFallback() {
        float mouseY;
        float mouseX;
        if (!this.draggable || mc.getWindow() == null) {
            return;
        }
        boolean leftMouseDown = GLFW.glfwGetMouseButton((long)mc.getWindow().getHandle(), (int)0) == 1;
        boolean inHudEditContext = this.isHudEditContext();
        if (!inHudEditContext) {
            if (this.dragging) {
                this.dragging = false;
                this.saveHudLayout();
            }
            this.leftMouseWasDown = leftMouseDown;
            return;
        }
        if (!this.leftMouseWasDown && leftMouseDown && this.isHovered(mouseX = this.getMouseX(), mouseY = this.getMouseY())) {
            this.dragging = true;
            this.dragOffsetX = mouseX - this.getX();
            this.dragOffsetY = mouseY - this.getY();
        }
        if (this.leftMouseWasDown && !leftMouseDown && this.dragging) {
            this.dragging = false;
            this.saveHudLayout();
        }
        this.leftMouseWasDown = leftMouseDown;
    }

    private void saveHudLayout() {
        VurstVisual.getInstance().getConfigManager().saveConfig("current_config");
    }

    private boolean isHovered(float mouseX, float mouseY) {
        float width = this.getScaledWidth();
        float height = this.getScaledHeight();
        float x = this.getX();
        float y = this.getY();
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private boolean isHudEditContext() {
        return HudModule.mc.currentScreen instanceof ChatScreen || HudModule.mc.currentScreen instanceof MenuScreen;
    }

    private void updateForResize() {
        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();
        if (screenWidth <= 0 || screenHeight <= 0) {
            return;
        }
        if (this.lastScreenWidth <= 0 || this.lastScreenHeight <= 0) {
            this.lastScreenWidth = screenWidth;
            this.lastScreenHeight = screenHeight;
            return;
        }
        if (screenWidth == this.lastScreenWidth && screenHeight == this.lastScreenHeight) {
            return;
        }
        float relX = this.lastScreenWidth == 0 ? 0.0f : this.getX() / (float)this.lastScreenWidth;
        float relY = this.lastScreenHeight == 0 ? 0.0f : this.getY() / (float)this.lastScreenHeight;
        this.setX(relX * (float)screenWidth);
        this.setY(relY * (float)screenHeight);
        this.clampToScreen();
        this.lastScreenWidth = screenWidth;
        this.lastScreenHeight = screenHeight;
    }

    private void clampToScreen() {
        if (!this.clampPositionToScreen()) {
            return;
        }
        float width = this.getScaledWidth();
        float height = this.getScaledHeight();
        if (width <= 0.0f || height <= 0.0f) {
            return;
        }
        float maxX = (float)mc.getWindow().getScaledWidth() - width;
        float maxY = (float)mc.getWindow().getScaledHeight() - height;
        this.setX(MathHelper.clamp((float)this.getX(), (float)0.0f, (float)Math.max(0.0f, maxX)));
        this.setY(MathHelper.clamp((float)this.getY(), (float)0.0f, (float)Math.max(0.0f, maxY)));
    }

    private float getScaledWidth() {
        return this.width * this.getScaleX();
    }

    private float getScaledHeight() {
        return this.height * this.getScaleY();
    }

    private float getMouseX() {
        if (mc.getWindow() != null) {
            long handle = mc.getWindow().getHandle();
            GLFW.glfwGetCursorPos((long)handle, (double[])this.cursorXBuffer, (double[])this.cursorYBuffer);
            return (float)(this.cursorXBuffer[0] / mc.getWindow().getScaleFactor());
        }
        return (float)HudModule.mc.mouse.getX();
    }

    private float getMouseY() {
        if (mc.getWindow() != null) {
            long handle = mc.getWindow().getHandle();
            GLFW.glfwGetCursorPos((long)handle, (double[])this.cursorXBuffer, (double[])this.cursorYBuffer);
            return (float)(this.cursorYBuffer[0] / mc.getWindow().getScaleFactor());
        }
        return (float)HudModule.mc.mouse.getY();
    }
}

