
package crol.client.utility.mixin.client;

import com.darkmagician6.eventapi.EventManager;
import net.minecraft.Window;
import net.minecraft.MinecraftClient;
import net.minecraft.DownloadingTerrainScreen;
import net.minecraft.Screen;
import net.minecraft.RunArgs;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import crol.client.CrolClient;
import crol.client.base.events.impl.input.EventSetScreen;
import crol.client.modules.impl.utility.AutoResell;
import crol.client.utility.interfaces.IMinecraft;

@Mixin(value={MinecraftClient.class})
public abstract class MinecraftClientMixin {
    @Unique
    private static final long VURSTVISUAL_TERRAIN_SCREEN_TIMEOUT_MS = 3000L;
    @Shadow
    @Final
    private Window window;
    @Shadow
    @Nullable
    public Screen currentScreen;
    @Unique
    private long CrolClient$terrainScreenSince;

    @Shadow
    public abstract Window getWindow();

    @Shadow
    public abstract void setScreen(@Nullable Screen var1);

    @Inject(method={"<init>"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/MinecraftClient$1;<init>(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/RunArgs;)V")})
    public void init(RunArgs args, CallbackInfo ci) {
        CrolClient.getInstance().init();
    }

    @Inject(method={"onResolutionChanged"}, at={@At(value="TAIL")})
    private void captureResize(CallbackInfo ci) {
    }

    @ModifyVariable(method={"setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"}, at=@At(value="HEAD"), argsOnly=true)
    private Screen mixin$modifySetScreenArg(Screen original) {
        EventSetScreen event = new EventSetScreen(original);
        EventManager.call(event);
        return event.getScreen();
    }

    @Inject(method={"isWindowFocused"}, at={@At(value="HEAD")}, cancellable=true)
    private void forceFocusForAutoResell(CallbackInfoReturnable<Boolean> cir) {
        if (AutoResell.INSTANCE.shouldRunInBackground()) {
            cir.setReturnValue((Object)true);
        }
    }

    @Inject(method={"tick"}, at={@At(value="TAIL")})
    private void closeStuckTerrainScreen(CallbackInfo ci) {
        if (!(this.currentScreen instanceof DownloadingTerrainScreen)) {
            this.CrolClient$terrainScreenSince = 0L;
            return;
        }
        if (IMinecraft.mc.world == null || IMinecraft.mc.player == null || IMinecraft.mc.getNetworkHandler() == null) {
            this.CrolClient$terrainScreenSince = 0L;
            return;
        }
        long now = System.currentTimeMillis();
        if (this.CrolClient$terrainScreenSince == 0L) {
            this.CrolClient$terrainScreenSince = now;
            return;
        }
        if (now - this.CrolClient$terrainScreenSince < 3000L) {
            return;
        }
        this.setScreen(null);
        this.CrolClient$terrainScreenSince = 0L;
    }
}

