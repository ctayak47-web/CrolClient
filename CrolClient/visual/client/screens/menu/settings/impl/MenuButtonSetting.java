
package crol.client.screens.menu.settings.impl;

import crol.client.base.animations.base.Animation;
import crol.client.base.animations.base.Easing;
import crol.client.base.font.Fonts;
import crol.client.base.theme.Theme;
import crol.client.modules.api.setting.impl.ButtonSetting;
import crol.client.screens.menu.settings.api.MenuSetting;
import crol.client.utility.game.other.MouseButton;
import crol.client.utility.render.display.base.BorderRadius;
import crol.client.utility.render.display.base.Rect;
import crol.client.utility.render.display.base.UIContext;
import crol.client.utility.render.display.base.color.ColorRGBA;

public class MenuButtonSetting
extends MenuSetting {
    private final ButtonSetting button;
    private final Animation animHovered = new Animation(200L, 0.0f, Easing.QUAD_IN_OUT);
    Rect bounds;

    public MenuButtonSetting(ButtonSetting button) {
        this.button = button;
    }

    @Override
    public void render(UIContext ctx, float mouseX, float mouseY, float x, float settingY, float moduleWidth, float alpha, float animEnable, ColorRGBA themeColor, ColorRGBA textColor, ColorRGBA descriptionColor, Theme theme) {
        this.bounds = new Rect(x + 8.0f, settingY, moduleWidth - 16.0f, 16.0f);
        ctx.drawRoundedRect(x + 8.0f, settingY, moduleWidth - 16.0f, 16.0f, BorderRadius.all(4.0f), theme.getForegroundColor().mulAlpha(alpha));
        ctx.drawRoundedRect(x + 8.0f, settingY, moduleWidth - 16.0f, 16.0f, BorderRadius.all(4.0f), theme.getForegroundStroke().mulAlpha(alpha));
        String displayName = this.button.getDisplayName();
        ctx.drawText(Fonts.MEDIUM.getFont(7.0f), displayName, x + (moduleWidth - Fonts.MEDIUM.getWidth(displayName, 7.0f)) / 2.0f, settingY + 5.0f, textColor);
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (this.bounds != null && this.bounds.contains(mouseX, mouseY) && button == MouseButton.LEFT) {
            this.button.toggle();
            return true;
        }
        return false;
    }

    @Override
    public float getWidth() {
        return 0.0f;
    }

    @Override
    public float getHeight() {
        return 16.0f;
    }

    @Override
    public boolean isVisible() {
        return true;
    }
}

