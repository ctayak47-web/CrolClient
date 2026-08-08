package crol.client.ui.draggable.impl;

import crol.client.ui.draggable.Draggable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BindsDraggable extends Draggable {
   public BindsDraggable() {
      super("BindsDraggable", 150, 180, 100, 20);
   }
}
