package crol.client.mixins.hooks;

import crol.client.CrolClient;
import crol.client.event.classes.ReceivePacketEvent;
import crol.client.event.classes.SendPacketEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ClientConnection.class)
public class ClientConnectionMixin {

    /**
     * ПЕРЕХВАТ ВХОДЯЩИХ ПАКЕТОВ (Receive)
     * В 1.21.4 используем метод handlePacket.
     * ВНИМАНИЕ: Метод handlePacket в коде игры статический, поэтому и в миксине он должен быть static.
     */
    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static <T extends PacketListener> void onHandlePacket(Packet<T> packet, PacketListener listener, CallbackInfo ci) {
        ReceivePacketEvent event = new ReceivePacketEvent(packet);
        CrolClient.INSTANCE.getEventManager().hookEvent(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    /**
     * ПЕРЕХВАТ ИСХОДЯЩИХ ПАКЕТОВ (Send)
     * В 1.21.4 метод называется "send".
     */
    @Inject(
            method = "send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketCallbacks;Z)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onSendPacket(Packet<?> packet, @Nullable PacketCallbacks callbacks, boolean flush, CallbackInfo ci) {
        SendPacketEvent event = new SendPacketEvent(packet);
        CrolClient.INSTANCE.getEventManager().hookEvent(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}