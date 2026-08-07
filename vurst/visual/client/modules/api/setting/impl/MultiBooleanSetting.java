
package vurst.visual.client.modules.api.setting.impl;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Generated;
import vurst.visual.client.modules.api.setting.Setting;
import vurst.visual.utility.text.UiTranslation;

public class MultiBooleanSetting
extends Setting {
    private final List<Value> booleanSettings;

    public MultiBooleanSetting(String name) {
        super(name);
        this.booleanSettings = new ArrayList<Value>();
    }

    public MultiBooleanSetting(String name, Value ... settings) {
        super(name);
        this.booleanSettings = new ArrayList<Value>(Arrays.asList(settings));
    }

    public Value getValueByName(String settingName) {
        return this.booleanSettings.stream().filter(s -> s.getName().equalsIgnoreCase(settingName)).findFirst().orElse(null);
    }

    public static MultiBooleanSetting create(String name, List<String> values2) {
        Value[] booleanSettings = (Value[])values2.stream().map(value -> new Value((String)value, true)).toArray(Value[]::new);
        return new MultiBooleanSetting(name, booleanSettings);
    }

    public Value get(int index) {
        return this.booleanSettings.get(index);
    }

    public boolean isEnable(String name) {
        Value setting = this.getValueByName(name);
        return setting != null && setting.isEnabled();
    }

    public boolean isEnable(int index) {
        if (index >= this.getBooleanSettings().size()) {
            return false;
        }
        Value setting = this.get(index);
        return setting != null && setting.isEnabled();
    }

    public List<Value> getSelectedValues() {
        return this.booleanSettings.stream().filter(Value::isEnabled).collect(Collectors.toList());
    }

    public List<String> getSelectedNames() {
        return this.booleanSettings.stream().filter(Value::isEnabled).map(Value::getName).collect(Collectors.toList());
    }

    @Override
    public void safe(JsonObject propertiesObject) {
        StringBuilder builder = new StringBuilder();
        int j = 0;
        for (Value s : this.getBooleanSettings()) {
            if (this.getValueByName(s.getName()).isEnabled()) {
                builder.append(s.getName()).append("\n");
            }
            ++j;
        }
        propertiesObject.addProperty(this.getName(), builder.toString());
    }

    @Override
    public void load(JsonObject propertiesObject) {
        String[] strs;
        this.getBooleanSettings().forEach(booleanSetting -> booleanSetting.setEnabled(false));
        for (String str : strs = propertiesObject.get(String.valueOf(this.name)).getAsString().split("\n")) {
            Value booleanSetting2 = this.getValueByName(str);
            if (booleanSetting2 == null) continue;
            this.getValueByName(str).setEnabled(true);
        }
    }

    @Generated
    public List<Value> getBooleanSettings() {
        return this.booleanSettings;
    }

    public static class Value {
        private boolean enabled;
        private final String name;

        public Value(String name, boolean state) {
            this.enabled = state;
            this.name = name;
        }

        public Value(MultiBooleanSetting parent, String name, boolean state) {
            this.enabled = state;
            this.name = name;
            parent.booleanSettings.add(this);
        }

        public static Value of(String name, boolean state) {
            return new Value(name, state);
        }

        public static Value of(String name) {
            return new Value(name, true);
        }

        public void toggle() {
            this.enabled = !this.enabled;
        }

        public String getDisplayName() {
            return UiTranslation.translate(this.name);
        }

        @Generated
        public boolean isEnabled() {
            return this.enabled;
        }

        @Generated
        public String getName() {
            return this.name;
        }

        @Generated
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}

