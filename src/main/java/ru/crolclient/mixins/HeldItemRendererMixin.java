package ru.crolclient.mixins;

import net.minecraft.client.render.item.HeldItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.crolclient.api.event.EventManager;
import ru.crolclient.implement.events.player.HeldItemRendererEvent;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
}
