package crol.client.mixins.hooks;

import crol.client.CrolClient;
import crol.client.event.classes.PlaceBlockEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(BlockItem.class) // Убрал фигурные скобки, так как класс один
public class BlockItemMixin {

    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void onPlace(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (context.getWorld().isClient) {
            BlockItem item = (BlockItem) (Object) this;

            PlaceBlockEvent placeBlockEvent = new PlaceBlockEvent(context.getBlockPos(), item.getBlock());
            CrolClient.INSTANCE.getEventManager().hookEvent(placeBlockEvent);

            if (placeBlockEvent.isCancelled()) {
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }
}