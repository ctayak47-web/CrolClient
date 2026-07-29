package com.crolclient.hud.element;

import com.crolclient.feature.Feature;
import com.crolclient.feature.FeatureManager;
import com.crolclient.hud.HUDElement;
import com.crolclient.render.RenderUtils;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ArrayListHUD extends HUDElement {
    public ArrayListHUD(int x, int y) {
        super("ArrayList", x, y);
        this.enabled = true;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        List<Feature> active = new ArrayList<>();
        for (Feature f : FeatureManager.getFeatures()) {
            if (f.isEnabled()) active.add(f);
        }
        active.sort(Comparator.comparingInt(f -> -mc.textRenderer.getWidth(f.getName())));

        int offsetY = y;
        for (Feature f : active) {
            String name = f.getName();
            int tw = mc.textRenderer.getWidth(name);
            int pad = 4;
            int h = mc.textRenderer.fontHeight + pad * 2;

            // Right-align each item
            int drawX = x + getWidth() - tw - pad * 2;
            RenderUtils.drawRoundedRect(context, drawX, offsetY, tw + pad * 2, h, 3, 0xAA1A1A2E);
            context.drawTextWithShadow(mc.textRenderer, name, drawX + pad, offsetY + pad, 0xFF6C5CE7);

            offsetY += h + 2;
        }
    }

    @Override
    public int getWidth() {
        int max = 0;
        for (Feature f : FeatureManager.getFeatures()) {
            if (f.isEnabled()) {
                int w = mc.textRenderer.getWidth(f.getName());
                if (w > max) max = w;
            }
        }
        return max + 8;
    }

    @Override
    public int getHeight() {
        int count = 0;
        for (Feature f : FeatureManager.getFeatures()) {
            if (f.isEnabled()) count++;
        }
        return count * (mc.textRenderer.fontHeight + 10);
    }
}
