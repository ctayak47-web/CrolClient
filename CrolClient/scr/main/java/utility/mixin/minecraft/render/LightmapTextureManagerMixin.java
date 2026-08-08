
package crol.client.utility.mixin.minecraft.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import crol.client.modules.impl.render.FullBright;

@Mixin(value={LightmapTextureManager.class})
public abstract class LightmapTextureManagerMixin {
    @ModifyExpressionValue(method={"update"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/network/ClientPlayerEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z", ordinal=0)})
    private boolean CrolClient$forceNightVisionBranch(boolean original) {
        return original || FullBright.INSTANCE.shouldForceNightVisionStrength();
    }

    @ModifyExpressionValue(method={"update"}, at={@At(value="INVOKE", target="Ljava/lang/Math;max(FF)F")})
    private float CrolClient$forceGammaBrightnessFactor(float original) {
        return FullBright.INSTANCE.getForcedGammaBrightnessFactor(original);
    }
}

