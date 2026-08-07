
package vurst.visual.client.modules.impl.utility;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.PlayerEntity;
import net.minecraft.SlotActionType;
import net.minecraft.Slot;
import net.minecraft.Item;
import vurst.visual.base.events.impl.other.EventClickSlot;
import vurst.visual.base.events.impl.render.EventHandledScreen;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.NumberSetting;
import vurst.visual.utility.game.player.PlayerIntersectionUtil;
import vurst.visual.utility.game.player.PlayerInventoryUtil;
import vurst.visual.utility.math.StopWatch;

@ModuleAnnotation(name="Item Scroller", description="Быстрая прокрутка предметов.", category=Category.MOVEMENT)
public final class ItemScroller
extends Module {
    public static final ItemScroller INSTANCE = new ItemScroller();
    private final NumberSetting scrollerSetting = new NumberSetting("Скорость", 100.0f, 0.0f, 200.0f, 10.0f);
    private final StopWatch timer = new StopWatch();

    private ItemScroller() {
    }

    @EventTarget
    public void onHandledScreen(EventHandledScreen e) {
        SlotActionType actionType;
        Slot hoverSlot = e.getSlotHover();
        if (PlayerIntersectionUtil.isKey(ItemScroller.mc.options.dropKey.getDefaultKey())) {
            return;
        }
        Object object = actionType = PlayerIntersectionUtil.isKey(ItemScroller.mc.options.attackKey.getDefaultKey()) ? SlotActionType.QUICK_MOVE : null;
        if (this.isShift() && !this.isCtrl() && hoverSlot != null && hoverSlot.hasStack() && actionType != null && this.timer.getElapsedTime() >= (long)Math.round(this.scrollerSetting.getCurrent())) {
            ItemScroller.mc.interactionManager.clickSlot(ItemScroller.mc.player.currentScreenHandler.syncId, hoverSlot.id, 0, actionType, (PlayerEntity)ItemScroller.mc.player);
        }
    }

    @EventTarget
    public void onClickSlot(EventClickSlot e) {
        if (e.getActionType() == SlotActionType.THROW) {
            return;
        }
        int slotId = e.getSlotId();
        if (slotId < 0 || slotId > ItemScroller.mc.player.currentScreenHandler.slots.size()) {
            return;
        }
        Slot slot = ItemScroller.mc.player.currentScreenHandler.getSlot(slotId);
        Item item = slot.getStack().getItem();
        if (item != null && this.isCtrl() && this.timer.getElapsedTime() >= 50L) {
            PlayerInventoryUtil.slots().filter(s -> s.getStack().getItem().equals(item) && s.inventory.equals((Object)slot.inventory)).forEach(s -> ItemScroller.mc.interactionManager.clickSlot(ItemScroller.mc.player.currentScreenHandler.syncId, s.id, 1, e.getActionType(), (PlayerEntity)ItemScroller.mc.player));
        }
    }

    private boolean isShift() {
        return PlayerIntersectionUtil.isKey(ItemScroller.mc.options.sneakKey.getDefaultKey());
    }

    private boolean isCtrl() {
        return PlayerIntersectionUtil.isKey(ItemScroller.mc.options.sprintKey.getDefaultKey());
    }
}

