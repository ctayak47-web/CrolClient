package ru.crolclient.api.feature.module.setting.implement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.lwjgl.glfw.GLFW;
import ru.crolclient.api.feature.module.setting.Setting;

import java.util.function.Supplier;

@Getter
@Setter
@Accessors(chain = true)
public class BindSetting extends Setting {
    private int key = GLFW.GLFW_KEY_UNKNOWN;
    private int type = 1; // 0 - hold, 1 - toggle

    public BindSetting(String name, String description) {
        super(name, description);
    }

    public BindSetting visible(Supplier<Boolean> visible) {
        setVisible(visible);
        return this;
    }

    public boolean isMouseBind() {
        return key >= GLFW.GLFW_MOUSE_BUTTON_1 && key <= GLFW.GLFW_MOUSE_BUTTON_LAST;
    }

    public int getMouseButton() {
        return isMouseBind() ? key : -1;
    }

    public BindSetting setMouseButton(int mouseButton) {
        if (mouseButton >= 0 && mouseButton <= GLFW.GLFW_MOUSE_BUTTON_LAST) {
            this.key = mouseButton;
        }
        return this;
    }
}