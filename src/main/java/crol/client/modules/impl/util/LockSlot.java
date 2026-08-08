package crol.client.modules.impl.util;

import crol.client.event.classes.DropItemEvent;
import crol.client.event.interfaces.IDropable;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.BooleanSetting;
import crol.client.modules.settings.impl.MultiBoxSetting;
import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class LockSlot extends Module implements IDropable, IUtil {
   private final MultiBoxSetting lockSlots = ((MultiBoxSetting)(new MultiBoxSetting()).name("Lock Slot")).booleanSettings(((BooleanSetting)(new BooleanSetting()).name("Slot 1")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Slot 2")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Slot 3")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Slot 4")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Slot 5")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Slot 6")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Slot 7")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Slot 8")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Slot 9")).value(false));

   public LockSlot() {
      super(new ModuleInfo("LockSlot", Category.UTIL, "Не позволяет выбрасывать вещи из выбранных слотов хотбара"));
      this.addSetting(new Setting[]{this.lockSlots});
   }

   public void onDrop(DropItemEvent event) {
      int currentSlot = mc.player.getInventory().selectedSlot;
      if (this.lockSlots.getValueByName("Slot " + (currentSlot + 1))) {
         event.cancel();
      }

   }
}
