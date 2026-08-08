package crol.client.modules.impl.util;

import crol.client.event.classes.SendPacketEvent;
import crol.client.event.interfaces.ISendPacketable;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.util.player.ChatUtil;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

@Environment(EnvType.CLIENT)
public class ReallyWorldHelper extends Module implements ISendPacketable {
   private final Set<String> bannedWords = new HashSet(Arrays.asList("акриен(а|у|ом|е|чик)?", "рич(а|у|ом|ей|е)?", "ньюкод(ом|а|у|ами|ик|е)?", "экспенсив(ом|а|у|ами|е)?", "импакт(ом|а|у|ами|ик|е)?", "экселлент(ом|а|у|ами|ик|е)?", "экселент(ом|а|у|ами|ик)?", "катлаван(ом|а|у|ами|чик)?", "катлован(ом|а|у|ами|чик)?", "целестиал(ом|а|у|ами|е)?", "целк(ой|а|у|ами|очка|е)?", "матикс(ом|а|у|ами|е)?", "инерти(я|ей|ю|ями|е)?", "эксп(а|ой|ою|у|уличка|е)?", "флюгер(ом|а|у|ами)?", "рикер(а|у|ом|очек)?", "фанпе(й|ю|я|ем|е|йчик)?", "вексайд(ом|а|у|ами|ик|е)?", "нурсултан(а|у|е|ом|чик)?", "нурик(а|у|ом|е)?", "нурлан(а|у|ом|чик|е)?", "векс(ом|у|а|ами|ик|е)?", "релейк(ом|у|а|ами|е)?", "арбуз(ом|а|у|ами|ик|е)?", "вилд(ом|у|а|ами|ик|е)?", "фантайм(е|а|у)?", "холик(е|а|у)?", "холиворлд(а|у|е)?", "рокстар(ом|а|у|ами|чик|е)?", "рогалик(а|у|ом|е)?", "тандерхак(ом|у|и|ами|а|е)?", "ликвидбаунс(а|у|ами|е)?", "expensive", "celestial", "newcode", "arbuz", "akrien", "nursultan", "relake", "crol", "wurst", "catlovan", "excellent", "rockstar", "catlavan", "impact", "matix", "inertia", "wex", "wexside", "nurik", "nurlan", "rich", "funpay", "fluger", "riker", "funtime", "holyworld", "wwe", "hvh", "rogalik", "thunderhack", "liquidbounce"));

   public ReallyWorldHelper() {
      super(new ModuleInfo("ReallyWorldHelper", Category.UTIL, "Не дает написать в чат запрещенные слова"));
   }

   public void onSendPacket(SendPacketEvent sendPacketEvent) {
      Packet var3 = sendPacketEvent.getPacket();
      if (var3 instanceof ChatMessageC2SPacket chatPacket) {
         String message = chatPacket.chatMessage().toLowerCase();
         boolean banwords = false;

         for(String pattern : this.bannedWords) {
            if (message.matches(".*" + pattern + ".*")) {
               banwords = true;
               break;
            }
         }

         if (banwords) {
            sendPacketEvent.cancel();
            ChatUtil.addMessage("A forbidden word was found in your message, message sending has been cancelled!");
         }
      }

   }
}
