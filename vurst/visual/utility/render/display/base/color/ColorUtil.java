
package vurst.visual.utility.render.display.base.color;

import java.awt.Color;
import java.util.regex.Pattern;
import lombok.Generated;
import net.minecraft.MathHelper;
import vurst.visual.VurstVisual;
import vurst.visual.utility.render.display.base.color.ColorRGBA;

public final class ColorUtil {
    public static final int LIGHT_RED = ColorUtil.getColor(255, 85, 85, 255);
    private static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("(?i)§[0-9a-f-or]");

    public static int red(int c) {
        return c >> 16 & 0xFF;
    }

    public static int green(int c) {
        return c >> 8 & 0xFF;
    }

    public static int blue(int c) {
        return c & 0xFF;
    }

    public static int alpha(int c) {
        return c >> 24 & 0xFF;
    }

    public static float redf(int c) {
        return (float)ColorUtil.red(c) / 255.0f;
    }

    public static float greenf(int c) {
        return (float)ColorUtil.green(c) / 255.0f;
    }

    public static float bluef(int c) {
        return (float)ColorUtil.blue(c) / 255.0f;
    }

    public static float alphaf(int c) {
        return (float)ColorUtil.alpha(c) / 255.0f;
    }

    public static int[] getRGBA(int c) {
        return new int[]{ColorUtil.red(c), ColorUtil.green(c), ColorUtil.blue(c), ColorUtil.alpha(c)};
    }

    public static int[] getRGB(int c) {
        return new int[]{ColorUtil.red(c), ColorUtil.green(c), ColorUtil.blue(c)};
    }

    public static float[] getRGBAf(int c) {
        return new float[]{ColorUtil.redf(c), ColorUtil.greenf(c), ColorUtil.bluef(c), ColorUtil.alphaf(c)};
    }

    public static float[] getRGBf(int c) {
        return new float[]{ColorUtil.redf(c), ColorUtil.greenf(c), ColorUtil.bluef(c)};
    }

    public static boolean isValidHexColor(String input) {
        return input != null && input.matches("(?i)^[a-f0-9]{6}$");
    }

    public static ColorRGBA hexToRgb(String colorStr, ColorRGBA fallbackColor) {
        if (!ColorUtil.isValidHexColor(colorStr)) {
            return fallbackColor;
        }
        int rgb = Integer.parseInt(colorStr, 16);
        int red = rgb >> 16 & 0xFF;
        int green = rgb >> 8 & 0xFF;
        int blue = rgb & 0xFF;
        return new ColorRGBA(new Color(red, green, blue));
    }

    public static String colorToHex(ColorRGBA color) {
        int rgb = color.getRGB();
        return String.format("%06X", rgb & 0xFFFFFF);
    }

    public static ColorRGBA lerp(int speed, int index, ColorRGBA start, ColorRGBA end) {
        int angle = (int)((System.currentTimeMillis() / (long)speed + (long)index) % 360L);
        angle = (angle >= 180 ? 360 - angle : angle) * 2;
        return ColorUtil.interpolate(start, end, (float)angle / 360.0f);
    }

    public static ColorRGBA gradient(int speed, int index, ColorRGBA ... colors) {
        int angle = (int)((System.currentTimeMillis() / (long)speed + (long)index) % 360L);
        angle = (angle > 180 ? 360 - angle : angle) + 180;
        int colorIndex = (int)((float)angle / 360.0f * (float)colors.length);
        if (colorIndex == colors.length) {
            --colorIndex;
        }
        ColorRGBA color1 = colors[colorIndex];
        ColorRGBA color2 = colors[colorIndex == colors.length - 1 ? 0 : colorIndex + 1];
        return ColorUtil.interpolate(color1, color2, (float)angle / 360.0f * (float)colors.length - (float)colorIndex);
    }

    public static ColorRGBA interpolate(ColorRGBA color1, ColorRGBA color2, float amount) {
        return color1.mix(color2, amount);
    }

    public static String removeFormatting(String text) {
        return text == null || text.isEmpty() ? null : FORMATTING_CODE_PATTERN.matcher(text).replaceAll("");
    }

    public static int multAlpha(int color, float percent01) {
        return ColorUtil.getColor(ColorUtil.red(color), ColorUtil.green(color), ColorUtil.blue(color), Math.round((float)ColorUtil.alpha(color) * percent01));
    }

    private static int getColor(int red, int green, int blue, int alpha) {
        return MathHelper.clamp((int)alpha, (int)0, (int)255) << 24 | MathHelper.clamp((int)red, (int)0, (int)255) << 16 | MathHelper.clamp((int)green, (int)0, (int)255) << 8 | MathHelper.clamp((int)blue, (int)0, (int)255);
    }

    public static int fade(int index) {
        return VurstVisual.getInstance().getThemeManager().getClientColor(index).getRGB();
    }

    @Generated
    private ColorUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

