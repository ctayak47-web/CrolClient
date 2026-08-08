package crol.client.modules.impl.combat;

import crol.client.CrolClient;
import crol.client.event.classes.ClickEvent;
import crol.client.event.interfaces.IClickaable;
import crol.client.modules.Category;
import crol.client.modules.IEnableable;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.impl.movement.Sprint;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.KeySetting;
import crol.client.modules.settings.impl.ModeSetting;
import crol.client.util.IUtil;
import crol.client.util.math.TimerUtil;
import crol.client.util.player.inventory.InventoryUtil;
import java.util.Comparator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Hand;

@Environment(EnvType.CLIENT)
public class AutoSwap extends Module implements IClickaable, IUtil, IEnableable {
   private final ModeSetting mode = ((ModeSetting)(new ModeSetting()).name("Item")).modes("Shield", "GApple", "Totem", "Head", "EnchantedGApple", "Bow").value("Shield");
   private final ModeSetting swapTo = ((ModeSetting)(new ModeSetting()).name("SwapTo")).modes("GApple", "Shield", "Totem", "Head", "EnchantedGApple", "Bow").value("GApple");
   private final KeySetting bind = ((KeySetting)(new KeySetting()).name("Bind")).value(0);
   private final TimerUtil timerUtil = new TimerUtil();
   private Sprint sprint;

   public AutoSwap() {
      super(new ModuleInfo("AutoSwap", Category.COMBAT, "Чередует выбранные предметы во второй руке"));
      this.addSetting(new Setting[]{this.mode, this.swapTo, this.bind});
   }

   private Item getItemByType(String itemType) {
      Item var10000;
      switch (itemType) {
         case "Shield" -> var10000 = Items.SHIELD;
         case "Totem" -> var10000 = Items.TOTEM_OF_UNDYING;
         case "GApple" -> var10000 = Items.GOLDEN_APPLE;
         case "Head" -> var10000 = Items.PLAYER_HEAD;
         case "EnchantedGApple" -> var10000 = Items.ENCHANTED_GOLDEN_APPLE;
         case "Bow" -> var10000 = Items.BOW;
         default -> var10000 = Items.AIR;
      }

      return var10000;
   }

   public void onClick(ClickEvent event) {
      if (mc.currentScreen == null && this.bind.getValue() == event.getKey() && event.getAction() == 1) {
         this.sprint.setCanSprint(false);
         Slot first = InventoryUtil.getSlot(this.getItemByType(this.mode.getValue()), Comparator.comparing((s) -> s.getStack().hasEnchantments()), (s) -> s.id != 46 && s.id != 45);
         Slot second = InventoryUtil.getSlot(this.getItemByType(this.swapTo.getValue()), Comparator.comparing((s) -> s.getStack().hasEnchantments()), (s) -> s.id != 46 && s.id != 45);
         Slot validSlot = first != null && mc.player.getOffHandStack().getItem() != first.getStack().getItem() ? first : second;
         InventoryUtil.swapHand(validSlot, Hand.OFF_HAND, false);
         InventoryUtil.closeScreen(true);
         this.sprint.getTimerUtil().reset();
         this.sprint.setCanSprint(true);
      }

   }

   public void onEnable() {
      this.sprint = (Sprint)CrolClient.INSTANCE.getModuleManager().getByClass(Sprint.class);
   }
}
