package ru.crolclient.common.trait;

import ru.crolclient.api.feature.module.setting.Setting;

public interface Setupable {
    void setup(Setting... settings);
}