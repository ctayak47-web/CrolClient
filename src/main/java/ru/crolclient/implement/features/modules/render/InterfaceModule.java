package ru.crolclient.implement.features.modules.render;

import ru.crolclient.api.event.EventHandler;
import ru.crolclient.api.feature.module.setting.implement.BooleanSetting;
import ru.crolclient.api.feature.module.setting.implement.ValueSetting;
import ru.crolclient.implement.events.render.DrawEvent;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.feature.module.setting.implement.MultiSelectSetting;

public class InterfaceModule extends Module {

    private final MultiSelectSetting interfaceSettings = new MultiSelectSetting("Elements", "Customize the interface elements")
            .value("Watermark", "HotKeys", "Potions", "StaffList", "Target Hud", "Armor Hud", "Coords", "Speed", "TotemCount");

    private final BooleanSetting showEmpty = new BooleanSetting("Show Empty", "Show modules even when they have no content")
            .setValue(false);

    private final ValueSetting cornerRadius = new ValueSetting("Radius", "Adjust the corner radius of interface elements")
            .range(1, 20)
            .increment(0.5f)
            .setValue(8);

    public InterfaceModule() {
        super("Interface", ModuleCategory.RENDER);
        setup(interfaceSettings, showEmpty, cornerRadius);
    }

    @EventHandler
    public void onDraw(DrawEvent drawEvent) {
        if (isState()) {
            if (interfaceSettings.isSelected("Watermark")) {
            }
            if (interfaceSettings.isSelected("HotKeys")) {
            }
            if (interfaceSettings.isSelected("Potions")) {
            }
            if (interfaceSettings.isSelected("StaffList")) {
            }
            if (interfaceSettings.isSelected("TargetHud")) {
            }
            if (interfaceSettings.isSelected("Armor")) {
            }
            if (interfaceSettings.isSelected("Coords")) {
            }
            if (interfaceSettings.isSelected("Speed")) {
            }
            if (interfaceSettings.isSelected("Totem")) {
            }
        }
    }

    public MultiSelectSetting getInterfaceSettings() {
        return interfaceSettings;
    }

    public BooleanSetting getShowEmpty() {
        return showEmpty;
    }

    public ValueSetting getCornerRadius() {
        return cornerRadius;
    }
}
