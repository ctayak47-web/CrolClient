package crol.client.modules.impl.util;

import crol.client.event.classes.ClickEvent;
import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.IClickaable;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.IEnableable;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.KeySetting;
import crol.client.util.IUtil;
import crol.client.util.math.TimerUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ItemStack;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class ClickPearl extends Module implements ITickable, IEnableable, IUtil, IClickaable {
   private TimerUtil timerUtil = new TimerUtil();
   private final KeySetting bind = ((KeySetting)(new KeySetting()).name("Bind")).value(0);
   private static int curSlot = 0;
   private boolean changed = false;

   public ClickPearl() {
      super(new ModuleInfo("ClickPearl", Category.UTIL, "Бросает эндер жемчуг по нажатию кнопки"));
      this.addSetting(new Setting[]{this.bind});
   }

   @Compile
   public void onTick(TickEvent event) {
      if (this.changed && this.timerUtil.isReached(100L)) {
         mc.options.useKey.setPressed(false);
         mc.player.getInventory().selectedSlot = curSlot;
         this.changed = false;
      }

   }

   @Compile
   public void onEnable() {
      curSlot = mc.player.getInventory().selectedSlot;
   }

   public void onClick(ClickEvent event) {
      if (mc.currentScreen == null && event.getAction() == 1 && event.getKey() == this.bind.getValue()) {
         for(int i = 0; i < 9; ++i) {
            if (((ItemStack)mc.player.getInventory().main.get(i)).getItem() instanceof EnderPearlItem) {
               curSlot = mc.player.getInventory().selectedSlot;
               mc.player.getInventory().selectedSlot = i;
               mc.options.useKey.setPressed(true);
               this.timerUtil.reset();
               this.changed = true;
            }
         }
      }

   }
}
