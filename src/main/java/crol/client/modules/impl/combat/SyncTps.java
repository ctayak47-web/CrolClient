package crol.client.modules.impl.combat;

import crol.client.CrolClient;
import crol.client.event.classes.ReceivePacketEvent;
import crol.client.event.interfaces.IReceivePacketable;
import crol.client.modules.Category;
import crol.client.modules.IDisableable;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class SyncTps extends Module implements IReceivePacketable, IDisableable {
   public static float TPS = 20.0F;
   public static float adjustTicks = 0.0F;
   public static float tickAdjustmentFactor = 1.0F;
   private long timestamp;
   private float emaTPS = 20.0F;
   private static final float SMOOTHING_FACTOR = 0.1F;
   private static final float MAX_TPS = 20.0F;

   public SyncTps() {
      super(new ModuleInfo("SyncTps", Category.COMBAT, "Синхронизирует удары на клиенте и сервере, помогая при серверных лагах"));
   }

   public void onReceivePacket(ReceivePacketEvent receivePacketEvent) {
      if (receivePacketEvent.getPacket() instanceof WorldTimeUpdateS2CPacket) {
         long delay = System.nanoTime() - this.timestamp;
         float rawTPS = 20.0F * (1.0E9F / (float)delay);
         float boundedTPS = MathHelper.clamp(rawTPS, 0.0F, 20.0F);
         this.emaTPS += 0.1F * (boundedTPS - this.emaTPS);
         TPS = (float)this.round((double)this.emaTPS);
         adjustTicks = this.emaTPS - 20.0F;
         tickAdjustmentFactor = 20.0F / TPS;
         this.timestamp = System.nanoTime();
      }

   }

   public double round(double input) {
      return (double)Math.round(input * (double)100.0F) / (double)100.0F;
   }

   public void onDisable() {
      CrolClient.INSTANCE.setTimerValue(1.0F);
   }
}
