
package crol.client.modules.impl.movement;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.KeyBinding;
import net.minecraft.InputUtil;
import crol.client.base.events.impl.player.EventUpdate;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.api.setting.impl.MultiBooleanSetting;
import crol.client.modules.api.setting.impl.NumberSetting;
import crol.client.utility.math.StopWatch;

@ModuleAnnotation(name="TapeMouse", category=Category.MOVEMENT, description="Автокликер для левой и правой кнопки.")
public final class TapeMouse
extends Module {
    public static final TapeMouse INSTANCE = new TapeMouse();
    private final NumberSetting leftCps = new NumberSetting("Скорость удара левой", 1.0f, 1.0f, 20.0f, 1.0f);
    private final NumberSetting rightCps = new NumberSetting("Скорость удара право", 1.0f, 1.0f, 20.0f, 1.0f);
    private final MultiBooleanSetting button = new MultiBooleanSetting("Кнопка", new MultiBooleanSetting.Value("Левая", true), new MultiBooleanSetting.Value("Правая", false));
    private final StopWatch leftTimer = new StopWatch();
    private final StopWatch rightTimer = new StopWatch();

    private TapeMouse() {
    }

    @Override
    public void onEnable() {
        this.leftTimer.reset();
        this.rightTimer.reset();
        super.onEnable();
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (TapeMouse.mc.player == null || TapeMouse.mc.world == null) {
            return;
        }
        long leftDelay = TapeMouse.cpsToDelay(this.leftCps.getCurrent());
        long rightDelay = TapeMouse.cpsToDelay(this.rightCps.getCurrent());
        if (this.button.isEnable("Левая") && TapeMouse.mc.player.getAttackCooldownProgress(0.5f) > 0.92f && this.leftTimer.getElapsedTime() >= leftDelay) {
            KeyBinding.onKeyPressed((InputUtil.Key)TapeMouse.mc.options.attackKey.getDefaultKey());
            this.leftTimer.reset();
            return;
        }
        if (this.button.isEnable("Правая") && this.rightTimer.getElapsedTime() >= rightDelay) {
            KeyBinding.onKeyPressed((InputUtil.Key)TapeMouse.mc.options.useKey.getDefaultKey());
            this.rightTimer.reset();
        }
    }

    private static long cpsToDelay(float cps) {
        float safe = Math.max(1.0f, cps);
        return Math.round(1000.0f / safe);
    }
}

