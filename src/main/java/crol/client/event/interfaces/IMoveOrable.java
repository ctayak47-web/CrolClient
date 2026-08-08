package crol.client.event.interfaces;

import crol.client.event.classes.MoveOrEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IMoveOrable {
   void onMoveOrable(MoveOrEvent var1);
}
