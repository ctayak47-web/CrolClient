package crol.client.event.interfaces;

import crol.client.event.classes.ClickSlotEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IClickSlotable {
   void onClickSlot(ClickSlotEvent var1);
}
