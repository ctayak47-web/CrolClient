
package crol.client.utility.mixin.client;

import net.minecraft.Identifier;
import net.minecraft.AbstractClientPlayerEntity;
import net.minecraft.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import crol.client.modules.impl.render.CustomCape;
import crol.client.utility.interfaces.IMinecraft;

@Mixin(value={AbstractClientPlayerEntity.class})
public abstract class AbstractClientPlayerEntityMixin
implements IMinecraft {
    private static final Identifier DEFAULT_ELYTRA_TEXTURE = Identifier.ofVanilla((String)"textures/entity/elytra.png");

    @Inject(method={"getSkinTextures"}, at={@At(value="RETURN")}, cancellable=true)
    private void overrideCapeTexture(CallbackInfoReturnable<SkinTextures> cir) {
        if (AbstractClientPlayerEntityMixin.mc.player == null) {
            return;
        }
        if (!CustomCape.INSTANCE.isEnabled()) {
            return;
        }
        if (AbstractClientPlayerEntityMixin.mc.currentScreen != null) {
            return;
        }
        if (this != AbstractClientPlayerEntityMixin.mc.player) {
            return;
        }
        SkinTextures original = (SkinTextures)cir.getReturnValue();
        if (original == null) {
            return;
        }
        Identifier elytraTexture = original.comp_1628() != null ? original.comp_1628() : DEFAULT_ELYTRA_TEXTURE;
        SkinTextures updated = new SkinTextures(original.comp_1626(), original.comp_1911(), CustomCape.INSTANCE.getCapeTexture(), elytraTexture, original.comp_1629(), original.comp_1630());
        cir.setReturnValue((Object)updated);
    }
}

