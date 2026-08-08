
package crol.client.modules.impl.movement;

import com.darkmagician6.eventapi.EventTarget;
import com.google.gson.JsonObject;
import net.minecraft.Identifier;
import crol.client.CrolClient;
import crol.client.base.events.impl.render.EventHudRender;
import crol.client.base.font.Font;
import crol.client.base.font.Fonts;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.api.setting.impl.ModeSetting;
import crol.client.modules.api.setting.impl.NumberSetting;
import crol.client.utility.render.display.base.CustomDrawContext;
import crol.client.utility.render.display.base.color.ColorRGBA;

@ModuleAnnotation(name="HP Alert", category=Category.MOVEMENT, description="Оповещает о низком здоровье.")
public final class HealingTracker
extends Module {
    public static final HealingTracker INSTANCE = new HealingTracker();
    private static final Identifier BROKEN_TEX = CrolClient.id("hud/healing/lowhp_broken.png");
    private static final Identifier FIRE_TEX = CrolClient.id("hud/healing/lowhp_fire.png");
    private static final Identifier THREE_D_TEX = CrolClient.id("hud/healing/lowhp_3d.png");
    private static final Identifier ALERT_TEX = CrolClient.id("hud/healing/lowhp_alert.png");
    private static final float IMAGE_WIDTH = 120.0f;
    private static final float IMAGE_HEIGHT = 95.0f;
    private static final float IMAGE_Y = 50.0f;
    private static final float TEXT_OFFSET_Y = 15.0f;
    private static final float TEXT_SIZE = 10.0f;
    private static final String SETTING_HP_THRESHOLD = "Порог HP";
    private static final String SETTING_MESSAGE = "Сообщение";
    private static final String SETTING_FONT = "Шрифт";
    private static final String SETTING_STYLE = "Стиль";
    private static final String MESSAGE_NEED_HEAL = "Нужен хил!";
    private static final String MESSAGE_LOW_HP = "Мало HP!";
    private static final String MESSAGE_HEAL_ME = "Хильте меня!";
    private static final String FONT_MEDIUM = "Средний";
    private static final String FONT_BOLD = "Жирный";
    private static final String STYLE_BROKEN = "Сломанный";
    private static final String STYLE_FIRE = "Огонь";
    private static final String STYLE_THREE_D = "3D";
    private static final String STYLE_ALERT = "Тревога";
    private final NumberSetting hpThreshold = new NumberSetting("Порог HP", 8.0f, 1.0f, 20.0f, 1.0f);
    private final ModeSetting message = new ModeSetting("Сообщение", "Нужен хил!", "Мало HP!", "Хильте меня!");
    private final ModeSetting font = new ModeSetting("Шрифт", "Средний", "Жирный");
    private final ModeSetting style = new ModeSetting("Стиль", "Сломанный", "Огонь", "3D", "Тревога");
    private float pulse = 0.0f;
    private boolean pulseUp = true;
    private long lastPulseAt = 0L;

    private HealingTracker() {
    }

    @Override
    public void load(JsonObject object) {
        this.migrateLegacySettings(object);
        super.load(object);
    }

    @EventTarget
    public void onRender(EventHudRender event) {
        if (HealingTracker.mc.player == null || HealingTracker.mc.world == null) {
            return;
        }
        if (HealingTracker.mc.player.getHealth() >= this.hpThreshold.getCurrent()) {
            this.pulse = 0.0f;
            this.pulseUp = true;
            return;
        }
        this.updatePulse();
        if (this.pulse <= 0.0f) {
            return;
        }
        int screenWidth = mc.getWindow().getScaledWidth();
        float imageX = ((float)screenWidth - 120.0f) / 2.0f;
        float textY = 160.0f;
        String text = this.message.get();
        Font textFont = this.font.is(FONT_BOLD) ? Fonts.BOLD.getFont(10.0f) : Fonts.MEDIUM.getFont(10.0f);
        float textWidth = textFont.width(text);
        float textX = ((float)screenWidth - textWidth) / 2.0f;
        int alpha = HealingTracker.clamp255(this.pulse * 255.0f);
        ColorRGBA color = new ColorRGBA(255, 255, 255, alpha);
        CustomDrawContext ctx = event.getContext();
        ctx.drawTexture(this.getStyleTexture(), imageX, 50.0f, 120.0f, 95.0f, color);
        ctx.drawText(textFont, text, textX, textY, color);
    }

    private Identifier getStyleTexture() {
        if (this.style.is(STYLE_FIRE)) {
            return FIRE_TEX;
        }
        if (this.style.is(STYLE_THREE_D)) {
            return THREE_D_TEX;
        }
        if (this.style.is(STYLE_ALERT)) {
            return ALERT_TEX;
        }
        return BROKEN_TEX;
    }

    private void updatePulse() {
        long now = System.currentTimeMillis();
        if (now - this.lastPulseAt < 50L) {
            return;
        }
        this.lastPulseAt = now;
        if (this.pulseUp) {
            this.pulse += 0.07f;
            if (this.pulse >= 1.0f) {
                this.pulse = 1.0f;
                this.pulseUp = false;
            }
        } else {
            this.pulse -= 0.07f;
            if (this.pulse <= 0.0f) {
                this.pulse = 0.0f;
                this.pulseUp = true;
            }
        }
    }

    private void migrateLegacySettings(JsonObject object) {
        if (object == null || !object.has("Settings")) {
            return;
        }
        JsonObject settings = object.getAsJsonObject("Settings");
        if (settings == null) {
            return;
        }
        this.migrateSettingKey(settings, "HP Threshold", SETTING_HP_THRESHOLD);
        this.migrateSettingKey(settings, "Message", SETTING_MESSAGE);
        this.migrateSettingKey(settings, "Font", SETTING_FONT);
        this.migrateSettingKey(settings, "Style", SETTING_STYLE);
        this.migrateSettingValue(settings, SETTING_MESSAGE, "Need heal!", MESSAGE_NEED_HEAL);
        this.migrateSettingValue(settings, SETTING_MESSAGE, "Low HP!", MESSAGE_LOW_HP);
        this.migrateSettingValue(settings, SETTING_MESSAGE, "Heal me!", MESSAGE_HEAL_ME);
        this.migrateSettingValue(settings, SETTING_FONT, "Medium", FONT_MEDIUM);
        this.migrateSettingValue(settings, SETTING_FONT, "Bold", FONT_BOLD);
        this.migrateSettingValue(settings, SETTING_STYLE, "Broken", STYLE_BROKEN);
        this.migrateSettingValue(settings, SETTING_STYLE, "Fire", STYLE_FIRE);
        this.migrateSettingValue(settings, SETTING_STYLE, "ThreeD", STYLE_THREE_D);
        this.migrateSettingValue(settings, SETTING_STYLE, "Alert", STYLE_ALERT);
    }

    private void migrateSettingKey(JsonObject settings, String legacyName, String newName) {
        if (!settings.has(legacyName) || settings.has(newName)) {
            return;
        }
        settings.add(newName, settings.get(legacyName));
    }

    private void migrateSettingValue(JsonObject settings, String settingName, String legacyValue, String newValue) {
        if (!settings.has(settingName)) {
            return;
        }
        String currentValue = settings.get(settingName).getAsString();
        if (legacyValue.equals(currentValue)) {
            settings.addProperty(settingName, newValue);
        }
    }

    private static int clamp255(float value) {
        if (value < 0.0f) {
            return 0;
        }
        if (value > 255.0f) {
            return 255;
        }
        return Math.round(value);
    }
}

