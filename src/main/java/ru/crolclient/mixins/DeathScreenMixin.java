package ru.crolclient.mixins;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.crolclient.core.Extra;
import ru.crolclient.implement.features.modules.render.ClearRenderModule;

@Mixin(DeathScreen.class)
public class DeathScreenMixin {
    @Shadow
    private int ticksSinceDeath;

    @Inject(method = "render", at = @At("HEAD"))
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            ItemStack mainHandStack = player.getMainHandStack();
            if (mainHandStack.getItem() == Items.TOTEM_OF_UNDYING && player.getHealth() <= 0) {
                ClearRenderModule clearRenderModule = (ClearRenderModule) Extra.getInstance().getModuleProvider().module("ClearRender");
                if (clearRenderModule != null && clearRenderModule.isState() && clearRenderModule.getClearRenderSettings().isSelected("Totem")) {
                    ci.cancel();
                    return;
                }
            }
        }
    }
}
