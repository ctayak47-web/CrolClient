
package crol.client.modules.impl.hud;

import com.darkmagician6.eventapi.EventTarget;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.StatusEffect;
import net.minecraft.StatusEffectInstance;
import net.minecraft.Identifier;
import net.minecraft.Registries;
import crol.client.CrolClient;
import crol.client.base.events.impl.input.EventMouse;
import crol.client.base.events.impl.player.EventUpdate;
import crol.client.base.events.impl.render.EventHudRender;
import crol.client.base.font.Font;
import crol.client.base.font.Fonts;
import crol.client.modules.api.Category;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.impl.hud.HudModule;
import crol.client.utility.game.player.PlayerIntersectionUtil;
import crol.client.utility.render.display.base.BorderRadius;
import crol.client.utility.render.display.base.CustomDrawContext;
import crol.client.utility.render.display.base.color.ColorRGBA;

@ModuleAnnotation(name="Potions", category=Category.HUD, description="Показывает активные эффекты.")
public final class Potions
extends HudModule {
    public static final Potions INSTANCE = new Potions();
    private static final String ICON_PATH = "hud/potions/";
    private static final Identifier HEADER_ICON = CrolClient.id("hud/hud/potion.png");
    private static final float HEADER_HEIGHT = 11.0f;
    private static final float ROW_GAP = 1.5f;
    private static final float ROW_PADDING = 3.5f;
    private static final float TEXT_SIZE = 6.0f;
    private static final float ICON_SIZE = 7.0f;
    private static final float HEADER_ICON_SIZE = 8.0f;
    private static final float HEADER_ICON_GAP = 4.0f;
    private static final float ARROW_GAP = 0.7f;
    private static final Map<Identifier, Integer> EFFECT_ICON_INDEX = Map.ofEntries(Map.entry(Identifier.of((String)"minecraft", (String)"speed"), 1), Map.entry(Identifier.of((String)"minecraft", (String)"slowness"), 2), Map.entry(Identifier.of((String)"minecraft", (String)"haste"), 3), Map.entry(Identifier.of((String)"minecraft", (String)"mining_fatigue"), 4), Map.entry(Identifier.of((String)"minecraft", (String)"strength"), 5), Map.entry(Identifier.of((String)"minecraft", (String)"instant_health"), 6), Map.entry(Identifier.of((String)"minecraft", (String)"instant_damage"), 7), Map.entry(Identifier.of((String)"minecraft", (String)"jump_boost"), 8), Map.entry(Identifier.of((String)"minecraft", (String)"nausea"), 9), Map.entry(Identifier.of((String)"minecraft", (String)"regeneration"), 10), Map.entry(Identifier.of((String)"minecraft", (String)"resistance"), 11), Map.entry(Identifier.of((String)"minecraft", (String)"fire_resistance"), 12), Map.entry(Identifier.of((String)"minecraft", (String)"water_breathing"), 13), Map.entry(Identifier.of((String)"minecraft", (String)"invisibility"), 14), Map.entry(Identifier.of((String)"minecraft", (String)"blindness"), 15), Map.entry(Identifier.of((String)"minecraft", (String)"night_vision"), 16), Map.entry(Identifier.of((String)"minecraft", (String)"hunger"), 17), Map.entry(Identifier.of((String)"minecraft", (String)"weakness"), 18), Map.entry(Identifier.of((String)"minecraft", (String)"poison"), 19), Map.entry(Identifier.of((String)"minecraft", (String)"wither"), 20), Map.entry(Identifier.of((String)"minecraft", (String)"health_boost"), 21), Map.entry(Identifier.of((String)"minecraft", (String)"absorption"), 22), Map.entry(Identifier.of((String)"minecraft", (String)"saturation"), 23), Map.entry(Identifier.of((String)"minecraft", (String)"glowing"), 24), Map.entry(Identifier.of((String)"minecraft", (String)"levitation"), 25), Map.entry(Identifier.of((String)"minecraft", (String)"luck"), 26), Map.entry(Identifier.of((String)"minecraft", (String)"unluck"), 27), Map.entry(Identifier.of((String)"minecraft", (String)"slow_falling"), 28), Map.entry(Identifier.of((String)"minecraft", (String)"conduit_power"), 29), Map.entry(Identifier.of((String)"minecraft", (String)"dolphins_grace"), 30), Map.entry(Identifier.of((String)"minecraft", (String)"bad_omen"), 31), Map.entry(Identifier.of((String)"minecraft", (String)"hero_of_the_village"), 32), Map.entry(Identifier.of((String)"minecraft", (String)"darkness"), 33));
    private final Map<String, Float> animMap = new HashMap<String, Float>();
    private final Map<Identifier, Boolean> iconCache = new HashMap<Identifier, Boolean>();
    private List<StatusEffectInstance> active = new ArrayList<StatusEffectInstance>();
    private long lastAnimNs = System.nanoTime();

    private Potions() {
        super(210.0f, 10.0f, 90.0f, 22.0f, true);
    }

    @Override
    protected boolean isElementVisible() {
        return !this.active.isEmpty() || PlayerIntersectionUtil.isChat(Potions.mc.currentScreen);
    }

    @Override
    protected void onTick() {
        if (PlayerIntersectionUtil.nullCheck()) {
            this.active = new ArrayList<StatusEffectInstance>();
            this.animMap.clear();
            this.lastAnimNs = System.nanoTime();
            return;
        }
        float deltaSeconds = this.pollDeltaSeconds();
        float appearBlend = 1.0f - (float)Math.pow(0.85f, deltaSeconds * 20.0f);
        float fadeKeep = (float)Math.pow(0.85f, deltaSeconds * 20.0f);
        this.active = new ArrayList<StatusEffectInstance>(Potions.mc.player.getStatusEffects());
        this.active.sort(Comparator.comparingInt(StatusEffectInstance::getDuration).reversed());
        for (StatusEffectInstance effect2 : this.active) {
            String key = effect2.getTranslationKey();
            float current = this.animMap.getOrDefault(key, Float.valueOf(0.0f)).floatValue();
            this.animMap.put(key, Float.valueOf(current + (1.0f - current) * appearBlend));
        }
        ArrayList<String> keys2 = new ArrayList<String>(this.animMap.keySet());
        for (String key : keys2) {
            boolean stillActive = this.active.stream().anyMatch(effect -> effect.getTranslationKey().equals(key));
            if (stillActive) continue;
            float next = this.animMap.getOrDefault(key, Float.valueOf(0.0f)).floatValue() * fadeKeep;
            if (next < 0.05f) {
                this.animMap.remove(key);
                continue;
            }
            this.animMap.put(key, Float.valueOf(next));
        }
    }

    @Override
    protected void draw(CustomDrawContext ctx) {
        Font font = Fonts.MEDIUM.getFont(6.0f);
        float arrowWidth = font.width(">");
        HashMap<String, Float> widthMap = new HashMap<String, Float>();
        float maxRowWidth = 0.0f;
        for (StatusEffectInstance effect : this.active) {
            String key = effect.getTranslationKey();
            String name = this.formatEffectName(effect);
            String duration = this.formatDuration(effect);
            float rowWidth = 12.95f + arrowWidth + 2.45f + font.width(name) + 10.5f + font.width(duration) + 3.5f;
            widthMap.put(key, Float.valueOf(rowWidth));
            maxRowWidth = Math.max(maxRowWidth, rowWidth);
        }
        float titleWidth = font.width("Potions");
        float minHeaderWidth = titleWidth + 7.0f + 4.0f + 8.0f;
        float headerWidth = Math.max(minHeaderWidth, maxRowWidth);
        float x = this.getX();
        float y = this.getY();
        this.drawCard(ctx, x, y, headerWidth, 11.0f, 1.0f, 3.0f);
        ctx.drawText(font, "Potions", x + 3.5f, y + 5.5f - font.height() / 2.0f, this.getTextColor());
        float headerIconX = x + headerWidth - 3.5f - 8.0f;
        float headerIconY = y + 5.5f - 4.0f;
        ctx.drawTexture(HEADER_ICON, headerIconX, headerIconY, 8.0f, 8.0f, ColorRGBA.WHITE);
        float currentY = y + 11.0f + 1.5f;
        for (StatusEffectInstance effect : this.active) {
            String key = effect.getTranslationKey();
            float anim = this.animMap.getOrDefault(key, Float.valueOf(0.0f)).floatValue();
            if (anim <= 0.05f) continue;
            float rowWidth = widthMap.getOrDefault(key, Float.valueOf(headerWidth)).floatValue();
            float rowHeight = 11.0f * anim;
            int alpha = Math.max(0, Math.min(255, (int)(255.0f * anim)));
            this.drawCard(ctx, x, currentY, rowWidth, rowHeight, anim, 3.0f);
            float textY = currentY + rowHeight / 2.0f - font.height() / 2.0f;
            float cursorX = x + 3.5f;
            Identifier icon = this.getEffectIcon(effect);
            if (icon != null && this.hasIcon(icon)) {
                ctx.drawTexture(icon, cursorX, currentY + rowHeight / 2.0f - 3.5f, 7.0f, 7.0f, new ColorRGBA(255, 255, 255, alpha));
            }
            ctx.drawText(font, ">", cursorX += 9.45f, textY, this.resolveTextColor(new ColorRGBA(160, 160, 160, alpha)));
            String name = this.formatEffectName(effect);
            ctx.drawText(font, name, cursorX += arrowWidth + 2.45f, textY, this.resolveTextColor(ColorRGBA.WHITE.withAlpha(alpha)));
            String duration = this.formatDuration(effect);
            float durationWidth = font.width(duration);
            float boxWidth = durationWidth + 4.0f;
            float boxHeight = 8.0f * anim;
            float boxX = x + rowWidth - 3.5f - boxWidth;
            float boxY = currentY + rowHeight / 2.0f - boxHeight / 2.0f;
            if (anim > 0.5f) {
                ctx.drawRoundedRect(boxX, boxY, boxWidth, boxHeight, BorderRadius.all(2.0f), new ColorRGBA(0, 0, 0, (int)(120.0f * anim)));
                ctx.drawRoundedBorder(boxX, boxY, boxWidth, boxHeight, 0.5f, BorderRadius.all(2.0f), new ColorRGBA(100, 100, 100, alpha));
                ctx.drawText(font, duration, boxX + (boxWidth - durationWidth) / 2.0f, textY, this.resolveTextColor(ColorRGBA.WHITE.withAlpha(alpha)));
            }
            currentY += rowHeight + 1.5f;
        }
        this.setBounds(headerWidth, Math.max(11.0f, currentY - y));
    }

    private void drawCard(CustomDrawContext ctx, float x, float y, float width, float height, float alphaMul, float radius) {
        this.drawMediaCard(ctx, x, y, width, height, radius, alphaMul);
    }

    private float pollDeltaSeconds() {
        long now = System.nanoTime();
        float delta = Math.max(0.0f, (float)(now - this.lastAnimNs) / 1.0E9f);
        delta = Math.min(delta, 0.25f);
        this.lastAnimNs = now;
        return delta;
    }

    private String formatEffectName(StatusEffectInstance effect) {
        String name = ((StatusEffect)effect.getEffectType().comp_349()).getName().getString();
        int level = effect.getAmplifier() + 1;
        return level > 1 ? name + " " + level : name;
    }

    private String formatDuration(StatusEffectInstance effect) {
        if (effect.isInfinite()) {
            return "**:**";
        }
        int totalSeconds = Math.max(0, effect.getDuration() / 20);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return minutes + ":" + String.format("%02d", seconds);
    }

    private Identifier getEffectIcon(StatusEffectInstance effect) {
        Identifier effectId = Registries.STATUS_EFFECT.getId((Object)((StatusEffect)effect.getEffectType().comp_349()));
        if (effectId == null) {
            return null;
        }
        Integer index = EFFECT_ICON_INDEX.get(effectId);
        if (index == null) {
            return null;
        }
        return CrolClient.id(ICON_PATH + index + ".png");
    }

    private boolean hasIcon(Identifier icon) {
        if (mc == null || mc.getResourceManager() == null) {
            return false;
        }
        return this.iconCache.computeIfAbsent(icon, id -> mc.getResourceManager().getResource(id).isPresent());
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (Potions.mc.player == null || Potions.mc.world == null) {
            this.active = new ArrayList<StatusEffectInstance>();
            this.animMap.clear();
            this.lastAnimNs = System.nanoTime();
        }
    }

    @EventTarget
    public void onRender(EventHudRender event) {
        if (Potions.mc.player == null || Potions.mc.world == null) {
            return;
        }
        this.updateHud();
        this.renderHud(event.getContext());
    }

    @EventTarget
    public void onMouse(EventMouse event) {
        if (Potions.mc.player == null || Potions.mc.world == null) {
            return;
        }
        this.handleMouse(event);
    }
}

