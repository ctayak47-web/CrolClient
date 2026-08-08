package crol.client.util.player.inventory;

import crol.client.util.IUtil;
import crol.client.util.math.MathUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.item.AxeItem;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class InventoryUtil implements IUtil {
   @Compile
   public static void clickSlot(Slot slot, int button, SlotActionType clickType, boolean silent) {
      if (slot != null) {
         clickSlot(slot.id, button, clickType, silent);
      }

   }

   @Compile
   public static void clickSlot(int slotId, int buttonId, SlotActionType clickType, boolean silent) {
      clickSlot(mc.player.currentScreenHandler.syncId, slotId, buttonId, clickType, silent);
   }

   @Compile
   public static void clickSlot(int windowId, int slotId, int buttonId, SlotActionType clickType, boolean silent) {
      mc.interactionManager.clickSlot(windowId, slotId, buttonId, clickType, mc.player);
      if (silent) {
         mc.player.currentScreenHandler.onSlotClick(slotId, buttonId, clickType, mc.player);
      }

   }

   @Compile
   public static int getAxeInInventoryOrHotbar(boolean inHotBar) {
      int firstSlot = inHotBar ? 0 : 9;
      int lastSlot = inHotBar ? 9 : 36;
      int finalSlot = -1;

      for(int i = firstSlot; i < lastSlot; ++i) {
         if (mc.player.getInventory().getStack(i).getItem() instanceof AxeItem) {
            finalSlot = i;
         }
      }

      return finalSlot;
   }

   @Compile
   public static void sendFireWork(float yaw, float pitch) {
      for(int i = 0; i < 9; ++i) {
         ItemStack itemStack = mc.player.getInventory().getStack(i);
         if (itemStack.getItem() instanceof FireworkRocketItem) {
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(i));
            mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, i, yaw, pitch));
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
            return;
         }
      }

   }

   @Compile
   public static int findBestSlotInHotBar() {
      int emptySlot = findEmptySlot();
      return emptySlot != -1 ? emptySlot : findNonSwordSlot();
   }

   @Compile
   private static int findEmptySlot() {
      for(int i = 0; i < 9; ++i) {
         if (mc.player.getInventory().getStack(i).isEmpty() && mc.player.getInventory().selectedSlot != i) {
            return i;
         }
      }

      return -1;
   }

   @Compile
   private static int findNonSwordSlot() {
      for(int i = 0; i < 9; ++i) {
         if (!(mc.player.getInventory().getStack(i).getItem() instanceof SwordItem) && mc.player.getInventory().getStack(i).getItem() != Items.ELYTRA && mc.player.getInventory().selectedSlot != i) {
            return i;
         }
      }

      return -1;
   }

   public static SearchInvResult findItemInInventory(List<Item> items) {
      return findInInventory((stack) -> items.contains(stack.getItem()));
   }

   public static SearchInvResult findInInventory(InventoryToolkit.Searcher searcher) {
      if (mc.player != null) {
         for(int i = 36; i >= 0; --i) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (searcher.isValid(stack)) {
               if (i < 9) {
                  i += 36;
               }

               return new SearchInvResult(i, true, stack);
            }
         }
      }

      return SearchInvResult.notFound();
   }

   public static SearchInvResult findItemInInventory(Item... items) {
      return findItemInInventory(Arrays.asList(items));
   }

   public static Stream<Slot> slots() {
      return mc.player.currentScreenHandler.slots.stream();
   }

   public static Slot getSlot(Item item, Comparator<Slot> comparator, Predicate<Slot> filter) {
      return (Slot)slots().filter((s) -> s.getStack().getItem().equals(item)).filter(filter).max(comparator).orElse((Slot) null);
   }

   public static void closeScreen(boolean packet) {
      if (packet) {
         mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
      } else {
         mc.player.closeHandledScreen();
      }

   }

   public static void swapHand(Slot slot, Hand hand, boolean updateInventory) {
      if (slot != null && slot.id != -1 && (!hand.equals(Hand.OFF_HAND) || slot.inventory instanceof PlayerInventory || slot.inventory instanceof EnderChestInventory)) {
         int button = hand.equals(Hand.MAIN_HAND) ? mc.player.getInventory().selectedSlot : 40;
         swapHand(slot, button, updateInventory);
      }
   }

   public static void swapHand(Slot slot, int button, boolean updateInventory) {
      clickSlot(slot, button, SlotActionType.SWAP, false);
      if (updateInventory) {
         updateSlots();
      }

   }

   public static void updateSlots() {
      ScreenHandler screenHandler = mc.player.currentScreenHandler;
      ItemStack stack = ((Item)Registries.ITEM.get(MathUtil.getRandom(0, 100))).getDefaultStack();
      mc.player.networkHandler.sendPacket(new ClickSlotC2SPacket(screenHandler.syncId, screenHandler.getRevision(), 0, 0, SlotActionType.PICKUP_ALL, stack, Int2ObjectMaps.singleton(0, stack)));
   }

   public void swapHand(Slot slot, int button) {
      clickSlot(slot, button, SlotActionType.SWAP, false);
   }
}
