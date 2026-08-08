package crol.client.event.interfaces;

import crol.client.event.classes.ReceivePacketEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IReceivePacketable {
   void onReceivePacket(ReceivePacketEvent var1);
}
