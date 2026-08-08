package crol.client.event.interfaces;

import crol.client.event.classes.DropItemEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IDropable {
   void onDrop(DropItemEvent var1);
}
