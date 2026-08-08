package crol.client.modules.impl.util;

import crol.client.event.classes.ReceivePacketEvent;
import crol.client.event.interfaces.IReceivePacketable;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket.Status;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;

@Environment(EnvType.CLIENT)
public class RpSpoof extends Module implements IReceivePacketable, IUtil {
   public RpSpoof() {
      super(new ModuleInfo("RpSpoof", Category.UTIL, "Убирает необходимость установки серверного ресурс-пака"));
   }

   public void onReceivePacket(ReceivePacketEvent receivePacketEvent) {
      if (receivePacketEvent.getPacket() instanceof ResourcePackSendS2CPacket && mc.world != null && mc.player != null && mc.player.networkHandler != null) {
         mc.player.networkHandler.sendPacket(new ResourcePackStatusC2SPacket(mc.player.getUuid(), Status.ACCEPTED));
         mc.player.networkHandler.sendPacket(new ResourcePackStatusC2SPacket(mc.player.getUuid(), Status.SUCCESSFULLY_LOADED));
         receivePacketEvent.cancel();
      }

   }
}
