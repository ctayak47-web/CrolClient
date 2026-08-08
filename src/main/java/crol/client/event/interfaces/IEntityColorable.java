package crol.client.event.interfaces;

import crol.client.event.classes.EntityColorEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IEntityColorable {
   void changeColor(EntityColorEvent var1);
}
