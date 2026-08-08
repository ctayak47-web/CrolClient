package crol.client.event.classes;

import crol.client.event.CancellableEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;

@Environment(EnvType.CLIENT)
public class EntityDeathEvent extends CancellableEvent {
   private final Entity entity;
   private final DamageSource source;

   public EntityDeathEvent(Entity entity, DamageSource source) {
      this.entity = entity;
      this.source = source;
   }

   public Entity getEntity() {
      return this.entity;
   }

   public DamageSource getSource() {
      return this.source;
   }
}
