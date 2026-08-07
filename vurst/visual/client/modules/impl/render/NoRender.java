
package vurst.visual.client.modules.impl.render;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.StatusEffects;
import vurst.visual.base.events.impl.player.EventUpdate;
import vurst.visual.base.events.impl.render.EventFog;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.MultiBooleanSetting;

@ModuleAnnotation(name="Render Tweaks", category=Category.RENDER, description="Скрывает визуальные элементы.")
public final class NoRender
extends Module {
    public static final NoRender INSTANCE = new NoRender();
    private static final String REMOVE_WITHER_HEARTS = "Серца";
    private final MultiBooleanSetting settings = new MultiBooleanSetting("Удалять", MultiBooleanSetting.Value.of("Огонь на экране", true), MultiBooleanSetting.Value.of("Линия босса", false), MultiBooleanSetting.Value.of("Анимация тотема", true), MultiBooleanSetting.Value.of("Тайтлы", false), MultiBooleanSetting.Value.of("Таблица", false), MultiBooleanSetting.Value.of("Туман", true), MultiBooleanSetting.Value.of("Тряску камеры", true), MultiBooleanSetting.Value.of("Дождь", true), MultiBooleanSetting.Value.of("Броня", false), MultiBooleanSetting.Value.of("Эффект свечения", true), MultiBooleanSetting.Value.of("Игроки", false), MultiBooleanSetting.Value.of("Серца", false));

    private NoRender() {
    }

    public boolean isRemoveFire() {
        return this.isEnabled() && this.settings.isEnable("Огонь на экране");
    }

    public boolean isRemoveBossBar() {
        return this.isEnabled() && this.settings.isEnable("Линия босса");
    }

    public boolean isRemoveTotemAnimation() {
        return this.isEnabled() && this.settings.isEnable("Анимация тотема");
    }

    public boolean isRemoveTitles() {
        return this.isEnabled() && this.settings.isEnable("Тайтлы");
    }

    public boolean isRemoveScoreboard() {
        return this.isEnabled() && this.settings.isEnable("Таблица");
    }

    public boolean isRemoveFog() {
        return this.isEnabled() && this.settings.isEnable("Туман");
    }

    public boolean isRemoveHurtCamera() {
        return this.isEnabled() && this.settings.isEnable("Тряску камеры");
    }

    public boolean isRemoveRain() {
        return this.isEnabled() && this.settings.isEnable("Дождь");
    }

    public boolean isHideArmor() {
        return this.isEnabled() && this.settings.isEnable("Броня");
    }

    public boolean isDisableGlow() {
        return this.isEnabled() && this.settings.isEnable("Эффект свечения");
    }

    public boolean isHidePlayers() {
        return this.isEnabled() && this.settings.isEnable("Игроки");
    }

    public boolean isRemoveWitherHearts() {
        return this.isEnabled() && this.settings.isEnable(REMOVE_WITHER_HEARTS);
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (NoRender.mc.world == null || NoRender.mc.player == null) {
            return;
        }
        if (this.isRemoveRain()) {
            NoRender.mc.world.setRainGradient(0.0f);
            NoRender.mc.world.setThunderGradient(0.0f);
        }
    }

    @EventTarget
    public void onFog(EventFog event) {
        if (!this.isRemoveFog()) {
            return;
        }
        if (NoRender.mc.player != null && NoRender.mc.player.hasStatusEffect(StatusEffects.BLINDNESS)) {
            return;
        }
        event.setColor(-1);
        event.setDistance(1000.0f);
        event.setCancelled(true);
    }

    @Override
    public String[] getLegacyNames() {
        return new String[]{"No Render"};
    }
}

