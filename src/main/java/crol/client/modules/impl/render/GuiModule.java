package crol.client.modules.impl.render;

import crol.client.CrolClient;
import crol.client.modules.Category;
import crol.client.modules.IDisableable;
import crol.client.modules.IEnableable;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class GuiModule extends Module implements IEnableable, IDisableable {
   public GuiModule() {
      super(new ModuleInfo("Gui", Category.RENDER, "Показывает меню чита"));
      this.setBind(344);
   }

   @Compile
   public void onEnable() {
      if (MinecraftClient.getInstance().currentScreen == null) {
         MinecraftClient.getInstance().setScreen(CrolClient.INSTANCE.getGui());
      }

   }

   @Compile
   public void onDisable() {
      MinecraftClient.getInstance().setScreen((Screen)null);
   }
}
