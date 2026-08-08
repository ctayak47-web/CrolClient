package crol.client.ui.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IKeyPressible {
   void keyPressed(int var1, int var2, int var3);
}
