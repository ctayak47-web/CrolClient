
package vurst.visual.utility.mixin.client;

import net.minecraft.Team;
import net.minecraft.Scoreboard;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Scoreboard.class})
public abstract class ScoreboarMixin {
    @Shadow
    @Nullable
    public abstract Team getScoreHolderTeam(String var1);

    @Inject(method={"removeScoreHolderFromTeam"}, at={@At(value="HEAD")}, cancellable=true)
    public void remove(String scoreHolderName, Team team, CallbackInfo ci) {
        if (this.getScoreHolderTeam(scoreHolderName) != team) {
            ci.cancel();
        }
    }
}

