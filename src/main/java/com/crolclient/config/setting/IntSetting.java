package com.crolclient.config.setting;
public class IntSetting extends Setting<Integer> {
    private final int min, max;
    public IntSetting(String name, int defaultValue, int min, int max) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
    }
    public int getMin() { return min; }
    public int getMax() { return max; }
    public void set(int v) {
        if (v < min) v = min;
        if (v > max) v = max;
        setValue(v);
    }
}
