package crol.client.event.classes;

import crol.client.event.CancellableEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.Packet;

@Environment(EnvType.CLIENT)
public class SendPacketEvent extends CancellableEvent {
   private final Packet<?> packet;

   public SendPacketEvent(Packet<?> packet) {
      this.packet = packet;
   }

   public Packet<?> getPacket() {
      return this.packet;
   }
}
