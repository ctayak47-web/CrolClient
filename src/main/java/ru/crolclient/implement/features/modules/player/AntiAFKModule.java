package ru.crolclient.implement.features.modules.player;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import ru.crolclient.api.feature.module.Module;
import ru.crolclient.api.feature.module.ModuleCategory;
import ru.crolclient.api.feature.module.setting.implement.SelectSetting;
import ru.crolclient.api.feature.module.setting.implement.ValueSetting;
import ru.crolclient.api.event.EventHandler;
import ru.crolclient.implement.events.player.TickEvent;

import java.util.Random;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class AntiAFKModule extends Module {

    final SelectSetting actionSetting = new SelectSetting("Action", "Selects the action to perform")
            .value("Rotate", "Jump", "Command", "Move");

    final ValueSetting intervalSetting = new ValueSetting("Interval", "How often to perform the action (in minutes)")
            .setValue(1.0F)
            .range(1.0F, 10.0F);

    long lastActionTime = 0;
    final Random random = new Random();
    boolean isRotating = false;
    float currentYawOffset = 0.0F;
    boolean isMoving = false;

    public AntiAFKModule() {
        super("AntiAFK", "Anti AFK", ModuleCategory.PLAYER);
        setup(actionSetting, intervalSetting);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (isRotating) {
            continueRotation();
        } else if (currentTime - lastActionTime >= intervalSetting.getValue() * 60 * 1000) {
            performAction();
            lastActionTime = currentTime;
        }
    }

    private void performAction() {
        switch (actionSetting.getSelected()) {
            case "Rotate":
                startRotation();
                break;
            case "Jump":
                jump();
                break;
            case "Command":
                sendRandomCommand();
                break;
            case "Move":
                startMovement();
                break;
        }
    }

    private void startRotation() {
        isRotating = true;
        currentYawOffset = 0.0F;
    }

    private void continueRotation() {
        float yaw = mc.player.getYaw();
        if (currentYawOffset < 180.0F) {
            currentYawOffset += 5.0F;
            mc.player.setYaw(yaw + 5.0F);
        } else {
            isRotating = false;
        }
    }

    private void jump() {
        if (mc.player.isOnGround()) {
            mc.player.jump();
        }
    }

    private void sendRandomCommand() {
        String randomCommand = "/" + generateRandomString(5);
        assert mc.player != null;
        mc.player.networkHandler.sendChatMessage(randomCommand);
    }

    private void startMovement() {
        isMoving = true;
        new Thread(() -> {
            try {
                assert mc.player != null;
                mc.player.setVelocity(0, mc.player.getVelocity().y, 0.2);
                Thread.sleep(300);
                mc.player.setVelocity(0, mc.player.getVelocity().y, -0.2);
                Thread.sleep(300);
                mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                isMoving = false;
            }
        }).start();
    }

    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
