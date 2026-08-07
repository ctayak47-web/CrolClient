
package vurst.visual.client.modules.impl.utility;

import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;

@ModuleAnnotation(name="ItemPhysics", category=Category.MOVEMENT, description="Физика предметов.")
public final class ItemPhysics
extends Module {
    public static final ItemPhysics INSTANCE = new ItemPhysics();

    private ItemPhysics() {
    }

    @Override
    public String[] getLegacyNames() {
        return new String[]{"ItemPhysic", "ItemPhysirc"};
    }
}

