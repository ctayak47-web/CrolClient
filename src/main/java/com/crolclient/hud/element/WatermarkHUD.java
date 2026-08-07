package com.crolclient.hud.element;
import com.crolclient.hud.HUDElement;
import com.crolclient.render.RenderUtils;
import net.minecraft.client.gui.DrawContext;
public class WatermarkHUD extends HUDElement {
    private static final String TEXT = "CrolClient v1.0";
    public WatermarkHUD(int x, int y) {
        super("Watermark", x, y);
        this.enabled = true;
    }
    @Override
    public void render(DrawContext context, float tickDelta) {
        int textWidth = mc.textRenderer.getWidth(TEXT);
        int padding = 6;
        int bgWidth = textWidth + padding * 2;
        int bgHeight = mc.textRenderer.fontHeight + padding * 2;
        RenderUtils.drawRoundedRect(context, x, y, bgWidth, bgHeight, 4, 0xBB1A1A2E);
        RenderUtils.drawRoundedRect(context, x, y + bgHeight - 1, bgWidth, 1, 0, 0xFF6C5CE7);
        context.drawTextWithShadow(mc.textRenderer, TEXT, x + padding, y + padding, 0xFFFFFFFF);
    }
    @Override
    public int getWidth() {
        return mc.textRenderer.getWidth(TEXT) + 12;
    }
    @Override
    public int getHeight() {
        return mc.textRenderer.fontHeight + 12;
    }
}
