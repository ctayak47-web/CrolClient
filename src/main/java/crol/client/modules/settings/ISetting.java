package crol.client.modules.settings;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface ISetting {
   String getName();
}
