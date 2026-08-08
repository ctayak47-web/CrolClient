package ru.crolclient.api.feature.module.setting.implement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.crolclient.api.feature.module.setting.Setting;

import java.util.Random;
import java.util.function.Supplier;

@Getter
@Setter
@Accessors(chain = true)
public class RangeSetting extends Setting {
    private final Random random = new Random();

    private float firstValue, secondValue;
    private float min, max;
    private float firstIncrement = 1.0f;
    private float secondIncrement = 1.0f;

    public RangeSetting(String name, String description) {
        super(name, description);
    }

    public RangeSetting range(float min, float max) {
        this.min = min;
        this.max = max;
        return this;
    }

    public RangeSetting visible(Supplier<Boolean> visible) {
        setVisible(visible);
        return this;
    }

    public RangeSetting increment(float firstIncrement, float secondIncrement) {
        this.firstIncrement = firstIncrement;
        this.secondIncrement = secondIncrement;
        return this;
    }

    public RangeSetting setValue(float first, float second) {
        if (first > second) {
            float temp = first;
            first = second;
            second = temp;
        }

        float firstMultiplier = 1.0f / firstIncrement;
        float secondMultiplier = 1.0f / secondIncrement;

        this.firstValue = Math.round(first * firstMultiplier) / firstMultiplier;
        this.secondValue = Math.round(second * secondMultiplier) / secondMultiplier;

        this.firstValue = Math.max(min, Math.min(max, this.firstValue));
        this.secondValue = Math.max(min, Math.min(max, this.secondValue));

        return this;
    }

    public float getValue() {
        float range = secondValue - firstValue;
        return firstValue + random.nextFloat() * range;
    }
}