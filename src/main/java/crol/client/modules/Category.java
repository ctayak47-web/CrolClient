package crol.client.modules;

import crol.client.util.animations.Animation;
import crol.client.util.animations.Direction;
import crol.client.util.animations.impl.EaseBackIn;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum Category {
   COMBAT("Combat", "N"),
   MOVEMENT("Movement", "M"),
   RENDER("Render", "O"),
   PLAYER("Player", "P"),
   UTIL("Util", "Q");

   private String name;
   private String icon;
   private Animation animation;

   private Category(String name, String icon) {
      this.name = name;
      this.icon = icon;
      this.animation = new EaseBackIn(400, (double)1.0F, 0.1F, Direction.BACKWARDS);
   }

   public Animation getAnimation() {
      return this.animation;
   }

   public String getName() {
      return this.name;
   }

   public String getIcon() {
      return this.icon;
   }

   // $FF: synthetic method
   private static Category[] $values() {
      return new Category[]{COMBAT, MOVEMENT, RENDER, PLAYER, UTIL};
   }
}
