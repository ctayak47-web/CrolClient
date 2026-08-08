package crol.client.modules.impl.util;

import crol.client.event.classes.ReceivePacketEvent;
import crol.client.event.interfaces.IReceivePacketable;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.BooleanSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class BetterChat extends Module implements IReceivePacketable {
   public final BooleanSetting antiSpam = ((BooleanSetting)(new BooleanSetting()).name("AntiSpam")).value(false);
   private String lastMessage = null;
   private int spamCount = 0;
   private static final MinecraftClient mc = MinecraftClient.getInstance();

   public BetterChat() {
      super(new ModuleInfo("BetterChat", Category.UTIL, "Запоминает историю чата и убирает повторные сообщения"));
      this.addSetting(new Setting[]{this.antiSpam});
   }

   public void onReceivePacket(ReceivePacketEvent event) {
      Packet var3 = event.getPacket();
      if (var3 instanceof GameMessageS2CPacket packet) {
         String var4 = packet.content().getString();
         if (this.antiSpam.getValue()) {
            if (var4.equals(this.lastMessage)) {
               event.cancel();
               ++this.spamCount;
               mc.inGameHud.getChatHud().addMessage(Text.literal(var4 + " §7[x" + this.spamCount + "]"));
            } else {
               this.lastMessage = var4;
               this.spamCount = 1;
            }
         }

      }
   }
}
