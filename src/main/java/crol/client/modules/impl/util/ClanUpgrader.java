package crol.client.modules.impl.util;

import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.IDisableable;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.BooleanSetting;
import crol.client.modules.settings.impl.MultiBoxSetting;
import crol.client.util.IUtil;
import crol.client.util.math.TimerUtil;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

@Environment(EnvType.CLIENT)
public class ClanUpgrader extends Module implements ITickable, IUtil, IDisableable {
   private final MultiBoxSetting options = ((MultiBoxSetting)(new MultiBoxSetting()).name("Options")).booleanSettings(((BooleanSetting)(new BooleanSetting()).name("Torch")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Redstone")).value(false));
   private final TimerUtil timerUtil = new TimerUtil();

   public ClanUpgrader() {
      super(new ModuleInfo("ClanUpgrader", Category.UTIL, "Автоматически прокачивает уровень клана"));
      this.addSetting(new Setting[]{this.options});
   }

   public void onTick(TickEvent event) {
      if (event instanceof TickEvent && (this.getItems().contains(mc.player.getMainHandStack().getItem()) || this.getItems().contains(mc.player.getOffHandStack().getItem())) && this.timerUtil.isReached(100L)) {
         mc.options.useKey.setPressed(true);
         mc.options.attackKey.setPressed(true);
         this.timerUtil.reset();
      }

   }

   private List<Item> getItems() {
      List<Item> list = new ArrayList();
      if (this.options.getValueByName("Torch")) {
         list.add(Items.TORCH);
         list.add(Items.REDSTONE_TORCH);
      }

      if (this.options.getValueByName("Redstone")) {
         list.add(Items.REDSTONE);
      }

      return list;
   }

   public void onDisable() {
      mc.options.useKey.setPressed(false);
      mc.options.attackKey.setPressed(false);
   }
}
