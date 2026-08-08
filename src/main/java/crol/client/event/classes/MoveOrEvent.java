package crol.client.event.classes;

import crol.client.event.CancellableEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;

@Environment(EnvType.CLIENT)
public class MoveOrEvent extends CancellableEvent {
   public Vector3d from;
   public Vector3d to;
   public Vector3d motion;
   private boolean toGround;
   private Box aabbFrom;
   public boolean ignoreHorizontal;
   public boolean ignoreVertical;
   public boolean collidedHorizontal;
   public boolean collidedVertical;

   public MoveOrEvent(Vec3d from, Vec3d to, Vec3d motion, boolean toGround, boolean isCollidedHorizontal, boolean isCollidedVertical, Box aabbFrom) {
      this.from = new Vector3d(from.x, from.y, from.z);
      this.to = new Vector3d(to.x, to.y, to.z);
      this.motion = new Vector3d(motion.x, motion.y, motion.z);
      this.toGround = toGround;
      this.collidedHorizontal = isCollidedHorizontal;
      this.collidedVertical = isCollidedVertical;
      this.aabbFrom = aabbFrom;
   }

   public void setFrom(Vector3d from) {
      this.from = from;
   }

   public void setTo(Vector3d to) {
      this.to = to;
   }

   public void setMotion(Vector3d motion) {
      this.motion = motion;
   }

   public Vector3d getFrom() {
      return this.from;
   }

   public Vector3d getTo() {
      return this.to;
   }

   public Vector3d getMotion() {
      return this.motion;
   }

   public boolean isToGround() {
      return this.toGround;
   }

   public Box getAabbFrom() {
      return this.aabbFrom;
   }

   public boolean isIgnoreHorizontal() {
      return this.ignoreHorizontal;
   }

   public boolean isIgnoreVertical() {
      return this.ignoreVertical;
   }

   public boolean isCollidedHorizontal() {
      return this.collidedHorizontal;
   }

   public boolean isCollidedVertical() {
      return this.collidedVertical;
   }

   public void setToGround(boolean toGround) {
      this.toGround = toGround;
   }

   public void setAabbFrom(Box aabbFrom) {
      this.aabbFrom = aabbFrom;
   }

   public void setIgnoreHorizontal(boolean ignoreHorizontal) {
      this.ignoreHorizontal = ignoreHorizontal;
   }

   public void setIgnoreVertical(boolean ignoreVertical) {
      this.ignoreVertical = ignoreVertical;
   }

   public void setCollidedHorizontal(boolean collidedHorizontal) {
      this.collidedHorizontal = collidedHorizontal;
   }

   public void setCollidedVertical(boolean collidedVertical) {
      this.collidedVertical = collidedVertical;
   }
}
