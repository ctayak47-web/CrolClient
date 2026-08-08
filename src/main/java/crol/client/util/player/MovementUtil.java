package crol.client.util.player;

import crol.client.event.classes.InputEvent;
import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Camera;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class MovementUtil implements IUtil {
   public static double[] calculateDirection(double distance) {
      return calculateDirection(mc.player.input.movementForward, mc.player.input.movementSideways, distance);
   }

   public static double[] calculateDirection(float forward, float sideways, double distance) {
      float yaw = mc.getCameraEntity().getYaw();
      if (forward != 0.0F) {
         if (sideways > 0.0F) {
            yaw += forward > 0.0F ? -45.0F : 45.0F;
         } else if (sideways < 0.0F) {
            yaw += forward > 0.0F ? 45.0F : -45.0F;
         }

         sideways = 0.0F;
         forward = forward > 0.0F ? 1.0F : -1.0F;
      }

      double sinYaw = Math.sin(Math.toRadians((double)(yaw + 90.0F)));
      double cosYaw = Math.cos(Math.toRadians((double)(yaw + 90.0F)));
      double xMovement = (double)forward * distance * cosYaw + (double)sideways * distance * sinYaw;
      double zMovement = (double)forward * distance * sinYaw - (double)sideways * distance * cosYaw;
      return new double[]{xMovement, zMovement};
   }

   public static boolean isMove() {
      return mc.options.backKey.isPressed() || mc.options.forwardKey.isPressed() || mc.options.rightKey.isPressed() || mc.options.leftKey.isPressed();
   }

   public static boolean isMotionMove() {
      return mc.player.getMovement().x != (double)0.0F && mc.player.getMovement().y != (double)0.0F && mc.player.getMovement().z != (double)0.0F;
   }

   public static boolean isCameraInWater() {
      if (mc.player != null && mc.world != null) {
         Camera camera = mc.gameRenderer.getCamera();
         Vec3d cameraPos = camera.getPos();
         BlockPos blockPos = BlockPos.ofFloored(cameraPos);
         FluidState fluidState = mc.world.getFluidState(blockPos);
         if (fluidState.isIn(FluidTags.WATER)) {
            float fluidHeight = fluidState.getHeight(mc.world, blockPos);
            float fluidTop = (float)blockPos.getY() + fluidHeight;
            return (float)cameraPos.y < fluidTop;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static float getDirection() {
      float rotationYaw = mc.player.getYaw();
      float strafeFactor = 0.0F;
      if (mc.player.input.movementForward > 0.0F) {
         strafeFactor = 1.0F;
      }

      if (mc.player.input.movementForward < 0.0F) {
         strafeFactor = -1.0F;
      }

      if (strafeFactor == 0.0F) {
         if (mc.player.input.movementSideways > 0.0F) {
            rotationYaw -= 90.0F;
         }

         if (mc.player.input.movementSideways < 0.0F) {
            rotationYaw += 90.0F;
         }
      } else {
         if (mc.player.input.movementSideways > 0.0F) {
            rotationYaw -= 45.0F * strafeFactor;
         }

         if (mc.player.input.movementSideways < 0.0F) {
            rotationYaw += 45.0F * strafeFactor;
         }
      }

      if (strafeFactor < 0.0F) {
         rotationYaw -= 180.0F;
      }

      return (float)Math.toRadians((double)rotationYaw);
   }

   public static void setStrafe(double motion) {
      if (isMove()) {
         double radians = (double)getDirection();
         Vec3d velocity = mc.player.getVelocity();
         mc.player.setVelocity(-Math.sin(radians) * motion, velocity.y, Math.cos(radians) * motion);
      }
   }

   public static void fixMovement(InputEvent event, float yaw) {
      float forward = event.getForward();
      float strafe = event.getStrafe();
      double angle = MathHelper.wrapDegrees(Math.toDegrees(direction(mc.player.isGliding() ? yaw : mc.player.getYaw(), (double)forward, (double)strafe)));
      if (forward != 0.0F || strafe != 0.0F) {
         float closestForward = 0.0F;
         float closestStrafe = 0.0F;
         float closestDifference = Float.MAX_VALUE;

         for(float predictedForward = -1.0F; predictedForward <= 1.0F; ++predictedForward) {
            for(float predictedStrafe = -1.0F; predictedStrafe <= 1.0F; ++predictedStrafe) {
               if (predictedStrafe != 0.0F || predictedForward != 0.0F) {
                  double predictedAngle = MathHelper.wrapDegrees(Math.toDegrees(direction(yaw, (double)predictedForward, (double)predictedStrafe)));
                  double difference = Math.abs(angle - predictedAngle);
                  if (difference < (double)closestDifference) {
                     closestDifference = (float)difference;
                     closestForward = predictedForward;
                     closestStrafe = predictedStrafe;
                  }
               }
            }
         }

         event.setForward(closestForward);
         event.setStrafe(closestStrafe);
      }
   }

   public static double direction(float rotationYaw, double moveForward, double moveStrafing) {
      if (moveForward < (double)0.0F) {
         rotationYaw += 180.0F;
      }

      float forward = 1.0F;
      if (moveForward < (double)0.0F) {
         forward = -0.5F;
      } else if (moveForward > (double)0.0F) {
         forward = 0.5F;
      }

      if (moveStrafing > (double)0.0F) {
         rotationYaw -= 90.0F * forward;
      }

      if (moveStrafing < (double)0.0F) {
         rotationYaw += 90.0F * forward;
      }

      return Math.toRadians((double)rotationYaw);
   }
}
