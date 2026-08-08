package crol.client.mixins.hooks;

import crol.client.CrolClient;
import crol.client.event.classes.HandledScreenEvent;
import crol.client.modules.Module;
import crol.client.modules.impl.util.AHHelper;
import crol.client.util.other.AutoBuyUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({HandledScreen.class})
public abstract class HandledScreenMixin {
   @Shadow
   public int backgroundWidth;
   @Shadow
   public int backgroundHeight;
   @Shadow
   protected @Nullable Slot focusedSlot;
   @Unique
   @Mutable
   private boolean isAuc;
   @Unique
   @Mutable
   private Slot lowSumSlotId = null;
   @Unique
   @Mutable
   private Slot lowAllSumSlotId = null;
   @Shadow
   @Final
   protected ScreenHandler handler;

   @Inject(
      method = {"render"},
      at = {@At("RETURN")}
   )
   public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      CrolClient.INSTANCE.getEventManager().hookEvent(new HandledScreenEvent(context, this.focusedSlot, this.backgroundWidth, this.backgroundHeight));
   }

   @Inject(
      method = {"tick"},
      at = {@At("HEAD")}
   )
   private void tickScreen(CallbackInfo ci) {
      Module ahHelper = CrolClient.INSTANCE.getModuleManager().getByClass(AHHelper.class);
      if (!this.isAuc && ahHelper.isEnabled()) {
         this.isAuc = AutoBuyUtil.getInstance().isAuction(this.handler);
      }

      if (this.isAuc && ahHelper.isEnabled()) {
         int lowSum = Integer.MAX_VALUE;
         int allSum = Integer.MAX_VALUE;

         for(int i = 0; i < 44; ++i) {
            Slot slot = (Slot)this.handler.slots.get(i);
            if (!slot.getStack().isEmpty()) {
               int sum = AutoBuyUtil.getInstance().getPrice(slot.getStack());
               if (sum < lowSum) {
                  this.lowSumSlotId = slot;
                  lowSum = sum;
               }

               if (sum / slot.getStack().getCount() < allSum) {
                  allSum = sum / slot.getStack().getCount();
                  this.lowAllSumSlotId = slot;
               }
            }
         }
      }

   }

    @Inject(method = "drawSlot", at = @At("HEAD"))
    private void onDrawSlotInject(DrawContext context, Slot slot, CallbackInfo ci) {
        AHHelper ahHelper = (AHHelper) CrolClient.INSTANCE.getModuleManager().getByClass(AHHelper.class);
        if (ahHelper != null && ahHelper.isEnabled()) {
            if (slot == this.lowSumSlotId) {
                ahHelper.renderCheat(context, slot);
            } else if (slot == this.lowAllSumSlotId) {
                ahHelper.renderGood(context, slot);
            }
        }
    }
}
