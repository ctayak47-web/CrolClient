package com.crolclient.render;

import com.crolclient.config.ConfigManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;

public class GlassmorphismRenderer {
    public static void renderBackground(DrawContext context, int width, int height) {
        float opacity = ConfigManager.getConfig().glassOpacity;
        int alpha = (int) (opacity * 255);
        int bgColor = (alpha << 24) | 0x0F0F23;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        context.fill(0, 0, width, height, bgColor);
        RenderSystem.disableBlend();
    }

    public static void renderGlassPanel(DrawContext context, int x, int y, int width, int height, int radius) {
        float opacity = ConfigManager.getConfig().glassOpacity;
        int alpha = (int) (opacity * 255);
        int panelColor = (alpha << 24) | 0x1A1A2E;
        int borderColor = (0x33 << 24) | 0xFFFFFF;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Background
        RenderUtils.drawRoundedRect(context, x, y, width, height, radius, panelColor);
        // Border
        RenderUtils.drawRoundedRect(context, x, y, width, 1, 0, borderColor);
        RenderUtils.drawRoundedRect(context, x, y + height - 1, width, 1, 0, borderColor);
        RenderUtils.drawRoundedRect(context, x, y, 1, height, 0, borderColor);
        RenderUtils.drawRoundedRect(context, x + width - 1, y, 1, height, 0, borderColor);

        RenderSystem.disableBlend();
    }
}
