
package vurst.visual.client.modules.impl.render;

import vurst.visual.base.theme.Theme;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.ColorSetting;
import vurst.visual.client.modules.api.setting.impl.ModeSetting;
import vurst.visual.utility.render.display.base.color.ColorRGBA;

@ModuleAnnotation(name="Hit Color", category=Category.RENDER, description="Цвет при получении урона.")
public final class HitColor
extends Module {
    public static final HitColor INSTANCE = new HitColor();
    private final ModeSetting mode = new ModeSetting("Режим", "Красить только скин", "Красить полностью");
    private final ModeSetting.Value modeSkinOnly = this.mode.getValues().get(0);
    private final ModeSetting.Value modeFull = this.mode.getValues().get(1);
    private final ColorSetting colorSetting = new ColorSetting("Цвет", Theme.DARK.getColor(), Theme.DARK::getColor);

    private HitColor() {
    }

    public boolean isFullColor() {
        return this.mode.is(this.modeFull);
    }

    public boolean isSkinOnlyColor() {
        return this.mode.is(this.modeSkinOnly);
    }

    public ColorRGBA getColor() {
        return this.colorSetting.getColor();
    }
}

