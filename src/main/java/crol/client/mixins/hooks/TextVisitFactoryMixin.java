package crol.client.mixins.hooks;

import crol.client.CrolClient;
import crol.client.modules.impl.player.NameProtect;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.TextVisitFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Environment(EnvType.CLIENT)
@Mixin(TextVisitFactory.class)
public class TextVisitFactoryMixin {

    @ModifyArg(
            method = "visitFormatted(Ljava/lang/String;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/text/TextVisitFactory;visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z"
            ),
            index = 0
    )
    private static String adjustText(String text) {
        NameProtect module = (NameProtect) CrolClient.INSTANCE.getModuleManager().getByClass(NameProtect.class);
        return (module != null && module.isEnabled()) ? module.replace(text) : text;
    }
}