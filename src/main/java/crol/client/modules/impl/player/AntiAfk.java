package crol.client.modules.impl.player;

import crol.client.event.classes.Render2DEvent;
import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.IRenderable2D;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.IDisableable;
import crol.client.modules.IEnableable;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.BooleanSetting;
import crol.client.modules.settings.impl.FloatSetting;
import crol.client.ui.gui.Gui;
import crol.client.util.IUtil;
import crol.client.util.math.MathUtil;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru_crol.crol_meta.sdk.Compile;

@Environment(EnvType.CLIENT)
public class AntiAfk extends Module implements IRenderable2D, ITickable, IEnableable, IDisableable, IUtil {
   private final BooleanSetting rotate = ((BooleanSetting)(new BooleanSetting()).name("Rotate")).value(false);
   private final FloatSetting speed = ((FloatSetting)(new FloatSetting() {
      public boolean isVisible() {
         return AntiAfk.this.rotate.getValue();
      }
   }).name("Speed")).minValue(5.0F).maxValue(60.0F).incriment(1.0F).value(10.0F);
   private final BooleanSetting jump = ((BooleanSetting)(new BooleanSetting()).name("Jump")).value(false);
   private final BooleanSetting walk = ((BooleanSetting)(new BooleanSetting()).name("Walk")).value(false);
   private float yaw;
   private final Random random;

   public AntiAfk() {
      super(new ModuleInfo("AntiAfk   ", Category.PLAYER, "Делает выбранные действия, предотвращая кик с сервера"));
      this.addSetting(new Setting[]{this.rotate, this.speed, this.jump, this.walk});
      this.random = new Random();
   }

   public void onRender2D(Render2DEvent event) {
      if ((mc.currentScreen == null || mc.currentScreen instanceof Gui) && this.rotate.getValue()) {
         this.yaw = MathUtil.fast(this.yaw, this.yaw + 10.0F, this.speed.getValue() * 0.8F + this.random.nextFloat(0.5F));
         mc.player.setYaw(this.yaw);
      }

   }

   @Compile
   public void onTick(TickEvent event) {
      if (this.jump.getValue()) {
         mc.options.jumpKey.setPressed(true);
      }

      if (this.walk.getValue()) {
         mc.options.forwardKey.setPressed(true);
      }

   }

   @Compile
   public void onDisable() {
      if (this.jump.getValue()) {
         mc.options.jumpKey.setPressed(false);
      }

      if (this.walk.getValue()) {
         mc.options.forwardKey.setPressed(false);
      }

   }

   @Compile
   public void onEnable() {
      this.yaw = mc.player.getYaw();
   }
}
