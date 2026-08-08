package crol.client.ui.altmanager;

import crol.client.util.animations.Animation;
import crol.client.util.animations.Direction;
import crol.client.util.animations.impl.EaseBackIn;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class NickName {
   private final Animation animation;
   private final String tag;
   private final String nickname;

   public NickName(String nickname, String tag) {
      this.nickname = nickname;
      this.tag = tag;
      this.animation = new EaseBackIn(400, (double)1.0F, 0.1F, Direction.BACKWARDS);
   }

   public Animation getAnimation() {
      return this.animation;
   }

   public String getTag() {
      return this.tag;
   }

   public String getNickname() {
      return this.nickname;
   }
}
