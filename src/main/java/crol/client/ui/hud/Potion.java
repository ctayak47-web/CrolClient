package crol.client.ui.hud;

import crol.client.util.animations.Animation;
import crol.client.util.animations.Direction;
import crol.client.util.animations.impl.EaseBackIn;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.effect.StatusEffectInstance;

@Environment(EnvType.CLIENT)
public class Potion {
   private StatusEffectInstance effect;
   private Animation animation;

   public Animation getAnimation() {
      return this.animation;
   }

   public StatusEffectInstance getEffect() {
      return this.effect;
   }

   public Potion(StatusEffectInstance statusEffectInstance) {
      this.animation = new EaseBackIn(500, (double)1.0F, 0.1F, Direction.FORWARDS);
      this.effect = statusEffectInstance;
   }

   public void setEffect(StatusEffectInstance effect) {
      this.effect = effect;
   }
}
