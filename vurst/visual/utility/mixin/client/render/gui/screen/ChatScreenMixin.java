
package vurst.visual.utility.mixin.client.render.gui.screen;

import net.minecraft.Text;
import net.minecraft.ChatScreen;
import net.minecraft.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vurst.visual.utility.interfaces.IMinecraft;

@Mixin(value={ChatScreen.class})
public class ChatScreenMixin
extends Screen
implements IMinecraft {
    protected ChatScreenMixin(Text title) {
        super(title);
    }

    @Inject(method={"sendMessage(Ljava/lang/String;Z)V"}, at={@At(value="HEAD")}, cancellable=false)
    private void onSendMessage(String text, boolean addToHistory, CallbackInfo ci) {
    }
}

