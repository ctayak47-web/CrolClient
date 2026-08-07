
package vurst.visual.client.modules.impl.utility;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.Hand;
import net.minecraft.Entity;
import net.minecraft.PlayerEntity;
import net.minecraft.PlayerInventory;
import net.minecraft.AbstractMinecartEntity;
import net.minecraft.Slot;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.SwordItem;
import net.minecraft.Packet;
import net.minecraft.UpdateSelectedSlotC2SPacket;
import net.minecraft.DrawContext;
import vurst.visual.base.events.impl.input.EventKey;
import vurst.visual.base.events.impl.player.EventUpdate;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.ItemIconProvider;
import vurst.visual.client.modules.api.setting.impl.KeySetting;
import vurst.visual.client.modules.api.setting.impl.ModeSetting;
import vurst.visual.client.modules.api.setting.impl.NumberSetting;
import vurst.visual.utility.game.other.MessageUtil;
import vurst.visual.utility.math.StopWatch;

@ModuleAnnotation(name="FastSwap", category=Category.MOVEMENT, description="Бинды PvP-предметов: свап или использование.")
public final class FastSwap
extends Module {
    public static final FastSwap INSTANCE = new FastSwap();
    private static final String MODE_SWAP = "Свапать";
    private static final String MODE_USE = "Использовать";
    private static final long MIN_RESTORE_DELAY_MS = 40L;
    private static final long MAX_RESTORE_DELAY_MS = 350L;
    private static final long SWAP_MODE_PACKET_COOLDOWN_MS = 120L;
    private static final int LABEL_COLOR = -1;
    private static final float BASE_LABEL_SCALE = 0.7f;
    private static final float MAX_LABEL_WIDTH = 12.0f;
    private final ModeSetting actionMode = new ModeSetting("Режим", "Свапать", "Использовать");
    private final ModeSetting.Value modeSwap = this.actionMode.getValues().get(0);
    private final ModeSetting.Value modeUse = this.actionMode.getValues().get(1);
    private final NumberSetting useDelay = new NumberSetting("Задержка", 200.0f, 50.0f, 1000.0f, 10.0f);
    private final KeySetting pearlKey = new ItemKeySetting("Бинд Жемчуг", Items.ENDER_PEARL);
    private final KeySetting trapkaKey = new ItemKeySetting("Бинд Трапка", Items.NETHERITE_SCRAP);
    private final KeySetting plastKey = new ItemKeySetting("Бинд Пласт", Items.DRIED_KELP);
    private final KeySetting dezkaKey = new ItemKeySetting("Бинд Дезка", Items.ENDER_EYE);
    private final KeySetting yavkaKey = new ItemKeySetting("Бинд Явка", Items.SUGAR);
    private final KeySetting fireChargeKey = new ItemKeySetting("Бинд Огненный Заряд", Items.FIRE_CHARGE);
    private final KeySetting godAuraKey = new ItemKeySetting("Бинд Божья Аура", Items.PHANTOM_MEMBRANE);
    private final KeySetting windChargeKey = new ItemKeySetting("Бинд Заряд ветра", Items.WIND_CHARGE);
    private final KeySetting snowballKey = new ItemKeySetting("Бинд Снежок", Items.SNOWBALL);
    private final StopWatch useTimer = new StopWatch();
    private int restoreSlot = -1;
    private int tempSlot = -1;
    private long restoreAtMillis = -1L;
    private Item pendingSwapItem;
    private long lastSwapModePacketMs = 0L;

    private FastSwap() {
    }

    public static void onRawKeyInput(int keyCode, int action) {
    }

    public static void onRawMouseInput(int button, int action) {
    }

    @Override
    public void onDisable() {
        this.resetRestore();
        this.pendingSwapItem = null;
        this.lastSwapModePacketMs = 0L;
        super.onDisable();
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (this.pendingSwapItem != null && FastSwap.mc.player != null) {
            int hotbarSlot = this.findHotbarItemSlot(this.pendingSwapItem);
            if (hotbarSlot == -1) {
                MessageUtil.displayWarning("Предмет не найден в хотбаре: " + this.pendingSwapItem.getName().getString());
                this.pendingSwapItem = null;
            } else if (this.selectHotbarSlotSwapSafe(hotbarSlot)) {
                this.pendingSwapItem = null;
            }
        }
        if (this.restoreAtMillis <= 0L || FastSwap.mc.player == null) {
            return;
        }
        if (System.currentTimeMillis() < this.restoreAtMillis) {
            return;
        }
        int currentSlot = FastSwap.mc.player.getInventory().selectedSlot;
        if (this.restoreSlot >= 0 && this.tempSlot >= 0 && currentSlot == this.tempSlot) {
            this.selectHotbarSlot(this.restoreSlot);
        }
        this.resetRestore();
    }

    @EventTarget
    public void onKey(EventKey event) {
        boolean done;
        if (FastSwap.mc.player == null || FastSwap.mc.world == null) {
            return;
        }
        if (FastSwap.mc.currentScreen != null || event.getAction() != 1) {
            return;
        }
        Item item = this.resolveItemByBind(event);
        if (item == Items.AIR) {
            return;
        }
        if (this.actionMode.is(this.modeSwap)) {
            this.handleSwapModePress(item);
            return;
        }
        long delayMs = Math.max(0L, (long)Math.round(this.useDelay.getCurrent()));
        if (delayMs > 0L && this.useTimer.getElapsedTime() < delayMs) {
            return;
        }
        int hotbarSlot = this.findHotbarItemSlot(item);
        if (hotbarSlot == -1) {
            return;
        }
        boolean bl = done = this.actionMode.is(this.modeUse) ? this.useFromHotbar(hotbarSlot, delayMs) : this.swapOnly(hotbarSlot);
        if (done) {
            this.useTimer.reset();
        }
    }

    private void handleSwapModePress(Item item) {
        if (FastSwap.mc.player == null) {
            return;
        }
        if (FastSwap.mc.options.attackKey.isPressed()) {
            return;
        }
        if (FastSwap.mc.options.useKey.isPressed()) {
            return;
        }
        if (this.pendingSwapItem != null) {
            return;
        }
        ItemStack selectedStack = FastSwap.mc.player.getInventory().getStack(FastSwap.mc.player.getInventory().selectedSlot);
        if (!selectedStack.isEmpty() && selectedStack.getItem() == item) {
            return;
        }
        int hotbarSlot = this.findHotbarItemSlot(item);
        if (hotbarSlot == -1) {
            MessageUtil.displayWarning("Предмет не найден в хотбаре: " + item.getName().getString());
            return;
        }
        this.pendingSwapItem = item;
    }

    public void renderHotbarHighlight(DrawContext context, int x, int y, ItemStack stack) {
        if (!this.isEnabled()) {
            return;
        }
        String label = this.getBindLabel(stack);
        if (label != null) {
            this.drawBindLabel(context, x, y, label);
        }
    }

    public void renderSlotHighlight(DrawContext context, Slot slot) {
        if (!this.isEnabled() || slot == null || !slot.hasStack()) {
            return;
        }
        if (!(slot.inventory instanceof PlayerInventory)) {
            return;
        }
        String label = this.getBindLabel(slot.getStack());
        if (label != null) {
            this.drawBindLabel(context, slot.x, slot.y, label);
        }
    }

    private Item resolveItemByBind(EventKey event) {
        if (event.isKeyDown(this.pearlKey.getKeyCode())) {
            return Items.ENDER_PEARL;
        }
        if (event.isKeyDown(this.trapkaKey.getKeyCode())) {
            return Items.NETHERITE_SCRAP;
        }
        if (event.isKeyDown(this.plastKey.getKeyCode())) {
            return Items.DRIED_KELP;
        }
        if (event.isKeyDown(this.dezkaKey.getKeyCode())) {
            return Items.ENDER_EYE;
        }
        if (event.isKeyDown(this.yavkaKey.getKeyCode())) {
            return Items.SUGAR;
        }
        if (event.isKeyDown(this.fireChargeKey.getKeyCode())) {
            return Items.FIRE_CHARGE;
        }
        if (event.isKeyDown(this.godAuraKey.getKeyCode())) {
            return Items.PHANTOM_MEMBRANE;
        }
        if (event.isKeyDown(this.windChargeKey.getKeyCode())) {
            return Items.WIND_CHARGE;
        }
        if (event.isKeyDown(this.snowballKey.getKeyCode())) {
            return Items.SNOWBALL;
        }
        return Items.AIR;
    }

    private String getBindLabel(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        if (FastSwap.mc.player != null && FastSwap.mc.player.getItemCooldownManager().getCooldownProgress(stack, 0.0f) > 0.0f) {
            return null;
        }
        Item item = stack.getItem();
        if (item == Items.ENDER_PEARL) {
            return this.getBindLabel(this.pearlKey);
        }
        if (item == Items.NETHERITE_SCRAP) {
            return this.getBindLabel(this.trapkaKey);
        }
        if (item == Items.DRIED_KELP) {
            return this.getBindLabel(this.plastKey);
        }
        if (item == Items.ENDER_EYE) {
            return this.getBindLabel(this.dezkaKey);
        }
        if (item == Items.SUGAR) {
            return this.getBindLabel(this.yavkaKey);
        }
        if (item == Items.FIRE_CHARGE) {
            return this.getBindLabel(this.fireChargeKey);
        }
        if (item == Items.PHANTOM_MEMBRANE) {
            return this.getBindLabel(this.godAuraKey);
        }
        if (item == Items.WIND_CHARGE) {
            return this.getBindLabel(this.windChargeKey);
        }
        if (item == Items.SNOWBALL) {
            return this.getBindLabel(this.snowballKey);
        }
        return null;
    }

    private String getBindLabel(KeySetting bind) {
        if (bind.getKeyCode() == -1) {
            return null;
        }
        String label = bind.getNameKey();
        return label != null && !label.isEmpty() ? label : null;
    }

    private void drawBindLabel(DrawContext context, int x, int y, String label) {
        float scale = this.getLabelScale(label);
        float drawX = ((float)x + 1.0f) / scale;
        float drawY = ((float)y + 1.0f) / scale;
        context.getMatrices().push();
        context.getMatrices().translate(0.0f, 0.0f, 200.0f);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.drawText(FastSwap.mc.textRenderer, label, (int)drawX, (int)drawY, -1, false);
        context.getMatrices().pop();
    }

    private float getLabelScale(String label) {
        int width = FastSwap.mc.textRenderer.getWidth(label);
        if ((float)width <= 12.0f) {
            return 0.7f;
        }
        float scaled = 0.7f * (12.0f / (float)width);
        return Math.max(0.5f, scaled);
    }

    private int findHotbarItemSlot(Item item) {
        for (int slot = 0; slot < 9; ++slot) {
            ItemStack stack = FastSwap.mc.player.getInventory().getStack(slot);
            if (stack.isEmpty() || stack.getItem() != item) continue;
            return slot;
        }
        return -1;
    }

    private boolean selectHotbarSlot(int slot) {
        if (slot < 0 || slot > 8) {
            return false;
        }
        if (FastSwap.mc.player.getInventory().selectedSlot == slot) {
            return true;
        }
        FastSwap.mc.player.getInventory().selectedSlot = slot;
        this.syncSelectedSlot(slot);
        return true;
    }

    private boolean selectHotbarSlotSwapSafe(int slot) {
        if (slot < 0 || slot > 8) {
            return false;
        }
        if (FastSwap.mc.player.getInventory().selectedSlot == slot) {
            return true;
        }
        long now = System.currentTimeMillis();
        if (now - this.lastSwapModePacketMs < 120L) {
            return false;
        }
        FastSwap.mc.player.getInventory().selectedSlot = slot;
        this.syncSelectedSlot(slot);
        this.lastSwapModePacketMs = now;
        return true;
    }

    private boolean swapOnly(int slot) {
        this.resetRestore();
        return this.selectHotbarSlot(slot);
    }

    private boolean useFromHotbar(int slot, long configuredDelayMs) {
        if (FastSwap.mc.interactionManager == null) {
            return false;
        }
        if (this.isUseBlockedByVehicle()) {
            return false;
        }
        ItemStack stack = FastSwap.mc.player.getInventory().getStack(slot);
        if (stack.isEmpty()) {
            return false;
        }
        if (FastSwap.mc.player.getItemCooldownManager().getCooldownProgress(stack, 0.0f) > 0.0f) {
            return false;
        }
        int selectedSlot = FastSwap.mc.player.getInventory().selectedSlot;
        int restoreTo = this.resolveRestoreSlot(selectedSlot);
        if (selectedSlot != slot) {
            this.selectHotbarSlot(slot);
        }
        FastSwap.mc.interactionManager.interactItem((PlayerEntity)FastSwap.mc.player, Hand.MAIN_HAND);
        FastSwap.mc.player.swingHand(Hand.MAIN_HAND);
        if (restoreTo != slot) {
            this.scheduleRestore(restoreTo, slot, configuredDelayMs);
        } else {
            this.resetRestore();
        }
        return true;
    }

    private boolean isUseBlockedByVehicle() {
        if (FastSwap.mc.player == null) {
            return false;
        }
        Entity vehicle = FastSwap.mc.player.getVehicle();
        return vehicle instanceof AbstractMinecartEntity;
    }

    private int resolveRestoreSlot(int selectedSlot) {
        ItemStack selectedStack = FastSwap.mc.player.getInventory().getStack(selectedSlot);
        if (!selectedStack.isEmpty() && selectedStack.getItem() instanceof SwordItem) {
            return selectedSlot;
        }
        for (int slot = 0; slot < 9; ++slot) {
            ItemStack stack = FastSwap.mc.player.getInventory().getStack(slot);
            if (stack.isEmpty() || !(stack.getItem() instanceof SwordItem)) continue;
            return slot;
        }
        return selectedSlot;
    }

    private void scheduleRestore(int previousSlot, int activeSlot, long configuredDelayMs) {
        this.restoreSlot = previousSlot;
        this.tempSlot = activeSlot;
        long restoreDelay = Math.max(40L, Math.min(350L, configuredDelayMs));
        this.restoreAtMillis = System.currentTimeMillis() + restoreDelay;
    }

    private void resetRestore() {
        this.restoreSlot = -1;
        this.tempSlot = -1;
        this.restoreAtMillis = -1L;
    }

    private void syncSelectedSlot(int slot) {
        if (mc.getNetworkHandler() == null) {
            return;
        }
        mc.getNetworkHandler().sendPacket((Packet)new UpdateSelectedSlotC2SPacket(slot));
    }

    @Override
    public String[] getLegacyNames() {
        return new String[]{"PvpHelper"};
    }

    private static final class ItemKeySetting
    extends KeySetting
    implements ItemIconProvider {
        private final Item item;

        private ItemKeySetting(String name, Item item) {
            super(name, -1);
            this.item = item;
        }

        @Override
        public ItemStack getMenuIconStack() {
            return this.item.getDefaultStack();
        }
    }
}

