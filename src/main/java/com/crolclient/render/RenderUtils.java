package com.crolclient.render;

import net.minecraft.client.gui.DrawContext;

public class RenderUtils {
    public static void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
        context.fill(x + radius, y, x + width - radius, y + height, color);
        context.fill(x, y + radius, x + width, y + height - radius, color);
        // Corner approximation
        context.fill(x + radius, y + radius, x + width - radius, y + height - radius, color);
    }

    public static int withAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }
}
