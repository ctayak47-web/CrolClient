package crol.client.modules.impl.movement;

import crol.client.event.classes.SendPacketEvent;
import crol.client.event.classes.TickEvent;
import crol.client.event.interfaces.ISendPacketable;
import crol.client.event.interfaces.ITickable;
import crol.client.modules.Category;
import crol.client.modules.ClassMode;
import crol.client.modules.IDisableable;
import crol.client.modules.IEnableable;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import crol.client.modules.impl.movement.blink.AutoTP;
import crol.client.modules.impl.movement.blink.DefaultTP;
import crol.client.modules.settings.Setting;
import crol.client.modules.settings.impl.FloatSetting;
import crol.client.modules.settings.impl.ModeSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Blink extends Module implements ITickable, ISendPacketable, IDisableable, IEnableable {
   private final ClassMode defaultTP = new DefaultTP();
   private final ClassMode autoTP = new AutoTP();
   private ClassMode current;
   private final ModeSetting mode = ((ModeSetting)(new ModeSetting() {
      public void onChangeState(String val) {
         if (val.equalsIgnoreCase("Default")) {
            Blink.this.autoTP.onDisable();
            Blink.this.current = Blink.this.defaultTP;
         } else {
            Blink.this.defaultTP.onDisable();
            Blink.this.current = Blink.this.autoTP;
         }

      }
   }).name("Mode")).value("Default").modes("Default", "AutoTP");
   public FloatSetting delay = ((FloatSetting)(new FloatSetting() {
      public boolean isVisible() {
         return Blink.this.mode.is("AutoTP");
      }
   }).name("Delay")).minValue(1.0F).maxValue(20.0F).value(5.0F).incriment(1.0F);

   public Blink() {
      super(new ModuleInfo("Blink", Category.MOVEMENT, "При включении — зависание, при выключении — телепорт на новое место."));
      this.addSetting(new Setting[]{this.mode, this.delay});
   }

   public void onTick(TickEvent event) {
      this.current.onEvent(event);
   }

   public void onSendPacket(SendPacketEvent sendPacketEvent) {
      this.current.onEvent(sendPacketEvent);
   }

   public void onDisable() {
      this.current.onDisable();
   }

   public void onEnable() {
      this.current.onEnable();
   }
}
