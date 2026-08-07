
package vurst.visual.base.theme;

import lombok.Generated;
import vurst.visual.utility.render.display.base.color.ColorRGBA;
import vurst.visual.utility.text.UiTranslation;

public class Theme {
    private final String name;
    private final String icon;
    private final Defaults defaults;
    private ColorRGBA color;
    private ColorRGBA secondColor;
    private ColorRGBA friendColor;
    private boolean useSecondColor;
    private ColorRGBA gray;
    private ColorRGBA grayLight;
    private ColorRGBA foregroundLight;
    private ColorRGBA whiteGray;
    private ColorRGBA foregroundGray;
    private ColorRGBA foregroundLightStroke;
    private ColorRGBA foregroundColor;
    private ColorRGBA foregroundStroke;
    private ColorRGBA foregroundDark;
    private ColorRGBA white;
    private ColorRGBA backgroundColor;
    private boolean glow;
    private boolean blur;
    private boolean corners;
    public static final Theme DARK = ThemeBuilder.builder().color(new ColorRGBA(181, 162, 255)).secondColor(new ColorRGBA(255, 203, 162)).friendColor(new ColorRGBA(181, 162, 255)).friendSecond(new ColorRGBA(255, 203, 162)).gray(new ColorRGBA(88, 87, 93)).grayLight(new ColorRGBA(128, 127, 133)).foregroundLight(new ColorRGBA(32, 31, 37)).whiteGray(new ColorRGBA(68, 67, 73)).foregroundGray(new ColorRGBA(48, 47, 53)).foregroundLightStroke(new ColorRGBA(38, 37, 43)).foreground(new ColorRGBA(28, 27, 33)).foregroundStroke(new ColorRGBA(35, 34, 40)).foregroundDark(new ColorRGBA(25, 24, 30)).white(new ColorRGBA(255, 255, 255)).background(new ColorRGBA(23, 22, 28)).glow(false).blur(false).corners(false).build("Dark", "8");
    public static final Theme LIGHT = ThemeBuilder.builder().color(new ColorRGBA(123, 93, 234)).secondColor(new ColorRGBA(255, 192, 121)).friendColor(new ColorRGBA(123, 93, 234)).friendSecond(new ColorRGBA(255, 192, 121)).gray(new ColorRGBA(138, 137, 143)).grayLight(new ColorRGBA(148, 147, 153)).foregroundLight(new ColorRGBA(236, 236, 236)).whiteGray(new ColorRGBA(178, 177, 183)).foregroundGray(new ColorRGBA(188, 187, 193)).foregroundLightStroke(new ColorRGBA(229, 229, 229)).foreground(new ColorRGBA(246, 246, 246)).foregroundStroke(new ColorRGBA(229, 229, 229)).foregroundDark(new ColorRGBA(251, 251, 251)).white(new ColorRGBA(23, 22, 28)).background(new ColorRGBA(255, 255, 255)).glow(false).blur(false).corners(false).build("Light", "T");
    public static final Theme CUSTOM_THEME = ThemeBuilder.builder().color(new ColorRGBA(181, 162, 255)).secondColor(new ColorRGBA(255, 203, 162)).friendColor(new ColorRGBA(181, 162, 255)).friendSecond(new ColorRGBA(255, 203, 162)).gray(new ColorRGBA(88, 87, 93)).grayLight(new ColorRGBA(128, 127, 133)).foregroundLight(new ColorRGBA(32, 31, 37)).whiteGray(new ColorRGBA(68, 67, 73)).foregroundGray(new ColorRGBA(48, 47, 53)).foregroundLightStroke(new ColorRGBA(38, 37, 43)).foreground(new ColorRGBA(28, 27, 33)).foregroundStroke(new ColorRGBA(35, 34, 40)).foregroundDark(new ColorRGBA(25, 24, 30)).white(new ColorRGBA(255, 255, 255)).background(new ColorRGBA(23, 22, 28)).glow(false).blur(false).corners(false).build("Кастом", "F");

    public Theme(String name, String icon, ThemeBuilder builder) {
        this.name = name;
        this.icon = icon;
        this.color = builder.color;
        this.secondColor = builder.secondColor;
        this.friendColor = builder.friendColor;
        this.useSecondColor = builder.useSecondColor;
        this.gray = builder.gray;
        this.grayLight = builder.grayLight;
        this.foregroundLight = builder.foregroundLight;
        this.whiteGray = builder.whiteGray;
        this.foregroundGray = builder.foregroundGray;
        this.foregroundLightStroke = builder.foregroundLightStroke;
        this.foregroundColor = builder.foreground;
        this.foregroundStroke = builder.foregroundStroke;
        this.foregroundDark = builder.foregroundDark;
        this.white = builder.white;
        this.backgroundColor = builder.background;
        this.glow = builder.glow;
        this.blur = builder.blur;
        this.corners = builder.corners;
        this.defaults = new Defaults(builder.color, builder.secondColor, builder.friendColor, builder.friendSecond, builder.gray, builder.grayLight, builder.foregroundLight, builder.whiteGray, builder.foregroundGray, builder.foregroundLightStroke, builder.foreground, builder.foregroundStroke, builder.foregroundDark, builder.white, builder.background, builder.glow, builder.blur, builder.corners, builder.useSecondColor);
    }

    public void reset() {
        this.color = this.defaults.color;
        this.secondColor = this.defaults.secondColor;
        this.friendColor = this.defaults.friendColor;
        this.useSecondColor = this.defaults.useSecondColor;
        this.gray = this.defaults.gray;
        this.grayLight = this.defaults.grayLight;
        this.foregroundLight = this.defaults.foregroundLight;
        this.whiteGray = this.defaults.whiteGray;
        this.foregroundGray = this.defaults.foregroundGray;
        this.foregroundLightStroke = this.defaults.foregroundLightStroke;
        this.foregroundColor = this.defaults.foreground;
        this.foregroundStroke = this.defaults.foregroundStroke;
        this.foregroundDark = this.defaults.foregroundDark;
        this.white = this.defaults.white;
        this.backgroundColor = this.defaults.background;
        this.glow = this.defaults.glow;
        this.blur = this.defaults.blur;
        this.corners = this.defaults.corners;
    }

    public Theme interpolateTheme(Theme other, float delta) {
        return ThemeBuilder.builder().color(this.color.mix(other.getColor(), delta)).secondColor(this.getSecondColor().mix(other.getSecondColor(), delta)).friendColor(this.friendColor.mix(other.getFriendColor(), delta)).gray(this.gray.mix(other.getGray(), delta)).grayLight(this.grayLight.mix(other.getGrayLight(), delta)).foregroundLight(this.foregroundLight.mix(other.getForegroundLight(), delta)).whiteGray(this.whiteGray.mix(other.getWhiteGray(), delta)).foregroundGray(this.foregroundGray.mix(other.getForegroundGray(), delta)).foregroundLightStroke(this.foregroundLightStroke.mix(other.getForegroundLightStroke(), delta)).foreground(this.foregroundColor.mix(other.getForegroundColor(), delta)).foregroundStroke(this.foregroundStroke.mix(other.getForegroundStroke(), delta)).foregroundDark(this.foregroundDark.mix(other.getForegroundDark(), delta)).white(this.white.mix(other.getWhite(), delta)).background(this.backgroundColor.mix(other.getBackgroundColor(), delta)).glow(this.glow).blur(this.blur).corners(this.corners).useSecondColor(other.useSecondColor).build(other.name, other.icon);
    }

    public ColorRGBA getSecondColor() {
        return this.useSecondColor ? this.secondColor : this.color;
    }

    public ColorRGBA getSecondColorRaw() {
        return this.secondColor;
    }

    public String getDisplayName() {
        return UiTranslation.translate(this.name);
    }

    @Generated
    public String getName() {
        return this.name;
    }

    @Generated
    public String getIcon() {
        return this.icon;
    }

    @Generated
    public Defaults getDefaults() {
        return this.defaults;
    }

    @Generated
    public ColorRGBA getColor() {
        return this.color;
    }

    @Generated
    public ColorRGBA getFriendColor() {
        return this.friendColor;
    }

    @Generated
    public boolean isUseSecondColor() {
        return this.useSecondColor;
    }

    @Generated
    public ColorRGBA getGray() {
        return this.gray;
    }

    @Generated
    public ColorRGBA getGrayLight() {
        return this.grayLight;
    }

    @Generated
    public ColorRGBA getForegroundLight() {
        return this.foregroundLight;
    }

    @Generated
    public ColorRGBA getWhiteGray() {
        return this.whiteGray;
    }

    @Generated
    public ColorRGBA getForegroundGray() {
        return this.foregroundGray;
    }

    @Generated
    public ColorRGBA getForegroundLightStroke() {
        return this.foregroundLightStroke;
    }

    @Generated
    public ColorRGBA getForegroundColor() {
        return this.foregroundColor;
    }

    @Generated
    public ColorRGBA getForegroundStroke() {
        return this.foregroundStroke;
    }

    @Generated
    public ColorRGBA getForegroundDark() {
        return this.foregroundDark;
    }

    @Generated
    public ColorRGBA getWhite() {
        return this.white;
    }

    @Generated
    public ColorRGBA getBackgroundColor() {
        return this.backgroundColor;
    }

    @Generated
    public boolean isGlow() {
        return this.glow;
    }

    @Generated
    public boolean isBlur() {
        return this.blur;
    }

    @Generated
    public boolean isCorners() {
        return this.corners;
    }

    @Generated
    public void setColor(ColorRGBA color) {
        this.color = color;
    }

    @Generated
    public void setSecondColor(ColorRGBA secondColor) {
        this.secondColor = secondColor;
    }

    @Generated
    public void setFriendColor(ColorRGBA friendColor) {
        this.friendColor = friendColor;
    }

    @Generated
    public void setUseSecondColor(boolean useSecondColor) {
        this.useSecondColor = useSecondColor;
    }

    @Generated
    public void setGray(ColorRGBA gray) {
        this.gray = gray;
    }

    @Generated
    public void setGrayLight(ColorRGBA grayLight) {
        this.grayLight = grayLight;
    }

    @Generated
    public void setForegroundLight(ColorRGBA foregroundLight) {
        this.foregroundLight = foregroundLight;
    }

    @Generated
    public void setWhiteGray(ColorRGBA whiteGray) {
        this.whiteGray = whiteGray;
    }

    @Generated
    public void setForegroundGray(ColorRGBA foregroundGray) {
        this.foregroundGray = foregroundGray;
    }

    @Generated
    public void setForegroundLightStroke(ColorRGBA foregroundLightStroke) {
        this.foregroundLightStroke = foregroundLightStroke;
    }

    @Generated
    public void setForegroundColor(ColorRGBA foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    @Generated
    public void setForegroundStroke(ColorRGBA foregroundStroke) {
        this.foregroundStroke = foregroundStroke;
    }

    @Generated
    public void setForegroundDark(ColorRGBA foregroundDark) {
        this.foregroundDark = foregroundDark;
    }

    @Generated
    public void setWhite(ColorRGBA white) {
        this.white = white;
    }

    @Generated
    public void setBackgroundColor(ColorRGBA backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    @Generated
    public void setGlow(boolean glow) {
        this.glow = glow;
    }

    @Generated
    public void setBlur(boolean blur) {
        this.blur = blur;
    }

    @Generated
    public void setCorners(boolean corners) {
        this.corners = corners;
    }

    public static class ThemeBuilder {
        private ColorRGBA color;
        private ColorRGBA secondColor;
        private ColorRGBA friendColor;
        private ColorRGBA friendSecond;
        private ColorRGBA gray;
        private ColorRGBA grayLight;
        private ColorRGBA foregroundLight;
        private ColorRGBA whiteGray;
        private ColorRGBA foregroundGray;
        private ColorRGBA foregroundLightStroke;
        private ColorRGBA foreground;
        private ColorRGBA foregroundStroke;
        private ColorRGBA foregroundDark;
        private ColorRGBA white;
        private ColorRGBA background;
        private boolean glow = false;
        private boolean blur = false;
        private boolean corners = false;
        private boolean useSecondColor = true;

        public static ThemeBuilder builder() {
            return new ThemeBuilder();
        }

        public ThemeBuilder color(ColorRGBA color) {
            this.color = color;
            return this;
        }

        public ThemeBuilder secondColor(ColorRGBA secondColor) {
            this.secondColor = secondColor;
            return this;
        }

        public ThemeBuilder friendColor(ColorRGBA friendColor) {
            this.friendColor = friendColor;
            return this;
        }

        public ThemeBuilder friendSecond(ColorRGBA friendSecond) {
            this.friendSecond = friendSecond;
            return this;
        }

        public ThemeBuilder gray(ColorRGBA gray) {
            this.gray = gray;
            return this;
        }

        public ThemeBuilder grayLight(ColorRGBA grayLight) {
            this.grayLight = grayLight;
            return this;
        }

        public ThemeBuilder foregroundLight(ColorRGBA foregroundLight) {
            this.foregroundLight = foregroundLight;
            return this;
        }

        public ThemeBuilder whiteGray(ColorRGBA whiteGray) {
            this.whiteGray = whiteGray;
            return this;
        }

        public ThemeBuilder foregroundGray(ColorRGBA foregroundGray) {
            this.foregroundGray = foregroundGray;
            return this;
        }

        public ThemeBuilder foregroundLightStroke(ColorRGBA foregroundLightStroke) {
            this.foregroundLightStroke = foregroundLightStroke;
            return this;
        }

        public ThemeBuilder foreground(ColorRGBA foreground) {
            this.foreground = foreground;
            return this;
        }

        public ThemeBuilder foregroundStroke(ColorRGBA foregroundStroke) {
            this.foregroundStroke = foregroundStroke;
            return this;
        }

        public ThemeBuilder foregroundDark(ColorRGBA foregroundDark) {
            this.foregroundDark = foregroundDark;
            return this;
        }

        public ThemeBuilder white(ColorRGBA white) {
            this.white = white;
            return this;
        }

        public ThemeBuilder background(ColorRGBA background) {
            this.background = background;
            return this;
        }

        public ThemeBuilder glow(boolean glow) {
            this.glow = glow;
            return this;
        }

        public ThemeBuilder blur(boolean blur) {
            this.blur = blur;
            return this;
        }

        public ThemeBuilder corners(boolean corners) {
            this.corners = corners;
            return this;
        }

        public ThemeBuilder useSecondColor(boolean useSecondColor) {
            this.useSecondColor = useSecondColor;
            return this;
        }

        public Theme build(String name, String icon) {
            return new Theme(name, icon, this);
        }
    }

    private record Defaults(ColorRGBA color, ColorRGBA secondColor, ColorRGBA friendColor, ColorRGBA friendSecond, ColorRGBA gray, ColorRGBA grayLight, ColorRGBA foregroundLight, ColorRGBA whiteGray, ColorRGBA foregroundGray, ColorRGBA foregroundLightStroke, ColorRGBA foreground, ColorRGBA foregroundStroke, ColorRGBA foregroundDark, ColorRGBA white, ColorRGBA background, boolean glow, boolean blur, boolean corners, boolean useSecondColor) {
    }
}

