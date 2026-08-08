package crol.client.modules.impl.render;

import crol.client.CrolClient;
import crol.client.event.classes.EntityColorEvent;
import crol.client.event.interfaces.IEntityColorable;
import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SeeInvisible extends Module implements IEntityColorable {
   public SeeInvisible() {
      super(new ModuleInfo("SeeInvisible", Category.RENDER, "Показывает противников в невидимости"));
   }

   public void changeColor(EntityColorEvent event) {
      event.setColor(CrolClient.INSTANCE.getThemeManager().getTheme().color().getRGB());
      event.cancel();
   }
}
