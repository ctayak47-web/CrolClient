package ru.crolclient.implement.features.modules.misc;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;
import ru.crolclient.api.event.EventHandler;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.feature.module.setting.implement.BindSetting;
import ru.crolclient.api.repository.friend.FriendRepository;
import ru.crolclient.implement.events.player.TickEvent;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClickFriendModule extends Module {
    final BindSetting friendBind = new BindSetting("Friend Bind", "Bind to add/remove friends");
    boolean lastKeyState = false;

    public ClickFriendModule() {
        super("ClickFriend", "Click Friend", ModuleCategory.MISC);
        setup(friendBind);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.currentScreen != null) {
            return;
        }

        boolean currentKeyState = isKeyPressed();

        if (currentKeyState && !lastKeyState) {
            HitResult crosshairTarget = mc.crosshairTarget;

            if (crosshairTarget != null && crosshairTarget.getType() == HitResult.Type.ENTITY) {
                if (((EntityHitResult) crosshairTarget).getEntity() instanceof PlayerEntity player) {
                    String playerName = player.getName().getString();

                    if (FriendRepository.isFriend(playerName)) {
                        FriendRepository.removeFriend(playerName);
                        logDirect("Удален из друзей: " + playerName, Formatting.RED);
                    } else {
                        FriendRepository.addFriend(playerName);
                        logDirect("Добавлен в друзья: " + playerName, Formatting.GREEN);
                    }
                }
            }
        }

        lastKeyState = currentKeyState;
    }

    private boolean isKeyPressed() {
        int key = friendBind.getKey();
        if (key == GLFW.GLFW_KEY_UNKNOWN) return false;
        return GLFW.glfwGetKey(mc.getWindow().getHandle(), key) == GLFW.GLFW_PRESS;
    }
}