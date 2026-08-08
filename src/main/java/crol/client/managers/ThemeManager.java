package crol.client.managers;

import crol.client.ui.themes.Theme;
import crol.client.util.color.SmoothColorTransition;
import crol.client.util.render.SkyConfig;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class ThemeManager {
   private final List<Theme> themes = new ArrayList();
   private Theme theme;
   private final SmoothColorTransition smoothColorTransition;

   public ThemeManager() {
      this.init();
      this.theme = (Theme)this.themes.get(0);
      this.smoothColorTransition = new SmoothColorTransition(this.theme.color());
      SkyConfig.BASE_COLOR = this.theme.color();
      SkyConfig.compute();
   }

   @Compile
   private void init() {
      this.addThemes(new Theme(new Color(-6375937, true)), new Theme(new Color(-359806, true)), new Theme(new Color(-24854, true)), new Theme(new Color(-126, true)), new Theme(new Color(-17278, true)), new Theme(new Color(-7437569, true)), new Theme(new Color(-6364417, true)), new Theme(new Color(-8192089, true)), new Theme(new Color(-8192026, true)), new Theme(new Color(-32076, true)));
   }

   private void addThemes(Theme... themes) {
      this.themes.addAll(Arrays.asList(themes));
   }

   public List<Theme> getThemes() {
      return this.themes;
   }

   public Theme getTheme() {
      if (this.theme.color() != this.smoothColorTransition.getCurrentColor()) {
         this.theme = new Theme(this.smoothColorTransition.getCurrentColor());
         SkyConfig.BASE_COLOR = this.theme.color();
         SkyConfig.compute();
      }

      return this.theme;
   }

   public void setTheme(Theme theme) {
      this.smoothColorTransition.transitionTo(theme.color());
      this.theme = theme;
   }
}
