package ru.crolclient.api.feature.module;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import ru.crolclient.core.Extra;
import ru.crolclient.api.feature.module.setting.SettingRepository;
import ru.crolclient.api.event.EventManager;
import ru.crolclient.common.QuickImports;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Module extends SettingRepository implements QuickImports {
    protected final MinecraftClient mc = MinecraftClient.getInstance();

    String name;
    String visibleName;
    ModuleCategory category;

    public boolean isMouseBind() {
        return key >= GLFW.GLFW_MOUSE_BUTTON_1 && key <= GLFW.GLFW_MOUSE_BUTTON_LAST;
    }

    public int getMouseButton() {
        return isMouseBind() ? key : -1;
    }

    public Module(String name, ModuleCategory category) {
        this.name = name;
        this.category = category;
        this.visibleName = name;
    }

    public Module(String name, String visibleName, ModuleCategory category) {
        this.name = name;
        this.visibleName = visibleName;
        this.category = category;
    }

    @NonFinal
    int key = GLFW.GLFW_KEY_UNKNOWN;

    @NonFinal
    boolean state;

    @NonFinal
    int type = 1; // 0 - hold, 1 - toggle

    public void switchState() {
        setState(!state);
    }

    public void setState(boolean state) {
        if (state != this.state) {
            this.state = state;
            handleStateChange();
        }
    }

    private void handleStateChange() {
        if (mc.player != null && mc.world != null) {
            if (state) {
                activate();
            } else {
                deactivate();
            }
        }
        toggleSilent(state);
    }

    private void toggleSilent(boolean activate) {
        EventManager eventManager = Extra.getInstance().getEventManager();
        if (activate) {
            eventManager.register(this);
        } else {
            eventManager.unregister(this);
        }
    }

    public void activate() {
    }

    public void deactivate() {
    }
}
