
package vurst.visual.client.modules.api.setting.impl;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;
import lombok.Generated;
import vurst.visual.client.modules.api.setting.Setting;
import vurst.visual.utility.text.UiTranslation;

public class ModeSetting
extends Setting {
    private final List<Value> values = new ArrayList<Value>();
    private Value value;
    private boolean expanded;

    public ModeSetting(String name, String ... modes) {
        super(name);
        for (String mode : modes) {
            if (mode.isEmpty()) continue;
            new Value(this, mode);
        }
        if (!this.values.isEmpty()) {
            this.value = this.values.getFirst();
        }
    }

    public ModeSetting(String name, Supplier<Boolean> visible, String ... modes) {
        super(name);
        for (String mode : modes) {
            if (mode.isEmpty()) continue;
            new Value(this, mode);
        }
        if (!this.values.isEmpty()) {
            this.value = this.values.getFirst();
        }
        this.setVisible(visible);
    }

    public void set(String mode) {
        this.values.stream().filter(v -> v.getName().equals(mode)).findFirst().ifPresent(v -> {
            this.value = v;
        });
    }

    public String get() {
        return this.value != null ? this.value.getName() : "";
    }

    public boolean is(String mode) {
        return this.value != null && this.value.getName().equals(mode);
    }

    public boolean is(Value otherValue) {
        return this.value == otherValue;
    }

    public Value getRandomEnabledElement() {
        List<Value> selectedValues = this.values.stream().filter(Value::isSelected).toList();
        if (!selectedValues.isEmpty()) {
            return selectedValues.get(new Random().nextInt(selectedValues.size()));
        }
        return null;
    }

    @Override
    public void safe(JsonObject propertiesObject) {
        propertiesObject.addProperty(String.valueOf(this.name), this.get());
    }

    @Override
    public void load(JsonObject propertiesObject) {
        this.set(propertiesObject.get(String.valueOf(this.name)).getAsString());
    }

    @Generated
    public List<Value> getValues() {
        return this.values;
    }

    @Generated
    public Value getValue() {
        return this.value;
    }

    @Generated
    public boolean isExpanded() {
        return this.expanded;
    }

    @Generated
    public void setValue(Value value) {
        this.value = value;
    }

    public static class Value {
        private final ModeSetting parent;
        private final String name;
        private final String description;

        public Value(ModeSetting parent, String name) {
            this.parent = parent;
            this.name = name;
            this.description = "";
            if (parent.values.isEmpty()) {
                this.select();
            }
            parent.values.add(this);
        }

        public Value(ModeSetting parent, String name, String description) {
            this.parent = parent;
            this.name = name;
            this.description = description;
            if (parent.values.isEmpty()) {
                this.select();
            }
            parent.values.add(this);
        }

        public Value select() {
            this.parent.setValue(this);
            return this;
        }

        public boolean isSelected() {
            return this.parent.getValue() == this;
        }

        public String toString() {
            return this.name;
        }

        public String getDisplayName() {
            return UiTranslation.translate(this.name);
        }

        public String getDisplayDescription() {
            return UiTranslation.translate(this.description);
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            Value that = (Value)obj;
            return Objects.equals(this.parent, that.parent) && Objects.equals(this.name, that.name) && Objects.equals(this.description, that.description);
        }

        public int hashCode() {
            return Objects.hash(this.parent, this.name, this.description);
        }

        @Generated
        public ModeSetting getParent() {
            return this.parent;
        }

        @Generated
        public String getName() {
            return this.name;
        }

        @Generated
        public String getDescription() {
            return this.description;
        }
    }
}

