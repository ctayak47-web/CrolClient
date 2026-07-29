package com.crolclient.config.setting;

import java.util.List;

public class ModeSetting extends Setting<String> {
    private final List<String> modes;

    public ModeSetting(String name, String defaultValue, List<String> modes) {
        super(name, defaultValue);
        this.modes = modes;
    }

    public List<String> getModes() { return modes; }

    public void cycle() {
        int idx = modes.indexOf(value);
        setValue(modes.get((idx + 1) % modes.size()));
    }
}
