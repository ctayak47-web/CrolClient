package com.crolclient.gui;

import com.crolclient.config.setting.*;
import com.crolclient.config.setting.FloatSetting;
import com.crolclient.feature.Feature;
import com.crolclient.render.GlassmorphismRenderer;
import com.crolclient.render.RenderUtils;
import com.crolclient.sound.SoundManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class FeatureSettingsScreen extends Screen {
    private final Feature feature;
    private final Screen parent;
    private int scroll = 0;

    public FeatureSettingsScreen(Feature feature, Screen parent) {
        super(Text.literal(feature.getName() + " Settings"));
        this.feature = feature;
        this.parent = parent;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        GlassmorphismRenderer.renderBackground(context, width, height);

        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFFFF);

        int y = 40 + scroll;
        for (Setting<?> s : feature.getSettings()) {
            RenderUtils.drawRoundedRect(context, 20, y, width - 40, 28, 6, 0xAA1A1A2E);
            String val;
            if (s instanceof BooleanSetting bs) val = bs.getValue() ? "ON" : "OFF";
            else if (s instanceof ModeSetting ms) val = ms.getValue();
            else if (s instanceof IntSetting is) val = String.valueOf(is.getValue());
            else if (s instanceof FloatSetting fs) val = String.format("%.2f", fs.getValue());
            else val = s.getValue().toString();

            context.drawTextWithShadow(textRenderer, s.getName() + ": " + val, 30, y + 9, 0xFFFFFFFF);
            y += 36;
        }

        // Back button
        RenderUtils.drawRoundedRect(context, width / 2 - 40, height - 30, 80, 22, 6, 0xFF6C5CE7);
        context.drawCenteredTextWithShadow(textRenderer, "Back", width / 2, height - 24, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseY >= height - 30 && mouseY <= height - 8 && mouseX >= width / 2 - 40 && mouseX <= width / 2 + 40) {
            SoundManager.playUI("closegui");
            client.setScreen(parent);
            return true;
        }

        int y = 40 + scroll;
        for (Setting<?> s : feature.getSettings()) {
            if (mouseX >= 20 && mouseX <= width - 20 && mouseY >= y && mouseY <= y + 28) {
                if (s instanceof BooleanSetting bs) bs.setValue(!bs.getValue());
                else if (s instanceof ModeSetting ms) ms.cycle();
                else if (s instanceof FloatSetting fs) {
                    float newVal = fs.getValue() + fs.getStep();
                    if (newVal > fs.getMax()) newVal = fs.getMin();
                    fs.set(newVal);
                    // Sync ViewModel to config immediately
                    if (feature instanceof com.crolclient.feature.render.ViewModelFeature vm) {
                        com.crolclient.feature.render.ViewModelFeature.updateFromSettings(vm);
                    }
                }
                SoundManager.playUI("enable");
                return true;
            }
            y += 36;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override public boolean shouldPause() { return false; }
}
