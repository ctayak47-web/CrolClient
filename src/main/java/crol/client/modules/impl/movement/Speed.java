package crol.client.modules.impl.movement;

import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.FloatSetting;
import crol.client.modules.settings.impl.ModeSetting;
import crol.client.util.IUtil;
import crol.client.util.math.TimerUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class Speed extends Module implements ITickable, IUtil {
   private final ModeSetting mode = ((ModeSetting)(new ModeSetting()).name("Mode")).value("Boat").modes("Boat", "Collission", "MetaHvH");
   public FloatSetting dist = ((FloatSetting)(new FloatSetting() {
      public boolean isVisible() {
         return Speed.this.mode.is("Collission");
      }
   }).name("Boost")).minValue(0.1F).value(0.75F).maxValue(1.1F).incriment(0.01F);
   public FloatSetting boost = ((FloatSetting)(new FloatSetting() {
      public boolean isVisible() {
         return Speed.this.mode.is("Collission");
      }
   }).name("Boost")).minValue(0.05F).value(0.25F).maxValue(0.5F).incriment(0.01F);
   private final TimerUtil timerUtil = new TimerUtil();

   public Speed() {
      super(new ModuleInfo("Speed", Category.MOVEMENT, "Ускоряет перемещение игрока"));
      this.addSetting(new Setting[]{this.mode, this.dist, this.boost});
   }

   public void onTick(TickEvent event) {
      switch (this.mode.getValue()) {
         case "Boat":
            for(Entity entity : mc.world.getEntities()) {
               if (entity instanceof BoatEntity boatEntity) {
                  if (mc.player.distanceTo(boatEntity) < 2.0F) {
                     Vec3d velocity = mc.player.getVelocity();
                     mc.player.setVelocity(velocity.x * (double)2.0F, velocity.y + (mc.player.isOnGround() ? (double)0.5F : (double)0.0F), velocity.z * (double)2.0F);
                  }
               }
            }
            break;
         case "Collission":
            for(PlayerEntity player : mc.world.getPlayers()) {
               if (player != mc.player && mc.player.distanceTo(player) < this.dist.getValue() && !mc.player.isOnGround()) {
                  Vec3d velocity = mc.player.getVelocity();
                  if (this.timerUtil.isReached(100L)) {
                     mc.player.setVelocity(velocity.x * (double)this.boost.getValue(), velocity.y, velocity.z * (double)this.boost.getValue());
                     this.timerUtil.reset();
                  }
               }
            }
      }

   }
}
