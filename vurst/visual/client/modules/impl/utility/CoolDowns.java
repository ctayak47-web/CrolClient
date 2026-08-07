
package vurst.visual.client.modules.impl.utility;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;
import net.minecraft.ItemCooldownManager;
import net.minecraft.ItemStack;
import net.minecraft.Identifier;
import net.minecraft.DrawContext;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.utility.game.player.PlayerIntersectionUtil;
import vurst.visual.utility.text.UiTranslation;

@ModuleAnnotation(name="CoolDowns", category=Category.MOVEMENT, description="Показывает кулдауны предметов над слотами.")
public final class CoolDowns
extends Module {
    public static final CoolDowns INSTANCE = new CoolDowns();
    private static final int COLOR_RED = -43691;
    private static final int COLOR_YELLOW = -171;
    private static final int COLOR_GREEN = -11141291;
    private static final String LEGACY_MODE_NAME = "Режим";
    private static final String LEGACY_PULSE_MODE = "Пульс";

    private CoolDowns() {
    }

    @Override
    public void load(JsonObject object) {
        String legacyMode;
        JsonObject migrated;
        JsonObject jsonObject = migrated = object == null ? null : object.deepCopy();
        if (migrated != null && (legacyMode = this.resolveLegacyMode(migrated)) != null && !LEGACY_PULSE_MODE.equals(legacyMode)) {
            migrated.addProperty("enabled", false);
        }
        super.load(migrated);
    }

    public void renderCooldownText(DrawContext context, int x, int y, ItemStack stack) {
        if (!this.isEnabled() || PlayerIntersectionUtil.nullCheck()) {
            return;
        }
        if (stack == null || stack.isEmpty()) {
            return;
        }
        int remainingSeconds = this.getRemainingSeconds(stack);
        if (remainingSeconds <= 0) {
            return;
        }
        String text = Integer.toString(remainingSeconds);
        int color = this.getColorForSeconds(remainingSeconds);
        float scale = this.getScaleForText(text);
        float drawX = ((float)x + 1.0f) / scale;
        float drawY = ((float)y + 1.0f) / scale;
        context.getMatrices().push();
        context.getMatrices().translate(0.0f, 0.0f, 200.0f);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.drawTextWithShadow(CoolDowns.mc.textRenderer, text, (int)drawX, (int)drawY, color);
        context.getMatrices().pop();
    }

    private int getRemainingSeconds(ItemStack stack) {
        if (CoolDowns.mc.player == null) {
            return 0;
        }
        ItemCooldownManager cooldowns = CoolDowns.mc.player.getItemCooldownManager();
        Identifier group = cooldowns.getGroup(stack);
        ItemCooldownManager.Entry entry = (ItemCooldownManager.Entry)cooldowns.entries.get(group);
        if (entry == null) {
            return 0;
        }
        int remainingTicks = entry.comp_3084() - cooldowns.tick;
        if (remainingTicks <= 0) {
            return 0;
        }
        return (remainingTicks + 19) / 20;
    }

    private int getColorForSeconds(int seconds) {
        if (seconds <= 5) {
            return -11141291;
        }
        if (seconds <= 20) {
            return -171;
        }
        return -43691;
    }

    private float getScaleForText(String text) {
        int length = text.length();
        if (length >= 3) {
            return 0.8f;
        }
        if (length == 2) {
            return 0.9f;
        }
        return 1.0f;
    }

    private String resolveLegacyMode(JsonObject object) {
        if (!object.has("Settings") || !object.get("Settings").isJsonObject()) {
            return null;
        }
        JsonObject settings = object.getAsJsonObject("Settings");
        for (Map.Entry<String, JsonElement> entry : settings.entrySet()) {
            if (!LEGACY_MODE_NAME.equals(this.normalize(entry.getKey()))) continue;
            if (!entry.getValue().isJsonPrimitive()) {
                return null;
            }
            return this.normalize(entry.getValue().getAsString());
        }
        return null;
    }

    private String normalize(String value) {
        return UiTranslation.translate(value == null ? "" : value);
    }
}

