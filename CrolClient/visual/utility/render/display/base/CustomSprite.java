
package crol.client.utility.render.display.base;

import lombok.Generated;
import net.minecraft.Identifier;
import crol.client.CrolClient;

public class CustomSprite {
    private final Identifier texture;

    public CustomSprite(String path) {
        this.texture = path.contains(":") ? Identifier.of((String)path) : (path.contains("/") ? CrolClient.id(path) : CrolClient.id("icons/category/" + path));
    }

    @Generated
    public Identifier getTexture() {
        return this.texture;
    }
}

