
package crol.client.modules.impl.render;

import net.minecraft.MathHelper;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.api.setting.impl.BooleanSetting;
import crol.client.modules.api.setting.impl.ColorSetting;
import crol.client.modules.api.setting.impl.NumberSetting;
import crol.client.utility.render.display.base.color.ColorRGBA;

@ModuleAnnotation(name="ShaderHands", category=Category.RENDER, description="Шейдер рук: блюр и свечение.")
public final class ShaderHands
extends Module {
    public static final ShaderHands INSTANCE = new ShaderHands();
    private final BooleanSetting glow = new BooleanSetting("Глов", true);
    private final NumberSetting glowSize = new NumberSetting("Сила глова", 4.0f, 1.0f, 8.0f, 1.0f, this.glow::isEnabled);
    private final NumberSetting glowBrightness = new NumberSetting("Яркость глова", 1.0f, 0.0f, 4.0f, 0.05f, this.glow::isEnabled);
    private final BooleanSetting blur = new BooleanSetting("Блюр", true);
    private final NumberSetting blurBrightness = new NumberSetting("Яркость блюра", 1.0f, 0.0f, 2.0f, 0.05f, this.blur::isEnabled);
    private final NumberSetting blurSize = new NumberSetting("Сила блюра", 4.0f, 1.0f, 8.0f, 1.0f, this.blur::isEnabled);
    private final ColorSetting handColor = new ColorSetting("Цвет рук", ColorRGBA.WHITE);
    private final NumberSetting handAlpha = new NumberSetting("Прозрачность рук", 0.65f, 0.2f, 1.0f, 0.05f);
    private final NumberSetting handBrightness = new NumberSetting("Яркость рук", 1.0f, 0.5f, 2.5f, 0.05f);

    private ShaderHands() {
    }

    public boolean shouldShaderHands() {
        return this.isEnabled();
    }

    public float getHandAlpha() {
        if (!this.isEnabled()) {
            return 1.0f;
        }
        float alpha = this.handAlpha.getCurrent();
        if (this.blur.isEnabled()) {
            alpha -= (this.blurSize.getCurrent() - 1.0f) * 0.03f;
            alpha -= this.blurBrightness.getCurrent() * 0.05f;
        }
        return MathHelper.clamp((float)alpha, (float)0.2f, (float)1.0f);
    }

    public float getHandCombinedAlpha() {
        if (!this.isEnabled()) {
            return 1.0f;
        }
        float colorAlpha = (float)this.handColor.getColor().getAlpha() / 255.0f;
        return MathHelper.clamp((float)(this.getHandAlpha() * colorAlpha), (float)0.0f, (float)1.0f);
    }

    public ColorRGBA getHandColor() {
        return this.handColor.getColor();
    }

    public float getHandBrightness() {
        if (!this.isEnabled()) {
            return 1.0f;
        }
        float value = this.handBrightness.getCurrent();
        if (this.glow.isEnabled()) {
            value += this.glowBrightness.getCurrent() * 0.1f;
            value += (this.glowSize.getCurrent() - 1.0f) * 0.05f;
        }
        return MathHelper.clamp((float)value, (float)0.5f, (float)2.8f);
    }
}

