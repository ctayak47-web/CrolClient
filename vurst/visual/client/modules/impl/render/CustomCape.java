
package vurst.visual.client.modules.impl.render;

import net.minecraft.Identifier;
import vurst.visual.VurstVisual;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;

@ModuleAnnotation(name="Custom Cape", category=Category.RENDER, description="Заменяет плащ на кастомную текстуру.")
public final class CustomCape
extends Module {
    public static final CustomCape INSTANCE = new CustomCape();
    private static final Identifier CAPE_TEXTURE = VurstVisual.id("capes/custom.png");

    private CustomCape() {
    }

    public Identifier getCapeTexture() {
        return CAPE_TEXTURE;
    }
}

