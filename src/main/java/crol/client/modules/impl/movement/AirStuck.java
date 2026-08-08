package crol.client.modules.impl.movement;

import crol.client.event.classes.SendPacketEvent;
import crol.client.event.interfaces.ISendPacketable;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.util.IUtil;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

@Environment(EnvType.CLIENT)
public class AirStuck extends Module implements ISendPacketable, IUtil {
   public AirStuck() {
      super(new ModuleInfo("AirStuck", Category.MOVEMENT, "Вызывает зависание игрока в воздухе"));
   }

   public void onSendPacket(SendPacketEvent sendPacketEvent) {
      if (mc.player != null && mc.world != null) {
         mc.player.setVelocity((double)0.0F, (double)0.0F, (double)0.0F);
         mc.player.setMovementSpeed(0.0F);
         Packet var10000 = sendPacketEvent.getPacket();
         Objects.requireNonNull(var10000);
         Packet var2 = var10000;
         if (var2 instanceof PlayerMoveC2SPacket) {
            sendPacketEvent.cancel();
         }
      }
   }
}
