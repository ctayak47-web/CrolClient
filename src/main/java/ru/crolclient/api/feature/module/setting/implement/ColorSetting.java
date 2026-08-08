package ru.crolclient.api.feature.module.setting.implement;

import lombok.Getter;
import lombok.Setter;
import ru.crolclient.api.feature.module.setting.Setting;
import ru.crolclient.common.util.math.MathUtil;

import java.awt.*;
import java.util.function.Supplier;

import static ru.crolclient.common.util.math.MathUtil.*;

@Getter
@Setter
public class ColorSetting extends Setting {
    private float hue = 0,
            saturation = 1,
            brightness = 1,
            alpha = 1;

    private int[] presets;

    public ColorSetting(String name, String description) {
        super(name, description);
    }

    public ColorSetting value(int value) {
        setColor(value);
        return this;
    }

    public ColorSetting presets(int... presets) {
        this.presets = presets;
        return this;
    }

    public ColorSetting visible(Supplier<Boolean> visible) {
        setVisible(visible);
        return this;
    }

    public int getColor() {
        return (getColorWithAlpha() & 0x00FFFFFF) | (Math.round(alpha * 255) << 24);
    }

    public int getColorWithAlpha() {
        return Color.HSBtoRGB(hue, saturation, brightness);
    }

    public ColorSetting setColor(int color) {
        float[] hsb = Color.RGBtoHSB(
                getRed(color),
                getGreen(color),
                getBlue(color),
                null
        );

        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
        alpha = (MathUtil.getAlpha(color) / 255f);

        return this;
    }

    public int[] getGradientColors(int steps) {
        int[] colors = new int[steps];
        float originalBrightness = brightness;
        float originalSaturation = saturation;

        for (int i = 0; i < steps; i++) {
            float fraction = (float) i / (steps - 1);
            brightness = 0.5f + (0.5f * fraction);
            saturation = originalSaturation * (0.9f + (0.1f * fraction));
            colors[i] = getColor();
        }

        brightness = originalBrightness;
        saturation = originalSaturation;

        return colors;
    }

    public int interpolateColor(float progress) {
        int[] colors = getGradientColors(2);
        int color1 = colors[0];
        int color2 = colors[1];

        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        int a1 = (color1 >> 24) & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        int a2 = (color2 >> 24) & 0xFF;

        int r = (int) (r1 + (r2 - r1) * progress);
        int g = (int) (g1 + (g2 - g1) * progress);
        int b = (int) (b1 + (b2 - b1) * progress);
        int a = (int) (a1 + (a2 - a1) * progress);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public int getDarkerColor(float factor) {
        float newBrightness = Math.max(0, brightness * (1 - factor));
        return Color.HSBtoRGB(hue, saturation, newBrightness);
    }

    public int getLighterColor(float factor) {
        float newBrightness = Math.min(1, brightness + (1 - brightness) * factor);
        return Color.HSBtoRGB(hue, saturation, newBrightness);
    }
}