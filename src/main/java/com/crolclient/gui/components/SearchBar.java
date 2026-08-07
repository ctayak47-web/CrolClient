package com.crolclient.gui.components;

import com.crolclient.render.RenderUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/** Minimal self-drawn search input, no vanilla widget dependency. */
public class SearchBar {
    private int x, y, width, height;
    private String text = "";
    private boolean focused = false;
    private float focusProgress = 0f;
    private int cursorBlink = 0;

    public SearchBar(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setPosition(int x, int y) { this.x = x; this.y = y; }
    public void setWidth(int width) { this.width = width; }
    public String getText() { return text; }
    public void clear() { text = ""; }
    public boolean isFocused() { return focused; }
    public void setFocused(boolean focused) { this.focused = focused; }

    public void tick(float deltaSeconds) {
        float target = focused ? 1f : 0f;
        focusProgress += (target - focusProgress) * Math.min(1f, 14f * deltaSeconds);
        cursorBlink++;
    }

    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY) {
        int bg = RenderUtils.lerpColor(0x552A2A3E, 0x663A3A56, focusProgress);
        RenderUtils.drawRoundedRect(context, x, y, width, height, 7, bg);

        int borderAlpha = (int) (140 * focusProgress);
        if (borderAlpha > 0) {
            RenderUtils.drawRoundedRectBorderOnly(context, x, y, width, height, 7, RenderUtils.withAlpha(0xFF7C5CFF, borderAlpha));
        }

        // magnifier glyph (drawn, no texture dependency)
        int gx = x + 9, gy = y + height / 2 - 3;
        int glyphColor = 0xFF9C9CB8;
        context.fill(gx, gy, gx + 6, gy + 1, glyphColor);
        context.fill(gx, gy, gx + 1, gy + 6, glyphColor);
        context.fill(gx, gy + 5, gx + 6, gy + 6, glyphColor);
        context.fill(gx + 5, gy, gx + 6, gy + 6, glyphColor);
        context.fill(gx + 5, gy + 5, gx + 8, gy + 8, glyphColor);

        String display = text.isEmpty() && !focused ? "Search modules..." : text;
        int textColor = text.isEmpty() && !focused ? 0xFF7A7A90 : 0xFFEAEAF5;
        context.drawTextWithShadow(textRenderer, display, x + 22, y + (height - 8) / 2, textColor);

        if (focused && (cursorBlink / 15) % 2 == 0) {
            int caretX = x + 22 + textRenderer.getWidth(text) + 1;
            context.fill(caretX, y + 4, caretX + 1, y + height - 4, 0xFFFFFFFF);
        }
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public boolean charTyped(char chr) {
        if (!focused) return false;
        if (text.length() < 40) {
            text += chr;
        }
        return true;
    }

    public boolean keyPressed(int keyCode) {
        if (!focused) return false;
        // GLFW_KEY_BACKSPACE = 259
        if (keyCode == 259 && !text.isEmpty()) {
            text = text.substring(0, text.length() - 1);
            return true;
        }
        return false;
    }
}
