
package crol.client.screens.menu.elements.api;

import crol.client.base.font.Font;
import crol.client.modules.api.Category;
import crol.client.utility.game.other.MouseButton;
import crol.client.utility.render.display.base.UIContext;

public abstract class AbstractMenuElement {
    public abstract void render(UIContext var1, float var2, float var3, Font var4, float var5, float var6, float var7, float var8, int var9);

    public abstract float getHeight();

    public abstract void onMouseClicked(double var1, double var3, MouseButton var5);

    public abstract void onMouseReleased(double var1, double var3, MouseButton var5);

    public abstract void onMouseDragged(double var1, double var3, MouseButton var5, double var6, double var8);

    public abstract boolean keyPressed(int var1, int var2, int var3);

    public boolean charTyped(char chr, int modifiers) {
        return false;
    }

    public abstract boolean mouseScrolled(double var1, double var3, double var5, double var7);

    public abstract Category getCategory();

    public abstract String getName();
}

