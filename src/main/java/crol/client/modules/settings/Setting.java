package crol.client.modules.settings;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public abstract class Setting<T extends Setting<T>> implements ISetting {
   protected String name;
   protected boolean visible = true;

   public T name(String name) {
      this.name = name;
      return (T)this;
   }

   public T visible(boolean visible) {
      this.visible = visible;
      return (T)this;
   }

   public String getName() {
      return this.name;
   }

   public boolean isVisible() {
      return this.visible;
   }

   public void setVisible(boolean visible) {
      this.visible = visible;
   }
}
