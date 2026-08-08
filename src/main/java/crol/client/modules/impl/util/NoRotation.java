package crol.client.modules.impl.util;

import crol.client.event.classes.ReceivePacketEvent;
import crol.client.event.interfaces.IReceivePacketable;
import crol.client.mixins.other.IPlayerPosition;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

@Environment(EnvType.CLIENT)
public class NoRotation extends Module implements IReceivePacketable, IUtil {
   public NoRotation() {
      super(new ModuleInfo("NoRotation", Category.UTIL, "Отменяет поворот камеры сервером"));
   }

   public void onReceivePacket(ReceivePacketEvent receivePacketEvent) {
      if (mc.player != null && mc.world != null) {
         Packet var3 = receivePacketEvent.getPacket();
         if (var3 instanceof PlayerPositionLookS2CPacket) {
            PlayerPositionLookS2CPacket pac = (PlayerPositionLookS2CPacket)var3;
         }

      }
   }
}
