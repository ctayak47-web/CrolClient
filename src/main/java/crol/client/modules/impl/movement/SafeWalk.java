package crol.client.modules.impl.movement;

import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.BlockPos;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class SafeWalk extends Module implements ITickable, IUtil {
   public SafeWalk() {
      super(new ModuleInfo("SafeWalk", Category.MOVEMENT, "Приседает на краю блоков, спасая от падения"));
   }

   @Compile
   public void onTick(TickEvent event) {
      BlockPos blockPos = new BlockPos((int)mc.player.getX(), (int)(mc.player.getY() - (double)1.0F), (int)mc.player.getZ());
      if (mc.player.fallDistance <= 4.0F) {
         boolean noBlockBelow = mc.world.getBlockState(blockPos).isAir();
         mc.options.sneakKey.setPressed(noBlockBelow);
      } else {
         mc.options.sneakKey.setPressed(false);
      }

   }
}
