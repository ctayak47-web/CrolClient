package crol.client.modules.impl.util;

import crol.client.CrolClient;
import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.impl.FloatSetting;
import crol.client.util.IUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class AutoLeave extends Module implements ITickable, IUtil {
   private final FloatSetting distanceSetting = ((FloatSetting)(new FloatSetting()).name("Distance")).minValue(5.0F).maxValue(40.0F).incriment(1.0F).value(10.0F);

   public AutoLeave() {
      super(new ModuleInfo("AutoLeave", Category.UTIL, "Выходит с сервера при появлении противника рядом с игроком"));
   }

   public void leave(Text text) {
      mc.getNetworkHandler().sendChatCommand("hub");
      this.setEnabled(false);
   }

   @Compile
   public void onTick(TickEvent event) {
      mc.world.getPlayers().stream().filter((p) -> mc.player.distanceTo(p) < this.distanceSetting.getValue() && mc.player != p && !CrolClient.INSTANCE.getFriendManager().isFriend(p.getNameForScoreboard())).findFirst().ifPresent((p) -> this.leave(p.getName().copy().append(" - Появился рядом " + mc.player.distanceTo(p) + "м")));
   }
}
