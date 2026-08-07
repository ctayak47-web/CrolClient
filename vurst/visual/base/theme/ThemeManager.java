
package vurst.visual.base.theme;

import com.darkmagician6.eventapi.EventManager;
import com.darkmagician6.eventapi.EventTarget;
import java.util.Arrays;
import java.util.List;
import lombok.Generated;
import vurst.visual.base.animations.base.Animation;
import vurst.visual.base.animations.base.Easing;
import vurst.visual.base.animations.types.ColorCycleRGBA;
import vurst.visual.base.events.impl.render.EventHudRender;
import vurst.visual.base.theme.Theme;
import vurst.visual.utility.render.display.base.Gradient;
import vurst.visual.utility.render.display.base.color.ColorRGBA;
import vurst.visual.utility.render.display.base.color.ColorUtil;

public class ThemeManager {
    private Theme prevTheme;
    private Theme currentTheme = Theme.DARK;
    private final Animation animation;
    private final ColorCycleRGBA colorCycleIcon;
    private final List<Theme> themes = Arrays.asList(Theme.DARK, Theme.LIGHT, Theme.CUSTOM_THEME);
    private int themeIndex = 0;

    public ThemeManager() {
        EventManager.register(this);
        this.animation = new Animation(200L, 1.0f, Easing.LINEAR);
        this.animation.setDone(true);
        this.colorCycleIcon = new ColorCycleRGBA(List.of(this.getCurrentTheme().getColor(), this.getCurrentTheme().getColor(), this.getCurrentTheme().getColor(), this.getCurrentTheme().getColor()), 500L);
    }

    public void switchTheme(Theme theme) {
        if (this.animation.isDone()) {
            this.prevTheme = this.currentTheme;
            this.currentTheme = theme;
            int index = this.themes.indexOf(theme);
            if (index != -1) {
                this.themeIndex = index;
            }
            this.animation.reset();
        }
    }

    public void switchTheme() {
        if (this.animation.isDone()) {
            this.prevTheme = this.currentTheme;
            this.themeIndex = (this.themeIndex + 1) % this.themes.size();
            this.currentTheme = this.themes.get(this.themeIndex);
            this.animation.reset();
        }
    }

    public void switchThemeByName(String name) {
        Theme theme;
        if (name == null) {
            return;
        }
        switch (name) {
            case "Dark": {
                theme = Theme.DARK;
                break;
            }
            case "Light": {
                theme = Theme.LIGHT;
                break;
            }
            case "Кастом": {
                theme = Theme.CUSTOM_THEME;
                break;
            }
            default: {
                return;
            }
        }
        this.switchTheme(theme);
    }

    @EventTarget
    public void eventRender(EventHudRender event) {
        this.setColorCycle(this.colorCycleIcon.getBaseColors(), this.getCurrentTheme().getColor(), this.getCurrentTheme().getColor(), this.getCurrentTheme().getColor(), this.getCurrentTheme().getColor());
        this.colorCycleIcon.update();
        this.animation.update(1.0f);
    }

    private void setColorCycle(List<ColorRGBA> colorCycle, ColorRGBA one, ColorRGBA two, ColorRGBA three, ColorRGBA four) {
        colorCycle.set(0, one);
        colorCycle.set(1, two);
        colorCycle.set(2, three);
        colorCycle.set(3, four);
    }

    public Theme getCurrentTheme() {
        return this.animation.isDone() ? this.currentTheme : this.prevTheme.interpolateTheme(this.currentTheme, this.animation.getValue());
    }

    public boolean is(Theme theme) {
        return this.currentTheme == theme;
    }

    public Gradient getClientColor() {
        return Gradient.of(this.getClientColor(0), this.getClientColor(90), this.getClientColor(180), this.getClientColor(270));
    }

    public ColorRGBA getClientColor(int index) {
        return ColorUtil.lerp(4, index, this.getCurrentTheme().getColor(), this.getCurrentTheme().getSecondColor());
    }

    @Generated
    public Theme getPrevTheme() {
        return this.prevTheme;
    }

    @Generated
    public Animation getAnimation() {
        return this.animation;
    }

    @Generated
    public ColorCycleRGBA getColorCycleIcon() {
        return this.colorCycleIcon;
    }

    @Generated
    public List<Theme> getThemes() {
        return this.themes;
    }

    @Generated
    public int getThemeIndex() {
        return this.themeIndex;
    }
}

