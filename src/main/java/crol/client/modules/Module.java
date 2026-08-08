package crol.client.modules;

import crol.client.CrolClient;
import crol.client.managers.FontManager;
import crol.client.modules.impl.render.Interface;
import crol.client.modules.impl.util.ClientSound;
import crol.client.modules.settings.Setting;
import crol.client.ui.notify.Status;
import crol.client.util.animations.Animation;
import crol.client.util.animations.Direction;
import crol.client.util.animations.impl.EaseBackIn;
import crol.client.util.other.BindUtill;
import crol.client.util.other.SoundUtil;
import crol.client.util.render.msdf.MsdfFont;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public abstract class Module {
   private final ModuleInfo moduleInfo;
   private boolean enabled;
   private boolean binded;
   private int bind;
   private final List<Setting> settings;
   private double height;
   private final Animation animation;
   private final Animation animSettings;
   private boolean opened;
   private String nameBind;
   private double defaultHeight;
   private String[] desc;
   private boolean doubleDesc = false;

   public Module(ModuleInfo moduleInfo) {
      this.animation = new EaseBackIn(325, (double)1.0F, 0.1F, Direction.BACKWARDS);
      this.animSettings = new EaseBackIn(325, (double)1.0F, 0.1F, Direction.BACKWARDS);
      this.moduleInfo = moduleInfo;
      this.enabled = false;
      this.bind = -1;
      this.settings = new ArrayList();
      this.height = (double)38.5F;
      this.defaultHeight = (double)38.5F;
      this.nameBind = "n/a";
      this.opened = false;
      this.binded = false;
      this.desc = new String[2];
   }

   public void initDesc() {
      this.defaultHeight = (double)38.5F;
      double xBind = (double)159.5F - (double)((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).getWidth(this.getNameBind(), 5.8F);
      String[] array = this.getModuleInfo().desc().split(" ");
      double maxWidthText = xBind - (double)28.0F;
      this.desc = new String[2];
      this.desc[0] = "";
      this.desc[1] = "";
      boolean twoLine = false;

      for(String s : array) {
         if ((double)((MsdfFont)FontManager.SUISSEINTMEDIUM.get()).getWidth(this.desc[0] + s, 5.8F) < maxWidthText && !twoLine) {
            String[] var11 = this.desc;
            var11[0] = var11[0] + s + " ";
         } else {
            String[] var10000 = this.desc;
            var10000[1] = var10000[1] + s + " ";
            twoLine = true;
         }
      }

      if (this.desc[1] != null && !this.desc[1].isEmpty()) {
         if (!this.opened) {
            this.height = (double)45.5F;
         }

         this.defaultHeight = (double)45.5F;
         this.doubleDesc = true;
      }

   }

   public ModuleInfo getModuleInfo() {
      return this.moduleInfo;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setBind(int bind) {
      this.nameBind = BindUtill.getBind(bind);
      this.bind = bind;
      if (CrolClient.INSTANCE.getMainScreen().isInit()) {
         this.initDesc();
      }

   }

   public String getNameBind() {
      return this.nameBind;
   }

   public void setEnabled(boolean enabled) {
      if (enabled && this instanceof IEnableable iEnableable) {
         iEnableable.onEnable();
      } else if (!enabled && this instanceof IDisableable iDisableable) {
         iDisableable.onDisable();
      }

      Interface hud = CrolClient.INSTANCE.getModuleManager() == null ? null : (Interface)CrolClient.INSTANCE.getModuleManager().getByClass(Interface.class);
      boolean hasNofify = hud != null && hud.isEnabled() & hud.getElements().getValueByName("Notify");
      if (enabled) {
         CrolClient.INSTANCE.getEventManager().register(this);
         this.animation.setDirection(Direction.FORWARDS);
         if (hasNofify) {
            CrolClient.INSTANCE.getNotifyManager().addNotify(this.getModuleInfo().name() + " was enabled", Status.SUCCESS);
         }
      } else {
         CrolClient.INSTANCE.getEventManager().unregister(this);
         this.animation.setDirection(Direction.BACKWARDS);
         if (hasNofify) {
            CrolClient.INSTANCE.getNotifyManager().addNotify(this.getModuleInfo().name() + " was disabled", Status.SUCCESS);
         }
      }

      if (MinecraftClient.getInstance().player != null) {
         ClientSound clientSound = (ClientSound)CrolClient.INSTANCE.getModuleManager().getByClass(ClientSound.class);
         if (clientSound.isEnabled()) {
            if (enabled) {
               SoundUtil.playSound("module_disable.wav", 65.0F + clientSound.value.getValue(), false);
            } else {
               SoundUtil.playSound("module_enable.wav", 65.0F + clientSound.value.getValue(), false);
            }
         }
      }

      this.enabled = enabled;
   }

   public void setEnabledNotNotify(boolean enabled) {
      if (enabled && this instanceof IEnableable iEnableable) {
         iEnableable.onEnable();
      } else if (!enabled && this instanceof IDisableable iDisableable) {
         iDisableable.onDisable();
      }

      if (enabled) {
         CrolClient.INSTANCE.getEventManager().register(this);
         this.animation.setDirection(Direction.FORWARDS);
      } else {
         CrolClient.INSTANCE.getEventManager().unregister(this);
         this.animation.setDirection(Direction.BACKWARDS);
      }

      this.enabled = enabled;
   }

   public int getBind() {
      return this.bind;
   }

   public List<Setting> getSettings() {
      return this.settings;
   }

   protected void addSetting(Setting... settings) {
      this.settings.addAll(Arrays.asList(settings));
   }

   public double getHeight() {
      return this.height;
   }

   public void setHeight(double height) {
      this.height = height;
   }

   public Animation getAnimation() {
      return this.animation;
   }

   public boolean isOpened() {
      return this.opened;
   }

   public void setOpened(boolean opened) {
      this.opened = opened;
      if (opened) {
         this.animSettings.setDirection(Direction.FORWARDS);
      } else {
         this.animSettings.setDirection(Direction.BACKWARDS);
      }

   }

   public Animation getAnimSettings() {
      return this.animSettings;
   }

   public void setBinded(boolean binded) {
      this.binded = binded;
   }

   public boolean isBinded() {
      return this.binded;
   }

   public double getDefaultHeight() {
      return this.defaultHeight;
   }

   public String[] getDesc() {
      return this.desc;
   }

   public boolean isDoubleDesc() {
      return this.doubleDesc;
   }
}
