
package crol.client.screens.menu.settings.api;

import lombok.Generated;
import crol.client.base.animations.base.Animation;
import crol.client.base.animations.base.Easing;
import crol.client.base.theme.Theme;
import crol.client.screens.menu.settings.api.MenuSetting;
import crol.client.utility.render.display.base.ChangeRect;
import crol.client.utility.render.display.base.UIContext;
import crol.client.utility.render.display.base.color.ColorRGBA;

public abstract class MenuPopupSetting
extends MenuSetting {
    protected final ChangeRect bounds;
    protected Animation animationScale = new Animation(200L, 0.01f, Easing.QUAD_IN_OUT);

    protected MenuPopupSetting(ChangeRect bounds) {
        this.bounds = bounds;
    }

    public abstract void render(UIContext var1, float var2, float var3, float var4, Theme var5);

    @Override
    public final void render(UIContext ctx, float mouseX, float mouseY, float x, float settingY, float moduleWidth, float alpha, float animEnable, ColorRGBA themeColor, ColorRGBA textColor, ColorRGBA descriptionColor, Theme theme) {
    }

    @Override
    public abstract boolean charTyped(char var1, int var2);

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return false;
    }

    @Generated
    public ChangeRect getBounds() {
        return this.bounds;
    }

    @Generated
    public Animation getAnimationScale() {
        return this.animationScale;
    }
}

