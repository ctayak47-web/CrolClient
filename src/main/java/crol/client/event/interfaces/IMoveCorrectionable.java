package crol.client.event.interfaces;

import crol.client.event.classes.MoveCorrectionEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IMoveCorrectionable {
   void moveCorrection(MoveCorrectionEvent var1);
}
