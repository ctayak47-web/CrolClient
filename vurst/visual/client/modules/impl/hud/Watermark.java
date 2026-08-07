
package vurst.visual.client.modules.impl.hud;

import com.darkmagician6.eventapi.EventTarget;
import com.google.gson.JsonObject;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import vurst.visual.base.events.impl.input.EventMouse;
import vurst.visual.base.events.impl.player.EventUpdate;
import vurst.visual.base.events.impl.render.EventHudRender;
import vurst.visual.base.font.Font;
import vurst.visual.base.font.Fonts;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.BooleanSetting;
import vurst.visual.client.modules.impl.hud.HudModule;
import vurst.visual.utility.render.display.base.BorderRadius;
import vurst.visual.utility.render.display.base.CustomDrawContext;
import vurst.visual.utility.render.display.base.color.ColorRGBA;
import vurst.visual.utility.render.display.shader.DrawUtil;

@ModuleAnnotation(name="Watermark", category=Category.HUD, description="Shows compact brand watermark.")
public final class Watermark
extends HudModule {
    public static final Watermark INSTANCE = new Watermark();
    private static final String LEGACY_SHOW_TIME_KEY = "Show Time";
    private static final String BRAND_TEXT = "Vurst Visual".replace(" ", "");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final float PILL_HEIGHT = 16.0f;
    private static final float PILL_RADIUS = 6.5f;
    private static final float PILL_GAP = 7.0f;
    private static final float OUTER_PADDING = 8.0f;
    private static final float TOTAL_HEIGHT = 16.0f;
    private static final float MAX_BRAND_WIDTH = 86.0f;
    private static final ColorRGBA TIME_BG = new ColorRGBA(19, 54, 52, 214);
    private static final ColorRGBA TIME_BORDER = new ColorRGBA(74, 223, 196, 120);
    private static final ColorRGBA TIME_TEXT = new ColorRGBA(247, 248, 251, 255);
    private static final ColorRGBA BRAND_BG = new ColorRGBA(9, 10, 13, 226);
    private static final ColorRGBA BRAND_BORDER = new ColorRGBA(255, 255, 255, 34);
    private static final ColorRGBA BRAND_TEXT_COLOR = new ColorRGBA(247, 248, 251, 255);
    private static final ColorRGBA SEPARATOR_COLOR = new ColorRGBA(255, 255, 255, 36);
    private static final ColorRGBA FPS_BG = new ColorRGBA(24, 68, 50, 214);
    private static final ColorRGBA FPS_BORDER = new ColorRGBA(121, 255, 160, 120);
    private static final ColorRGBA FPS_TEXT = new ColorRGBA(247, 248, 251, 255);
    private final BooleanSetting showTime = new BooleanSetting("Show Time", true);
    private final BooleanSetting showFps = new BooleanSetting("Показывать FPS", true);

    private Watermark() {
        super(10.0f, 10.0f, 136.0f, 16.0f, true);
    }

    @Override
    public void load(JsonObject object) {
        this.migrateLegacySettings(object);
        super.load(object);
    }

    @Override
    protected void draw(CustomDrawContext ctx) {
        Font timeFont = Fonts.ROUND_BOLD.getFont(7.0f);
        Font brandFont = Fonts.ROUND_BOLD.getFont(7.2f);
        Font fpsFont = Fonts.ROUND_BOLD.getFont(7.0f);
        String brand = this.trimToWidth(brandFont, BRAND_TEXT, 86.0f);
        String time = LocalTime.now().format(TIME_FORMATTER);
        String fps = mc.getCurrentFps() + " FPS";
        boolean drawTime = this.showTime.isEnabled();
        boolean drawFps = this.showFps.isEnabled();
        float brandWidth = brandFont.width(brand);
        float timeWidth = drawTime ? timeFont.width(time) : 0.0f;
        float fpsWidth = drawFps ? fpsFont.width(fps) : 0.0f;
        float contentWidth = brandWidth;
        if (drawTime) {
            contentWidth += timeWidth + 7.0f;
        }
        if (drawFps) {
            contentWidth += fpsWidth + 7.0f;
        }
        float totalWidth = contentWidth + 16.0f;
        float x = this.getX();
        float y = this.getY();
        this.drawCapsule(ctx, x, y, totalWidth, BRAND_BG, BRAND_BORDER);
        float cursorX = x + (totalWidth - contentWidth) / 2.0f;
        float centerY = y + 8.0f;
        if (drawTime) {
            float textY = centerY - timeFont.height() / 2.0f + 0.2f;
            ctx.drawText(timeFont, time, cursorX, textY, this.resolveTextColor(TIME_TEXT));
            this.drawSeparator(ctx, (cursorX += timeWidth) + 3.5f, y);
            cursorX += 7.0f;
        }
        float brandTextY = centerY - brandFont.height() / 2.0f + 0.15f;
        ctx.drawText(brandFont, brand, cursorX, brandTextY, this.resolveTextColor(BRAND_TEXT_COLOR));
        cursorX += brandWidth;
        if (drawFps) {
            this.drawSeparator(ctx, cursorX + 3.5f, y);
            float textY = y + 8.0f - fpsFont.height() / 2.0f + 0.2f;
            ctx.drawText(fpsFont, fps, cursorX += 7.0f, textY, this.resolveTextColor(FPS_TEXT));
        }
        this.setBounds(totalWidth, 16.0f);
    }

    private void drawCapsule(CustomDrawContext ctx, float x, float y, float width, ColorRGBA fill, ColorRGBA border) {
        BorderRadius radius = BorderRadius.all(6.5f);
        if (!this.transparentBackground.isEnabled()) {
            if (this.getTheme().isBlur()) {
                DrawUtil.drawBlur(ctx.getMatrices(), x, y, width, 16.0f, 8.0f, radius, new ColorRGBA(255, 255, 255, 100));
            }
            ctx.drawRoundedRect(x, y, width, 16.0f, radius, fill);
        }
        ctx.drawRoundedBorder(x, y, width, 16.0f, 0.85f, radius, border);
    }

    private void drawSeparator(CustomDrawContext ctx, float x, float y) {
        float lineHeight = 10.0f;
        float lineY = y + (16.0f - lineHeight) / 2.0f;
        ctx.drawRoundedRect(x, lineY, 0.75f, lineHeight, BorderRadius.all(1.0f), SEPARATOR_COLOR);
    }

    private String trimToWidth(Font font, String text, float maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        String trimmed = text;
        while (trimmed.length() > 3 && font.width(trimmed + "...") > maxWidth) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed.length() > 3 ? trimmed + "..." : trimmed;
    }

    private void migrateLegacySettings(JsonObject object) {
        if (object == null || !object.has("Settings") || !object.get("Settings").isJsonObject()) {
            return;
        }
        JsonObject settings = object.getAsJsonObject("Settings");
        this.migrateSettingKey(settings, LEGACY_SHOW_TIME_KEY, this.showTime.getName());
    }

    private void migrateSettingKey(JsonObject settings, String legacyName, String newName) {
        if (!settings.has(newName) && settings.has(legacyName)) {
            settings.add(newName, settings.get(legacyName).deepCopy());
        }
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (Watermark.mc.player == null || Watermark.mc.world == null) {
            return;
        }
        this.updateHud();
    }

    @EventTarget
    public void onRender(EventHudRender event) {
        if (Watermark.mc.player == null || Watermark.mc.world == null) {
            return;
        }
        this.renderHud(event.getContext());
    }

    @EventTarget
    public void onMouse(EventMouse event) {
        if (Watermark.mc.player == null || Watermark.mc.world == null) {
            return;
        }
        this.handleMouse(event);
    }
}

