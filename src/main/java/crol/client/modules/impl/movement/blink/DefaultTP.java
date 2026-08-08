package crol.client.modules.impl.movement.blink;

import crol.client.event.CancellableEvent;
import crol.client.event.classes.SendPacketEvent;
import crol.client.modules.ClassMode;
import java.util.concurrent.CopyOnWriteArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.Packet;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class DefaultTP extends ClassMode {
   private CopyOnWriteArrayList<Packet> packets = new CopyOnWriteArrayList();

   public CopyOnWriteArrayList<Packet> getPackets() {
      return this.packets;
   }

   public void onEnable() {
      this.packets.clear();
   }

   @Compile
   public void onDisable() {
      for(Packet packet : this.packets) {
         mc.player.networkHandler.sendPacket(packet);
         this.packets.remove(packet);
      }

      this.packets.clear();
   }

   public void onEvent(CancellableEvent event) {
      if (event instanceof SendPacketEvent sendPacketEvent) {
         this.packets.add(sendPacketEvent.getPacket());
         event.cancel();
      }

   }
}
