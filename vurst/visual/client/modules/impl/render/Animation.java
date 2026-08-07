
package vurst.visual.client.modules.impl.render;

import com.google.gson.JsonObject;
import net.minecraft.Screen;
import net.minecraft.InventoryScreen;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.BooleanSetting;

@ModuleAnnotation(name="Animation", category=Category.RENDER, description="Анимирует открытие и закрытие контейнеров.")
public final class Animation
extends Module {
    public static final String TARGET_INVENTORY = "Инвентарь";
    private static final String LEGACY_ANIMATED_SCREENS = "Что анимировать";
    public static final Animation INSTANCE = new Animation();
    public final BooleanSetting animateInventory = new BooleanSetting("Инвентарь", true);

    private Animation() {
    }

    public boolean shouldAnimate(Screen screen) {
        if (screen instanceof InventoryScreen) {
            return this.animateInventory.isEnabled();
        }
        return false;
    }

    @Override
    public void load(JsonObject object) {
        JsonObject migrated = this.migrateLegacyAnimatedScreens(object);
        super.load(migrated);
    }

    private JsonObject migrateLegacyAnimatedScreens(JsonObject object) {
        if (object == null || !object.has("Settings") || !object.get("Settings").isJsonObject()) {
            return object;
        }
        JsonObject migrated = object.deepCopy();
        JsonObject settings = migrated.getAsJsonObject("Settings");
        if (settings.has(TARGET_INVENTORY) || !settings.has(LEGACY_ANIMATED_SCREENS)) {
            return migrated;
        }
        String legacyValue = settings.get(LEGACY_ANIMATED_SCREENS).getAsString();
        boolean enabled = false;
        for (String value : legacyValue.split("\n")) {
            if (!TARGET_INVENTORY.equalsIgnoreCase(value.trim())) continue;
            enabled = true;
            break;
        }
        settings.addProperty(TARGET_INVENTORY, enabled);
        return migrated;
    }

    @Override
    public String[] getLegacyNames() {
        return new String[]{"InventoryAnimation", "Inventory Animation"};
    }
}

