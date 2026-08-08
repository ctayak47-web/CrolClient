package crol.client.event.interfaces;

import crol.client.event.classes.PlaceBlockEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IPlaceBlockable {
   void onPlace(PlaceBlockEvent var1);
}
