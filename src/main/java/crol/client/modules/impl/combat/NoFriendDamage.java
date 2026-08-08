package crol.client.modules.impl.combat;

import crol.client.CrolClient;
import crol.client.event.classes.SendPacketEvent;
import crol.client.event.interfaces.ISendPacketable;
import crol.client.mixins.other.IPlayerInteractEntityC2SPacketMixin;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

@Environment(EnvType.CLIENT)
public class NoFriendDamage extends Module implements ISendPacketable, IUtil {
   public NoFriendDamage() {
      super(new ModuleInfo("NoFriendDamage", Category.COMBAT, "Отменяет урон по клиентским друзьям"));
   }

   public void onSendPacket(SendPacketEvent sendPacketEvent) {
      Packet var3 = sendPacketEvent.getPacket();
      if (var3 instanceof PlayerInteractEntityC2SPacket packet) {
         if (mc.world != null && CrolClient.INSTANCE.getFriendManager().isFriend(mc.world.getEntityById(((IPlayerInteractEntityC2SPacketMixin)packet).getId()).getNameForScoreboard())) {
            sendPacketEvent.cancel();
         }
      }

   }
}
