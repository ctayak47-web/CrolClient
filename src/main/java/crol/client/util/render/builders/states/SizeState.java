package crol.client.util.render.builders.states;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record SizeState(float width, float height) {
   public static final SizeState NONE = new SizeState(0.0F, 0.0F);

   public SizeState(double width, double height) {
      this((float)width, (float)height);
   }
}
