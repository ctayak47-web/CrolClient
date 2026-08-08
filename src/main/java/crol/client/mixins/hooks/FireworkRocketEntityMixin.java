package crol.client.mixins.hooks;

import crol.client.CrolClient;
import crol.client.modules.impl.movement.ElytraBooster;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin({FireworkRocketEntity.class})
public class FireworkRocketEntityMixin {
   @Shadow
   @Final
   private static TrackedData<OptionalInt> SHOOTER_ENTITY_ID;
   @Shadow
   private LivingEntity shooter;

   @Inject(
      method = {"tick"},
      at = {@At("RETURN")}
   )
   void tick(CallbackInfo ci) {
      if (this.shooter != null && this.shooter.isGliding() && this.wasShotByEntityCustom()) {
         Vec3d vec3d = this.shooter.getRotationVector();
         Vec3d vec3d2 = this.shooter.getVelocity();
         ElytraBooster elytraBooster = (ElytraBooster)CrolClient.INSTANCE.getModuleManager().getByClass(ElytraBooster.class);
         float boostValue = elytraBooster.isEnabled() ? elytraBooster.boostValue.getValue() : 1.5F;
         this.shooter.setVelocity(vec3d2.add(vec3d.x * 0.1 + (vec3d.x * (double)boostValue - vec3d2.x) * (double)0.5F, vec3d.y * 0.1 + (vec3d.y * (double)boostValue - vec3d2.y) * (double)0.5F, vec3d.z * 0.1 + (vec3d.z * (double)boostValue - vec3d2.z) * (double)0.5F));
      }

   }

   @Unique
   private boolean wasShotByEntityCustom() {
      FireworkRocketEntity entity = (FireworkRocketEntity)(Object)this;
      return ((OptionalInt)entity.getDataTracker().get(SHOOTER_ENTITY_ID)).isPresent();
   }
}
