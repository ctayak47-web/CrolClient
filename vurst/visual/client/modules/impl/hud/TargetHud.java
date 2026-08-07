
package vurst.visual.client.modules.impl.hud;

import com.darkmagician6.eventapi.EventTarget;
import java.util.ArrayDeque;
import java.util.UUID;
import net.minecraft.StatusEffectInstance;
import net.minecraft.StatusEffects;
import net.minecraft.Entity;
import net.minecraft.LivingEntity;
import net.minecraft.ArmorStandEntity;
import net.minecraft.PlayerEntity;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.Items;
import net.minecraft.PotionContentsComponent;
import net.minecraft.HitResult;
import net.minecraft.Vec3d;
import net.minecraft.Identifier;
import net.minecraft.DrawContext;
import net.minecraft.MathHelper;
import net.minecraft.EntityHitResult;
import net.minecraft.ChatScreen;
import net.minecraft.InventoryScreen;
import net.minecraft.RegistryEntry;
import net.minecraft.AbstractClientPlayerEntity;
import net.minecraft.DataComponentTypes;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import vurst.visual.base.animations.base.Animation;
import vurst.visual.base.animations.base.Easing;
import vurst.visual.base.events.impl.input.EventMouse;
import vurst.visual.base.events.impl.player.EventUpdate;
import vurst.visual.base.events.impl.render.EventHudRender;
import vurst.visual.base.font.Font;
import vurst.visual.base.font.Fonts;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.BooleanSetting;
import vurst.visual.client.modules.api.setting.impl.NumberSetting;
import vurst.visual.client.modules.impl.hud.HudModule;
import vurst.visual.utility.game.player.PlayerIntersectionUtil;
import vurst.visual.utility.game.player.RealHpUtility;
import vurst.visual.utility.render.display.base.BorderRadius;
import vurst.visual.utility.render.display.base.CustomDrawContext;
import vurst.visual.utility.render.display.base.Gradient;
import vurst.visual.utility.render.display.base.color.ColorRGBA;
import vurst.visual.utility.render.display.shader.DrawUtil;

@ModuleAnnotation(name="Target Hud", category=Category.HUD, description="Показывает информацию о цели.")
public final class TargetHud
extends HudModule {
    public static final TargetHud INSTANCE = new TargetHud();
    private final Animation scale = new Animation(200L, 0.0f, Easing.QUAD_OUT);
    private final Animation useAnimation = new Animation(200L, 0.0f, Easing.QUAD_OUT);
    private LivingEntity target;
    private long lastTargetAt;
    private float healthWidth;
    private float absorptionWidth;
    private final NumberSetting hideDelay = new NumberSetting("Задержка скрытия", 0.5f, 0.0f, 10.0f, 0.1f);
    private final BooleanSetting showOutcome = new BooleanSetting("Отображать выигрыш", true);
    private final BooleanSetting showAbsorption = new BooleanSetting("Золотые сердца", true);
    private static final ColorRGBA WIN_COLOR = new ColorRGBA(92, 220, 120);
    private static final ColorRGBA LOSE_COLOR = new ColorRGBA(255, 90, 96);
    private static final ColorRGBA DRAW_COLOR = new ColorRGBA(255, 214, 77);
    private static final ColorRGBA ABSORPTION_COLOR = new ColorRGBA(255, 214, 77);
    private static final ColorRGBA HP_FILL_COLOR = new ColorRGBA(60, 255, 50, 255);
    private static final ColorRGBA HP_DANGER_COLOR = new ColorRGBA(255, 70, 70, 235);
    private static final float HEAD_HEIGHT_RATIO = 0.3f;
    private static final float HEAD_CENTER_RATIO = 0.85f;
    private static final float LOW_HP_THRESHOLD = 0.4f;
    private static final float LOW_HP_OVERLAY_MAX = 0.6f;
    private static final float INFO_GAP = 4.0f;
    private static final float HP_ROW_SIDE_PADDING = 3.0f;
    private static final float HP_ROW_TEXT_GAP = 4.0f;
    private static final float HP_BAR_HEIGHT = 4.0f;
    private static final float HP_BAR_Y_OFFSET = 2.0f;
    private static final float HUD_VIEW_YAW_LIMIT = 95.0f;
    private static final float HUD_VIEW_PITCH_LIMIT = 70.0f;
    private static final float INVIS_ARMOR_UNKNOWN_HP_THRESHOLD = 50.0f;
    private static final String INVIS_ARMOR_UNKNOWN_HP_TEXT = "HP НЕИЗВЕСТНО";
    private static final float CARD_BG_PADDING = 2.0f;
    private static final float CARD_BG_RADIUS = 7.0f;
    private static final int TARGET_CONSUME_MIN_USE_TICKS = 8;
    private static final long TARGET_ABSORPTION_GRACE_MS = 5000L;
    private static final long HIT_RATE_WINDOW_MS = 8000L;
    private static final long COMBO_RESET_MS = 1400L;
    private static final long SPLASH_DETECT_COOLDOWN_MS = 350L;
    private static final long OUTCOME_CACHE_MS = 80L;
    private static final double OUTCOME_THRESHOLD = 0.9;
    private static final double LOW_HP_FORCE_LOSE = 6.0;
    private static final double GOLDEN_APPLE_HEAL = 8.0;
    private static final double ENCHANTED_GOLDEN_APPLE_HEAL = 16.0;
    private static final double GOLDEN_CARROT_HEAL = 3.0;
    private LivingEntity trackedCombatTarget;
    private final ArrayDeque<Long> myHitTimeline = new ArrayDeque();
    private final ArrayDeque<Long> enemyHitTimeline = new ArrayDeque();
    private int prevPlayerHurtTime;
    private int prevTargetHurtTime;
    private int myTotalHits;
    private int enemyTotalHits;
    private int myCritHits;
    private int enemyCritHits;
    private int myComboStreak;
    private int enemyComboStreak;
    private int myBestComboStreak;
    private int enemyBestComboStreak;
    private int mySplashHits;
    private int enemySplashHits;
    private long lastMyHitAt;
    private long lastEnemyHitAt;
    private long lastMySplashAt;
    private long lastEnemySplashAt;
    private double prevTargetNegativePressure;
    private double prevSelfNegativePressure;
    private Outcome cachedOutcome = Outcome.DRAW;
    private LivingEntity cachedOutcomeTarget;
    private long lastOutcomeCalcAt;
    private final TargetConsumableState targetConsumableState = new TargetConsumableState();

    private TargetHud() {
        super(10.0f, 40.0f, 110.0f, 38.0f, true);
    }

    @Override
    public void onEnable() {
        this.target = null;
        this.lastTargetAt = 0L;
        this.healthWidth = 0.0f;
        this.absorptionWidth = 0.0f;
        this.scale.setValue(0.0f);
        this.useAnimation.setValue(0.0f);
        this.resetCombatTracking(null);
        this.resetTargetConsumableState(null);
        this.invalidateOutcomeCache();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.target = null;
        this.lastTargetAt = 0L;
        this.healthWidth = 0.0f;
        this.absorptionWidth = 0.0f;
        this.scale.setValue(0.0f);
        this.useAnimation.setValue(0.0f);
        this.resetCombatTracking(null);
        this.resetTargetConsumableState(null);
        this.invalidateOutcomeCache();
        super.onDisable();
    }

    @Override
    protected boolean isElementVisible() {
        return this.scale.getValue() > 0.01f;
    }

    @Override
    protected void onTick() {
        if (PlayerIntersectionUtil.nullCheck()) {
            this.target = null;
            this.resetCombatTracking(null);
            this.resetTargetConsumableState(null);
            this.invalidateOutcomeCache();
            this.scale.setValue(0.0f);
            return;
        }
        LivingEntity previousTarget = this.target;
        LivingEntity candidate = this.getCrosshairTarget();
        long now = System.currentTimeMillis();
        if (candidate != null) {
            this.target = candidate;
            this.lastTargetAt = now;
        } else if (TargetHud.mc.currentScreen instanceof ChatScreen && !this.isInvisibleTarget((LivingEntity)TargetHud.mc.player)) {
            this.target = TargetHud.mc.player;
            this.lastTargetAt = now;
        }
        if (this.target != null && this.isInvisibleTarget(this.target)) {
            this.target = null;
        }
        if (previousTarget != this.target) {
            this.healthWidth = 0.0f;
            this.absorptionWidth = 0.0f;
            this.resetTargetConsumableState(this.target);
            this.invalidateOutcomeCache();
        }
        this.updateTargetConsumableState(this.target);
        long hideDelayMs = this.getHideDelayMs();
        boolean withinDelay = hideDelayMs > 0L && now - this.lastTargetAt < hideDelayMs;
        boolean shouldShow = this.target != null && (candidate != null || TargetHud.mc.currentScreen instanceof ChatScreen || withinDelay && (this.target == TargetHud.mc.player || this.isTargetInView(this.target)));
        this.scale.animateTo(shouldShow ? 1.0f : 0.0f);
        this.scale.update();
        if (!shouldShow && this.scale.getValue() <= 0.01f) {
            this.target = null;
            this.resetTargetConsumableState(null);
            this.invalidateOutcomeCache();
        }
        if (this.target != null) {
            this.useAnimation.animateTo(this.target.isUsingItem() ? 1.0f : 0.0f);
        } else {
            this.useAnimation.animateTo(0.0f);
        }
        this.useAnimation.update();
    }

    @Override
    protected void draw(CustomDrawContext ctx) {
        if (this.target == null) {
            return;
        }
        float x = this.getX();
        float y = this.getY();
        float headSize = 24.0f;
        Font rowFont = Fonts.MEDIUM.getFont(6.0f);
        String nameText = this.target.getName().getString();
        boolean invisibleArmoredTarget = this.isInvisibleArmoredTarget(this.target);
        HealthDisplayState healthState = this.getHealthDisplayState(this.target);
        float displayHealth = healthState.displayHealth();
        boolean maskHp = invisibleArmoredTarget && displayHealth > 50.0f;
        String hpText = maskHp ? INVIS_ARMOR_UNKNOWN_HP_TEXT : this.formatHpText(displayHealth);
        float nameRowWidth = 8.0f + rowFont.width(nameText);
        float hpBarWidth = headSize * 2.0f - 6.0f;
        float hpRowWidth = 3.0f + hpBarWidth + 4.0f + rowFont.width(hpText) + 3.0f;
        float rowsWidth = Math.max(nameRowWidth, hpRowWidth);
        float width = headSize + 4.0f + rowsWidth + 2.0f;
        float height = headSize;
        this.setBounds(width, height);
        float scaleValue = this.scale.getValue();
        if (scaleValue <= 0.03f) {
            return;
        }
        ctx.getMatrices().push();
        float centerX = x + width / 2.0f;
        float centerY = y + height / 2.0f;
        ctx.getMatrices().translate(centerX, centerY, 0.0f);
        ctx.getMatrices().scale(scaleValue, scaleValue, 1.0f);
        ctx.getMatrices().translate(-centerX, -centerY, 0.0f);
        this.drawMainBackground(ctx, x, y, width, height);
        this.drawRoundedBack(ctx, x, y, headSize, headSize, 10.0f);
        this.drawFace(ctx, x + 2.0f, y + 2.0f, headSize - 4.0f, scaleValue, centerX, centerY);
        if (this.showOutcome.isEnabled() && this.target != TargetHud.mc.player && !invisibleArmoredTarget) {
            this.drawOutcomeLabel(ctx, x + 2.0f, y + 2.0f, headSize - 4.0f);
        }
        this.drawNameRow(ctx, rowFont, nameText, x, y, headSize, 4.0f, nameRowWidth);
        this.drawHpRow(ctx, rowFont, hpText, x, y, headSize, 4.0f, hpRowWidth, hpBarWidth, this.target, maskHp);
        ctx.getMatrices().pop();
    }

    private void drawRoundedBack(CustomDrawContext ctx, float x, float y, float width, float height, float radius) {
        this.drawMediaCard(ctx, x, y, width, height, radius, 0.85f);
    }

    private void drawMainBackground(CustomDrawContext ctx, float x, float y, float width, float height) {
        float bgX = x - 2.0f;
        float bgY = y - 2.0f;
        float bgW = width + 4.0f;
        float bgH = height + 4.0f;
        BorderRadius radius = BorderRadius.all(7.0f);
        this.drawMediaCard(ctx, bgX, bgY, bgW, bgH, 7.0f, 1.0f);
        ColorRGBA accent = this.getAccentColor();
        Gradient glowGradient = Gradient.of(new ColorRGBA(accent.getRed(), accent.getGreen(), accent.getBlue(), 36), new ColorRGBA(0, 0, 0, 20), new ColorRGBA(accent.getRed(), accent.getGreen(), accent.getBlue(), 14), new ColorRGBA(0, 0, 0, 28));
        ctx.drawRoundedRect(bgX + 0.8f, bgY + 0.8f, bgW - 1.6f, bgH - 1.6f, BorderRadius.all(6.0f), glowGradient);
        ctx.drawRoundedBorder(bgX, bgY, bgW, bgH, 0.8f, radius, new ColorRGBA(255, 255, 255, 38));
        ctx.drawRoundedBorder(bgX + 0.9f, bgY + 0.9f, bgW - 1.8f, bgH - 1.8f, 0.8f, BorderRadius.all(6.0f), new ColorRGBA(accent.getRed(), accent.getGreen(), accent.getBlue(), 52));
    }

    private void drawNameRow(CustomDrawContext ctx, Font font, String text, float x, float y, float headSize, float infoGap, float rowW) {
        float rowX = x + headSize + infoGap;
        float rowY = y + 4.0f;
        this.drawRoundedBack(ctx, rowX, rowY, rowW, 8.0f, 2.0f);
        ctx.drawText(font, text, rowX + 4.0f, rowY + (8.0f - font.height()) / 2.0f, this.getTextColor());
    }

    private void drawHpRow(CustomDrawContext ctx, Font font, String hpText, float x, float y, float headSize, float infoGap, float rowW, float barW, LivingEntity entity, boolean maskHp) {
        float rowX = x + headSize + infoGap;
        float rowY = y + 16.0f;
        float barX = rowX + 3.0f;
        float barY = rowY + 2.0f;
        this.drawRoundedBack(ctx, rowX, rowY, rowW, 8.0f, 2.0f);
        ctx.drawRoundedRect(barX, barY, barW, 4.0f, BorderRadius.all(1.0f), new ColorRGBA(14, 14, 14, 120));
        if (maskHp) {
            this.healthWidth = barW;
            this.absorptionWidth = 0.0f;
            ctx.drawRoundedRect(barX, barY, barW, 4.0f, BorderRadius.all(1.0f), HP_FILL_COLOR);
            float hpTextX = barX + barW + 4.0f;
            float hpTextY = rowY + (8.0f - font.height()) / 2.0f;
            ctx.drawText(font, hpText, hpTextX, hpTextY, this.getTextColor());
            return;
        }
        HealthDisplayState healthState = this.getHealthDisplayState(entity);
        float health = healthState.health();
        float absorption = healthState.absorption();
        float maxHp = healthState.maxHealth();
        float hpPercent = MathHelper.clamp((float)(health / maxHp), (float)0.0f, (float)1.0f);
        if (this.showAbsorption.isEnabled() && absorption > 0.0f) {
            float total = Math.max(1.0f, maxHp + absorption);
            float targetHealthWidth = barW * MathHelper.clamp((float)(health / total), (float)0.0f, (float)1.0f);
            float targetAbsorptionWidth = barW * MathHelper.clamp((float)(absorption / total), (float)0.0f, (float)1.0f);
            this.healthWidth += (targetHealthWidth - this.healthWidth) * 0.2f;
            this.absorptionWidth += (targetAbsorptionWidth - this.absorptionWidth) * 0.2f;
        } else {
            float targetHealthWidth = barW * hpPercent;
            this.healthWidth += (targetHealthWidth - this.healthWidth) * 0.2f;
            this.absorptionWidth += (0.0f - this.absorptionWidth) * 0.2f;
        }
        if (this.healthWidth > 0.5f) {
            ctx.drawRoundedRect(barX, barY, this.healthWidth, 4.0f, BorderRadius.all(1.0f), HP_FILL_COLOR);
            float danger = MathHelper.clamp((float)((0.4f - hpPercent) / 0.4f), (float)0.0f, (float)1.0f);
            if (danger > 0.0f) {
                float overlayWidth = this.healthWidth * (0.18f + 0.6f * danger);
                overlayWidth = Math.min(overlayWidth, this.healthWidth);
                int pulseAlpha = (int)(150.0f + 70.0f * ((float)Math.sin((double)System.currentTimeMillis() / 120.0) * 0.5f + 0.5f));
                ctx.drawRoundedRect(barX, barY, overlayWidth, 4.0f, BorderRadius.all(1.0f), new ColorRGBA(HP_DANGER_COLOR.getRed(), HP_DANGER_COLOR.getGreen(), HP_DANGER_COLOR.getBlue(), pulseAlpha));
            }
        }
        if (this.showAbsorption.isEnabled() && this.absorptionWidth > 0.5f) {
            float segmentX = barX + Math.max(0.0f, this.healthWidth);
            float cappedAbsorptionWidth = Math.max(0.0f, Math.min(this.absorptionWidth, barW - Math.max(0.0f, this.healthWidth)));
            if (cappedAbsorptionWidth > 0.1f) {
                ctx.drawRoundedRect(segmentX, barY, cappedAbsorptionWidth, 4.0f, BorderRadius.all(1.0f), ABSORPTION_COLOR);
            }
        }
        float hpTextX = barX + barW + 4.0f;
        float hpTextY = rowY + (8.0f - font.height()) / 2.0f;
        ctx.drawText(font, hpText, hpTextX, hpTextY, this.getTextColor());
    }

    private void drawOutcomeLabel(CustomDrawContext ctx, float headX, float headY, float headSize) {
        Outcome outcome = this.getOutcome();
        String label = switch (outcome.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> "WIN";
            case 1 -> "LOSE";
            case 2 -> "DRAW";
        };
        ColorRGBA labelColor = switch (outcome.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> WIN_COLOR;
            case 1 -> LOSE_COLOR;
            case 2 -> DRAW_COLOR;
        };
        Font font = Fonts.MEDIUM.getFont(5.8f);
        float paddingX = 4.0f;
        float paddingY = 1.6f;
        float labelWidth = font.width(label) + paddingX * 2.0f;
        float labelHeight = font.height() + paddingY * 2.0f;
        float maxAllowedWidth = Math.max(16.0f, headSize + 10.0f);
        labelWidth = Math.min(labelWidth, maxAllowedWidth);
        float labelX = headX + (headSize - labelWidth) / 2.0f;
        float labelY = headY - labelHeight - 8.0f;
        BorderRadius radius = BorderRadius.all(2.6f);
        ctx.drawRoundedRect(labelX, labelY, labelWidth, labelHeight, radius, new ColorRGBA(0, 0, 0, 120));
        String renderText = this.trimToWidth(font, label, Math.max(8.0f, labelWidth - paddingX * 2.0f));
        ctx.drawText(font, renderText, labelX + paddingX, labelY + paddingY, this.resolveTextColor(labelColor));
    }

    private String trimToWidth(Font font, String text, float maxWidth) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (font.width(text) <= maxWidth) {
            return text;
        }
        String trimmed = text;
        while (trimmed.length() > 2 && font.width(trimmed + "...") > maxWidth) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed.length() > 2 ? trimmed + "..." : trimmed;
    }

    private Outcome getOutcome() {
        if (TargetHud.mc.player == null || this.target == null || this.target == TargetHud.mc.player) {
            this.invalidateOutcomeCache();
            return Outcome.DRAW;
        }
        long now = System.currentTimeMillis();
        if (this.cachedOutcomeTarget != this.target || now - this.lastOutcomeCalcAt >= 80L) {
            this.cachedOutcome = this.calculateOutcome();
            this.cachedOutcomeTarget = this.target;
            this.lastOutcomeCalcAt = now;
        }
        return this.cachedOutcome;
    }

    private Outcome calculateOutcome() {
        double splashAdvantage;
        double pressureAdvantage;
        if (TargetHud.mc.player == null || this.target == null || this.target == TargetHud.mc.player) {
            return Outcome.DRAW;
        }
        CombatSnapshot self = this.buildCombatSnapshot((LivingEntity)TargetHud.mc.player, true);
        CombatSnapshot enemy = this.buildCombatSnapshot(this.target, false);
        double selfTtk = enemy.effectiveHealth() / Math.max(0.15, self.dps());
        double enemyTtk = self.effectiveHealth() / Math.max(0.15, enemy.dps());
        if (self.effectiveHealth() <= 6.0 && enemy.effectiveHealth() > self.effectiveHealth() * 1.9 && enemyTtk <= selfTtk * 1.25) {
            return Outcome.LOSE;
        }
        double ttkAdvantage = enemyTtk - selfTtk;
        double statAdvantage = (self.statScore() - enemy.statScore()) * 0.1;
        double finalScore = ttkAdvantage + statAdvantage + (pressureAdvantage = (self.pressureScore() - enemy.pressureScore()) * 0.12) + (splashAdvantage = (double)MathHelper.clamp((int)(this.mySplashHits - this.enemySplashHits), (int)-3, (int)3) * 0.06);
        if (finalScore > 0.9) {
            return Outcome.WIN;
        }
        if (finalScore < -0.9) {
            return Outcome.LOSE;
        }
        return Outcome.DRAW;
    }

    private CombatSnapshot buildCombatSnapshot(LivingEntity entity, boolean selfSide) {
        double consumableHeal = this.getConsumableHealPotential(entity, selfSide);
        double effectiveHealth = this.getEffectiveHealthPool(entity) + consumableHeal * 0.7;
        double weaponDamage = this.getWeaponDamage(entity.getMainHandStack());
        double armorMaterialScore = this.getArmorMaterialScore(entity);
        double beneficialPotions = this.getBeneficialPotionScore(entity);
        double negativePotions = this.getNegativePotionPressure(entity);
        double critRate = this.getCurrentCritRate(selfSide);
        double comboScore = this.getCurrentComboScore(selfSide);
        double splashScore = this.getSplashPressure(selfSide);
        double pressureScore = critRate * 2.0 + comboScore * 1.7 + splashScore * 1.2;
        double statScore = effectiveHealth * 0.8 + weaponDamage * 1.7 + (double)entity.getArmor() * 0.75 + armorMaterialScore * 1.4 + beneficialPotions * 1.1 - negativePotions * 0.9 + consumableHeal * 0.6;
        double dps = this.estimateDps(entity, selfSide);
        return new CombatSnapshot(effectiveHealth, statScore, pressureScore, dps);
    }

    private double estimateDps(LivingEntity attacker, boolean selfSide) {
        if (TargetHud.mc.player == null || this.target == null) {
            return 0.0;
        }
        LivingEntity defender = selfSide ? this.target : TargetHud.mc.player;
        double weaponDamage = Math.max(1.0, this.getWeaponDamage(attacker.getMainHandStack()));
        double strength = this.getEffectContribution(attacker, StatusEffects.STRENGTH);
        double weakness = this.getEffectContribution(attacker, StatusEffects.WEAKNESS);
        double speed = this.getEffectContribution(attacker, StatusEffects.SPEED);
        double slowness = this.getEffectContribution(attacker, StatusEffects.SLOWNESS);
        double critRate = this.getCurrentCritRate(selfSide);
        double comboScore = this.getCurrentComboScore(selfSide);
        double splashScore = this.getSplashPressure(selfSide);
        double damageModifier = 1.0 + strength * 0.22 - weakness * 0.28;
        damageModifier = MathHelper.clamp((double)damageModifier, (double)0.25, (double)2.0);
        double tempo = 1.05 + speed * 0.08 - slowness * 0.08;
        tempo = MathHelper.clamp((double)tempo, (double)0.35, (double)3.0);
        double critModifier = 1.0 + Math.min(0.24, critRate * 0.3);
        double comboModifier = 1.0 + Math.min(0.3, comboScore * 0.06);
        double splashModifier = 1.0 + Math.min(0.16, splashScore * 0.05);
        return weaponDamage * damageModifier * tempo * critModifier * comboModifier * splashModifier * this.getIncomingDamageMultiplier(defender);
    }

    private double getIncomingDamageMultiplier(LivingEntity defender) {
        double armorReduction = MathHelper.clamp((double)((double)defender.getArmor() / 35.0), (double)0.0, (double)0.72);
        double materialReduction = MathHelper.clamp((double)(this.getArmorMaterialScore(defender) * 0.03), (double)0.0, (double)0.2);
        double resistanceReduction = MathHelper.clamp((double)(this.getEffectContribution(defender, StatusEffects.RESISTANCE) * 0.2), (double)0.0, (double)0.5);
        double totalReduction = MathHelper.clamp((double)(armorReduction + materialReduction + resistanceReduction), (double)0.0, (double)0.85);
        return 1.0 - totalReduction;
    }

    private double getEffectiveHealthPool(LivingEntity entity) {
        HealthDisplayState healthState = this.getHealthDisplayState(entity);
        double health = Math.max(0.0f, healthState.displayHealth());
        double regeneration = this.getEffectContribution(entity, StatusEffects.REGENERATION) * 2.2;
        double healthBoost = this.getEffectContribution(entity, StatusEffects.HEALTH_BOOST) * 2.0;
        double poisonPenalty = this.getEffectContribution(entity, StatusEffects.POISON) * 1.6;
        double witherPenalty = this.getEffectContribution(entity, StatusEffects.WITHER) * 2.1;
        double weaknessPenalty = this.getEffectContribution(entity, StatusEffects.WEAKNESS) * 0.9;
        return Math.max(1.0, health + regeneration + healthBoost - poisonPenalty - witherPenalty - weaknessPenalty);
    }

    private double getConsumableHealPotential(LivingEntity entity, boolean selfSide) {
        if (!(entity instanceof PlayerEntity)) {
            return 0.0;
        }
        PlayerEntity player = (PlayerEntity)entity;
        double bestHeal = 0.0;
        bestHeal = Math.max(bestHeal, this.getConsumableHealValue(player.getMainHandStack(), player));
        bestHeal = Math.max(bestHeal, this.getConsumableHealValue(player.getOffHandStack(), player));
        ItemStack activeItem = player.getActiveItem();
        if (activeItem != null && !activeItem.isEmpty()) {
            bestHeal = Math.max(bestHeal, this.getConsumableHealValue(activeItem, player) * 1.15);
        }
        if (selfSide && player == TargetHud.mc.player) {
            for (int slot = 0; slot < 9; ++slot) {
                bestHeal = Math.max(bestHeal, this.getConsumableHealValue(player.getInventory().getStack(slot), player));
            }
        }
        return bestHeal;
    }

    private double getConsumableHealValue(ItemStack stack, PlayerEntity player) {
        if (stack == null || stack.isEmpty()) {
            return 0.0;
        }
        Item item = stack.getItem();
        if (item == Items.ENCHANTED_GOLDEN_APPLE) {
            return 16.0;
        }
        if (item == Items.GOLDEN_APPLE) {
            return 8.0;
        }
        if (item == Items.GOLDEN_CARROT) {
            return this.getGoldenCarrotHealPotential(player);
        }
        if (this.isHealingPotion(stack)) {
            return this.getHealingAmount(stack);
        }
        return 0.0;
    }

    private double getGoldenCarrotHealPotential(PlayerEntity player) {
        if (player == null) {
            return 3.0;
        }
        int food = player.getHungerManager().getFoodLevel();
        if (food <= 12) {
            return 5.0;
        }
        if (food < 20) {
            return 4.0;
        }
        return 1.5;
    }

    private double getWeaponDamage(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 1.0;
        }
        Item item = stack.getItem();
        if (item == Items.NETHERITE_SWORD) {
            return 8.0;
        }
        if (item == Items.DIAMOND_SWORD) {
            return 7.0;
        }
        if (item == Items.IRON_SWORD) {
            return 6.0;
        }
        if (item == Items.STONE_SWORD) {
            return 5.0;
        }
        if (item == Items.GOLDEN_SWORD || item == Items.WOODEN_SWORD) {
            return 4.0;
        }
        if (item == Items.NETHERITE_AXE) {
            return 10.0;
        }
        if (item == Items.DIAMOND_AXE) {
            return 9.0;
        }
        if (item == Items.IRON_AXE || item == Items.STONE_AXE) {
            return 9.0;
        }
        if (item == Items.GOLDEN_AXE || item == Items.WOODEN_AXE) {
            return 7.0;
        }
        if (item == Items.TRIDENT) {
            return 8.0;
        }
        return 1.0;
    }

    private double getArmorMaterialScore(LivingEntity entity) {
        double score = 0.0;
        for (ItemStack stack : entity.getArmorItems()) {
            score += this.getArmorPieceScore(stack);
        }
        return score;
    }

    private double getArmorPieceScore(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0.0;
        }
        Item item = stack.getItem();
        if (this.isNetheriteArmor(item)) {
            return 2.0;
        }
        if (this.isDiamondArmor(item)) {
            return 1.6;
        }
        if (this.isIronArmor(item)) {
            return 1.2;
        }
        if (this.isChainArmor(item)) {
            return 1.0;
        }
        if (this.isGoldArmor(item)) {
            return 0.9;
        }
        if (this.isLeatherArmor(item)) {
            return 0.6;
        }
        return 0.0;
    }

    private double getBeneficialPotionScore(LivingEntity entity) {
        double score = 0.0;
        for (StatusEffectInstance effect : entity.getStatusEffects()) {
            double weight = this.getEffectDurationWeight(effect);
            if (weight <= 0.0) continue;
            RegistryEntry type = effect.getEffectType();
            double levelWeight = (double)(effect.getAmplifier() + 1) * weight;
            if (type == StatusEffects.STRENGTH) {
                score += levelWeight * 2.2;
                continue;
            }
            if (type == StatusEffects.RESISTANCE) {
                score += levelWeight * 2.0;
                continue;
            }
            if (type == StatusEffects.REGENERATION) {
                score += levelWeight * 1.6;
                continue;
            }
            if (type == StatusEffects.SPEED) {
                score += levelWeight * 0.9;
                continue;
            }
            if (type == StatusEffects.HEALTH_BOOST) {
                score += levelWeight * 1.2;
                continue;
            }
            if (type == StatusEffects.ABSORPTION) {
                score += levelWeight * 1.0;
                continue;
            }
            if (type == StatusEffects.FIRE_RESISTANCE) {
                score += levelWeight * 0.35;
                continue;
            }
            if (type == StatusEffects.JUMP_BOOST) {
                score += levelWeight * 0.25;
                continue;
            }
            if (type != StatusEffects.HASTE) continue;
            score += levelWeight * 0.25;
        }
        return score;
    }

    private double getNegativePotionPressure(LivingEntity entity) {
        double pressure = 0.0;
        for (StatusEffectInstance effect : entity.getStatusEffects()) {
            double weight = this.getEffectDurationWeight(effect);
            if (weight <= 0.0) continue;
            RegistryEntry type = effect.getEffectType();
            double levelWeight = (double)(effect.getAmplifier() + 1) * weight;
            if (type == StatusEffects.WEAKNESS) {
                pressure += levelWeight * 2.2;
                continue;
            }
            if (type == StatusEffects.SLOWNESS) {
                pressure += levelWeight * 1.0;
                continue;
            }
            if (type == StatusEffects.POISON) {
                pressure += levelWeight * 1.5;
                continue;
            }
            if (type == StatusEffects.WITHER) {
                pressure += levelWeight * 1.9;
                continue;
            }
            if (type == StatusEffects.BLINDNESS) {
                pressure += levelWeight * 0.7;
                continue;
            }
            if (type == StatusEffects.MINING_FATIGUE) {
                pressure += levelWeight * 0.6;
                continue;
            }
            if (type == StatusEffects.HUNGER) {
                pressure += levelWeight * 0.5;
                continue;
            }
            if (type == StatusEffects.NAUSEA) {
                pressure += levelWeight * 0.4;
                continue;
            }
            if (type != StatusEffects.LEVITATION) continue;
            pressure += levelWeight * 1.2;
        }
        return pressure;
    }

    private double getEffectContribution(LivingEntity entity, Object effectType) {
        for (StatusEffectInstance effect : entity.getStatusEffects()) {
            if (effect.getEffectType() != effectType) continue;
            return (double)(effect.getAmplifier() + 1) * this.getEffectDurationWeight(effect);
        }
        return 0.0;
    }

    private double getEffectDurationWeight(StatusEffectInstance effect) {
        int duration = Math.max(0, effect.getDuration());
        if (duration <= 0) {
            return 0.0;
        }
        double normalized = MathHelper.clamp((double)((double)duration / 200.0), (double)0.0, (double)1.0);
        return Math.max(0.15, normalized);
    }

    private double getCurrentHitRate(boolean selfSide) {
        long now = System.currentTimeMillis();
        ArrayDeque<Long> timeline = selfSide ? this.myHitTimeline : this.enemyHitTimeline;
        this.pruneOldHits(timeline, now);
        if (timeline.isEmpty()) {
            return 0.0;
        }
        return (double)timeline.size() / 8.0;
    }

    private double getCurrentCritRate(boolean selfSide) {
        int hits;
        int n = hits = selfSide ? this.myTotalHits : this.enemyTotalHits;
        if (hits <= 0) {
            return 0.0;
        }
        int crits = selfSide ? this.myCritHits : this.enemyCritHits;
        return MathHelper.clamp((double)((double)crits / (double)hits), (double)0.0, (double)1.0);
    }

    private double getCurrentComboScore(boolean selfSide) {
        return selfSide ? (double)this.myComboStreak : (double)this.enemyComboStreak;
    }

    private double getSplashPressure(boolean selfSide) {
        return Math.min(selfSide ? this.mySplashHits : this.enemySplashHits, 3);
    }

    private void updateCombatTracking(LivingEntity combatTarget) {
        if (TargetHud.mc.player == null || combatTarget == null || !combatTarget.isAlive()) {
            this.resetCombatTracking(null);
            return;
        }
        if (this.trackedCombatTarget != combatTarget) {
            this.resetCombatTracking(combatTarget);
        }
        long now = System.currentTimeMillis();
        this.pruneOldHits(this.myHitTimeline, now);
        this.pruneOldHits(this.enemyHitTimeline, now);
        int currentTargetHurtTime = combatTarget.hurtTime;
        if (currentTargetHurtTime > this.prevTargetHurtTime) {
            this.registerMyHit(now);
        }
        this.prevTargetHurtTime = currentTargetHurtTime;
        int currentPlayerHurtTime = TargetHud.mc.player.hurtTime;
        if (currentPlayerHurtTime > this.prevPlayerHurtTime) {
            this.registerEnemyHit(now);
        }
        this.prevPlayerHurtTime = currentPlayerHurtTime;
        this.detectSplashInfluence(combatTarget, now);
    }

    private void registerMyHit(long now) {
        ++this.myTotalHits;
        this.myHitTimeline.addLast(now);
        if (this.isLikelyCriticalHit((LivingEntity)TargetHud.mc.player)) {
            ++this.myCritHits;
        }
        this.myComboStreak = now - this.lastMyHitAt <= 1400L && now - this.lastEnemyHitAt > 500L ? ++this.myComboStreak : 1;
        this.myBestComboStreak = Math.max(this.myBestComboStreak, this.myComboStreak);
        this.enemyComboStreak = 0;
        this.lastMyHitAt = now;
    }

    private void registerEnemyHit(long now) {
        ++this.enemyTotalHits;
        this.enemyHitTimeline.addLast(now);
        if (this.trackedCombatTarget != null && this.isLikelyCriticalHit(this.trackedCombatTarget)) {
            ++this.enemyCritHits;
        }
        this.enemyComboStreak = now - this.lastEnemyHitAt <= 1400L && now - this.lastMyHitAt > 500L ? ++this.enemyComboStreak : 1;
        this.enemyBestComboStreak = Math.max(this.enemyBestComboStreak, this.enemyComboStreak);
        this.myComboStreak = 0;
        this.lastEnemyHitAt = now;
    }

    private void detectSplashInfluence(LivingEntity combatTarget, long now) {
        double targetNegative = this.getNegativePotionPressure(combatTarget);
        if (targetNegative > this.prevTargetNegativePressure + 0.35 && now - this.lastMySplashAt > 350L) {
            ++this.mySplashHits;
            this.lastMySplashAt = now;
        }
        this.prevTargetNegativePressure = targetNegative;
        double selfNegative = this.getNegativePotionPressure((LivingEntity)TargetHud.mc.player);
        if (selfNegative > this.prevSelfNegativePressure + 0.35 && now - this.lastEnemySplashAt > 350L) {
            ++this.enemySplashHits;
            this.lastEnemySplashAt = now;
        }
        this.prevSelfNegativePressure = selfNegative;
    }

    private void resetCombatTracking(LivingEntity newTarget) {
        this.trackedCombatTarget = newTarget;
        this.myHitTimeline.clear();
        this.enemyHitTimeline.clear();
        this.prevPlayerHurtTime = TargetHud.mc.player != null ? TargetHud.mc.player.hurtTime : 0;
        this.prevTargetHurtTime = newTarget != null ? newTarget.hurtTime : 0;
        this.myTotalHits = 0;
        this.enemyTotalHits = 0;
        this.myCritHits = 0;
        this.enemyCritHits = 0;
        this.myComboStreak = 0;
        this.enemyComboStreak = 0;
        this.myBestComboStreak = 0;
        this.enemyBestComboStreak = 0;
        this.mySplashHits = 0;
        this.enemySplashHits = 0;
        this.lastMyHitAt = 0L;
        this.lastEnemyHitAt = 0L;
        this.lastMySplashAt = 0L;
        this.lastEnemySplashAt = 0L;
        this.prevTargetNegativePressure = newTarget != null ? this.getNegativePotionPressure(newTarget) : 0.0;
        this.prevSelfNegativePressure = TargetHud.mc.player != null ? this.getNegativePotionPressure((LivingEntity)TargetHud.mc.player) : 0.0;
    }

    private void invalidateOutcomeCache() {
        this.cachedOutcome = Outcome.DRAW;
        this.cachedOutcomeTarget = null;
        this.lastOutcomeCalcAt = 0L;
    }

    private void pruneOldHits(ArrayDeque<Long> timeline, long now) {
        while (!timeline.isEmpty() && now - timeline.peekFirst() > 8000L) {
            timeline.pollFirst();
        }
    }

    private boolean isLikelyCriticalHit(LivingEntity attacker) {
        if (!(attacker instanceof PlayerEntity)) {
            return false;
        }
        PlayerEntity player = (PlayerEntity)attacker;
        return player.fallDistance > 0.0f && !player.isOnGround() && !player.isClimbing() && !player.isTouchingWater() && !player.isSubmergedInWater() && !player.hasStatusEffect(StatusEffects.BLINDNESS) && !player.hasVehicle();
    }

    private boolean isNetheriteArmor(Item item) {
        return item == Items.NETHERITE_HELMET || item == Items.NETHERITE_CHESTPLATE || item == Items.NETHERITE_LEGGINGS || item == Items.NETHERITE_BOOTS;
    }

    private boolean isDiamondArmor(Item item) {
        return item == Items.DIAMOND_HELMET || item == Items.DIAMOND_CHESTPLATE || item == Items.DIAMOND_LEGGINGS || item == Items.DIAMOND_BOOTS;
    }

    private boolean isIronArmor(Item item) {
        return item == Items.IRON_HELMET || item == Items.IRON_CHESTPLATE || item == Items.IRON_LEGGINGS || item == Items.IRON_BOOTS;
    }

    private boolean isChainArmor(Item item) {
        return item == Items.CHAINMAIL_HELMET || item == Items.CHAINMAIL_CHESTPLATE || item == Items.CHAINMAIL_LEGGINGS || item == Items.CHAINMAIL_BOOTS;
    }

    private boolean isGoldArmor(Item item) {
        return item == Items.GOLDEN_HELMET || item == Items.GOLDEN_CHESTPLATE || item == Items.GOLDEN_LEGGINGS || item == Items.GOLDEN_BOOTS;
    }

    private boolean isLeatherArmor(Item item) {
        return item == Items.LEATHER_HELMET || item == Items.LEATHER_CHESTPLATE || item == Items.LEATHER_LEGGINGS || item == Items.LEATHER_BOOTS;
    }

    private void drawName(CustomDrawContext ctx, float x, float y, float maxWidth) {
        String name;
        Font font = Fonts.MEDIUM.getFont(8.0f);
        float nameWidth = font.width(name = this.target.getName().getString());
        if (nameWidth > maxWidth) {
            ctx.enableScissor((int)x, (int)y, (int)(x + maxWidth), (int)(y + 12.0f));
            ctx.drawText(font, name, x, y, this.getTextColor());
            ctx.disableScissor();
        } else {
            ctx.drawText(font, name, x, y, this.getTextColor());
        }
    }

    private void drawHealth(CustomDrawContext ctx, float x, float y, float width) {
        float displayWidth;
        HealthDisplayState healthState = this.getHealthDisplayState(this.target);
        float health = healthState.health();
        float absorption = healthState.absorption();
        float maxHp = healthState.maxHealth();
        float displayHealth = healthState.displayHealth();
        ctx.drawRect(x, y, width, 2.0f, this.getBorderColor().mulAlpha(0.7f));
        if (this.showAbsorption.isEnabled() && absorption > 0.0f) {
            float total = maxHp + absorption;
            float targetBase = width * (health / total);
            float targetAbsorb = width * (absorption / total);
            this.healthWidth += (targetBase - this.healthWidth) * 0.2f;
            this.absorptionWidth += (targetAbsorb - this.absorptionWidth) * 0.2f;
            ctx.drawRect(x, y, this.healthWidth, 2.0f, this.getAccentColor());
            ctx.drawRect(x + this.healthWidth, y, this.absorptionWidth, 2.0f, ABSORPTION_COLOR);
            displayWidth = this.healthWidth + this.absorptionWidth;
        } else {
            float effectiveHealth = health;
            float targetWidth = width * (effectiveHealth / maxHp);
            this.healthWidth += (targetWidth - this.healthWidth) * 0.2f;
            this.absorptionWidth = 0.0f;
            ctx.drawRect(x, y, this.healthWidth, 2.0f, this.getAccentColor());
            displayWidth = this.healthWidth;
        }
        Font font = Fonts.MEDIUM.getFont(6.0f);
        String hpText = PlayerIntersectionUtil.getHealthString(displayHealth);
        float textWidth = font.width(hpText);
        float textX = x + Math.max(0.0f, Math.min(width - textWidth, displayWidth - textWidth / 2.0f));
        ctx.drawText(font, hpText, textX, y - 7.0f, this.getTextColor().mulAlpha(0.9f));
    }

    private void drawFace(CustomDrawContext ctx, float x, float y, float size, float scaleValue, float panelCenterX, float panelCenterY) {
        this.drawEntityHead(ctx, x, y, size, scaleValue, panelCenterX, panelCenterY);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void drawEntityHead(CustomDrawContext ctx, float x, float y, float size, float scaleValue, float panelCenterX, float panelCenterY) {
        if (this.target == null) {
            return;
        }
        LivingEntity entity = this.target;
        if (entity instanceof AbstractClientPlayerEntity) {
            AbstractClientPlayerEntity player = (AbstractClientPlayerEntity)entity;
            float radius = Math.max(2.0f, size * 0.25f);
            BorderRadius borderRadius = BorderRadius.all(radius);
            Identifier skinTexture = player.getSkinTextures().comp_1626();
            DrawUtil.drawRoundedTextureWithUV(ctx.getMatrices(), skinTexture, x, y, size, size, borderRadius, ColorRGBA.WHITE, 0.125f, 0.125f, 0.25f, 0.25f);
            DrawUtil.drawRoundedTextureWithUV(ctx.getMatrices(), skinTexture, x, y, size, size, borderRadius, ColorRGBA.WHITE, 0.625f, 0.125f, 0.75f, 0.25f);
            return;
        }
        float sx1 = panelCenterX + (x - panelCenterX) * scaleValue;
        float sy1 = panelCenterY + (y - panelCenterY) * scaleValue;
        float sx2 = panelCenterX + (x + size - panelCenterX) * scaleValue;
        float sy2 = panelCenterY + (y + size - panelCenterY) * scaleValue;
        ctx.enableScissor(Math.round(sx1), Math.round(sy1), Math.round(sx2), Math.round(sy2));
        float height = Math.max(0.1f, this.target.getHeight());
        float scale = size / (height * 0.3f);
        scale = MathHelper.clamp((float)scale, (float)1.0f, (float)200.0f);
        Vector3f translate = new Vector3f(0.0f, height * 0.85f, 0.0f);
        float yawDelta = 0.0f;
        float pitchDelta = 0.0f;
        if (TargetHud.mc.getEntityRenderDispatcher().camera != null && this.isTargetInView(this.target)) {
            Vec3d cameraPos = TargetHud.mc.getEntityRenderDispatcher().camera.getPos();
            Vec3d targetCenter = this.target.getPos().add(0.0, (double)height * 0.5, 0.0);
            Vec3d toCamera = cameraPos.subtract(targetCenter);
            double horizontal = Math.sqrt(toCamera.x * toCamera.x + toCamera.z * toCamera.z);
            if (horizontal > 1.0E-5) {
                float yawToCamera = (float)(MathHelper.atan2((double)toCamera.z, (double)toCamera.x) * 57.29577951308232) - 90.0f;
                float pitchToCamera = (float)(-(MathHelper.atan2((double)toCamera.y, (double)horizontal) * 57.29577951308232));
                yawDelta = MathHelper.clamp((float)MathHelper.wrapDegrees((float)(yawToCamera - this.target.getYaw())), (float)-70.0f, (float)70.0f);
                pitchDelta = MathHelper.clamp((float)pitchToCamera, (float)-35.0f, (float)35.0f);
            }
        }
        Quaternionf rotation = new Quaternionf().rotateZ((float)Math.PI).rotateY((float)Math.toRadians(yawDelta * 0.25f));
        Quaternionf cameraRotation = new Quaternionf().rotateX((float)Math.toRadians(-pitchDelta * 0.2f));
        float centerX = x + size / 2.0f;
        float centerY = y + size / 2.0f;
        int originalFireTicks = this.target.getFireTicks();
        boolean hideFireOverlay = this.target.isOnFire();
        if (hideFireOverlay) {
            this.target.setFireTicks(0);
        }
        try {
            InventoryScreen.drawEntity((DrawContext)ctx, (float)centerX, (float)centerY, (float)scale, (Vector3f)translate, (Quaternionf)rotation, (Quaternionf)cameraRotation, (LivingEntity)this.target);
        }
        finally {
            if (hideFireOverlay) {
                this.target.setFireTicks(originalFireTicks);
            }
        }
        ctx.disableScissor();
    }

    private void drawUsingItem(CustomDrawContext ctx, float x, float y, float size) {
        if (this.target == null || this.target.getActiveItem().isEmpty()) {
            return;
        }
        float anim = this.useAnimation.getValue();
        if (anim <= 0.01f) {
            return;
        }
        float panelX = x - (size + 6.0f) * anim;
        ItemStack active = this.target.getActiveItem();
        if (this.showAbsorption.isEnabled() && this.isSpecialUseItem(active)) {
            float bonusHp = Math.max(0.0f, this.getBonusHp(active));
            String text = "+" + PlayerIntersectionUtil.getHealthString(bonusHp);
            this.drawPanel(ctx, panelX, y, size, size);
            this.drawBonusHp(ctx, panelX, y, size, text);
            return;
        }
        this.drawPanel(ctx, panelX, y, size, size);
        this.drawItem(ctx, active, panelX + 4.0f, y + 4.0f, 0.7f);
    }

    private void drawBonusHp(CustomDrawContext ctx, float x, float y, float size, String text) {
        Font font = Fonts.MEDIUM.getFont(6.5f);
        float textWidth = font.width(text);
        float textX = x + (size - textWidth) / 2.0f;
        float textY = y + (size - font.height()) / 2.0f;
        ctx.drawText(font, text, textX, textY, this.resolveTextColor(ABSORPTION_COLOR));
    }

    public float getHideDelaySeconds() {
        return this.hideDelay.getCurrent();
    }

    public float getVisibilityScale() {
        return this.isEnabled() ? this.scale.getValue() : 0.0f;
    }

    private long getHideDelayMs() {
        float seconds = this.hideDelay.getCurrent();
        if (seconds <= 0.0f) {
            return 0L;
        }
        return (long)(seconds * 1000.0f);
    }

    private float getRealHp(LivingEntity entity) {
        if (entity instanceof AbstractClientPlayerEntity) {
            AbstractClientPlayerEntity player = (AbstractClientPlayerEntity)entity;
            return RealHpUtility.getRealHp(player);
        }
        return -1.0f;
    }

    private boolean isSpecialUseItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        return item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE || item == Items.GOLDEN_CARROT || this.isHealingPotion(stack);
    }

    private float getBonusHp(ItemStack stack) {
        LivingEntity entity = this.target;
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)entity;
            return (float)this.getConsumableHealValue(stack, player);
        }
        if (this.isHealingPotion(stack)) {
            return this.getHealingAmount(stack);
        }
        return 0.0f;
    }

    private boolean isHealingPotion(ItemStack stack) {
        Item item = stack.getItem();
        if (item != Items.POTION && item != Items.SPLASH_POTION && item != Items.LINGERING_POTION) {
            return false;
        }
        PotionContentsComponent component = (PotionContentsComponent)stack.get(DataComponentTypes.POTION_CONTENTS);
        if (component == null) {
            return false;
        }
        for (StatusEffectInstance effect : component.getEffects()) {
            if (effect.getEffectType() != StatusEffects.INSTANT_HEALTH) continue;
            return true;
        }
        return false;
    }

    private float getHealingAmount(ItemStack stack) {
        PotionContentsComponent component = (PotionContentsComponent)stack.get(DataComponentTypes.POTION_CONTENTS);
        if (component == null) {
            return 0.0f;
        }
        float total = 0.0f;
        for (StatusEffectInstance effect : component.getEffects()) {
            if (effect.getEffectType() != StatusEffects.INSTANT_HEALTH) continue;
            total += 4.0f * (float)(effect.getAmplifier() + 1);
        }
        return total;
    }

    private float getFixedHealth(LivingEntity entity) {
        float vanilla = Math.max(0.0f, entity.getHealth());
        float realHp = this.getRealHp(entity);
        if (realHp >= 0.0f) {
            return realHp;
        }
        return vanilla;
    }

    private HealthDisplayState getHealthDisplayState(LivingEntity entity) {
        float health = Math.max(0.0f, this.getFixedHealth(entity));
        float vanillaHealth = Math.max(0.0f, entity.getHealth());
        float absorption = this.getConfirmedAbsorptionAmount(entity, health, vanillaHealth);
        float baseHealth = health;
        if (absorption > 0.0f && health > vanillaHealth + 0.25f) {
            float includedAbsorption = Math.min(absorption, health - vanillaHealth);
            baseHealth = Math.max(0.0f, health - includedAbsorption);
        }
        float displayHealth = Math.max(health, baseHealth + absorption);
        float maxHealth = Math.max(1.0f, Math.max(entity.getMaxHealth(), baseHealth));
        return new HealthDisplayState(baseHealth, maxHealth, absorption, displayHealth);
    }

    private String formatHpText(float health) {
        return PlayerIntersectionUtil.getHealthString(Math.max(0.0f, health));
    }

    private float getConfirmedAbsorptionAmount(LivingEntity entity, float trackedHealth, float vanillaHealth) {
        boolean recentlyConsumedAbsorption;
        float actualAbsorption = Math.max(0.0f, entity.getAbsorptionAmount());
        if (actualAbsorption <= 0.0f) {
            return 0.0f;
        }
        if (!(entity instanceof PlayerEntity)) {
            return actualAbsorption;
        }
        PlayerEntity player = (PlayerEntity)entity;
        boolean hasAbsorptionEffect = player.hasStatusEffect(StatusEffects.ABSORPTION);
        boolean bl = recentlyConsumedAbsorption = this.targetConsumableState.matches(player) && this.targetConsumableState.hasRecentAbsorptionConsume(System.currentTimeMillis());
        if (hasAbsorptionEffect || recentlyConsumedAbsorption || trackedHealth > vanillaHealth + 0.25f) {
            return actualAbsorption;
        }
        return 0.0f;
    }

    private void updateTargetConsumableState(LivingEntity entity) {
        if (!(entity instanceof PlayerEntity)) {
            this.resetTargetConsumableState(null);
            return;
        }
        PlayerEntity player = (PlayerEntity)entity;
        this.targetConsumableState.bind(player.getUuid());
        if (player.isUsingItem()) {
            ItemStack activeItem = player.getActiveItem();
            if (!this.targetConsumableState.using) {
                this.targetConsumableState.using = true;
                this.targetConsumableState.startAge = player.age;
                this.targetConsumableState.item = activeItem.copy();
            } else if ((this.targetConsumableState.item == null || this.targetConsumableState.item.isEmpty()) && activeItem != null && !activeItem.isEmpty()) {
                this.targetConsumableState.item = activeItem.copy();
            }
            return;
        }
        if (!this.targetConsumableState.using) {
            return;
        }
        int useTicks = Math.max(0, player.age - this.targetConsumableState.startAge);
        ItemStack usedItem = this.targetConsumableState.item;
        this.targetConsumableState.resetUse();
        if (useTicks < 8 || usedItem == null || usedItem.isEmpty()) {
            return;
        }
        if (this.isAbsorptionConsumable(usedItem)) {
            this.targetConsumableState.lastAbsorptionConsumeAt = System.currentTimeMillis();
        }
    }

    private void resetTargetConsumableState(LivingEntity entity) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)entity;
            this.targetConsumableState.reset(player.getUuid());
            return;
        }
        this.targetConsumableState.reset(null);
    }

    private boolean isAbsorptionConsumable(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        return item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE;
    }

    private boolean isTargetInView(LivingEntity entity) {
        if (TargetHud.mc.player == null || entity == null) {
            return false;
        }
        if (TargetHud.mc.getEntityRenderDispatcher().camera == null) {
            return false;
        }
        Vec3d from = TargetHud.mc.getEntityRenderDispatcher().camera.getPos();
        Vec3d to = entity.getPos().add(0.0, (double)entity.getHeight() * 0.5, 0.0).subtract(from);
        double horizontal = Math.sqrt(to.x * to.x + to.z * to.z);
        if (horizontal < 1.0E-5) {
            return true;
        }
        float yawToTarget = (float)(MathHelper.atan2((double)to.z, (double)to.x) * 57.29577951308232) - 90.0f;
        float pitchToTarget = (float)(-(MathHelper.atan2((double)to.y, (double)horizontal) * 57.29577951308232));
        float cameraYaw = TargetHud.mc.getEntityRenderDispatcher().camera.getYaw();
        float cameraPitch = TargetHud.mc.getEntityRenderDispatcher().camera.getPitch();
        float yawDiff = Math.abs(MathHelper.wrapDegrees((float)(cameraYaw - yawToTarget)));
        float pitchDiff = Math.abs(cameraPitch - pitchToTarget);
        return yawDiff <= 95.0f && pitchDiff <= 70.0f;
    }

    private boolean isInvisibleTarget(LivingEntity entity) {
        if (!(entity instanceof PlayerEntity)) {
            return false;
        }
        PlayerEntity player = (PlayerEntity)entity;
        return player.isInvisible() || player.hasStatusEffect(StatusEffects.INVISIBILITY);
    }

    private boolean isInvisibleArmoredTarget(LivingEntity entity) {
        if (!(entity instanceof PlayerEntity)) {
            return false;
        }
        PlayerEntity player = (PlayerEntity)entity;
        return this.isInvisibleTarget((LivingEntity)player) && this.hasArmorEquipped(player);
    }

    private boolean hasArmorEquipped(PlayerEntity player) {
        for (ItemStack armorPiece : player.getArmorItems()) {
            if (armorPiece == null || armorPiece.isEmpty()) continue;
            return true;
        }
        return false;
    }

    private LivingEntity getCrosshairTarget() {
        if (TargetHud.mc.crosshairTarget == null || TargetHud.mc.crosshairTarget.getType() != HitResult.Type.ENTITY) {
            return null;
        }
        HitResult ItemStackParticleEffect = TargetHud.mc.crosshairTarget;
        if (ItemStackParticleEffect instanceof EntityHitResult) {
            LivingEntity entity;
            EntityHitResult hit = (EntityHitResult)ItemStackParticleEffect;
            if (hit.getEntity() instanceof ArmorStandEntity) {
                return null;
            }
            Entity entity = hit.getEntity();
            if (entity instanceof LivingEntity && (entity = (LivingEntity)entity).isAlive()) {
                if (this.isInvisibleTarget(entity)) {
                    return null;
                }
                return entity;
            }
        }
        return null;
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (TargetHud.mc.player == null || TargetHud.mc.world == null) {
            this.resetCombatTracking(null);
            this.invalidateOutcomeCache();
            return;
        }
        LivingEntity combatTarget = this.target != null && this.target != TargetHud.mc.player && this.target.isAlive() ? this.target : null;
        this.updateCombatTracking(combatTarget);
    }

    @EventTarget
    public void onRender(EventHudRender event) {
        if (TargetHud.mc.player == null || TargetHud.mc.world == null) {
            return;
        }
        this.updateHud();
        this.renderHud(event.getContext());
    }

    @EventTarget
    public void onMouse(EventMouse event) {
        if (TargetHud.mc.player == null || TargetHud.mc.world == null) {
            return;
        }
        this.handleMouse(event);
    }

    private static enum Outcome {
        WIN,
        LOSE,
        DRAW;

    }

    private static final class TargetConsumableState {
        private UUID playerUuid;
        private boolean using;
        private int startAge;
        private ItemStack item = ItemStack.EMPTY;
        private long lastAbsorptionConsumeAt;

        private TargetConsumableState() {
        }

        private void bind(UUID uuid) {
            if (uuid != null && uuid.equals(this.playerUuid)) {
                return;
            }
            this.reset(uuid);
        }

        private boolean matches(PlayerEntity player) {
            return player != null && this.playerUuid != null && this.playerUuid.equals(player.getUuid());
        }

        private boolean hasRecentAbsorptionConsume(long now) {
            return this.lastAbsorptionConsumeAt > 0L && now - this.lastAbsorptionConsumeAt <= 5000L;
        }

        private void resetUse() {
            this.using = false;
            this.startAge = 0;
            this.item = ItemStack.EMPTY;
        }

        private void reset(UUID uuid) {
            this.playerUuid = uuid;
            this.lastAbsorptionConsumeAt = 0L;
            this.resetUse();
        }
    }

    private record HealthDisplayState(float health, float maxHealth, float absorption, float displayHealth) {
    }

    private record CombatSnapshot(double effectiveHealth, double statScore, double pressureScore, double dps) {
    }
}

