package crol.client.ui.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IMouseReleable {
   void onMouseRelease();
}
