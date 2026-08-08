package crol.client.modules.impl.render;

import crol.client.modules.Category;
import crol.client.modules.Module;
import crol.client.modules.ModuleInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ItemPhysics extends Module {
   public ItemPhysics() {
      super(new ModuleInfo("ItemPhysics", Category.RENDER, "Добавляет предметам на земле физику"));
   }
}
