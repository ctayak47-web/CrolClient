
package crol.client.modules.impl.render;

import com.darkmagician6.eventapi.EventTarget;
import crol.client.base.events.impl.render.EventAspectRatio;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.api.setting.impl.ModeSetting;
import crol.client.modules.api.setting.impl.NumberSetting;

@ModuleAnnotation(name="AspectRatio", category=Category.RENDER, description="Изменяет соотношение сторон.")
public final class AspectRatio
extends Module {
    public static final AspectRatio INSTANCE = new AspectRatio();
    private final ModeSetting mode = new ModeSetting("Режим", "4:3", "16:9", "16:10", "Кастом");
    private final NumberSetting customWidth = new NumberSetting("Ширина", 16.0f, 1.0f, 64.0f, 0.1f, () -> this.mode.is("Кастом"));
    private final NumberSetting customHeight = new NumberSetting("Высота", 9.0f, 1.0f, 64.0f, 0.1f, () -> this.mode.is("Кастом"));

    private AspectRatio() {
    }

    @EventTarget
    public void onAspectRatio(EventAspectRatio event) {
        if (!this.isEnabled()) {
            return;
        }
        event.setRatio(this.getTargetRatio());
        event.setCancelled(true);
    }

    private float getTargetRatio() {
        return switch (this.mode.get()) {
            case "4:3" -> 1.3333334f;
            case "16:9" -> 1.7777778f;
            case "16:10" -> 1.6f;
            case "Кастом" -> this.customWidth.getCurrent() / Math.max(0.1f, this.customHeight.getCurrent());
            default -> (float)mc.getWindow().getFramebufferWidth() / (float)mc.getWindow().getFramebufferHeight();
        };
    }
}

