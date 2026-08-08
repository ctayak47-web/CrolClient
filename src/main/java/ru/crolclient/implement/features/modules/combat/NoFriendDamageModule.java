package ru.crolclient.implement.features.modules.combat;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import org.jetbrains.annotations.NotNull;
import ru.crolclient.api.repository.friend.FriendRepository;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.event.EventHandler;
import ru.crolclient.implement.events.packet.PacketEvent;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class NoFriendDamageModule extends Module {
    public NoFriendDamageModule() {
        super("NoFriendDamage", "No Friend Damage", ModuleCategory.COMBAT);
    }

    @EventHandler
    public void onPacket(PacketEvent packetEvent) {
        Packet<?> packet = packetEvent.getPacket();
        if (packetEvent.isSend() && packet instanceof PlayerInteractEntityC2SPacket interactEntityC2SPacket) {
            Entity entity = getEntity(interactEntityC2SPacket);
            InteractType interactType = getInteractType(interactEntityC2SPacket);

            if (validate(entity, interactType)) {
                packetEvent.cancel();
            }
        }
    }

    private boolean validate(Entity entity, InteractType interactType) {
        if (!FriendRepository.isFriend(entity.getName().getString())) {
            return false;
        }
        if (interactType != InteractType.ATTACK) {
            return false;
        }

        return entity instanceof PlayerEntity;
    }


    private Entity getEntity(@NotNull PlayerInteractEntityC2SPacket packet) {
        return mc.world.getEntityById(packet.entityId);
    }

    private InteractType getInteractType(@NotNull PlayerInteractEntityC2SPacket packet) {
        try {
            var typeField = PlayerInteractEntityC2SPacket.class.getDeclaredField("type");
            typeField.setAccessible(true);
            Object typeValue = typeField.get(packet);
            String typeName = typeValue.toString();
            return switch (typeName) {
                case "INTERACT" -> InteractType.INTERACT;
                case "ATTACK" -> InteractType.ATTACK;
                case "INTERACT_AT" -> InteractType.INTERACT_AT;
                default -> InteractType.INTERACT;
            };
        } catch (Exception e) {
            return InteractType.INTERACT;
        }
    }

    public enum InteractType {
        INTERACT, ATTACK, INTERACT_AT
    }
}

