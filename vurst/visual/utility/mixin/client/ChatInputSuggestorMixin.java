
package vurst.visual.utility.mixin.client;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.concurrent.CompletableFuture;
import net.minecraft.CommandSource;
import net.minecraft.TextFieldWidget;
import net.minecraft.ChatInputSuggestor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import vurst.visual.VurstVisual;

@Mixin(value={ChatInputSuggestor.class})
public abstract class ChatInputSuggestorMixin {
    @Final
    @Shadow
    TextFieldWidget textField;
    @Shadow
    boolean completingSuggestions;
    @Shadow
    private ParseResults<CommandSource> parse;
    @Shadow
    private CompletableFuture<Suggestions> pendingSuggestions;
    @Shadow
    private ChatInputSuggestor.SuggestionWindow window;

    @Shadow
    protected abstract void showCommandSuggestions();

    @Inject(method={"refresh"}, at={@At(value="INVOKE", target="Lcom/mojang/brigadier/StringReader;canRead()Z", remap=false)}, cancellable=true, locals=LocalCapture.CAPTURE_FAILHARD)
    public void refreshHook(CallbackInfo ci, String string, StringReader reader) {
        if (reader.canRead(VurstVisual.getInstance().getCommandManager().getPrefix().length()) && reader.getString().startsWith(VurstVisual.getInstance().getCommandManager().getPrefix(), reader.getCursor())) {
            int cursor;
            reader.setCursor(reader.getCursor() + 1);
            if (this.parse == null) {
                this.parse = VurstVisual.getInstance().getCommandManager().getDispatcher().parse(reader, (Object)VurstVisual.getInstance().getCommandManager().getSource());
            }
            if (!((cursor = this.textField.getCursor()) < 1 || this.window != null && this.completingSuggestions)) {
                this.pendingSuggestions = VurstVisual.getInstance().getCommandManager().getDispatcher().getCompletionSuggestions(this.parse, cursor);
                this.pendingSuggestions.thenRun(() -> {
                    if (this.pendingSuggestions.isDone()) {
                        this.showCommandSuggestions();
                    }
                });
            }
            ci.cancel();
        }
    }
}

