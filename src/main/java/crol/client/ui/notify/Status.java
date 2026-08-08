package crol.client.ui.notify;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum Status {
   SUCCESS,
   WARNING,
   ERROR;

   // $FF: synthetic method
   private static Status[] $values() {
      return new Status[]{SUCCESS, WARNING, ERROR};
   }
}
