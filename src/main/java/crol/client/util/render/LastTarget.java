package crol.client.util.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class LastTarget {
   private Entity entity;
   private double health = (double)20.0F;
   private double maxHealth = (double)20.0F;
   private String name = "badluck";
   private int hurtTime = 1;
   private Identifier avatar = Identifier.ofVanilla("textures/entity/player/wide/steve.png");
   private boolean player = true;

   public void setPlayer(boolean player) {
      this.player = player;
   }

   public boolean isPlayer() {
      return this.player;
   }

   public void setAvatar(Identifier avatar) {
      this.avatar = avatar;
   }

   public Identifier getAvatar() {
      return this.avatar;
   }

   public void update(Entity entity) {
      if (entity instanceof PlayerEntity) {
         this.player = true;
      } else {
         this.player = false;
      }

      this.avatar = this.get(entity);
   }

   private Identifier get(Entity entity) {
      if (entity instanceof AbstractClientPlayerEntity player) {
         return player.getSkinTextures().texture();
      } else {
         return Identifier.of("minecraft", "textures/entity/player/wide/steve.png");
      }
   }

   public void setHurtTime(int hurtTime) {
      this.hurtTime = hurtTime;
   }

   public int getHurtTime() {
      return this.hurtTime;
   }

   public Entity getEntity() {
      return this.entity;
   }

   public String getName() {
      return this.name;
   }

   public void setEntity(Entity entity) {
      this.entity = entity;
   }

   public void setName(String name) {
      this.name = name;
   }

   public double getHealth() {
      return this.health;
   }

   public void setHealth(double health) {
      this.health = health;
   }

   public void setMaxHealth(double maxHealth) {
      this.maxHealth = maxHealth;
   }

   public double getMaxHealth() {
      return this.maxHealth;
   }
}
