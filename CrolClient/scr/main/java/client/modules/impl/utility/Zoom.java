
package crol.client.modules.impl.utility;

import com.darkmagician6.eventapi.EventTarget;
import com.google.gson.JsonObject;
import crol.client.base.events.impl.player.EventUpdate;
import crol.client.base.events.impl.render.EventFov;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.api.setting.impl.ModeSetting;
import crol.client.modules.api.setting.impl.NumberSetting;
import crol.client.utility.game.player.PlayerIntersectionUtil;
import crol.client.utility.render.level.Render3DUtil;

@ModuleAnnotation(name="Zoom", category=Category.MOVEMENT, description="Приближает камеру.")
public final class Zoom
extends Module {
    public static final Zoom INSTANCE = new Zoom();
    private static final float SMOOTH_SPEED = 5.0f;
    private static final String SETTING_MODE = "Режим";
    private static final String MODE_TOGGLE = "Нажатие";
    private static final String MODE_HOLD = "Удержание";
    private final ModeSetting mode = new ModeSetting("Режим", "Нажатие", "Удержание");
    private final NumberSetting zoomDistance = new NumberSetting("Дистанция", 3.0f, 1.0f, 15.0f, 0.1f);
    private float currentFov = -1.0f;
    private float originalFov = -1.0f;

    private Zoom() {
    }

    @Override
    public void load(JsonObject object) {
        this.migrateLegacySettings(object);
        super.load(object);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.currentFov = -1.0f;
        this.originalFov = -1.0f;
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (Zoom.mc.player == null || Zoom.mc.world == null) {
            return;
        }
        if (this.mode.is(MODE_HOLD) && !this.isHoldKeyDown()) {
            this.toggle();
        }
    }

    @EventTarget
    public void onFov(EventFov event) {
        if (Zoom.mc.player == null) {
            return;
        }
        float baseFov = event.getFov();
        float fov = this.applyZoom(baseFov, Render3DUtil.getTickDelta());
        int newFov = Math.max(1, Math.round(fov));
        if (newFov != event.getFov()) {
            event.setFov(newFov);
            event.setCancelled(true);
        }
    }

    public float applyZoom(float baseFov, float tickDelta) {
        float alpha;
        float dtSeconds;
        float targetFov;
        boolean active = this.isEnabled();
        if (this.originalFov < 0.0f || !active && Math.abs(baseFov - this.originalFov) > 0.001f) {
            this.originalFov = baseFov;
        }
        float strength = Math.max(1.0f, this.zoomDistance.getCurrent());
        float f = targetFov = active ? this.originalFov / strength : this.originalFov;
        if (this.currentFov < 0.0f) {
            this.currentFov = baseFov;
        }
        if ((dtSeconds = tickDelta / 20.0f) < 0.0f) {
            dtSeconds = 0.0f;
        }
        if ((alpha = 1.0f - (float)Math.exp(-5.0f * dtSeconds)) > 1.0f) {
            alpha = 1.0f;
        }
        this.currentFov += (targetFov - this.currentFov) * alpha;
        float epsilon = 5.0E-4f;
        if (Math.abs(targetFov - this.currentFov) <= epsilon) {
            this.currentFov = targetFov;
        }
        if (!active && this.currentFov == this.originalFov) {
            this.currentFov = -1.0f;
            this.originalFov = -1.0f;
            return baseFov;
        }
        return this.currentFov;
    }

    private boolean isHoldKeyDown() {
        int keyCode = this.getKeyCode();
        if (keyCode == -1) {
            return false;
        }
        return PlayerIntersectionUtil.isKey(PlayerIntersectionUtil.getKeyType(keyCode), keyCode);
    }

    private void migrateLegacySettings(JsonObject object) {
        String value;
        if (object == null || !object.has("Settings")) {
            return;
        }
        JsonObject settings = object.getAsJsonObject("Settings");
        if (settings == null) {
            return;
        }
        if (settings.has("Mode") && !settings.has(SETTING_MODE)) {
            settings.add(SETTING_MODE, settings.get("Mode"));
        }
        if (!settings.has(SETTING_MODE)) {
            return;
        }
        switch (value = settings.get(SETTING_MODE).getAsString()) {
            case "Toggle": {
                settings.addProperty(SETTING_MODE, MODE_TOGGLE);
                break;
            }
            case "Hold": {
                settings.addProperty(SETTING_MODE, MODE_HOLD);
                break;
            }
        }
    }
}

