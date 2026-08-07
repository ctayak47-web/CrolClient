
package vurst.visual.client.modules.impl.utility;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.Hand;
import vurst.visual.base.events.impl.player.EventAttack;
import vurst.visual.base.events.impl.player.EventUpdate;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.ModeSetting;
import vurst.visual.client.modules.api.setting.impl.NumberSetting;
import vurst.visual.utility.game.player.MovingUtil;
import vurst.visual.utility.math.StopWatch;

@ModuleAnnotation(name="AntiAfk", category=Category.MOVEMENT, description="Не даёт серверу кикнуть вас за AFK.")
public final class AntiAfk
extends Module {
    public static final AntiAfk INSTANCE = new AntiAfk();
    private final ModeSetting antiAfkMode = new ModeSetting("Режим", "Прыгать", "Взмах рукой");
    private final ModeSetting.Value modeJump = this.antiAfkMode.getValues().get(0);
    private final ModeSetting.Value modeSwing = this.antiAfkMode.getValues().get(1);
    private final NumberSetting delay = new NumberSetting("Задержка", 50.0f, 5.0f, 60.0f, 5.0f);
    private final StopWatch afkTimer = new StopWatch();
    private boolean activeAfk;

    private AntiAfk() {
    }

    @Override
    public void onEnable() {
        this.activeAfk = false;
        this.afkTimer.reset();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.activeAfk = false;
        this.afkTimer.reset();
        super.onDisable();
    }

    @EventTarget
    public void onAttack(EventAttack event) {
        this.activeAfk = false;
        this.afkTimer.reset();
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (AntiAfk.mc.player == null || AntiAfk.mc.world == null) {
            return;
        }
        long delayMs = (long)(this.delay.getCurrent() * 1000.0f);
        if (this.afkTimer.getElapsedTime() >= delayMs) {
            this.activeAfk = true;
        }
        if (this.isPlayerMoving()) {
            this.activeAfk = false;
            this.afkTimer.reset();
            return;
        }
        if (this.activeAfk && this.afkTimer.getElapsedTime() >= delayMs) {
            if (this.antiAfkMode.is(this.modeJump)) {
                AntiAfk.mc.player.jump();
            } else if (this.antiAfkMode.is(this.modeSwing)) {
                AntiAfk.mc.player.swingHand(Hand.MAIN_HAND);
            }
            this.afkTimer.reset();
            this.activeAfk = false;
        }
    }

    private boolean isPlayerMoving() {
        if (AntiAfk.mc.player == null || AntiAfk.mc.player.input == null) {
            return false;
        }
        if (MovingUtil.hasPlayerMovement()) {
            return true;
        }
        return AntiAfk.mc.player.getVelocity().horizontalLengthSquared() > 1.0E-4;
    }
}

