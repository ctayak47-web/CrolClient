
package vurst.visual.client.modules.impl.utility;

import com.darkmagician6.eventapi.EventTarget;
import vurst.visual.base.events.impl.input.EventKey;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.KeySetting;
import vurst.visual.utility.game.other.MessageUtil;

@ModuleAnnotation(name="Spec Bind", category=Category.MOVEMENT, description="Отправляет !Спек с вашим ником по бинду.")
public final class SpecBind
extends Module {
    public static final SpecBind INSTANCE = new SpecBind();
    private static final long SPEC_COOLDOWN_MS = 400L;
    private final KeySetting specKey = new KeySetting("Бинд спека", -1);
    private long lastSpecMs = 0L;

    private SpecBind() {
    }

    @Override
    public void onEnable() {
        this.lastSpecMs = 0L;
        super.onEnable();
    }

    @EventTarget
    public void onKey(EventKey event) {
        if (SpecBind.mc.player == null || SpecBind.mc.world == null || SpecBind.mc.currentScreen != null) {
            return;
        }
        if (event.getAction() != 1 || !event.isKeyDown(this.specKey.getKeyCode())) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - this.lastSpecMs < 400L) {
            return;
        }
        this.lastSpecMs = now;
        this.sendSpecCommand();
    }

    private void sendSpecCommand() {
        if (SpecBind.mc.player == null || SpecBind.mc.player.networkHandler == null) {
            MessageUtil.displayWarning("Нет подключения к серверу.");
            return;
        }
        String playerName = SpecBind.mc.player.getName().getString();
        String command = "!Спек " + playerName;
        SpecBind.mc.player.networkHandler.sendChatMessage(command);
    }
}

