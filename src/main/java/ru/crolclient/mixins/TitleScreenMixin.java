package ru.crolclient.mixins;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    /*@Inject(method = "init", at = @At("RETURN"))
    public void postInitHook(CallbackInfo callbackInfo) {
        MainMenuModule mainMenuModule = (MainMenuModule) Extra.getInstance().getModuleProvider().module("MainMenu");
        if (mainMenuModule != null && mainMenuModule.isState()) {
            MinecraftClient.getInstance().setScreen(new CustomTitleScreen());
        }
    }*/
}
