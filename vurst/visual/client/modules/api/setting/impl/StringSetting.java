
package vurst.visual.client.modules.api.setting.impl;

import com.google.gson.JsonObject;
import java.util.function.Supplier;
import lombok.Generated;
import vurst.visual.client.modules.api.setting.Setting;

public class StringSetting
extends Setting {
    private String value;
    private final int maxLength;

    public StringSetting(String name, String defaultValue) {
        this(name, defaultValue, 128);
    }

    public StringSetting(String name, String defaultValue, int maxLength) {
        super(name);
        this.maxLength = Math.max(1, maxLength);
        this.setValue(defaultValue);
    }

    public StringSetting(String name, String defaultValue, int maxLength, Supplier<Boolean> visible) {
        this(name, defaultValue, maxLength);
        this.setVisible(visible);
    }

    public void setValue(String value) {
        String safe;
        String string = safe = value == null ? "" : value;
        if (safe.length() > this.maxLength) {
            safe = safe.substring(0, this.maxLength);
        }
        this.value = safe;
    }

    @Override
    public void safe(JsonObject propertiesObject) {
        propertiesObject.addProperty(String.valueOf(this.name), this.value);
    }

    @Override
    public void load(JsonObject propertiesObject) {
        if (propertiesObject.has(String.valueOf(this.name))) {
            this.setValue(propertiesObject.get(String.valueOf(this.name)).getAsString());
        }
    }

    @Generated
    public String getValue() {
        return this.value;
    }

    @Generated
    public int getMaxLength() {
        return this.maxLength;
    }
}

