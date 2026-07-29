package com.crolclient.gui;

import com.crolclient.feature.Feature;
import com.crolclient.feature.FeatureCategory;
import com.crolclient.feature.FeatureManager;
import com.crolclient.gui.components.CategoryButton;
import com.crolclient.gui.components.ToggleButton;
import com.crolclient.render.GlassmorphismRenderer;
import com.crolclient.sound.SoundManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class CrolMenuScreen extends Screen {
    private static final Identifier BACKGUI = Identifier.of("crolclient", "textures/gui/backgui.png");
    private static final Identifier ALPHABAR = Identifier.of("crolclient", "textures/gui/alphabar.png");
    private FeatureCategory selectedCategory = FeatureCategory.VISUAL;
    private final List<ToggleButton> toggleButtons = new ArrayList<>();
    private final List<CategoryButton> categoryButtons = new ArrayList<>();

    public CrolMenuScreen() {
        super(Text.literal("CrolClient"));
    }

    @Override
    protected void init() {
        super.init();
        toggleButtons.clear();
        categoryButtons.clear();

        int catY = 40;
        int catX = 20;
        for (FeatureCategory cat : FeatureCategory.values()) {
            categoryButtons.add(new CategoryButton(catX, catY, 90, 22, cat, () -> {
                selectedCategory = cat;
                rebuildFeatureButtons();
            }));
            catY += 30;
        }

        rebuildFeatureButtons();
    }

    private void rebuildFeatureButtons() {
        toggleButtons.clear();
        int featY = 40;
        int featX = 130;
        for (Feature f : FeatureManager.getFeaturesByCategory(selectedCategory)) {
            toggleButtons.add(new ToggleButton(featX, featY, 160, 24, f));
            featY += 32;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        // Background image (backgui.png) stretched to screen
        context.drawTexture(
            RenderLayer::getGuiTextured,
            BACKGUI,
            0, 0,
            0.0f, 0.0f,
            width, height,
            width, height
        );

        // Glass overlay on top
        GlassmorphismRenderer.renderBackground(context, width, height);

        context.drawCenteredTextWithShadow(textRenderer, "CrolClient v1.0", width / 2, 12, 0xFFFFFFFF);

        // Category panel
        GlassmorphismRenderer.renderGlassPanel(context, 10, 30, 110, height - 60, 10);
        // Feature panel
        GlassmorphismRenderer.renderGlassPanel(context, 125, 30, 180, height - 60, 10);

        for (CategoryButton btn : categoryButtons) {
            btn.render(context, textRenderer, mouseX, mouseY, selectedCategory == btn.getCategory());
        }

        for (ToggleButton btn : toggleButtons) {
            btn.render(context, textRenderer, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (CategoryButton btn : categoryButtons) {
            if (btn.isMouseOver(mouseX, mouseY)) {
                btn.click();
                return true;
            }
        }
        for (ToggleButton btn : toggleButtons) {
            if (btn.isMouseOver(mouseX, mouseY)) {
                if (button == 1) { // RMB
                    if (!btn.getFeature().getSettings().isEmpty()) {
                        SoundManager.playUI("opengui");
                        client.setScreen(new FeatureSettingsScreen(btn.getFeature(), this));
                    }
                } else {
                    btn.click();
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
