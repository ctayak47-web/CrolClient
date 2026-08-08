package crol.client.ui.draggable.impl;

import crol.client.ui.draggable.Draggable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class TargetHudDraggable extends Draggable {
   public TargetHudDraggable() {
      super("TargetHudDraggable", 200, 200, 100, 20);
   }
}
