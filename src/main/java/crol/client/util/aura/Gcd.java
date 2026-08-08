package crol.client.util.aura;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class Gcd {
   private float yaw;
   private float pitch;
   static MinecraftClient mc = MinecraftClient.getInstance();

   public Gcd(float yaw, float pitch) {
      this.yaw = yaw;
      this.pitch = pitch;
   }

   public static float getSensitivity(float rot) {
      return getDeltaMouse(rot) * getGCDValue();
   }

   public static float getFixedRotation(float rot) {
      return getDeltaMouse(rot) * getGCDValue();
   }

   public static float getGCDValue() {
      return getGCD() * 0.15F;
   }

   public static float getGCD() {
      float f1;
      return (f1 = (float)((Double)mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2)) * f1 * f1 * 8.0F;
   }

   public static float getDeltaMouse(float delta) {
      return (float)Math.round(delta / getGCDValue());
   }

   public final float getYaw() {
      return this.yaw;
   }

   public final void setYaw(float var1) {
      this.yaw = var1;
   }

   public final float getPitch() {
      return this.pitch;
   }

   public final void setPitch(float var1) {
      this.pitch = var1;
   }

   public String toString() {
      return "Rotation(yaw=" + this.yaw + ", pitch=" + this.pitch + ")";
   }

   public int hashCode() {
      return Float.hashCode(this.yaw) * 31 + Float.hashCode(this.pitch);
   }
}
