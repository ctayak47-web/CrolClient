package crol.client.modules.settings.impl;

import crol.client.modules.settings.Setting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class KeySetting extends Setting<KeySetting> {
   private boolean select = false;
   private int value;

   public KeySetting value(int value) {
      this.value = value;
      return this;
   }

   public int getValue() {
      return this.value;
   }

   public void setValue(int key) {
      this.value = key;
   }

   public boolean isSelect() {
      return this.select;
   }

   public void setSelect(boolean select) {
      this.select = select;
   }
}
