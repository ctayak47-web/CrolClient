
package vurst.visual.client.modules.api;

import lombok.Generated;
import vurst.visual.utility.text.UiTranslation;

public enum Category {
    MOVEMENT("Utility", "1"),
    RENDER("Render", "3"),
    HUD("Hud", "2"),
    THEMES("Themes", "G");

    private final String name;
    private final String icon;

    private Category(String name, String icon) {
        this.name = name;
        this.icon = icon;
    }

    public String getDisplayName() {
        return UiTranslation.translate(this.name);
    }

    @Generated
    public String getIcon() {
        return this.icon;
    }

    @Generated
    public String getName() {
        return this.name;
    }
}

