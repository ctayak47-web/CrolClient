package crol.client.event.classes;

import crol.client.event.CancellableEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.screen.slot.SlotActionType;

@Environment(EnvType.CLIENT)
public class ClickSlotEvent extends CancellableEvent {
   private final SlotActionType slotActionType;
   private final int slot;
   private final int button;
   private final int id;

   public ClickSlotEvent(SlotActionType slotActionType, int slot, int button, int id) {
      this.slotActionType = slotActionType;
      this.slot = slot;
      this.button = button;
      this.id = id;
   }

   public SlotActionType getSlotActionType() {
      return this.slotActionType;
   }

   public int getSlot() {
      return this.slot;
   }

   public int getButton() {
      return this.button;
   }

   public int getId() {
      return this.id;
   }
}
