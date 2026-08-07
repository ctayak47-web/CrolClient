
package vurst.visual.utility.mixin.client.render.gui.hud;

import net.minecraft.ScoreboardObjective;
import net.minecraft.Scoreboard;
import net.minecraft.DrawContext;
import net.minecraft.PlayerListHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vurst.visual.client.modules.impl.hud.Armor;

@Mixin(value={PlayerListHud.class})
public class PlayerListHudMixin {
    @Inject(method={"render"}, at={@At(value="TAIL")}, require=0)
    private void renderArmorOverTab(DrawContext context, int scaledWindowWidth, Scoreboard scoreboard, ScoreboardObjective objective, CallbackInfo ci) {
        Armor.INSTANCE.renderOverPlayerList(context);
    }
}

