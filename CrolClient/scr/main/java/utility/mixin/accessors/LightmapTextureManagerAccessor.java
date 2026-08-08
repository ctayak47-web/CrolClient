
package crol.client.utility.mixin.accessors;

import net.minecraft.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={LightmapTextureManager.class})
public interface LightmapTextureManagerAccessor {
    @Accessor(value="dirty")
    public void CrolClient$setDirty(boolean var1);
}

