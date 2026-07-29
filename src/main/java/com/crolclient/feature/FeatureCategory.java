package com.crolclient.feature;

public enum FeatureCategory {
    VISUAL("Visual"),
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    PLAYER("Player"),
    HUD("HUD"),
    SOUND("Sound"),
    UTIL("Utility");

    private final String displayName;
    FeatureCategory(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
