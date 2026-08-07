
package vurst.visual.client.modules.impl.render;

import com.darkmagician6.eventapi.EventTarget;
import net.minecraft.PlayerEntity;
import net.minecraft.HitResult;
import net.minecraft.EntityHitResult;
import net.minecraft.Perspective;
import vurst.visual.base.events.impl.render.EventHudRender;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.BooleanSetting;
import vurst.visual.client.modules.api.setting.impl.NumberSetting;
import vurst.visual.utility.render.display.base.CustomDrawContext;
import vurst.visual.utility.render.display.base.color.ColorRGBA;

@ModuleAnnotation(name="Crosshair", category=Category.RENDER, description="Кастомный прицел.")
public final class Crosshair
extends Module {
    public static final Crosshair INSTANCE = new Crosshair();
    private final NumberSetting thickness = new NumberSetting("Толщина", 1.0f, 0.5f, 3.0f, 0.1f);
    private final NumberSetting length = new NumberSetting("Длина", 3.0f, 1.0f, 8.0f, 0.5f);
    private final NumberSetting gap = new NumberSetting("Зазор", 2.0f, 0.0f, 5.0f, 0.5f);
    private final BooleanSetting dynamicGap = new BooleanSetting("Динамический зазор", false);
    private final BooleanSetting useEntityColor = new BooleanSetting("Цвет цели", false);
    private final ColorRGBA entityColor = new ColorRGBA(255, 0, 0, 255);

    private Crosshair() {
    }

    @EventTarget
    public void onRender(EventHudRender event) {
        HitResult ItemStackParticleEffect;
        if (Crosshair.mc.player == null || Crosshair.mc.world == null) {
            return;
        }
        if (Crosshair.mc.options.getPerspective() != Perspective.FIRST_PERSON) {
            return;
        }
        CustomDrawContext ctx = event.getContext();
        float x = (float)mc.getWindow().getScaledWidth() / 2.0f;
        float y = (float)mc.getWindow().getScaledHeight() / 2.0f;
        float currentGap = this.gap.getCurrent();
        if (this.dynamicGap.isEnabled()) {
            float cooldown = 1.0f - Crosshair.mc.player.getAttackCooldownProgress(0.0f);
            currentGap += 8.0f * cooldown;
        }
        float currentThickness = this.thickness.getCurrent();
        float currentLength = this.length.getCurrent();
        boolean isPlayerTarget = false;
        if (this.useEntityColor.isEnabled() && (ItemStackParticleEffect = Crosshair.mc.crosshairTarget) instanceof EntityHitResult) {
            EntityHitResult hit = (EntityHitResult)ItemStackParticleEffect;
            isPlayerTarget = hit.getEntity() instanceof PlayerEntity;
        }
        ColorRGBA color = isPlayerTarget ? this.entityColor : new ColorRGBA(255, 255, 255, 255);
        this.drawLine(ctx, x - currentThickness / 2.0f, y - currentGap - currentLength, currentThickness, currentLength, color);
        this.drawLine(ctx, x - currentThickness / 2.0f, y + currentGap, currentThickness, currentLength, color);
        this.drawLine(ctx, x - currentGap - currentLength, y - currentThickness / 2.0f, currentLength, currentThickness, color);
        this.drawLine(ctx, x + currentGap, y - currentThickness / 2.0f, currentLength, currentThickness, color);
    }

    private void drawLine(CustomDrawContext ctx, float x, float y, float width, float height, ColorRGBA color) {
        ctx.drawRect(x, y, width, height, color);
    }
}

