
package vurst.visual.client.modules.impl.utility;

import com.darkmagician6.eventapi.EventTarget;
import java.util.List;
import java.util.Locale;
import net.minecraft.Hand;
import net.minecraft.PlayerEntity;
import net.minecraft.Item;
import net.minecraft.Items;
import net.minecraft.ClientBossBar;
import vurst.visual.base.events.impl.player.EventUpdate;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.BooleanSetting;
import vurst.visual.client.modules.api.setting.impl.NumberSetting;
import vurst.visual.client.modules.impl.utility.PvpSave;

@ModuleAnnotation(name="Fast Exp", category=Category.MOVEMENT, description="Быстро кидает бутылочки опыта.")
public final class FastExp
extends Module {
    public static final FastExp INSTANCE = new FastExp();
    private static final List<String> PVP_WORDS = List.of("режим боя", "пвп", "pvp", "дуэль", "дуель", "дуел", "duel");
    private final BooleanSetting expBottles = new BooleanSetting("Бутылочка опыта", true);
    private final NumberSetting useDelay = new NumberSetting("Задержка", 5.0f, 0.0f, 30.0f, 0.1f, this.expBottles::isEnabled);
    private int cooldownTimer;

    private FastExp() {
    }

    private void fastUseItem(Hand hand) {
        if (FastExp.mc.interactionManager == null || FastExp.mc.player == null) {
            return;
        }
        if ((float)this.cooldownTimer >= this.useDelay.getCurrent()) {
            FastExp.mc.interactionManager.interactItem((PlayerEntity)FastExp.mc.player, hand);
            this.cooldownTimer = 0;
        } else {
            ++this.cooldownTimer;
        }
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (FastExp.mc.player == null || FastExp.mc.world == null) {
            this.cooldownTimer = 0;
            return;
        }
        if (!this.expBottles.isEnabled() || PvpSave.INSTANCE.isPvpActive() || this.isPvpModeDetected()) {
            this.cooldownTimer = 0;
            return;
        }
        if (!FastExp.mc.options.useKey.isPressed()) {
            this.cooldownTimer = 0;
            return;
        }
        Item mainHand = FastExp.mc.player.getMainHandStack().getItem();
        if (mainHand == Items.EXPERIENCE_BOTTLE) {
            this.fastUseItem(Hand.MAIN_HAND);
            return;
        }
        Item offHand = FastExp.mc.player.getOffHandStack().getItem();
        if (offHand == Items.EXPERIENCE_BOTTLE) {
            this.fastUseItem(Hand.OFF_HAND);
        }
    }

    private boolean isPvpModeDetected() {
        if (FastExp.mc.inGameHud == null) {
            return false;
        }
        for (ClientBossBar bossBar : FastExp.mc.inGameHud.getBossBarHud().bossBars.values()) {
            if (bossBar == null || bossBar.getName() == null) continue;
            String lower = bossBar.getName().getString().toLowerCase(Locale.ROOT);
            for (String marker : PVP_WORDS) {
                if (!lower.contains(marker)) continue;
                return true;
            }
        }
        return false;
    }
}

