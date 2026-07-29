package com.crolclient.hud.element;

import com.crolclient.hud.HUDElement;
import com.crolclient.render.RenderUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

public class PingHUD extends HUDElement {
    public PingHUD(int x, int y) {
        super("Ping", x, y);
        this.enabled = true;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        int ping = entry != null ? entry.getLatency() : 0;

        String text = "Ping: " + ping + "ms";
        int tw = mc.textRenderer.getWidth(text);
        int pad = 4;
        int h = mc.textRenderer.fontHeight + pad * 2;

        int color = ping < 100 ? 0xFF66BB6A : (ping < 200 ? 0xFFFFA726 : 0xFFEF5350);

        RenderUtils.drawRoundedRect(context, x, y, tw + pad * 2, h, 3, 0xAA1A1A2E);
        context.drawTextWithShadow(mc.textRenderer, text, x + pad, y + pad, color);
    }

    @Override
    public int getWidth() {
        return mc.textRenderer.getWidth("Ping: 999ms") + 8;
    }

    @Override
    public int getHeight() {
        return mc.textRenderer.fontHeight + 8;
    }
}
