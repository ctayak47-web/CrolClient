package ru.crolclient.implement.features.modules.misc;

import ru.crolclient.implement.events.packet.PacketEvent;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import ru.crolclient.api.event.EventHandler;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.feature.module.setting.implement.BooleanSetting;
import ru.crolclient.api.repository.friend.FriendRepository;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class AutoTpAcceptModule extends Module {

    final BooleanSetting onlyFriends = new BooleanSetting("Only Friends", "Принимает тп только от друзей")
            .setValue(true);

    public AutoTpAcceptModule() {
        super("AutoTpaccept", "Auto Tpaccept", ModuleCategory.MISC);
        setup(onlyFriends);
    }

    @EventHandler
    public void onPacketReceive(PacketEvent event) {
        if (!event.isReceive() || !(event.getPacket() instanceof GameMessageS2CPacket packet)) {
            return;
        }

        String message = packet.content().getString().toLowerCase();
        if (message.contains("tpaccept") || message.contains("tpyes")) {
            String playerName = extractPlayerName(message);

            if (onlyFriends.isValue()) {
                if (playerName != null && FriendRepository.isFriend(playerName)) {
                    acceptTeleport();
                }
            } else {
                acceptTeleport();
            }
        }
    }

    private String extractPlayerName(String message) {
        for (var player : mc.getNetworkHandler().getPlayerList()) {
            String name = player.getProfile().getName();
            if (message.contains(name.toLowerCase())) {
                return name;
            }
        }
        return null;
    }

    private void acceptTeleport() {
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendChatCommand("tpaccept");
        }
    }
}