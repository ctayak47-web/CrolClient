package crol.client.modules.impl.util;

import crol.client.CrolClient;
import crol.client.event.classes.ClickEvent;
import crol.client.event.interfaces.IClickaable;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.KeySetting;
import crol.client.util.IUtil;
import crol.client.util.math.TimerUtil;
import crol.client.util.player.ChatUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

@Environment(EnvType.CLIENT)
public class ClickFriend extends Module implements IUtil, IClickaable {
   private final TimerUtil timerUtil = new TimerUtil();
   private final KeySetting bind = ((KeySetting)(new KeySetting()).name("Bind")).value(0);

   public ClickFriend() {
      super(new ModuleInfo("ClickFriend", Category.UTIL, "Добавляет наведенного игрока в клиентские друзья по нажатию кнопки"));
      this.addSetting(new Setting[]{this.bind});
   }

   public void onClick(ClickEvent event) {
      if (mc.currentScreen == null && event.getAction() == 1 && event.getKey() == this.bind.getValue()) {
         Entity entity = mc.targetedEntity;
         if (entity != null && entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)entity;
            if (this.timerUtil.isReached(250L)) {
               boolean deleted;
               if (CrolClient.INSTANCE.getFriendManager().isFriend(player.getNameForScoreboard())) {
                  CrolClient.INSTANCE.getFriendManager().removeFriend(player.getNameForScoreboard());
                  deleted = true;
               } else {
                  deleted = false;
                  CrolClient.INSTANCE.getFriendManager().addFriend(player.getNameForScoreboard());
               }

               String text = deleted ? "Игрок " + player.getNameForScoreboard() + " успешно удален из друзей!" : "Игрок " + player.getNameForScoreboard() + " успешно добавлен в друзья!";
               ChatUtil.addMessage(text);
               this.timerUtil.reset();
            }
         }
      }

   }
}
