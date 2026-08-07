
package vurst.visual.utility.mixin.accessors;

import net.minecraft.ItemStack;
import net.minecraft.MergedComponentMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ItemStack.class})
public interface ItemStackAccessor {
    @Accessor(value="components")
    public MergedComponentMap getComponents();
}

