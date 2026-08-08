package crol.client.event.interfaces;

import crol.client.event.classes.EntityDeathEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IEntityDeath {
   void onDeath(EntityDeathEvent var1);
}
