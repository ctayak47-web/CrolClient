package crol.client.modules.impl.util;

import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.BooleanSetting;
import crol.client.modules.settings.impl.FloatSetting;
import crol.client.modules.settings.impl.ModeSetting;
import crol.client.modules.settings.impl.MultiBoxSetting;
import crol.client.util.IUtil;
import crol.client.util.math.TimerUtil;
import crol.client.util.player.inventory.InventoryUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class ChestStealer extends Module implements ITickable, IUtil {
   private final ModeSetting mode = ((ModeSetting)(new ModeSetting()).name("Type")).value("FunTime").modes("FunTime", "WhiteList", "Default");
   private final FloatSetting delay = ((FloatSetting)(new FloatSetting() {
      public boolean isVisible() {
         return ChestStealer.this.mode.is("WhiteList") || ChestStealer.this.mode.is("Default");
      }
   }).name("Delay")).minValue(0.0F).maxValue(1000.0F).incriment(10.0F).value(100.0F);
   private final MultiBoxSetting items = ((MultiBoxSetting)(new MultiBoxSetting() {
      public boolean isVisible() {
         return ChestStealer.this.mode.is("WhiteList") || ChestStealer.this.mode.is("Default");
      }
   }).name("Items")).booleanSettings(((BooleanSetting)(new BooleanSetting()).name("Player Head")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Totem Of Undying")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Elytra")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Netherite Sword")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Netherite Helmet")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Netherite ChestPlate")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Netherite Leggings")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Netherite Boots")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Netherite Ingot")).value(false), ((BooleanSetting)(new BooleanSetting()).name("Netherite Scrap")).value(false));
   private final TimerUtil timerUtil = new TimerUtil();

   public ChestStealer() {
      super(new ModuleInfo("ChestStealer", Category.UTIL, "Забирает все предметы с сундука"));
      this.addSetting(new Setting[]{this.mode, this.delay, this.items});
   }

   @Compile
   public void onTick(TickEvent event) {
      switch (this.mode.getValue()) {
         case "FunTime":
            Screen var7 = mc.currentScreen;
            if (var7 instanceof GenericContainerScreen sh) {
               if (sh.getTitle().getString().toLowerCase().contains("мистический") && !mc.player.getItemCooldownManager().isCoolingDown(Items.GUNPOWDER.getDefaultStack())) {
                  ((GenericContainerScreenHandler)sh.getScreenHandler()).slots.stream().filter((s) -> s.hasStack() && !s.inventory.equals(mc.player.getInventory()) && this.timerUtil.every((double)150.0F)).forEach((s) -> InventoryUtil.clickSlot(s, 0, SlotActionType.QUICK_MOVE, true));
               }
            }
            break;
         case "WhiteList":
         case "Default":
            ScreenHandler var5 = mc.player.currentScreenHandler;
            if (var5 instanceof GenericContainerScreenHandler sh) {
               sh.slots.forEach((s) -> {
                  if (s.hasStack() && !s.inventory.equals(mc.player.getInventory()) && (this.mode.is("Default") || this.whiteList(s.getStack().getItem())) && this.timerUtil.every((double)this.delay.getValue())) {
                     InventoryUtil.clickSlot(s, 0, SlotActionType.QUICK_MOVE, true);
                  }

               });
            }
      }

   }

   private boolean whiteList(Item item) {
      BooleanSetting value = (BooleanSetting)this.items.getBooleanSettings().stream().filter((booleanSetting) -> booleanSetting.getName().toLowerCase().contains(item.toString().toLowerCase().replace("_", ""))).findFirst().orElse(((BooleanSetting)(new BooleanSetting()).name("s")).value(false));
      return value.getValue();
   }
}
