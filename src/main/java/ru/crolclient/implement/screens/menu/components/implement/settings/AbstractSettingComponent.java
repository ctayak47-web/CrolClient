package ru.crolclient.implement.screens.menu.components.implement.settings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.crolclient.api.feature.module.setting.Setting;
import ru.crolclient.implement.screens.menu.components.AbstractComponent;

@Getter
@RequiredArgsConstructor
public abstract class AbstractSettingComponent extends AbstractComponent {
    private final Setting setting;
}
