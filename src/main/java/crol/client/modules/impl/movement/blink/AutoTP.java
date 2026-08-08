package crol.client.modules.impl.movement.blink;

import crol.client.CrolClient;
import crol.client.event.CancellableEvent;
import crol.client.event.classes.SendPacketEvent;
import crol.client.event.classes.TickEvent;
import crol.client.modules.ClassMode;
import crol.client.modules.impl.movement.Blink;
import crol.client.util.math.TimerUtil;
import java.util.concurrent.CopyOnWriteArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.Packet;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class AutoTP extends ClassMode {
   private CopyOnWriteArrayList<Packet> packets = new CopyOnWriteArrayList();
   private boolean canSend = true;
   private TimerUtil timerUtil = new TimerUtil();

   public CopyOnWriteArrayList<Packet> getPackets() {
      return this.packets;
   }

   public void onEnable() {
      this.packets.clear();
   }

   @Compile
   public void onDisable() {
      this.canSend = true;

      for(Packet packet : this.packets) {
         mc.player.networkHandler.sendPacket(packet);
         this.packets.remove(packet);
      }

      this.packets.clear();
   }

   public void onEvent(CancellableEvent event) {
      if (event instanceof SendPacketEvent sendPacketEvent) {
         if (!this.canSend) {
            this.packets.add(sendPacketEvent.getPacket());
            event.cancel();
         } else if (!this.canSend || !this.packets.contains(sendPacketEvent.getPacket())) {
            this.packets.add(sendPacketEvent.getPacket());
            event.cancel();
         }
      }

      if (event instanceof TickEvent) {
         label49: {
            Blink blink = (Blink)CrolClient.INSTANCE.getModuleManager().getByClass(Blink.class);
            if (!this.canSend) {
               if (!this.timerUtil.isReached((long)(blink.delay.getValue() * 50.0F))) {
                  break label49;
               }
            } else if (!this.timerUtil.isReached(150L)) {
               break label49;
            }

            this.canSend = !this.canSend;
         }

         if (this.canSend) {
            for(Packet packet : this.packets) {
               mc.player.networkHandler.sendPacket(packet);
               this.packets.remove(packet);
            }
         }
      }

   }
}
