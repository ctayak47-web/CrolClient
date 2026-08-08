package crol.client.util.player;

import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.Packet;

@Environment(EnvType.CLIENT)
public class PacketUtil implements IUtil {
   public static void sendPacket(Packet<?> packet) {
      if (mc.getNetworkHandler() != null) {
         mc.getNetworkHandler().sendPacket(packet);
      }
   }
}
