package com.crolclient.feature;

import com.crolclient.config.setting.Setting;
import java.util.ArrayList;
import java.util.List;

public abstract class Feature {
    protected final String name;
    protected final String description;
    protected final FeatureCategory category;
    protected boolean enabled;
    protected final List<Setting<?>> settings = new ArrayList<>();

    public Feature(String name, String description, FeatureCategory category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public FeatureCategory getCategory() { return category; }
    public boolean isEnabled() { return enabled; }
    public List<Setting<?>> getSettings() { return settings; }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) onEnable(); else onDisable();
    }

    public void toggle() { setEnabled(!enabled); }

    protected void onEnable() {}
    protected void onDisable() {}
    public void onTick() {}
}
