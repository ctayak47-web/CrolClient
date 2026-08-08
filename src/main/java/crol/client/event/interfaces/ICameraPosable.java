package crol.client.event.interfaces;

import crol.client.event.classes.CameraPositionEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface ICameraPosable {
   void onCamera(CameraPositionEvent var1);
}
