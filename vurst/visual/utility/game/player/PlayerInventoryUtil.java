
package vurst.visual.utility.game.player;

import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.IntPredicate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.Generated;
import net.minecraft.Hand;
import net.minecraft.StatusEffect;
import net.minecraft.PlayerEntity;
import net.minecraft.PlayerInventory;
import net.minecraft.ScreenHandler;
import net.minecraft.SlotActionType;
import net.minecraft.EnderChestInventory;
import net.minecraft.Slot;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.PotionContentsComponent;
import net.minecraft.Packet;
import net.minecraft.ClickSlotC2SPacket;
import net.minecraft.CloseHandledScreenC2SPacket;
import net.minecraft.KeyBinding;
import net.minecraft.StatusEffectCategory;
import net.minecraft.FoodComponent;
import net.minecraft.RegistryEntry;
import net.minecraft.Registries;
import net.minecraft.DataComponentTypes;
import vurst.visual.VurstVisual;
import vurst.visual.utility.game.player.PlayerIntersectionUtil;
import vurst.visual.utility.game.player.PlayerInventoryComponent;
import vurst.visual.utility.game.player.rotation.Rotation;
import vurst.visual.utility.interfaces.IClient;
import vurst.visual.utility.math.MathUtil;

public final class PlayerInventoryUtil
implements IClient {
    public static final List<KeyBinding> moveKeys = List.of(PlayerInventoryUtil.mc.options.forwardKey, PlayerInventoryUtil.mc.options.backKey, PlayerInventoryUtil.mc.options.leftKey, PlayerInventoryUtil.mc.options.rightKey, PlayerInventoryUtil.mc.options.jumpKey);

    public static void updateSlots() {
        ScreenHandler screenHandler = PlayerInventoryUtil.mc.player.currentScreenHandler;
        ItemStack stack = ((Item)Registries.ITEM.get((int)MathUtil.getRandom(0.0, 100.0))).getDefaultStack();
        PlayerInventoryUtil.mc.player.networkHandler.sendPacket((Packet)new ClickSlotC2SPacket(screenHandler.syncId, screenHandler.getRevision(), 0, 0, SlotActionType.PICKUP_ALL, stack, Int2ObjectMaps.singleton((int)0, (Object)stack)));
    }

    public static void closeScreen(boolean packet) {
        if (packet) {
            PlayerInventoryUtil.mc.player.networkHandler.sendPacket((Packet)new CloseHandledScreenC2SPacket(PlayerInventoryUtil.mc.player.currentScreenHandler.syncId));
        } else {
            PlayerInventoryUtil.mc.player.closeHandledScreen();
        }
    }

    public static void swapHand(Slot slot, Hand hand, boolean updateInventory) {
        if (slot == null || slot.id == -1 || hand.equals((Object)Hand.OFF_HAND) && !(slot.inventory instanceof PlayerInventory) && !(slot.inventory instanceof EnderChestInventory)) {
            return;
        }
        int button = hand.equals((Object)Hand.MAIN_HAND) ? PlayerInventoryUtil.mc.player.getInventory().selectedSlot : 40;
        PlayerInventoryUtil.swapHand(slot, button, updateInventory);
    }

    public static void swapHand(Slot slot, int button, boolean updateInventory) {
        PlayerInventoryUtil.clickSlot(slot, button, SlotActionType.SWAP, false);
        if (updateInventory) {
            PlayerInventoryUtil.updateSlots();
        }
    }

    public static void swapHand(Slot slot, int button) {
        PlayerInventoryUtil.clickSlot(slot, button, SlotActionType.SWAP, false);
    }

    public static void clickSlot(Slot slot, int button, SlotActionType clickType, boolean silent) {
        if (slot != null) {
            PlayerInventoryUtil.clickSlot(slot.id, button, clickType, silent);
        }
    }

    public static void clickSlot(int slotId, int buttonId, SlotActionType clickType, boolean silent) {
        PlayerInventoryUtil.clickSlot(PlayerInventoryUtil.mc.player.currentScreenHandler.syncId, slotId, buttonId, clickType, silent);
    }

    public static void clickSlot(int windowId, int slotId, int buttonId, SlotActionType clickType, boolean silent) {
        PlayerInventoryUtil.mc.interactionManager.clickSlot(windowId, slotId, buttonId, clickType, (PlayerEntity)PlayerInventoryUtil.mc.player);
        if (silent) {
            PlayerInventoryUtil.mc.player.currentScreenHandler.onSlotClick(slotId, buttonId, clickType, (PlayerEntity)PlayerInventoryUtil.mc.player);
        }
    }

    public static Slot getSlot(Item item) {
        return PlayerInventoryUtil.getSlot(item, (Slot s) -> true);
    }

    public static Slot getSlot(Item item, Predicate<Slot> filter) {
        return PlayerInventoryUtil.getSlot(item, Comparator.comparingInt(s -> 0), filter);
    }

    public static Slot getSlot(Predicate<Slot> filter) {
        return PlayerInventoryUtil.slots().filter(filter).findFirst().orElse(null);
    }

    public static Slot getSlot(Predicate<Slot> filter, Comparator<Slot> comparator) {
        return PlayerInventoryUtil.slots().filter(filter).max(comparator).orElse(null);
    }

    public static Slot getSlot(Item item, Comparator<Slot> comparator, Predicate<Slot> filter) {
        return PlayerInventoryUtil.slots().filter(s -> s.getStack().getItem().equals(item)).filter(filter).max(comparator).orElse(null);
    }

    public static Slot getFoodMaxSaturationSlot() {
        return PlayerInventoryUtil.slots().filter(s -> s.getStack().get(DataComponentTypes.FOOD) != null && !((FoodComponent)s.getStack().get(DataComponentTypes.FOOD)).comp_2493()).max(Comparator.comparingDouble(s -> ((FoodComponent)s.getStack().get(DataComponentTypes.FOOD)).comp_2492())).orElse(null);
    }

    public static Slot getSlot(List<Item> item) {
        return PlayerInventoryUtil.slots().filter(s -> item.contains(s.getStack().getItem())).findFirst().orElse(null);
    }

    public static Slot getPotion(RegistryEntry<StatusEffect> effect) {
        return PlayerInventoryUtil.slots().filter(s -> {
            PotionContentsComponent component = (PotionContentsComponent)s.getStack().get(DataComponentTypes.POTION_CONTENTS);
            if (component == null) {
                return false;
            }
            return StreamSupport.stream(component.getEffects().spliterator(), false).anyMatch(e -> e.getEffectType().equals((Object)effect));
        }).findFirst().orElse(null);
    }

    public static Slot getPotionFromCategory(StatusEffectCategory category) {
        return PlayerInventoryUtil.slots().filter(s -> {
            ItemStack stack = s.getStack();
            PotionContentsComponent component = (PotionContentsComponent)stack.get(DataComponentTypes.POTION_CONTENTS);
            if (!stack.getItem().equals(Items.SPLASH_POTION) || component == null) {
                return false;
            }
            StatusEffectCategory category2 = category.equals((Object)StatusEffectCategory.BENEFICIAL) ? StatusEffectCategory.HARMFUL : StatusEffectCategory.BENEFICIAL;
            long effects = StreamSupport.stream(component.getEffects().spliterator(), false).filter(e -> ((StatusEffect)e.getEffectType().comp_349()).getCategory().equals((Object)category)).count();
            long effects2 = StreamSupport.stream(component.getEffects().spliterator(), false).filter(e -> ((StatusEffect)e.getEffectType().comp_349()).getCategory().equals((Object)category2)).count();
            return effects >= effects2;
        }).findFirst().orElse(null);
    }

    public static int getInventoryCount(Item item) {
        return IntStream.range(0, 45).filter(i -> Objects.requireNonNull(PlayerInventoryUtil.mc.player).getInventory().getStack(i).getItem().equals(item)).map(i -> PlayerInventoryUtil.mc.player.getInventory().getStack(i).getCount()).sum();
    }

    public static int getHotbarItems(List<Item> items) {
        return IntStream.range(0, 9).filter(i -> items.contains(PlayerInventoryUtil.mc.player.getInventory().getStack(i).getItem())).findFirst().orElse(-1);
    }

    public static int getHotbarSlotId(IntPredicate filter) {
        return IntStream.range(0, 9).filter((java.util.function.IntPredicate)filter).findFirst().orElse(-1);
    }

    public static int getCount(Predicate<Slot> filter) {
        return PlayerInventoryUtil.slots().filter(filter).mapToInt(s -> s.getStack().getCount()).sum();
    }

    public static Slot mainHandSlot() {
        long count = PlayerInventoryUtil.slots().count();
        int i = count == 46L ? 10 : 9;
        return PlayerInventoryUtil.slots().toList().get(Math.toIntExact(count - (long)i + (long)PlayerInventoryUtil.mc.player.getInventory().selectedSlot));
    }

    public static boolean isServerScreen() {
        return PlayerInventoryUtil.slots().toList().size() != 46;
    }

    public static Stream<Slot> slots() {
        return PlayerInventoryUtil.mc.player.currentScreenHandler.slots.stream();
    }

    public static void swapAndUse(Item item) {
        PlayerInventoryUtil.swapAndUse(item, VurstVisual.getInstance().getRotationManager().getCurrentRotation());
    }

    public static void swapAndUse(Item item, Rotation angle) {
        float cooldownProgress = PlayerInventoryUtil.mc.player.getItemCooldownManager().getCooldownProgress(item.getDefaultStack(), 0.0f);
        if (cooldownProgress > 0.0f) {
            String time = MathUtil.round(cooldownProgress, 0.1) + "s";
            return;
        }
        Slot slot = PlayerInventoryUtil.getSlot(item);
        if (slot == null) {
            return;
        }
        PlayerInventoryComponent.addTask(() -> PlayerInventoryUtil.swapAndUse(slot, angle));
    }

    public static void swapAndUse(Slot slot, Rotation angle) {
        PlayerInventoryUtil.swapHand(slot, Hand.MAIN_HAND, false);
        PlayerInventoryUtil.closeScreen(true);
        PlayerIntersectionUtil.useItem(Hand.MAIN_HAND, angle);
        PlayerInventoryUtil.swapHand(slot, Hand.MAIN_HAND, false);
        PlayerInventoryUtil.closeScreen(true);
    }

    public static void moveItem(Slot from, int to) {
        if (from != null) {
            PlayerInventoryUtil.moveItem(from.id, to, false, false);
        }
    }

    public static void moveItem(Slot from, int to, boolean task) {
        PlayerInventoryUtil.moveItem(from, to, task, false);
    }

    public static void moveItem(Slot from, int to, boolean task, boolean updateInventory) {
        if (from != null) {
            PlayerInventoryUtil.moveItem(from.id, to, task, updateInventory);
        }
    }

    public static void moveItem(int from, int to, boolean task, boolean updateInventory) {
        if (from == to || from == -1) {
            return;
        }
        int count = Math.toIntExact(PlayerInventoryUtil.slots().count()) - 9;
        if (from >= count && count == 36) {
            if (task) {
                PlayerInventoryComponent.addTask(() -> PlayerInventoryUtil.clickSlot(to, from - count, SlotActionType.SWAP, false));
            } else {
                PlayerInventoryUtil.clickSlot(to, from - count, SlotActionType.SWAP, false);
                PlayerInventoryUtil.closeScreen(true);
            }
            return;
        }
        if (task) {
            PlayerInventoryComponent.addTask(() -> PlayerInventoryUtil.moveItem(from, to, updateInventory));
        } else {
            PlayerInventoryUtil.moveItem(from, to, updateInventory);
            PlayerInventoryUtil.closeScreen(true);
        }
    }

    public static void moveItem(int from, int to, boolean updateInventory) {
        PlayerInventoryUtil.clickSlot(from, 0, SlotActionType.SWAP, false);
        PlayerInventoryUtil.clickSlot(to, 0, SlotActionType.SWAP, false);
        PlayerInventoryUtil.clickSlot(from, 0, SlotActionType.SWAP, false);
        if (updateInventory) {
            PlayerInventoryUtil.updateSlots();
        }
    }

    @Generated
    private PlayerInventoryUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

