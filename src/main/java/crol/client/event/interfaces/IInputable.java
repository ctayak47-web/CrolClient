package crol.client.event.interfaces;

import crol.client.event.classes.InputEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IInputable {
   void onInput(InputEvent var1);
}
