package crol.client.modules.impl.player;

import crol.client.event.classes.SendPacketEvent;
import crol.client.event.interfaces.ISendPacketable;
import crol.client.mixins.other.IPlayerInteractBlockC2SPacketMixin;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;

@Environment(EnvType.CLIENT)
public class PearlBlockThrow extends Module implements ISendPacketable, IUtil {
   public PearlBlockThrow() {
      super(new ModuleInfo("PearlBlockThrow", Category.PLAYER, "NoDesc"));
   }

   public void onSendPacket(SendPacketEvent sendPacketEvent) {
      Packet var3 = sendPacketEvent.getPacket();
      if (var3 instanceof PlayerInteractBlockC2SPacket p) {
         if (mc.player.getMainHandStack().getItem() == Items.ENDER_PEARL) {
            ((IPlayerInteractBlockC2SPacketMixin)p).setHand(Hand.OFF_HAND);
         }
      }

   }
}
