
package crol.client.modules.impl.render;

import net.minecraft.Perspective;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.impl.utility.FreeLook;

@ModuleAnnotation(name="Self NamTage", category=Category.RENDER, description="Показывает ник в режиме от третьего лица и свободном обзоре.")
public final class NameF5
extends Module {
    public static final NameF5 INSTANCE = new NameF5();

    private NameF5() {
    }

    public boolean shouldShowName() {
        if (!this.isEnabled() || mc == null || NameF5.mc.options == null) {
            return false;
        }
        if (FreeLook.INSTANCE.isEnabled()) {
            return true;
        }
        return NameF5.mc.options.getPerspective() != Perspective.FIRST_PERSON;
    }

    @Override
    public String[] getLegacyNames() {
        return new String[]{"Name F5"};
    }
}

