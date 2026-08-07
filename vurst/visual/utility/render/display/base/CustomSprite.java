
package vurst.visual.utility.render.display.base;

import lombok.Generated;
import net.minecraft.Identifier;
import vurst.visual.VurstVisual;

public class CustomSprite {
    private final Identifier texture;

    public CustomSprite(String path) {
        this.texture = path.contains(":") ? Identifier.of((String)path) : (path.contains("/") ? VurstVisual.id(path) : VurstVisual.id("icons/category/" + path));
    }

    @Generated
    public Identifier getTexture() {
        return this.texture;
    }
}

