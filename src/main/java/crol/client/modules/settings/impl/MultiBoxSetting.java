package crol.client.modules.settings.impl;

import crol.client.modules.settings.Setting;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class MultiBoxSetting extends Setting<MultiBoxSetting> {
   private boolean open = false;
   private float offset = 0.0F;
   private List<BooleanSetting> booleanSettings;
   private final Map<String, BooleanSetting> booleanSettingsMap = new HashMap();

   public MultiBoxSetting booleanSettings(BooleanSetting... booleanSettings) {
      this.booleanSettings = Arrays.asList(booleanSettings);
      this.booleanSettingsMap.clear();

      for(BooleanSetting setting : booleanSettings) {
         this.booleanSettingsMap.put(setting.getName(), setting);
      }

      return this;
   }

   public boolean getValueByName(String name) {
      BooleanSetting setting = (BooleanSetting)this.booleanSettingsMap.get(name);
      return setting != null && setting.getValue();
   }

   public BooleanSetting getByName(String name) {
      return (BooleanSetting)this.booleanSettingsMap.get(name);
   }

   public boolean isOpen() {
      return this.open;
   }

   public void setOpen(boolean open) {
      this.open = open;
   }

   public void setOffset(float offset) {
      this.offset = offset;
   }

   public float getOffset() {
      return this.offset;
   }

   public List<BooleanSetting> getBooleanSettings() {
      return this.booleanSettings;
   }
}
