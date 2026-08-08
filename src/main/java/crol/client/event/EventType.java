package crol.client.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum EventType {
   ON,
   PRE,
   POST,
   START;

   // $FF: synthetic method
   private static EventType[] $values() {
      return new EventType[]{ON, PRE, POST, START};
   }
}
