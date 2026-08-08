package crol.client.modules.impl.player;

import crol.client.event.classes.DeathScreenEvent;
import crol.client.event.interfaces.IDeathScreen;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class AutoRespawn extends Module implements IDeathScreen, IUtil {
   public AutoRespawn() {
      super(new ModuleInfo("AutoRespawn", Category.PLAYER, "Выбирает возрождение после гибели игрока"));
   }

   @Compile
   public void onDeathScreen(DeathScreenEvent event) {
      mc.player.requestRespawn();
      mc.setScreen((Screen)null);
   }
}
