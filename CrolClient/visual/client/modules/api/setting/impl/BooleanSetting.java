
package crol.client.modules.api.setting.impl;

import com.google.gson.JsonObject;
import java.util.function.Supplier;
import lombok.Generated;
import crol.client.modules.api.setting.Setting;
import crol.client.utility.text.UiTranslation;

public class BooleanSetting
extends Setting {
    private boolean enabled;
    private final String description;

    public BooleanSetting(String name, boolean state) {
        super(name);
        this.enabled = state;
        this.description = "";
    }

    public BooleanSetting(String name, String description, boolean state) {
        super(name);
        this.enabled = state;
        this.description = description;
    }

    public BooleanSetting(String name, String description, boolean state, Supplier<Boolean> supplier) {
        super(name);
        this.enabled = state;
        this.setVisible(supplier);
        this.description = description;
    }

    public BooleanSetting(String name, boolean state, Supplier<Boolean> visible) {
        super(name);
        this.enabled = state;
        this.setVisible(visible);
        this.description = "";
    }

    public static BooleanSetting of(String name, boolean state) {
        return new BooleanSetting(name, state);
    }

    public static BooleanSetting of(String name) {
        return new BooleanSetting(name, true);
    }

    public String getDisplayDescription() {
        return UiTranslation.translate(this.description);
    }

    public void toggle() {
        this.enabled = !this.enabled;
    }

    @Override
    public void safe(JsonObject propertiesObject) {
        propertiesObject.addProperty(String.valueOf(this.name), this.isEnabled());
    }

    @Override
    public void load(JsonObject propertiesObject) {
        this.setEnabled(propertiesObject.get(String.valueOf(this.name)).getAsBoolean());
    }

    @Generated
    public boolean isEnabled() {
        return this.enabled;
    }

    @Generated
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Generated
    public String getDescription() {
        return this.description;
    }
}

