
package vurst.visual.utility.game.player;

import java.util.List;
import lombok.Generated;
import net.minecraft.KeyBinding;
import net.minecraft.InputUtil;
import net.minecraft.AbstractCommandBlockScreen;
import net.minecraft.AnvilScreen;
import net.minecraft.StructureBlockScreen;
import net.minecraft.SignEditScreen;
import vurst.visual.VurstVisual;
import vurst.visual.base.events.impl.player.EventUpdate;
import vurst.visual.base.request.ScriptManager;
import vurst.visual.utility.game.player.MovingUtil;
import vurst.visual.utility.game.player.PlayerIntersectionUtil;
import vurst.visual.utility.game.player.PlayerInventoryUtil;
import vurst.visual.utility.interfaces.IMinecraft;

public final class PlayerInventoryComponent
implements IMinecraft {
    public static final List<KeyBinding> moveKeys = List.of(PlayerInventoryComponent.mc.options.forwardKey, PlayerInventoryComponent.mc.options.backKey, PlayerInventoryComponent.mc.options.leftKey, PlayerInventoryComponent.mc.options.rightKey, PlayerInventoryComponent.mc.options.jumpKey);
    public static ScriptManager.ScriptTask script = new ScriptManager.ScriptTask();
    public static boolean canMove = true;

    public static void addTask(Runnable task) {
        if (MovingUtil.hasPlayerMovement()) {
            VurstVisual.getInstance().getScriptManager().addTask(script);
            switch (VurstVisual.getInstance().getServerHandler().getServer()) {
                case "FunTime": 
                case "HolyWorld": {
                    script.schedule(EventUpdate.class, eventUpdate -> {
                        PlayerInventoryComponent.disableMoveKeys();
                        return true;
                    });
                    script.schedule(EventUpdate.class, eventUpdate -> {
                        task.run();
                        PlayerInventoryComponent.enableMoveKeys();
                        return true;
                    });
                    return;
                }
                case "ReallyWorld": {
                    if (!PlayerInventoryComponent.mc.player.isOnGround()) break;
                    script.schedule(EventUpdate.class, eventUpdate -> {
                        PlayerInventoryComponent.disableMoveKeys();
                        return true;
                    });
                    script.schedule(EventUpdate.class, eventUpdate -> {
                        PlayerInventoryComponent.disableMoveKeys();
                        return true;
                    });
                    script.schedule(EventUpdate.class, eventUpdate -> {
                        task.run();
                        return true;
                    });
                    script.schedule(EventUpdate.class, eventUpdate -> {
                        PlayerInventoryComponent.enableMoveKeys();
                        return true;
                    });
                    return;
                }
                case "CopyTime": {
                    script.schedule(EventUpdate.class, eventUpdate -> {
                        PlayerInventoryComponent.disableMoveKeys();
                        return true;
                    });
                    script.schedule(EventUpdate.class, eventUpdate -> {
                        task.run();
                        return true;
                    });
                    script.schedule(EventUpdate.class, eventUpdate -> {
                        PlayerInventoryComponent.enableMoveKeys();
                        return true;
                    });
                    return;
                }
            }
        }
        task.run();
    }

    public static void disableMoveKeys() {
        canMove = false;
        PlayerInventoryComponent.unPressMoveKeys();
    }

    public static void enableMoveKeys() {
        PlayerInventoryUtil.closeScreen(true);
        canMove = true;
        PlayerInventoryComponent.updateMoveKeys();
    }

    public static void unPressMoveKeys() {
        moveKeys.forEach(keyBinding -> keyBinding.setPressed(false));
    }

    public static void updateMoveKeys() {
        moveKeys.forEach(keyBinding -> keyBinding.setPressed(InputUtil.isKeyPressed((long)mc.getWindow().getHandle(), (int)keyBinding.getDefaultKey().getCode())));
    }

    public static boolean shouldSkipExecution() {
        if (PlayerInventoryComponent.mc.currentScreen == null) {
            return false;
        }
        if (PlayerIntersectionUtil.isChat(PlayerInventoryComponent.mc.currentScreen)) {
            return false;
        }
        if (PlayerInventoryComponent.mc.currentScreen instanceof SignEditScreen) {
            return false;
        }
        if (PlayerInventoryComponent.mc.currentScreen instanceof AnvilScreen) {
            return false;
        }
        if (PlayerInventoryComponent.mc.currentScreen instanceof AbstractCommandBlockScreen) {
            return false;
        }
        if (PlayerInventoryComponent.mc.currentScreen instanceof StructureBlockScreen) {
            return false;
        }
        if (PlayerInventoryComponent.mc.player != null && PlayerInventoryComponent.mc.player.currentScreenHandler != null) {
            int slotCount = PlayerInventoryComponent.mc.player.currentScreenHandler.slots.size();
            return slotCount >= 27;
        }
        return false;
    }

    @Generated
    private PlayerInventoryComponent() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

