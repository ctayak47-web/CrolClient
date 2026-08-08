package crol.client.event.interfaces;

import crol.client.event.classes.SendPacketEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface ISendPacketable {
   void onSendPacket(SendPacketEvent var1);
}
