package crol.client.event.interfaces;

import crol.client.event.classes.BoundingBoxControlEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IBoundingBoxControl {
   void onBoundingBoxControl(BoundingBoxControlEvent var1);
}
