package crol.client.event.interfaces;

import crol.client.event.classes.ClickEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IClickaable {
   void onClick(ClickEvent var1);
}
