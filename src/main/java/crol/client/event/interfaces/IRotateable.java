package crol.client.event.interfaces;

import crol.client.event.classes.RotationEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IRotateable {
   void onRotate(RotationEvent var1);
}
