package crol.client.mixins.hooks;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import org.spongepowered.asm.mixin.Mixin;

@Environment(EnvType.CLIENT)
@Mixin({PlayerPositionLookS2CPacket.class})
public class PlayerPositionLookS2CPacketMixin {
}
