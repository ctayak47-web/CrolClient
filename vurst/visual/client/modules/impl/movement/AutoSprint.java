
package vurst.visual.client.modules.impl.movement;

import com.darkmagician6.eventapi.EventTarget;
import vurst.visual.base.events.impl.player.EventAttack;
import vurst.visual.base.events.impl.player.EventMoveInput;
import vurst.visual.base.events.impl.player.EventUpdate;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.utility.game.player.MovingUtil;

@ModuleAnnotation(name="Auto Sprint", category=Category.MOVEMENT, description="Авто-спринт.")
public final class AutoSprint
extends Module {
    public static final AutoSprint INSTANCE = new AutoSprint();
    private int resetTicks;
    private boolean forceSprintReset;

    private AutoSprint() {
    }

    @Override
    public void onDisable() {
        this.resetTicks = 0;
        this.forceSprintReset = false;
        super.onDisable();
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (AutoSprint.mc.player == null || AutoSprint.mc.world == null) {
            return;
        }
        if (this.forceSprintReset || this.resetTicks > 0) {
            AutoSprint.mc.player.setSprinting(false);
            this.forceSprintReset = false;
            return;
        }
        if (this.shouldSprint()) {
            AutoSprint.mc.player.setSprinting(true);
        }
    }

    @EventTarget
    public void onMoveInput(EventMoveInput event) {
        if (this.resetTicks <= 0) {
            return;
        }
        event.setForward(0.0f);
        --this.resetTicks;
        this.forceSprintReset = true;
    }

    @EventTarget
    public void onAttack(EventAttack event) {
        if (event.getAction() != EventAttack.Action.PRE || AutoSprint.mc.player == null) {
            return;
        }
        if (AutoSprint.mc.player.isSprinting()) {
            this.resetTicks = 1;
        }
    }

    private boolean shouldSprint() {
        if (!MovingUtil.hasPlayerMovement()) {
            return false;
        }
        return !AutoSprint.mc.player.isSneaking() && !AutoSprint.mc.player.hasVehicle();
    }
}

