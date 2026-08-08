package crol.client.utility.mixin.client.render.gui.hud;

import net.minecraft.DrawContext;
import net.minecraft.BossBarHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import crol.client.modules.impl.render.NoRender;

@Mixin(value={BossBarHud.class})
public class BossBarHudMixin {
    @Inject(method={"render"}, at={@At(value="HEAD")}, cancellable=true)
    private void removeBossBar(DrawContext context, CallbackInfo ci) {
        if (NoRender.INSTANCE.isRemoveBossBar()) {
            ci.cancel();
        }
    }
}
