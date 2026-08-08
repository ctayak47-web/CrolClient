
package crol.client.modules.impl.render;

import com.darkmagician6.eventapi.EventTarget;
import crol.client.CrolClient;
import crol.client.base.events.impl.render.EventFog;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.api.setting.impl.ColorSetting;
import crol.client.modules.api.setting.impl.NumberSetting;

@ModuleAnnotation(name="Custom Fog", category=Category.RENDER, description="Настройки тумана.")
public final class CustomFog
extends Module {
    public static final CustomFog INSTANCE = new CustomFog();
    private final ColorSetting color = new ColorSetting("Цвет тумана", CrolClient.getInstance().getThemeManager().getCurrentTheme().getColor());
    private final NumberSetting distance = new NumberSetting("Дальность тумана", 80.0f, 10.0f, 255.0f, 5.0f);

    private CustomFog() {
    }

    @EventTarget
    public void onFog(EventFog event) {
        if (!this.isEnabled()) {
            return;
        }
        event.setDistance(this.distance.getCurrent());
        event.setColor(this.color.getIntColor());
        event.setCancelled(true);
    }
}

