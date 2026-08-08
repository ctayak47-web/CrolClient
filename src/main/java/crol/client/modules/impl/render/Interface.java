package crol.client.modules.impl.render;

import crol.client.event.classes.HudRenderEvent;
import crol.client.event.interfaces.IHudRenderable;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.BooleanSetting;
import crol.client.modules.settings.impl.MultiBoxSetting;
import crol.client.ui.hud.impl.BindsElement;
import crol.client.ui.hud.impl.NotifyElement;
import crol.client.ui.hud.impl.PotionsElement;
import crol.client.ui.hud.impl.TargetHudElement;
import crol.client.ui.hud.impl.WatermarkElement;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Interface extends Module implements IHudRenderable {
   private final MultiBoxSetting elements = ((MultiBoxSetting)(new MultiBoxSetting()).name("Elements")).booleanSettings(((BooleanSetting)(new BooleanSetting()).name("Watermark")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Binds")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Potions")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Staff")).value(false), ((BooleanSetting)(new BooleanSetting()).name("TargetHUD")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Scoreboard")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Hotbar")).value(true), ((BooleanSetting)(new BooleanSetting()).name("Music")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Notify")).value(false));
   private final WatermarkElement watermarkElement = new WatermarkElement();
   private final PotionsElement potionsElement = new PotionsElement();
   private final TargetHudElement targetHudElement = new TargetHudElement();
   private final BindsElement bindsElement = new BindsElement();
   private final NotifyElement notifyElement = new NotifyElement();

   public Interface() {
      super(new ModuleInfo("Interface", Category.RENDER, "Показывает худ чита"));
      this.addSetting(new Setting[]{this.elements});
      this.setEnabled(true);
   }

   public MultiBoxSetting getElements() {
      return this.elements;
   }

   public void onHudRender(HudRenderEvent event) {
      if (this.elements.getValueByName("Watermark")) {
         this.watermarkElement.render(event.getDrawContext());
      }

      if (this.elements.getValueByName("Potions")) {
         this.potionsElement.render(event.getDrawContext());
      }

      if (this.elements.getValueByName("TargetHUD")) {
         this.targetHudElement.render(event.getDrawContext());
      }

      if (this.elements.getValueByName("Binds")) {
         this.bindsElement.render(event.getDrawContext());
      }

      if (this.elements.getValueByName("Notify")) {
         this.notifyElement.render(event.getDrawContext());
      }

   }
}
