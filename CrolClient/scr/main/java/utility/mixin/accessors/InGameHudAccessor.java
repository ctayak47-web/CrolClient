
package crol.client.utility.mixin.accessors;

import net.minecraft.InGameHud;
import net.minecraft.DrawContext;
import net.minecraft.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={InGameHud.class})
public interface InGameHudAccessor {
    @Invoker(value="renderHotbar")
    public void invokeRenderHotbar(DrawContext var1, RenderTickCounter var2);

    @Invoker(value="renderStatusBars")
    public void invokeRenderStatusBars(DrawContext var1);
}

