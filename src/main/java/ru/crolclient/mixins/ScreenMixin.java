package ru.crolclient.mixins;

import ru.crolclient.core.Extra;
import ru.crolclient.implement.features.modules.render.ClearRenderModule;
import ru.crolclient.implement.screens.menu.MenuScreen;
import ru.crolclient.implement.screens.menu.components.implement.window.implement.module.InfoWindow;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.crolclient.api.event.EventManager;
import ru.crolclient.implement.events.chat.ChatEvent;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V", remap = false, ordinal = 1), method = "handleTextClick", cancellable = true)
    public void handleCustomClickEvent(Style style, CallbackInfoReturnable<Boolean> cir) {
        ClickEvent clickEvent = style.getClickEvent();
        if (clickEvent == null) {
            return;
        }
        EventManager.callEvent(new ChatEvent(clickEvent.getValue()));
        cir.setReturnValue(true);
        cir.cancel();
    }

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void disableBackgroundBlurAndDimmingForMenu(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Screen currentScreen = (Screen) (Object) this;
        ClearRenderModule clearRenderModule = (ClearRenderModule) Extra.getInstance().getModuleProvider().module("ClearRender");
        if (currentScreen instanceof MenuScreen || 
            (clearRenderModule != null && clearRenderModule.isState() && 
             clearRenderModule.getClearRenderSettings().isSelected("Container"))) {
            ci.cancel();
        }
    }
}