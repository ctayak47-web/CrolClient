package crol.client.modules.impl.render;

import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BlockOverlay extends Module {
   public BlockOverlay() {
      super(new ModuleInfo("BlockOverlay", Category.RENDER, "NoDesc"));
   }
}
