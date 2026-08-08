package crol.client.event.classes;

import crol.client.event.CancellableEvent;
import crol.client.event.EventType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class UsingItemEvent extends CancellableEvent {
   EventType eventType;

   public UsingItemEvent(EventType eventType) {
      this.eventType = eventType;
   }

   public EventType getEventType() {
      return this.eventType;
   }
}
