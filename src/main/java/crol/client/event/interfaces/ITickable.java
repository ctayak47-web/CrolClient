package crol.client.event.interfaces;

import crol.client.event.classes.TickEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface ITickable {
   void onTick(TickEvent var1);
}
