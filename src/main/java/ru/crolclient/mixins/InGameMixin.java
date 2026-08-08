package ru.crolclient.mixins;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.crolclient.core.Extra;
import ru.crolclient.api.system.draw.DrawEngineImpl;
import ru.crolclient.api.event.EventManager;
import ru.crolclient.implement.events.render.DrawEvent;
import ru.crolclient.common.util.math.MathUtil;

@Mixin(InGameHud.class)
public class InGameMixin {

    @Inject(method = "render", at = @At("RETURN"))
    public void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        float tickDelta = tickCounter.getTickDelta(false);

        DrawEvent event = new DrawEvent(
                context,
                new DrawEngineImpl(),
                tickDelta
        );

        EventManager.callEvent(event);

        Extra.getInstance()
                .getDraggableRepository()
                .draggable()
                .forEach(draggable -> {
                    draggable.tick(tickDelta);

                    if (draggable.visible()) {
                        draggable.startAnimation();
                    } else {
                        draggable.startCloseAnimation();
                    }

                    float scale = draggable
                            .getScaleAnimation()
                            .getOutput()
                            .floatValue();

                    if (!draggable.isCloseAnimationFinished()) {
                        MathUtil.scale(context.getMatrices(), draggable.getX() + (float) draggable.getWidth() / 2, draggable.getY() + (float) draggable.getHeight() / 2, scale, () -> draggable.drawDraggable(context));
                    }
                });
    }
}
