package crol.client.util.player.inventory;

import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public record InventoryResult(int slot, boolean found, ItemStack stack) implements IUtil {
   private static final InventoryResult NOT_FOUND_RESULT = new InventoryResult(-1, false, (ItemStack)null);

   public static InventoryResult notFound() {
      return NOT_FOUND_RESULT;
   }

   public static @NotNull InventoryResult inOffhand(ItemStack stack) {
      return new InventoryResult(999, true, stack);
   }

   public boolean isHolding() {
      if (mc.player == null) {
         return false;
      } else {
         return mc.player.getInventory().selectedSlot == this.slot;
      }
   }

   public boolean isInHotBar() {
      return this.slot < 9;
   }

   public void switchTo() {
      if (this.found && this.isInHotBar()) {
         InventoryToolkit.switchTo(this.slot);
      }

   }

   public void switchToSilent() {
      if (this.found && this.isInHotBar()) {
         InventoryToolkit.switchToSilent(this.slot);
      }

   }
}
