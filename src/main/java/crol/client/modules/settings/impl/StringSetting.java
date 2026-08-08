package crol.client.modules.settings.impl;

import crol.client.modules.settings.Setting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class StringSetting extends Setting<StringSetting> {
   private boolean select = false;
   private String value;

   public StringSetting select(boolean select) {
      this.select = select;
      return this;
   }

   public StringSetting value(String value) {
      this.value = value;
      return this;
   }

   public boolean isSelect() {
      return this.select;
   }

   public void setSelect(boolean select) {
      this.select = select;
   }

   public String getValue() {
      return this.value;
   }
}
