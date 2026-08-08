package crol.client.ui.altmanager;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum Type {
   NAME,
   TAG;

   // $FF: synthetic method
   private static Type[] $values() {
      return new Type[]{NAME, TAG};
   }
}
