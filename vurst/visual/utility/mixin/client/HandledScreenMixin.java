
package vurst.visual.utility.mixin.client;

import com.darkmagician6.eventapi.EventManager;
import net.minecraft.SlotActionType;
import net.minecraft.Slot;
import net.minecraft.DrawContext;
import net.minecraft.HandledScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vurst.visual.base.events.impl.render.EventHandledScreen;
import vurst.visual.client.modules.impl.utility.CoolDowns;
import vurst.visual.client.modules.impl.utility.FastSwap;
import vurst.visual.client.modules.impl.utility.ItemHighliter;
import vurst.visual.client.modules.impl.utility.LockSlot;

@Mixin(value={HandledScreen.class})
public abstract class HandledScreenMixin {
    @Shadow
    protected int backgroundWidth;
    @Shadow
    protected int backgroundHeight;
    @Shadow
    protected int x;
    @Shadow
    protected int y;
    @Shadow
    @Nullable
    protected Slot focusedSlot;

    @Inject(method={"render"}, at={@At(value="RETURN")})
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        EventManager.call(new EventHandledScreen(context, this.focusedSlot, this.backgroundWidth, this.backgroundHeight));
    }

    @Inject(method={"drawSlot"}, at={@At(value="HEAD")})
    private void drawSlotHighlight(DrawContext context, Slot slot, CallbackInfo ci) {
        ItemHighliter.INSTANCE.renderSlotHighlight(context, slot);
        FastSwap.INSTANCE.renderSlotHighlight(context, slot);
    }

    @Inject(method={"drawSlot"}, at={@At(value="TAIL")})
    private void drawSlotCooldown(DrawContext context, Slot slot, CallbackInfo ci) {
        if (slot == null || !slot.hasStack()) {
            return;
        }
        CoolDowns.INSTANCE.renderCooldownText(context, slot.x, slot.y, slot.getStack());
    }

    @Inject(method={"onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (actionType == SlotActionType.THROW && LockSlot.INSTANCE.shouldBlockDrop(slot)) {
            ci.cancel();
        }
    }
}

