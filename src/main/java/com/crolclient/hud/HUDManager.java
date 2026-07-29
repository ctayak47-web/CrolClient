package com.crolclient.hud;

import com.crolclient.config.ConfigManager;
import com.crolclient.hud.element.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class HUDManager {
    private static final List<HUDElement> elements = new ArrayList<>();
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void init() {
        // Positions are updated dynamically in render where needed
        elements.add(new WatermarkHUD(4, 4));
        elements.add(new ArrayListHUD(0, 4));       // x computed dynamically (right-aligned)
        elements.add(new CoordsHUD(4, 0));          // y computed dynamically (bottom)
        elements.add(new FPSHUD(0, 4));               // x computed dynamically (right-aligned)
        elements.add(new PingHUD(0, 20));             // x computed dynamically (right-aligned)

        // Load from config
        for (HUDElement e : elements) {
            if (e instanceof WatermarkHUD) e.setEnabled(ConfigManager.getConfig().hudWatermarkEnabled);
            if (e instanceof ArrayListHUD) e.setEnabled(ConfigManager.getConfig().hudArraylistEnabled);
            if (e instanceof CoordsHUD) e.setEnabled(ConfigManager.getConfig().hudCoordsEnabled);
            if (e instanceof FPSHUD) e.setEnabled(ConfigManager.getConfig().hudFpsEnabled);
            if (e instanceof PingHUD) e.setEnabled(ConfigManager.getConfig().hudPingEnabled);
        }
    }

    public static List<HUDElement> getElements() {
        return elements;
    }

    public static void render(DrawContext context, float tickDelta) {
        if (mc.getWindow() == null) return;
        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();

        for (HUDElement e : elements) {
            if (e.isEnabled()) {
                // Dynamic positioning for corner-aligned elements
                if (e instanceof ArrayListHUD) {
                    e.setX(screenW - e.getWidth() - 4);
                }
                if (e instanceof FPSHUD) {
                    e.setX(screenW - e.getWidth() - 4);
                }
                if (e instanceof PingHUD) {
                    e.setX(screenW - e.getWidth() - 4);
                }
                if (e instanceof CoordsHUD) {
                    e.setY(screenH - e.getHeight() - 4);
                }
                e.render(context, tickDelta);
            }
        }
    }

    public static void saveConfig() {
        for (HUDElement e : elements) {
            if (e instanceof WatermarkHUD) ConfigManager.getConfig().hudWatermarkEnabled = e.isEnabled();
            if (e instanceof ArrayListHUD) ConfigManager.getConfig().hudArraylistEnabled = e.isEnabled();
            if (e instanceof CoordsHUD) ConfigManager.getConfig().hudCoordsEnabled = e.isEnabled();
            if (e instanceof FPSHUD) ConfigManager.getConfig().hudFpsEnabled = e.isEnabled();
            if (e instanceof PingHUD) ConfigManager.getConfig().hudPingEnabled = e.isEnabled();
        }
        ConfigManager.save();
    }
}
