
package crol.client.utility.mixin.minecraft.network;

import com.darkmagician6.eventapi.EventManager;
import net.minecraft.ClientConnection;
import net.minecraft.PacketListener;
import net.minecraft.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import crol.client.base.events.impl.server.EventPacket;
import crol.client.utility.interfaces.IMinecraft;

@Mixin(value={ClientConnection.class})
public class ClientConnectionMixin
implements IMinecraft {
    @Unique
    private static boolean stackOverflowFix;

    @Inject(method={"handlePacket"}, at={@At(value="HEAD")}, cancellable=true)
    private static <T extends PacketListener> void triggerReceivePacketEvent(Packet<T> packet, PacketListener listener, CallbackInfo ci) {
        if (mc != null && !mc.isOnThread()) {
            Packet queuedPacket = packet;
            mc.execute(() -> EventManager.call(new EventPacket(EventPacket.Action.RECEIVE, queuedPacket)));
            return;
        }
        EventPacket event = new EventPacket(EventPacket.Action.RECEIVE, packet);
        EventManager.call(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method={"send(Lnet/minecraft/network/packet/Packet;)V"}, at={@At(value="HEAD")}, cancellable=true)
    public void triggerSendPacketEvent(Packet<?> packet, CallbackInfo ci) {
        Packet<?> newPacket;
        if (stackOverflowFix) {
            return;
        }
        EventPacket event = new EventPacket(EventPacket.Action.SENT, packet);
        EventManager.call(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
        if ((newPacket = event.getPacket()) != packet) {
            ci.cancel();
            stackOverflowFix = true;
            mc.getNetworkHandler().sendPacket(newPacket);
            stackOverflowFix = false;
        }
    }
}

