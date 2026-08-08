package ru.crolclient.implement.features.modules.render;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.feature.module.setting.implement.MultiSelectSetting;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ClearRenderModule extends Module {

    MultiSelectSetting clearRenderSettings = new MultiSelectSetting("Clear Render", "Customize rendering settings for clarity")
            .value("HurtCam", "Fire", "Water", "Container", "Boat");

    public ClearRenderModule() {
        super("ClearRender", "Clear Render", ModuleCategory.RENDER);
        setup(clearRenderSettings);
    }

    public void updateClearRenderSettings() {
        if (isState()) {
            if (clearRenderSettings.isSelected("HurtCam")) {
            }
            if (clearRenderSettings.isSelected("Fire")) {
            }
            if (clearRenderSettings.isSelected("Water")) {
            }
            if (clearRenderSettings.isSelected("Container")) {
            }
            if (clearRenderSettings.isSelected("Boat")) {
            }
        }
    }
}
