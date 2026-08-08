package crol.client.modules.impl.util;

import crol.client.CrolClient;
import crol.client.event.classes.ReceivePacketEvent;
import crol.client.event.interfaces.IReceivePacketable;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.BooleanSetting;
import crol.client.util.IUtil;
import crol.client.util.other.Friend;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

@Environment(EnvType.CLIENT)
public class AutoTpAccept extends Module implements IReceivePacketable, IUtil {
   private final BooleanSetting onlyFriends = ((BooleanSetting)(new BooleanSetting()).name("Only Friends")).value(false);

   public AutoTpAccept() {
      super(new ModuleInfo("AutoTpAccept", Category.UTIL, "Принимает запросы на телепортацию"));
      this.addSetting(new Setting[]{this.onlyFriends});
   }

   public void onReceivePacket(ReceivePacketEvent receivePacketEvent) {
      if (mc.player != null && mc.world != null) {
         Packet var3 = receivePacketEvent.getPacket();
         if (var3 instanceof ChatMessageC2SPacket) {
            ChatMessageC2SPacket p = (ChatMessageC2SPacket)var3;
            String raw = p.chatMessage().toLowerCase(Locale.ROOT);
            if (raw.contains("просит телепортироваться к вам") || raw.contains("has requested teleport") || raw.contains("������ � ��� �����������������")) {
               if (this.onlyFriends.getValue()) {
                  boolean yes = false;

                  for(Friend friend : CrolClient.INSTANCE.getFriendManager().getFriends()) {
                     if (raw.contains(friend.name().toLowerCase(Locale.ROOT))) {
                        yes = true;
                        break;
                     }
                  }

                  if (!yes) {
                     return;
                  }
               }

               mc.getNetworkHandler().sendChatCommand("tpaccept");
            }
         }

      }
   }
}
