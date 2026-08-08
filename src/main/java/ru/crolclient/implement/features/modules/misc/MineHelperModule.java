package ru.crolclient.implement.features.modules.misc;

import ru.crolclient.api.event.EventHandler;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.feature.module.setting.implement.*;
import ru.crolclient.implement.events.player.TickEvent;
import ru.crolclient.implement.features.modules.misc.minehelper.AutoTool;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class MineHelperModule extends Module {
    final GroupSetting toolSettings = new GroupSetting("Tool Settings", "Tool switching settings")
            .settings(
                    new MultiSelectSetting("Options", "Additional tool switching options")
                            .value("Swap Back", "Save Item", "Echest Silk")
            );

    final AutoTool autoTool;

    public MineHelperModule() {
        super("MineHelper", "Mine Helper", ModuleCategory.MISC);
        setup(toolSettings);

        this.autoTool = new AutoTool(toolSettings);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (toolSettings.isValue()) {
            autoTool.onTick();
        }
    }
}