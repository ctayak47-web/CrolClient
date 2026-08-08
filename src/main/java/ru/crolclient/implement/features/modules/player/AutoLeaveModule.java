package ru.crolclient.implement.features.modules.player;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.feature.module.setting.implement.BooleanSetting;
import ru.crolclient.api.feature.module.setting.implement.SelectSetting;
import ru.crolclient.api.feature.module.setting.implement.ValueSetting;
import ru.crolclient.api.repository.friend.FriendRepository;
import ru.crolclient.implement.events.player.TickEvent;
import ru.crolclient.api.event.EventHandler;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class AutoLeaveModule extends Module {

    final ValueSetting distanceSetting = new ValueSetting("Distance", "Specifies range to detect")
            .setValue(100.0F)
            .range(10.0F, 100.0F);

    final SelectSetting leaveAction = new SelectSetting("Leave Action", "Specifies the action to perform on detection")
            .value("/hub", "/spawn", "/clan home", "Disconnect");

    final BooleanSetting ignoreFriendsSetting = new BooleanSetting("Ignore Friends", "Ignores friends when checking for nearby players")
            .setValue(true);

    boolean triggered = false;

    public AutoLeaveModule() {
        super("AutoLeave", "Auto Leave", ModuleCategory.PLAYER);
        setup(distanceSetting, leaveAction, ignoreFriendsSetting);
    }

    @Override
    public void activate() {
        super.activate();
        triggered = false;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null || triggered) {
            return;
        }

        Vec3d playerPos = mc.player.getPos();
        float maxDistance = distanceSetting.getValue();

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity nearbyPlayer && isValid(nearbyPlayer)) {
                double distance = playerPos.distanceTo(nearbyPlayer.getPos());
                if (distance <= maxDistance) {
                    performLeaveAction(nearbyPlayer.getName().getString(), distance);
                    triggered = true;
                    deactivate();
                    return;
                }
            }
        }
    }

    private boolean isValid(PlayerEntity player) {
        assert mc.player != null;
        if (mc.player.getId() == player.getId()) {
            return false;
        }
        return !ignoreFriendsSetting.isValue() || !FriendRepository.isFriend(player.getName().getString());
    }

    private void performLeaveAction(String playerName, double distance) {
        String action = leaveAction.getSelected();
        switch (action) {
            case "/hub" -> {
                assert mc.player != null;
                mc.player.networkHandler.sendChatMessage("/hub");
            }
            case "/spawn" -> {
                assert mc.player != null;
                mc.player.networkHandler.sendChatMessage("/spawn");
            }
            case "/clan home" -> {
                assert mc.player != null;
                mc.player.networkHandler.sendChatMessage("/clan home");
            }
            case "Disconnect" -> disconnect(playerName, distance);
        }
    }

    private void disconnect(String playerName, double distance) {
        if (mc.getNetworkHandler() != null) {
            String reason = String.format("AutoLeave\n\nРядом обнаружен игрок %s на расстоянии %.2f блоков. Отключение.\nДля повторного использования модуля AutoLeave, необходимо его включить повторно", playerName, distance);
            mc.getNetworkHandler().getConnection().disconnect(Text.of(reason));
        }
    }
}
