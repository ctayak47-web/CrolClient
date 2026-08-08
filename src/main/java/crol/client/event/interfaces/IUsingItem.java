package crol.client.event.interfaces;

import crol.client.event.classes.UsingItemEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IUsingItem {
   void onUsing(UsingItemEvent var1);
}
