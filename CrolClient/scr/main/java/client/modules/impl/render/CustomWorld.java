
package crol.client.modules.impl.render;

import net.minecraft.ChatScreen;
import net.minecraft.HandledScreen;
import crol.client.CrolClient;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.api.setting.impl.ColorSetting;
import crol.client.utility.render.display.base.color.ColorRGBA;

@ModuleAnnotation(name="Custom World", category=Category.RENDER, description="Полностью перекрашивает небо и облака.")
public final class CustomWorld
extends Module {
    public static final CustomWorld INSTANCE = new CustomWorld();
    private final ColorSetting color = new ColorSetting("Цвет", CrolClient.getInstance().getThemeManager().getCurrentTheme().getColor());

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

