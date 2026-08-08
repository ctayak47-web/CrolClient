
package crol.client.utility.mixin.client.render.gui.screen;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.Formatting;
import net.minecraft.Text;
import net.minecraft.ConfirmScreen;
import net.minecraft.GameMenuScreen;
import net.minecraft.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import crol.client.modules.impl.utility.PvpSave;

@Mixin(value={GameMenuScreen.class})
public abstract class GameMenuScreenMixin
extends Screen {
    @Unique
    private boolean CrolClient$skipConfirm;

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Shadow
    private void disconnect() {
    }

    @Inject(method={"disconnect"}, at={@At(value="HEAD")}, cancellable=true)
    private void CrolClient$confirmDisconnect(CallbackInfo ci) {
        if (this.CrolClient$skipConfirm) {
            this.CrolClient$skipConfirm = false;
            return;
        }
        if (!PvpSave.INSTANCE.isEnabled() || !PvpSave.INSTANCE.isPvpActive()) {
            return;
        }
        ci.cancel();
        BooleanConsumer callback = confirmed -> {
            if (confirmed) {
                this.CrolClient$skipConfirm = true;
                this.disconnect();
            } else {
                this.client.setScreen((Screen)this);
            }
        };
        this.client.setScreen((Screen)new ConfirmScreen(callback, (Text)Text.literal((String)"Вы точно хотите выйти с ПВП?"), (Text)Text.literal((String)"Идет ПВП, вы точно хотите выйти?"), (Text)Text.literal((String)"Да").formatted(Formatting.GREEN), (Text)Text.literal((String)"Нет").formatted(Formatting.RED)));
    }
}

