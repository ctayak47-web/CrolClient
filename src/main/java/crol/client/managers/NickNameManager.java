package crol.client.managers;

import crol.client.CrolClient;
import crol.client.ui.altmanager.NickName;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class NickNameManager {
   private final List<NickName> nickNames = new ArrayList();
   private NickName currentNickName;

   public List<NickName> getNickNames() {
      return this.nickNames;
   }

   public NickName getCurrentNickName() {
      return this.currentNickName;
   }

   public void setCurrentNickName(NickName nickName) {
      this.currentNickName = nickName;
   }

   public void addNickname(NickName nickName) {
      if (!nickName.getNickname().isEmpty()) {
         this.nickNames.add(nickName);
         this.currentNickName = nickName;
         CrolClient.INSTANCE.getConfigManager().saveNickNames();
      }

   }

   public void remove(NickName nickName) {
      this.nickNames.remove(nickName);
      if (this.currentNickName == nickName) {
         this.currentNickName = null;
      }

      CrolClient.INSTANCE.getConfigManager().saveNickNames();
   }
}
