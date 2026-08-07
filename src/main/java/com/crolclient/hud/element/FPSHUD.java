package com.crolclient.hud.element;
import com.crolclient.hud.HUDElement;
import com.crolclient.render.RenderUtils;
import net.minecraft.client.gui.DrawContext;
public class FPSHUD extends HUDElement {
    public FPSHUD(int x, int y) {
        super("FPS", x, y);
        this.enabled = true;
    }
    @Override
    public void render(DrawContext context, float tickDelta) {
        String text = "FPS: " + mc.getCurrentFps();
        int tw = mc.textRenderer.getWidth(text);
        int pad = 4;
        int h = mc.textRenderer.fontHeight + pad * 2;
        RenderUtils.drawRoundedRect(context, x, y, tw + pad * 2, h, 3, 0xAA1A1A2E);
        context.drawTextWithShadow(mc.textRenderer, text, x + pad, y + pad, 0xFF66BB6A);
    }
    @Override
    public int getWidth() {
        return mc.textRenderer.getWidth("FPS: 9999") + 8;
    }
    @Override
    public int getHeight() {
        return mc.textRenderer.fontHeight + 8;
    }
}
