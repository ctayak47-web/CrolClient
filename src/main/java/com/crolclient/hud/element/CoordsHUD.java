package com.crolclient.hud.element;

import com.crolclient.hud.HUDElement;
import com.crolclient.render.RenderUtils;
import net.minecraft.client.gui.DrawContext;

public class CoordsHUD extends HUDElement {
    public CoordsHUD(int x, int y) {
        super("Coords", x, y);
        this.enabled = true;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (mc.player == null) return;

        String coords = String.format("XYZ: %.1f / %.1f / %.1f",
            mc.player.getX(), mc.player.getY(), mc.player.getZ());
        String dir = mc.player.getHorizontalFacing().getName().toUpperCase();
        String text = coords + " [" + dir + "]";

        int tw = mc.textRenderer.getWidth(text);
        int pad = 4;
        int h = mc.textRenderer.fontHeight + pad * 2;

        RenderUtils.drawRoundedRect(context, x, y, tw + pad * 2, h, 3, 0xAA1A1A2E);
        context.drawTextWithShadow(mc.textRenderer, text, x + pad, y + pad, 0xFFFFFFFF);
    }

    @Override
    public int getWidth() {
        return 200;
    }

    @Override
    public int getHeight() {
        return mc.textRenderer.fontHeight + 8;
    }
}
