package crol.client.util.player.inventory;

import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public record SearchInvResult(int slot, boolean found, ItemStack stack) implements IUtil {
   private static final SearchInvResult NOT_FOUND_RESULT = new SearchInvResult(-1, false, (ItemStack)null);

   public static SearchInvResult notFound() {
      return NOT_FOUND_RESULT;
   }

   public static @NotNull SearchInvResult inOffhand(ItemStack stack) {
      return new SearchInvResult(999, true, stack);
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
}
