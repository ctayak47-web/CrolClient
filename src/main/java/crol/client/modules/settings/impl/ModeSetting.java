package crol.client.modules.settings.impl;

import crol.client.modules.settings.Mode;
import crol.client.modules.settings.Setting;
import crol.client.util.animations.impl.EaseBackIn;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ModeSetting extends Setting<ModeSetting> {
   private boolean open = false;
   private List<Mode> modes;
   private String value;
   private float offset = 0.0F;

   public ModeSetting value(String value) {
      this.value = value;
      return this;
   }

   public ModeSetting modes(String... modes) {
      this.modes = new ArrayList();
      Arrays.stream(modes).forEach((value) -> this.modes.add(new Mode(value, new EaseBackIn(400, (double)1.0F, 0.3F))));
      return this;
   }

   public void onChangeState(String val) {
   }

   public List<Mode> getModes() {
      return this.modes;
   }

   public boolean isOpen() {
      return this.open;
   }

   public void setOpen(boolean open) {
      this.open = open;
   }

   public String getValue() {
      return this.value;
   }

   public boolean is(String value) {
      return value.equals(this.value);
   }

   public float getOffset() {
      return this.offset;
   }

   public void setOffset(float offset) {
      this.offset = offset;
   }

   public void setValue(String value) {
      this.value = value;
      this.onChangeState(value);
   }
}
