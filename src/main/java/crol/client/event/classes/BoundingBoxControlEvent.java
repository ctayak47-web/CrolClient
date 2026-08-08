package crol.client.event.classes;

import crol.client.event.CancellableEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;

@Environment(EnvType.CLIENT)
public class BoundingBoxControlEvent extends CancellableEvent {
   private Box box;
   private Entity entity;

   public BoundingBoxControlEvent(Box box, Entity entity) {
      this.box = box;
      this.entity = entity;
   }

   public Box getBox() {
      return this.box;
   }

   public Entity getEntity() {
      return this.entity;
   }

   public void setBox(Box box) {
      this.box = box;
   }

   public void setEntity(Entity entity) {
      this.entity = entity;
   }
}
