package crol.client.modules.impl.movement;

import crol.client.CrolClient;
import crol.client.event.classes.MoveEvent;
import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.IMoveable;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.IDisableable;
import crol.client.modules.IEnableable;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.impl.combat.Aura;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.FloatSetting;
import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class ElytraMotion extends Module implements IUtil, ITickable, IMoveable, IEnableable, IDisableable {
   private final FloatSetting range = ((FloatSetting)(new FloatSetting()).name("Range")).value(3.0F).minValue(0.1F).maxValue(5.0F).incriment(0.1F);
   public boolean freeze;
   private Aura aura;

   public ElytraMotion() {
      super(new ModuleInfo("ElytraMotion", Category.MOVEMENT, "NoDesc"));
      this.addSetting(new Setting[]{this.range});
   }

   public void onMove(MoveEvent moveEvent) {
      if (this.freeze) {
         moveEvent.setMovement(new Vec3d((double)0.0F, (double)0.0F, (double)0.0F));
      }

   }

   public void onTick(TickEvent event) {
      if (mc.player != null && mc.player.isGliding()) {
         this.freeze = this.check(this.aura);
      } else {
         this.freeze = false;
      }

   }

   public boolean check(Aura aura) {
      if (!aura.isEnabled()) {
         return false;
      } else {
         LivingEntity target = aura.getTarget();
         if (target != null && mc.player != null) {
            return target.distanceTo(mc.player) < this.range.getValue();
         } else {
            return false;
         }
      }
   }

   public void onDisable() {
      this.freeze = false;
   }

   public void onEnable() {
      this.aura = (Aura)CrolClient.INSTANCE.getModuleManager().getByClass(Aura.class);
      this.freeze = false;
   }
}
