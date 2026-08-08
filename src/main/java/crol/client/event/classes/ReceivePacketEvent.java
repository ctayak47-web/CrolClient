package crol.client.event.classes;

import crol.client.event.CancellableEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.Packet;

@Environment(EnvType.CLIENT)
public class ReceivePacketEvent extends CancellableEvent {
   private Packet<?> packet;

   public ReceivePacketEvent(Packet<?> packet) {
      this.packet = packet;
   }

   public Packet<?> getPacket() {
      return this.packet;
   }

   public void setPacket(Packet<?> packet) {
      this.packet = packet;
   }
}
