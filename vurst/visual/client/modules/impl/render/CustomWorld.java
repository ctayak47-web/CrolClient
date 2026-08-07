
package vurst.visual.client.modules.impl.render;

import net.minecraft.ChatScreen;
import net.minecraft.HandledScreen;
import vurst.visual.VurstVisual;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.ColorSetting;
import vurst.visual.utility.render.display.base.color.ColorRGBA;

@ModuleAnnotation(name="Custom World", category=Category.RENDER, description="Полностью перекрашивает небо и облака.")
public final class CustomWorld
extends Module {
    public static final CustomWorld INSTANCE = new CustomWorld();
    private final ColorSetting color = new ColorSetting("Цвет", VurstVisual.getInstance().getThemeManager().getCurrentTheme().getColor());

    private CustomWorld() {
    }

    public ColorRGBA getColor() {
        return this.color.getColor();
    }

    public boolean shouldApplyWorldColoring() {
        if (!this.isEnabled() || mc == null || CustomWorld.mc.world == null) {
            return false;
        }
        return CustomWorld.mc.currentScreen == null || CustomWorld.mc.currentScreen instanceof ChatScreen || !(CustomWorld.mc.currentScreen instanceof HandledScreen);
    }
}

