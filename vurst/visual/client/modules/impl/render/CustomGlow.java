
package vurst.visual.client.modules.impl.render;

import net.minecraft.Entity;
import net.minecraft.PlayerEntity;
import net.minecraft.Identifier;
import vurst.visual.VurstVisual;
import vurst.visual.base.theme.Theme;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.ColorSetting;
import vurst.visual.client.modules.api.setting.impl.ModeSetting;
import vurst.visual.utility.render.display.base.color.ColorRGBA;

@ModuleAnnotation(name="CustomGlow", category=Category.RENDER, description="Меняет цвет свечения у игроков с эффектом подсветки.")
public final class CustomGlow
extends Module {
    public static final CustomGlow INSTANCE = new CustomGlow();
    private static final Identifier SAFE_OUTLINE_TEXTURE = Identifier.ofVanilla((String)"textures/misc/white.png");
    private final ModeSetting colorMode = new ModeSetting("Цвет", "Клиентский", "Кастомный");
    private final ColorSetting customColor = new ColorSetting("Кастомный цвет", Theme.DARK.getColor(), () -> this.colorMode.is("Кастомный"), Theme.DARK::getColor);

    private CustomGlow() {
    }

    public boolean shouldApplyTo(Entity entity) {
        if (!this.isEnabled() || !(entity instanceof PlayerEntity)) {
            return false;
        }
        PlayerEntity player = (PlayerEntity)entity;
        if (player == CustomGlow.mc.player) {
            return false;
        }
        return player.isGlowing();
    }

    public int getGlowColor(Entity entity) {
        if (!this.shouldApplyTo(entity)) {
            return entity.getTeamColorValue();
        }
        ColorRGBA color = this.colorMode.is("Кастомный") ? this.customColor.getColor() : VurstVisual.getInstance().getThemeManager().getCurrentTheme().getColor();
        return color.withAlpha(255).getRGB();
    }

    public Identifier getSafeOutlineTexture() {
        return SAFE_OUTLINE_TEXTURE;
    }
}

