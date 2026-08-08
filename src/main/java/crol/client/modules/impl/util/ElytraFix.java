package crol.client.modules.impl.util;

import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

@Environment(EnvType.CLIENT)
public class ElytraFix extends Module implements ITickable, IUtil {
   public static long delay;

   public ElytraFix() {
      super(new ModuleInfo("ElytraFix", Category.UTIL, "NoDesc"));
   }

   public void onTick(TickEvent event) {
      PlayerEntity player = mc.player;
      if (player != null) {
         ItemStack carried = player.currentScreenHandler.getCursorStack();
         Item var5 = carried.getItem();
         if (var5 instanceof ArmorItem) {
            ArmorItem ia = (ArmorItem)var5;
            if (System.currentTimeMillis() > delay) {
               EquippableComponent equippable = (EquippableComponent)carried.get(DataComponentTypes.EQUIPPABLE);
               if (equippable != null && equippable.slot() == EquipmentSlot.CHEST && player.getInventory().getArmorStack(2).getItem() == Items.ELYTRA) {
                  mc.interactionManager.clickSlot(player.currentScreenHandler.syncId, 6, 1, SlotActionType.PICKUP, player);
                  int nullSlot = findNullSlot();
                  boolean needDrop = nullSlot == 999;
                  if (needDrop) {
                     nullSlot = 9;
                  }

                  mc.interactionManager.clickSlot(player.currentScreenHandler.syncId, nullSlot, 1, SlotActionType.PICKUP, player);
                  if (needDrop) {
                     mc.interactionManager.clickSlot(player.currentScreenHandler.syncId, -999, 1, SlotActionType.PICKUP, player);
                  }

                  delay = System.currentTimeMillis() + 300L;
               }
            }
         }

      }
   }

   public static int findNullSlot() {
      PlayerEntity player = mc.player;
      if (player == null) {
         return 999;
      } else {
         PlayerInventory inv = player.getInventory();

         for(int i = 0; i < 36; ++i) {
            if (inv.getStack(i).isEmpty()) {
               return i < 9 ? i + 36 : i;
            }
         }

         return 999;
      }
   }
}
