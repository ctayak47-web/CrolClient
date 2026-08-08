package ru.crolclient.api.feature.module.setting.implement;

import lombok.Getter;
import lombok.Setter;
import ru.crolclient.api.feature.module.setting.Setting;

@Getter
@Setter
public class ThemeSetting extends Setting {
    private int startColor;
    private int endColor;
    private int hudStartColor;
    private int hudEndColor;

    public ThemeSetting(String name, int startColor, int endColor, int hudStartColor, int hudEndColor) {
        super(name, "");
        this.startColor = startColor;
        this.endColor = endColor;
        this.hudStartColor = hudStartColor;
        this.hudEndColor = hudEndColor;
    }

    public ThemeSetting value(int startColor, int endColor, int hudStartColor, int hudEndColor) {
        this.startColor = startColor;
        this.endColor = endColor;
        this.hudStartColor = hudStartColor;
        this.hudEndColor = hudEndColor;
        return this;
    }
}