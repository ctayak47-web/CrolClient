package crol.client.event.interfaces;

import crol.client.event.classes.BreakEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IBreakable {
   void onBreak(BreakEvent var1);
}
