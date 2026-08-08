
package crol.client.modules.impl.render;

import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;

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

