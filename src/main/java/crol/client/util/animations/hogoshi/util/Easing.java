package crol.client.util.animations.hogoshi.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface Easing {
   double ease(double var1);
}
