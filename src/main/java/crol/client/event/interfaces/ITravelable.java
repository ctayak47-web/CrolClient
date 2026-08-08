package crol.client.event.interfaces;

import crol.client.event.classes.TravelEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface ITravelable {
   void onTravel(TravelEvent var1);
}
