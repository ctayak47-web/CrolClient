package crol.client.event.interfaces;

import crol.client.event.classes.MoveEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IMoveable {
   void onMove(MoveEvent var1);
}
