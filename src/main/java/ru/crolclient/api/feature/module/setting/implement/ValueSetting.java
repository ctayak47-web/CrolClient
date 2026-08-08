package ru.crolclient.api.feature.module.setting.implement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.crolclient.api.feature.module.setting.Setting;

import java.util.function.Supplier;

@Getter
@Setter
@Accessors(chain = true)
public class ValueSetting extends Setting {
    private float value,
            min,
            max,
            increment = 1.0f;

    public ValueSetting(String name, String description) {
        super(name, description);
    }

    public ValueSetting range(float min, float max) {
        this.min = min;
        this.max = max;
        return this;
    }

    public ValueSetting visible(Supplier<Boolean> visible) {
        setVisible(visible);
        return this;
    }

    public ValueSetting increment(float increment) {
        this.increment = increment;
        return this;
    }

    public ValueSetting setValue(float value) {
        float multiplier = 1.0f / increment;
        this.value = Math.round(value * multiplier) / multiplier;
        return this;
    }
}