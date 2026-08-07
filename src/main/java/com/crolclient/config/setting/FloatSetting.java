package com.crolclient.config.setting;
public class FloatSetting extends Setting<Float> {
    private final float min, max;
    private final float step;
    public FloatSetting(String name, float defaultValue, float min, float max, float step) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
    }
    public float getMin() { return min; }
    public float getMax() { return max; }
    public float getStep() { return step; }
    public void set(float v) {
        if (v < min) v = min;
        if (v > max) v = max;
        v = Math.round(v / step) * step;
        setValue(v);
    }
}
