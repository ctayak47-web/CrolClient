
package crol.client.modules.impl.render;

import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.api.setting.impl.ModeSetting;

@ModuleAnnotation(name="World Time", category=Category.RENDER, description="Меняет время мира.")
public final class WorldTime
extends Module {
    public static final WorldTime INSTANCE = new WorldTime();
    private final ModeSetting timeSetting = new ModeSetting("Время суток", "Закат", "День", "Ночь", "Полночь", "Рассвет");

    private WorldTime() {
    }

    public float getTimeHours() {
        if (this.timeSetting.is("День")) {
            return 1.0f;
        }
        if (this.timeSetting.is("Ночь")) {
            return 13.0f;
        }
        if (this.timeSetting.is("Полночь")) {
            return 18.0f;
        }
        if (this.timeSetting.is("Рассвет")) {
            return 23.0f;
        }
        return 12.0f;
    }
}

