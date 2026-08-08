
package crol.client.utility.mixin.accessors;

import net.minecraft.ItemStack;
import net.minecraft.DrawContext;
import net.minecraft.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={DrawContext.class})
public interface DrawContextAccessor {
    @Accessor(value="vertexConsumers")
    public VertexConsumerProvider.Immediate getVertexConsumers();

    @Invoker(value="drawItemBar")
    public void callDrawItemBar(ItemStack var1, int var2, int var3);

    @Invoker(value="drawCooldownProgress")
    public void callDrawCooldownProgress(ItemStack var1, int var2, int var3);
}

