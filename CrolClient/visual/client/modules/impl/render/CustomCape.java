
package crol.client.modules.impl.render;

import net.minecraft.Identifier;
import crol.client.CrolClient;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;

@ModuleAnnotation(name="Custom Cape", category=Category.RENDER, description="Заменяет плащ на кастомную текстуру.")
public final class CustomCape
extends Module {
    public static final CustomCape INSTANCE = new CustomCape();
    private static final Identifier CAPE_TEXTURE = CrolClient.id("capes/custom.png");

    private CustomCape() {
    }

    public Identifier getCapeTexture() {
        return CAPE_TEXTURE;
    }
}

