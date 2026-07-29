package com.crolclient.gui.components;

import com.crolclient.feature.FeatureCategory;
import com.crolclient.render.RenderUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class CategoryButton {
    private final int x, y, width, height;
    private final FeatureCategory category;
    private final Runnable onClick;

    public CategoryButton(int x, int y, int width, int height, FeatureCategory category, Runnable onClick) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.category = category;
        this.onClick = onClick;
    }

    public void render(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY, boolean selected) {
        boolean hovered = isMouseOver(mouseX, mouseY);
        int bgColor = selected ? 0xFF6C5CE7 : (hovered ? 0xAA5A5A8E : 0x00000000);

        if (bgColor != 0) {
            RenderUtils.drawRoundedRect(context, x, y, width, height, 6, bgColor);
        }
        context.drawTextWithShadow(textRenderer, category.getDisplayName(), x + 10, y + 7, 0xFFFFFFFF);
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void click() {
        onClick.run();
    }

    public FeatureCategory getCategory() {
        return category;
    }
}
