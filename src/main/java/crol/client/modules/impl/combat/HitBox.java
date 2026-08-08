package crol.client.modules.impl.combat;

import crol.client.CrolClient;
import crol.client.event.classes.BoundingBoxControlEvent;
import crol.client.event.interfaces.IBoundingBoxControl;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.FloatSetting;
import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

@Environment(EnvType.CLIENT)
public class HitBox extends Module implements IBoundingBoxControl, IUtil {
   private final FloatSetting xz = ((FloatSetting)(new FloatSetting()).name("XZ")).value(0.2F).minValue(0.0F).maxValue(3.0F).incriment(0.01F);
   private final FloatSetting y = ((FloatSetting)(new FloatSetting()).name("Y")).value(0.0F).minValue(0.0F).maxValue(3.0F).incriment(0.01F);

   public HitBox() {
      super(new ModuleInfo("HitBox", Category.COMBAT, "Увеличивает границы нанесения урона по противнику"));
      this.addSetting(new Setting[]{this.xz, this.y});
   }

   public void onBoundingBoxControl(BoundingBoxControlEvent event) {
      Entity var3 = event.getEntity();
      if (var3 instanceof LivingEntity living) {
         Box box = event.getBox();
         float xzExpand = this.xz.getValue();
         float yExpand = this.y.getValue();
         Box changedBox = new Box(box.minX - (double)(xzExpand / 2.0F), box.minY - (double)(yExpand / 2.0F), box.minZ - (double)(xzExpand / 2.0F), box.maxX + (double)(xzExpand / 2.0F), box.maxY + (double)(yExpand / 2.0F), box.maxZ + (double)(xzExpand / 2.0F));
         if (living != mc.player) {
            if (living instanceof PlayerEntity) {
               PlayerEntity player = (PlayerEntity)living;
               if (player.getNameForScoreboard().equalsIgnoreCase(mc.player.getNameForScoreboard())) {
                  return;
               }
            }

            if (!CrolClient.INSTANCE.getFriendManager().isFriend(living.getNameForScoreboard())) {
               event.setBox(changedBox);
            }
         }
      }

   }
}
