
package crol.client.screens.menu.settings.impl;

import lombok.Generated;
import crol.client.CrolClient;
import crol.client.base.font.Font;
import crol.client.base.font.Fonts;
import crol.client.base.theme.Theme;
import crol.client.modules.api.setting.impl.ItemSelectSetting;
import crol.client.screens.menu.settings.api.MenuSetting;
import crol.client.screens.menu.settings.impl.popup.MenuItemPopupSetting;
import crol.client.utility.game.other.MouseButton;
import crol.client.utility.render.display.base.BorderRadius;
import crol.client.utility.render.display.base.ChangeRect;
import crol.client.utility.render.display.base.Rect;
import crol.client.utility.render.display.base.UIContext;
import crol.client.utility.render.display.base.color.ColorRGBA;

public class MenuItemSetting
extends MenuSetting {
    private final ItemSelectSetting setting;
    private Rect bounds;
    private ChangeRect boundsColor;

    public MenuItemSetting(ItemSelectSetting setting) {
        this.setting = setting;
        this.boundsColor = new ChangeRect(0.0f, 0.0f, 78.0f, 96.0f);
    }

    @Override
    public void render(UIContext ctx, float mouseX, float mouseY, float x, float settingY, float moduleWidth, float alpha, float animEnable, ColorRGBA themeColor, ColorRGBA textColor, ColorRGBA descriptionColor, Theme theme) {
        float settingHeight = 24.0f;
        float settingX = x + 8.0f;
        Font settingFont = Fonts.MEDIUM.getFont(7.0f);
        Font descFont = Fonts.MEDIUM.getFont(6.0f);
        float textY = settingY + (8.0f - settingFont.height()) / 2.0f - 0.5f;
        ctx.drawText(settingFont, this.setting.getDisplayName(), x + 8.0f + 10.0f, textY, textColor);
        float iconSize = 6.0f;
        float iconY = textY - 1.0f;
        Font iconFont = Fonts.ICONS.getFont(6.0f);
        ctx.drawText(Fonts.ICONS.getFont(6.0f), "V", settingX + 1.5f, iconY + 1.0f, themeColor);
        float toggleSize = 8.0f;
        float toggleX = x + moduleWidth - toggleSize - 8.0f;
        float toggleY = settingY;
        ctx.drawRoundedBorder(toggleX, toggleY, toggleSize, toggleSize, 0.2f, BorderRadius.all(2.0f), themeColor);
        this.bounds = new Rect(toggleX, toggleY, toggleSize, toggleSize);
        this.boundsColor.setX(toggleX + 20.0f);
        this.boundsColor.setY(toggleY + toggleSize - this.boundsColor.getHeight() / 2.0f);
        this.boundsColor.setHeight(224.0f);
        this.boundsColor.setWidth(150.0f);
    }

    @Override
    public boolean onMouseClicked(double mouseX, double mouseY, MouseButton button) {
        if (this.bounds != null && this.bounds.contains(mouseX, mouseY)) {
            CrolClient.getInstance().getMenuScreen().addPopupMenuSetting(new MenuItemPopupSetting(this.setting, this.boundsColor));
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
        return 8.0f;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Generated
    public ItemSelectSetting getSetting() {
        return this.setting;
    }
}

