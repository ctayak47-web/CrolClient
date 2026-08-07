
package vurst.visual.client.modules.impl.utility;

import com.darkmagician6.eventapi.EventTarget;
import java.util.Locale;
import net.minecraft.PlayerEntity;
import net.minecraft.ScreenHandler;
import net.minecraft.SlotActionType;
import net.minecraft.Slot;
import net.minecraft.Item;
import net.minecraft.Items;
import vurst.visual.base.events.impl.player.EventUpdate;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.ModeSetting;
import vurst.visual.utility.math.StopWatch;

@ModuleAnnotation(name="AutoResell", category=Category.MOVEMENT, description="Автоматический реселл на аукционе.")
public final class AutoResell
extends Module {
    public static final AutoResell INSTANCE = new AutoResell();
    private static final String MODE_PRINCE = "Князь";
    private static final String MODE_PLAYER = "Игрок";
    private static final long PRINCE_RESELL_INTERVAL_MS = 65000L;
    private static final long PLAYER_RESELL_INTERVAL_MS = 65000L;
    private static final long ACTION_TIMEOUT_MS = 5000L;
    private static final long ACTION_DELAY_MS = 500L;
    private static final long CLOSE_DELAY_MS = 300L;
    private static final Item[] MODE_121_AUCTION_ITEMS = new Item[]{Items.ENDER_CHEST, Items.CHEST, Items.BARREL, Items.SHULKER_BOX, Items.PAPER, Items.PLAYER_HEAD};
    private static final Item[] MODE_121_STORAGE_ACTION_ITEMS = new Item[]{Items.TARGET, Items.RECOVERY_COMPASS, Items.COMPASS, Items.CLOCK, Items.PLAYER_HEAD};
    private static final Item[] MODE_121_STORAGE_CONFIRM_ITEMS = new Item[]{Items.LIME_DYE, Items.GREEN_DYE, Items.EMERALD, Items.SLIME_BALL, Items.GREEN_STAINED_GLASS_PANE, Items.LIME_STAINED_GLASS_PANE, Items.PLAYER_HEAD};
    private static final String[] MODE_121_AUCTION_KEYWORDS = new String[]{"хранилищ", "мои товары", "мои предметы", "мои лоты", "товары", "предметы", "склад"};
    private static final String[] MODE_121_STORAGE_ACTION_KEYWORDS = new String[]{"выбрать", "выставить", "продать", "resell", "sell", "цена"};
    private static final String[] MODE_121_STORAGE_CONFIRM_KEYWORDS = new String[]{"подтверд", "добав", "выставить", "продать", "готово", "sell", "confirm"};
    private final ModeSetting mode = new ModeSetting("Мод:", "Князь", "Игрок");
    private final StopWatch mainTimer = new StopWatch();
    private final StopWatch actionTimer = new StopWatch();
    private int stage;
    private boolean previousPauseOnLostFocus;
    private boolean pauseOnLostFocusForced;

    private AutoResell() {
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.mainTimer.reset();
        this.actionTimer.reset();
        this.stage = 0;
        this.pauseOnLostFocusForced = false;
    }

    @Override
    public void onDisable() {
        this.restorePauseOnLostFocus();
        super.onDisable();
    }

    public boolean shouldRunInBackground() {
        return this.isEnabled() && !this.isPrinceMode();
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        this.syncPauseOnLostFocus();
        if (AutoResell.mc.player == null) {
            return;
        }
        if (this.isPrinceMode()) {
            this.handlePrince();
            return;
        }
        if (this.isPlayerMode()) {
            this.handleMode121();
        }
    }

    private void handlePrince() {
        if (this.mainTimer.getElapsedTime() >= 65000L) {
            this.sendChat("/ah resell");
            this.mainTimer.reset();
        }
    }

    private void handleMode121() {
        if (this.stage == 0) {
            if (this.mainTimer.getElapsedTime() >= 65000L) {
                this.sendChat("/ah");
                this.mainTimer.reset();
                this.stage = 1;
                this.actionTimer.reset();
            }
            return;
        }
        if (this.actionTimer.getElapsedTime() >= 5000L) {
            this.resetCycle();
            return;
        }
        if (this.stage == 1) {
            if (this.actionTimer.getElapsedTime() < 500L || !this.isAuctionScreen()) {
                return;
            }
            if (this.clickMode121AuctionButton()) {
                this.stage = 2;
                this.actionTimer.reset();
            }
            return;
        }
        if (this.stage == 2) {
            if (this.actionTimer.getElapsedTime() < 500L || !this.isStorageScreen()) {
                return;
            }
            if (this.clickMode121StorageActionButton()) {
                this.stage = 3;
                this.actionTimer.reset();
                return;
            }
            if (this.clickMode121StorageConfirmButton()) {
                this.stage = 4;
                this.actionTimer.reset();
            }
            return;
        }
        if (this.stage == 3) {
            if (this.actionTimer.getElapsedTime() < 500L) {
                return;
            }
            if (AutoResell.mc.player.currentScreenHandler == null || AutoResell.mc.player.currentScreenHandler == AutoResell.mc.player.playerScreenHandler) {
                this.resetCycle();
                return;
            }
            if (this.clickMode121StorageConfirmButton()) {
                this.stage = 4;
                this.actionTimer.reset();
            }
            return;
        }
        if (this.stage == 4 && this.actionTimer.getElapsedTime() >= 300L) {
            AutoResell.mc.player.closeHandledScreen();
            this.resetCycle();
        }
    }

    private boolean clickMode121AuctionButton() {
        ScreenHandler handler = this.getHandledContainer();
        if (handler == null) {
            return false;
        }
        int containerSlots = this.getContainerSlotCount(handler);
        if (containerSlots < 9) {
            return false;
        }
        int rowStart = containerSlots - 9;
        if (this.clickSlotByKeywords(handler, rowStart, rowStart + 2, true, MODE_121_AUCTION_KEYWORDS)) {
            return true;
        }
        if (this.clickSlotByItems(handler, rowStart, rowStart + 2, true, MODE_121_AUCTION_ITEMS)) {
            return true;
        }
        return this.clickOccupiedSlot(handler, rowStart + 2, rowStart + 1, rowStart);
    }

    private boolean clickMode121StorageActionButton() {
        ScreenHandler handler = this.getHandledContainer();
        if (handler == null) {
            return false;
        }
        int containerSlots = this.getContainerSlotCount(handler);
        if (containerSlots < 9) {
            return false;
        }
        int rowStart = containerSlots - 9;
        int leftActionSlot = rowStart + 7;
        if (this.clickSlotByKeywords(handler, leftActionSlot, leftActionSlot, false, MODE_121_STORAGE_ACTION_KEYWORDS)) {
            return true;
        }
        if (this.clickSlotByItems(handler, leftActionSlot, leftActionSlot, false, MODE_121_STORAGE_ACTION_ITEMS)) {
            return true;
        }
        return this.clickOccupiedSlot(handler, leftActionSlot);
    }

    private boolean clickMode121StorageConfirmButton() {
        ScreenHandler handler = this.getHandledContainer();
        if (handler == null) {
            return false;
        }
        int containerSlots = this.getContainerSlotCount(handler);
        if (containerSlots < 9) {
            return false;
        }
        int rowStart = containerSlots - 9;
        int confirmSlot = rowStart + 8;
        if (this.clickSlotByKeywords(handler, confirmSlot, confirmSlot, false, MODE_121_STORAGE_CONFIRM_KEYWORDS)) {
            return true;
        }
        if (this.clickSlotByItems(handler, confirmSlot, confirmSlot, false, MODE_121_STORAGE_CONFIRM_ITEMS)) {
            return true;
        }
        return this.clickOccupiedSlot(handler, confirmSlot);
    }

    private void resetCycle() {
        this.stage = 0;
        this.actionTimer.reset();
    }

    private boolean clickSlotByKeywords(ScreenHandler handler, int start, int end, boolean reverse, String ... keywords) {
        if (keywords == null || keywords.length == 0) {
            return false;
        }
        int from = Math.max(0, Math.min(start, end));
        int to = Math.min(this.getContainerSlotCount(handler) - 1, Math.max(start, end));
        if (to < from) {
            return false;
        }
        if (reverse) {
            for (int slotId = to; slotId >= from; --slotId) {
                String name;
                Slot slot = handler.getSlot(slotId);
                if (!slot.hasStack() || !this.containsAny(name = slot.getStack().getName().getString().toLowerCase(Locale.ROOT), keywords)) continue;
                this.clickSlot(handler, slotId);
                return true;
            }
            return false;
        }
        for (int slotId = from; slotId <= to; ++slotId) {
            String name;
            Slot slot = handler.getSlot(slotId);
            if (!slot.hasStack() || !this.containsAny(name = slot.getStack().getName().getString().toLowerCase(Locale.ROOT), keywords)) continue;
            this.clickSlot(handler, slotId);
            return true;
        }
        return false;
    }

    private boolean clickSlotByItems(ScreenHandler handler, int start, int end, boolean reverse, Item ... items) {
        if (items == null || items.length == 0) {
            return false;
        }
        int from = Math.max(0, Math.min(start, end));
        int to = Math.min(this.getContainerSlotCount(handler) - 1, Math.max(start, end));
        if (to < from) {
            return false;
        }
        if (reverse) {
            for (int slotId = to; slotId >= from; --slotId) {
                Slot slot = handler.getSlot(slotId);
                if (!slot.hasStack()) continue;
                Item item = slot.getStack().getItem();
                for (Item target : items) {
                    if (item != target) continue;
                    this.clickSlot(handler, slotId);
                    return true;
                }
            }
            return false;
        }
        for (int slotId = from; slotId <= to; ++slotId) {
            Slot slot = handler.getSlot(slotId);
            if (!slot.hasStack()) continue;
            Item item = slot.getStack().getItem();
            for (Item target : items) {
                if (item != target) continue;
                this.clickSlot(handler, slotId);
                return true;
            }
        }
        return false;
    }

    private boolean clickOccupiedSlot(ScreenHandler handler, int ... slotIds) {
        if (slotIds == null || slotIds.length == 0) {
            return false;
        }
        int maxSlot = this.getContainerSlotCount(handler) - 1;
        for (int slotId : slotIds) {
            Slot slot;
            if (slotId < 0 || slotId > maxSlot || !(slot = handler.getSlot(slotId)).hasStack()) continue;
            this.clickSlot(handler, slotId);
            return true;
        }
        return false;
    }

    private void clickSlot(ScreenHandler handler, int slotId) {
        if (AutoResell.mc.player == null || AutoResell.mc.interactionManager == null) {
            return;
        }
        AutoResell.mc.interactionManager.clickSlot(handler.syncId, slotId, 0, SlotActionType.PICKUP, (PlayerEntity)AutoResell.mc.player);
    }

    private ScreenHandler getHandledContainer() {
        if (AutoResell.mc.player == null || AutoResell.mc.interactionManager == null) {
            return null;
        }
        ScreenHandler handler = AutoResell.mc.player.currentScreenHandler;
        if (handler == null || handler == AutoResell.mc.player.playerScreenHandler) {
            return null;
        }
        return handler;
    }

    private int getContainerSlotCount(ScreenHandler handler) {
        return Math.max(0, handler.slots.size() - 36);
    }

    private boolean isAuctionScreen() {
        String title = this.getCurrentScreenTitle();
        return title.contains("аукцион") || title.contains("auction");
    }

    private boolean isStorageScreen() {
        String title = this.getCurrentScreenTitle();
        return title.contains("хранилищ") || title.contains("storage");
    }

    private String getCurrentScreenTitle() {
        if (AutoResell.mc.currentScreen == null || AutoResell.mc.currentScreen.getTitle() == null) {
            return "";
        }
        return AutoResell.mc.currentScreen.getTitle().getString().toLowerCase(Locale.ROOT);
    }

    private boolean containsAny(String text, String ... values2) {
        if (text == null || text.isEmpty() || values2 == null) {
            return false;
        }
        for (String value : values2) {
            if (value == null || value.isEmpty() || !text.contains(value)) continue;
            return true;
        }
        return false;
    }

    private void sendChat(String message) {
        if (AutoResell.mc.player != null && AutoResell.mc.player.networkHandler != null) {
            AutoResell.mc.player.networkHandler.sendChatMessage(message);
        }
    }

    private boolean isPrinceMode() {
        return this.mode.is(MODE_PRINCE);
    }

    private boolean isPlayerMode() {
        return this.mode.is(MODE_PLAYER);
    }

    private void syncPauseOnLostFocus() {
        if (AutoResell.mc.options == null) {
            return;
        }
        if (this.shouldRunInBackground()) {
            if (!this.pauseOnLostFocusForced) {
                this.previousPauseOnLostFocus = AutoResell.mc.options.pauseOnLostFocus;
                this.pauseOnLostFocusForced = true;
            }
            if (AutoResell.mc.options.pauseOnLostFocus) {
                AutoResell.mc.options.pauseOnLostFocus = false;
            }
            return;
        }
        this.restorePauseOnLostFocus();
    }

    private void restorePauseOnLostFocus() {
        if (!this.pauseOnLostFocusForced || AutoResell.mc.options == null) {
            return;
        }
        AutoResell.mc.options.pauseOnLostFocus = this.previousPauseOnLostFocus;
        this.pauseOnLostFocusForced = false;
    }
}

