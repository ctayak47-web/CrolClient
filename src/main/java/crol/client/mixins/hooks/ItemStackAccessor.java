package crol.client.mixins.hooks;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.component.MergedComponentMap;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin({ItemStack.class})
public interface ItemStackAccessor {
   @Accessor("components")
   MergedComponentMap getComponents();
}
