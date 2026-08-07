
package vurst.visual.client.modules.impl.render;

import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;

@ModuleAnnotation(name="NoFluid", category=Category.RENDER, description="Убирает водный и лавовый overlay, а также туман в жидкостях.")
public final class NoFluid
extends Module {
    public static final NoFluid INSTANCE = new NoFluid();

    private NoFluid() {
    }

    public boolean shouldRemoveFluidFog() {
        return this.isEnabled();
    }

    public boolean shouldRemoveOverlay() {
        return this.isEnabled();
    }

    @Override
    public String[] getLegacyNames() {
        return new String[]{"No Fluid"};
    }
}

