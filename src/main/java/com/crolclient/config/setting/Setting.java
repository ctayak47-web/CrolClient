package com.crolclient.config.setting;
import java.util.function.Consumer;
public abstract class Setting<T> {
    protected final String name;
    protected T value;
    protected final Consumer<T> onChange;
    public Setting(String name, T defaultValue) {
        this(name, defaultValue, v -> {});
    }
    public Setting(String name, T defaultValue, Consumer<T> onChange) {
        this.name = name;
        this.value = defaultValue;
        this.onChange = onChange;
    }
    public String getName() { return name; }
    public T getValue() { return value; }
    public void setValue(T value) {
        this.value = value;
        onChange.accept(value);
    }
}
