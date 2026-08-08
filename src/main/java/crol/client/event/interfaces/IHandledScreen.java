package crol.client.event.interfaces;

import crol.client.event.classes.HandledScreenEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IHandledScreen {
   void onHandleScreen(HandledScreenEvent var1);
}
