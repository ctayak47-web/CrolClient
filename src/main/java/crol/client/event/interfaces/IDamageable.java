package crol.client.event.interfaces;

import crol.client.event.classes.DamageEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IDamageable {
   void onDamage(DamageEvent var1);
}
