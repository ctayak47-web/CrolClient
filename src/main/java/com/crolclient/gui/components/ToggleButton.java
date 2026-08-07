package com.crolclient.gui.components;
import com.crolclient.feature.Feature;
import com.crolclient.render.RenderUtils;
import com.crolclient.sound.SoundManager;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
public class ToggleButton {
    private final int x, y, width, height;
    private final Feature feature;
    public ToggleButton(int x, int y, int width, int height, Feature feature) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.feature = feature;
    }
    public Feature getFeature() { return feature; }
    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY) {
        boolean hovered = isMouseOver(mouseX, mouseY);
        boolean enabled = feature.isEnabled();
        int bgColor = enabled ? 0xFF4CAF50 : (hovered ? 0xAA3A3A5E : 0xAA2A2A4E);
        int textColor = 0xFFFFFFFF;
        RenderUtils.drawRoundedRect(context, x, y, width, height, 6, bgColor);
        context.drawTextWithShadow(textRenderer, feature.getName(), x + 10, y + 7, textColor);
        int toggleBgX = x + width - 36;
        int toggleBgY = y + 4;
        int toggleBgColor = enabled ? 0xFF66BB6A : 0xFF757575;
        RenderUtils.drawRoundedRect(context, toggleBgX, toggleBgY, 28, 16, 8, toggleBgColor);
        int circleX = enabled ? toggleBgX + 14 : toggleBgX + 2;
        int circleY = toggleBgY + 2;
        RenderUtils.drawRoundedRect(context, circleX, circleY, 12, 12, 6, 0xFFFFFFFF);
        if (!feature.getSettings().isEmpty()) {
            context.drawTextWithShadow(textRenderer, "...", x + width - 50, y + 7, 0xFFAAAAAA);
        }
    }
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    public void click() {
        feature.toggle();
        if (feature.isEnabled()) {
            SoundManager.playUI("enable");
        } else {
            SoundManager.playUI("disable");
        }
    }
}
