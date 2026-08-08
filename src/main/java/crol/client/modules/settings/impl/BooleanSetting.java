package crol.client.modules.settings.impl;

import crol.client.modules.settings.Setting;
import crol.client.util.animations.Animation;
import crol.client.util.animations.Direction;
import crol.client.util.animations.impl.EaseBackIn;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BooleanSetting extends Setting<BooleanSetting> {
   private boolean value;
   private final Animation animation;

   public BooleanSetting() {
      this.animation = new EaseBackIn(325, (double)1.0F, 0.1F, Direction.BACKWARDS);
   }

   public BooleanSetting value(boolean value) {
      this.value = value;
      if (value) {
         this.animation.setDirection(Direction.FORWARDS);
      } else {
         this.animation.setDirection(Direction.BACKWARDS);
      }

      return this;
   }

   public void setValue(boolean value) {
      this.preChangeState(value);
      this.value = value;
      if (value) {
         this.animation.setDirection(Direction.FORWARDS);
      } else {
         this.animation.setDirection(Direction.BACKWARDS);
      }

      this.onChangeState(value);
   }

   public void preChangeState(boolean value) {
   }

   public void onChangeState(boolean value) {
   }

   public boolean getValue() {
      return this.value;
   }

   public Animation getAnimation() {
      return this.animation;
   }
}
