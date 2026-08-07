
package vurst.visual.utility.mixin.accessors;

import net.minecraft.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={HandledScreen.class})
public interface HandledScreenAccessor {
    @Accessor(value="x")
    public int getX();

    @Accessor(value="y")
    public int getY();
}

