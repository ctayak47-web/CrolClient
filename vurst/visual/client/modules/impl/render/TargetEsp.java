
package vurst.visual.client.modules.impl.render;

import com.darkmagician6.eventapi.EventTarget;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.ShaderProgramKeys;
import net.minecraft.ShaderProgramKey;
import net.minecraft.StatusEffects;
import net.minecraft.Entity;
import net.minecraft.LivingEntity;
import net.minecraft.PlayerEntity;
import net.minecraft.HitResult;
import net.minecraft.Vec3d;
import net.minecraft.BufferRenderer;
import net.minecraft.BufferBuilder;
import net.minecraft.Tessellator;
import net.minecraft.VertexFormats;
import net.minecraft.VertexFormat;
import net.minecraft.Identifier;
import net.minecraft.MathHelper;
import net.minecraft.RaycastContext;
import net.minecraft.BlockHitResult;
import net.minecraft.EntityHitResult;
import net.minecraft.Camera;
import net.minecraft.MatrixStack;
import net.minecraft.RotationAxis;
import net.minecraft.BuiltBuffer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import vurst.visual.VurstVisual;
import vurst.visual.base.animations.base.Animation;
import vurst.visual.base.animations.base.Easing;
import vurst.visual.base.events.impl.player.EventUpdate;
import vurst.visual.base.events.impl.render.EventRender2D;
import vurst.visual.base.events.impl.render.EventRender3D;
import vurst.visual.base.theme.Theme;
import vurst.visual.base.theme.ThemeManager;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.Setting;
import vurst.visual.client.modules.api.setting.impl.BooleanSetting;
import vurst.visual.client.modules.api.setting.impl.ColorSetting;
import vurst.visual.client.modules.api.setting.impl.ModeSetting;
import vurst.visual.client.modules.api.setting.impl.NumberSetting;
import vurst.visual.client.modules.impl.hud.TargetHud;
import vurst.visual.client.modules.impl.render.FullBright;
import vurst.visual.utility.compat.LunarCompat;
import vurst.visual.utility.math.MathUtil;
import vurst.visual.utility.math.ProjectionUtil;
import vurst.visual.utility.render.display.base.CustomDrawContext;
import vurst.visual.utility.render.display.base.Gradient;
import vurst.visual.utility.render.display.base.color.ColorRGBA;
import vurst.visual.utility.render.display.base.color.ColorUtil;
import vurst.visual.utility.render.display.shader.DrawUtil;
import vurst.visual.utility.render.entity.EntityDamageTracker;
import vurst.visual.utility.render.level.Render3DUtil;

@ModuleAnnotation(name="Target ESP", category=Category.RENDER, description="Подсвечивает текущую цель.")
public final class TargetEsp
extends Module {
    public static final TargetEsp INSTANCE = new TargetEsp();
    private static final Identifier RHOMBUS_TEX = VurstVisual.id("hud/target.png");
    private static final Identifier GLOW_TEX = VurstVisual.id("hud/glow.png");
    private static final Identifier VEX_TEX = VurstVisual.id("hud/skull.png");
    private static final Identifier MARKER_TEX = VurstVisual.id("hud/marker.png");
    private static final Identifier SUPER_RHOMBUS_TEX = VurstVisual.id("hud/cacto.png");
    private static final Identifier FLOW_TEX = VurstVisual.id("hud/flow.png");
    private static final Identifier MARKER_V2_TEX = VurstVisual.id("hud/markerv2.png");
    private static final Identifier VERTUSHKA_TEX = VurstVisual.id("hud/vertyxua.png");
    private static final Identifier CHAIN_TEX = VurstVisual.id("hud/chain.png");
    private final ModeSetting type = new ModeSetting("Режим", "Ромб", "Кольцо", "Кольцо 2", "Призраки", "Векс", "Кристалики", "Скелет", "Маркер", "Цепи", "Кругляшок", "Гирлянды", "Супер ромб", "Флов", "Скобы", "Вертушка", "Души", "Кристалы");
    private final ModeSetting.Value modeRhombus = this.type.getValues().get(0);
    private final ModeSetting.Value modeRing = this.type.getValues().get(1);
    private final ModeSetting.Value modeRing2 = this.type.getValues().get(2);
    private final ModeSetting.Value modeGhosts = this.type.getValues().get(3);
    private final ModeSetting.Value modeVex = this.type.getValues().get(4);
    private final ModeSetting.Value modeCrystals = this.type.getValues().get(5);
    private final ModeSetting.Value modeSkeleton = this.type.getValues().get(6);
    private final ModeSetting.Value modeMarker = this.type.getValues().get(7);
    private final ModeSetting.Value modeChains = this.type.getValues().get(8);
    private final ModeSetting.Value modeCircle = this.type.getValues().get(9);
    private final ModeSetting.Value modeGarland = this.type.getValues().get(10);
    private final ModeSetting.Value modeSuperRhombus = this.type.getValues().get(11);
    private final ModeSetting.Value modeFlow = this.type.getValues().get(12);
    private final ModeSetting.Value modeMarkerV2 = this.type.getValues().get(13);
    private final ModeSetting.Value modeVertushka = this.type.getValues().get(14);
    private final ModeSetting.Value modeSouls = this.type.getValues().get(15);
    private final ModeSetting.Value modeCrystalsNew = this.type.getValues().get(16);
    private final NumberSetting sizeRhombus = new NumberSetting("Размер ромба", 5.0f, 2.0f, 20.0f, 0.5f, () -> this.type.is(this.modeRhombus));
    private final NumberSetting sizeRing = new NumberSetting("Размер кольца", 9.0f, 2.0f, 20.0f, 0.5f, () -> this.type.is(this.modeRing));
    private final NumberSetting sizeRing2 = new NumberSetting("Размер кольца 2", 9.0f, 2.0f, 20.0f, 0.5f, () -> this.type.is(this.modeRing2));
    private final NumberSetting sizeGhosts = new NumberSetting("Размер призраков", 9.0f, 2.0f, 20.0f, 0.5f, () -> this.type.is(this.modeGhosts));
    private final NumberSetting sizeVex = new NumberSetting("Размер векса", 4.0f, 2.0f, 20.0f, 0.5f, () -> this.type.is(this.modeVex));
    private final NumberSetting sizeCrystals = new NumberSetting("Размер кристаликов", 9.0f, 2.0f, 20.0f, 0.5f, () -> this.type.is(this.modeCrystals));
    private final NumberSetting sizeSkeleton = new NumberSetting("Размер скелета", 9.0f, 2.0f, 20.0f, 0.5f, () -> this.type.is(this.modeSkeleton));
    private final NumberSetting sizeMarker = new NumberSetting("Размер маркера", 4.0f, 2.0f, 20.0f, 0.5f, () -> this.type.is(this.modeMarker));
    private final NumberSetting sizeChains = new NumberSetting("Размер цепей", 9.0f, 2.0f, 20.0f, 0.5f, () -> this.type.is(this.modeChains));
    private final NumberSetting sizeGarland = new NumberSetting("Размер гирлянд", 9.0f, 2.0f, 20.0f, 0.5f, () -> this.type.is(this.modeGarland));
    private final NumberSetting sizeSuperRhombus = new NumberSetting("Размер супер ромба", 4.0f, 2.0f, 20.0f, 0.5f, () -> this.type.is(this.modeSuperRhombus));
    private final NumberSetting sizeFlow = new NumberSetting("Размер флов", 4.0f, 2.0f, 20.0f, 0.5f, () -> this.type.is(this.modeFlow));
    private final NumberSetting sizeMarkerV2 = new NumberSetting("Размер маркера V2", 4.0f, 2.0f, 20.0f, 0.5f, () -> this.type.is(this.modeMarkerV2));
    private final NumberSetting sizeVertushka = new NumberSetting("Размер вертушки", 6.0f, 2.0f, 20.0f, 0.5f, () -> this.type.is(this.modeVertushka));
    private final NumberSetting ghostCount = new NumberSetting("Количество призраков", 25.0f, 5.0f, 60.0f, 1.0f, () -> this.type.is(this.modeGhosts));
    private final NumberSetting speedRhombus = new NumberSetting("Скорость ромба", 1.0f, 0.1f, 5.0f, 0.05f, () -> this.type.is(this.modeRhombus));
    private final NumberSetting speedRing = new NumberSetting("Скорость кольца", 1.0f, 0.1f, 5.0f, 0.05f, () -> this.type.is(this.modeRing));
    private final NumberSetting ring2Speed = new NumberSetting("Скорость кольца 2", 1.0f, 0.2f, 3.0f, 0.05f, () -> this.type.is(this.modeRing2));
    private final NumberSetting speedGhosts = new NumberSetting("Скорость призраков", 1.0f, 0.1f, 5.0f, 0.05f, () -> this.type.is(this.modeGhosts));
    private final NumberSetting speedVex = new NumberSetting("Скорость векса", 1.0f, 0.1f, 5.0f, 0.05f, () -> this.type.is(this.modeVex));
    private final NumberSetting speedCrystals = new NumberSetting("Скорость кристаликов", 1.0f, 0.1f, 5.0f, 0.05f, () -> this.type.is(this.modeCrystals));
    private final NumberSetting speedSkeleton = new NumberSetting("Скорость скелета", 1.0f, 0.1f, 5.0f, 0.05f, () -> this.type.is(this.modeSkeleton));
    private final NumberSetting speedMarker = new NumberSetting("Скорость маркера", 1.0f, 0.1f, 5.0f, 0.05f, () -> this.type.is(this.modeMarker));
    private final NumberSetting speedChains = new NumberSetting("Скорость цепей", 1.0f, 0.1f, 5.0f, 0.05f, () -> this.type.is(this.modeChains));
    private final NumberSetting speedGarland = new NumberSetting("Скорость гирлянд", 1.0f, 0.1f, 5.0f, 0.05f, () -> this.type.is(this.modeGarland));
    private final NumberSetting speedSuperRhombus = new NumberSetting("Скорость супер ромба", 1.0f, 0.1f, 5.0f, 0.05f, () -> this.type.is(this.modeSuperRhombus));
    private final NumberSetting speedFlow = new NumberSetting("Скорость флов", 1.0f, 0.1f, 5.0f, 0.05f, () -> this.type.is(this.modeFlow));
    private final NumberSetting speedMarkerV2 = new NumberSetting("Скорость маркера V2", 1.0f, 0.1f, 5.0f, 0.05f, () -> this.type.is(this.modeMarkerV2));
    private final NumberSetting speedVertushka = new NumberSetting("Скорость вертушки", 1.0f, 0.1f, 5.0f, 0.05f, () -> this.type.is(this.modeVertushka));
    private final NumberSetting circleSpeed = new NumberSetting("Скорость круга", 2.5f, 0.1f, 5.0f, 0.05f, () -> this.type.is(this.modeCircle));
    private final NumberSetting circleSize = new NumberSetting("Размер круга", 1.0f, 0.2f, 3.0f, 0.05f, () -> this.type.is(this.modeCircle));
    private final BooleanSetting circleBloom = new BooleanSetting("Свечение круга", true, () -> this.type.is(this.modeCircle));
    private final NumberSetting circleBloomSize = new NumberSetting("Размер свечения", 0.5f, 0.0f, 2.0f, 0.05f, () -> this.type.is(this.modeCircle) && this.circleBloom.isEnabled());
    private final BooleanSetting circleRedOnImpact = new BooleanSetting("Красный при ударе", true, () -> this.type.is(this.modeCircle));
    private final NumberSetting circleImpactFadeIn = new NumberSetting("Удар: вход", 0.2f, 0.01f, 1.0f, 0.01f, () -> this.type.is(this.modeCircle) && this.circleRedOnImpact.isEnabled());
    private final NumberSetting circleImpactFadeOut = new NumberSetting("Удар: выход", 0.1f, 0.01f, 1.0f, 0.01f, () -> this.type.is(this.modeCircle) && this.circleRedOnImpact.isEnabled());
    private final NumberSetting circleImpactIntensity = new NumberSetting("Сила удара", 1.0f, 0.0f, 1.0f, 0.05f, () -> this.type.is(this.modeCircle) && this.circleRedOnImpact.isEnabled());
    private final BooleanSetting damageRed = new BooleanSetting("Покраснение", true);
    private final BooleanSetting randomColors = new BooleanSetting("Рандомные цвета", false);
    private final BooleanSetting customTheme = new BooleanSetting("Клиентский", false);
    private final ColorSetting primaryColor = new ColorSetting("Первый цвет", Theme.DARK.getColor(), this.customTheme::isEnabled, Theme.DARK::getColor);
    private final ColorSetting secondaryColor = new ColorSetting("Второй цвет", Theme.DARK.getSecondColor(), this.customTheme::isEnabled, Theme.DARK::getSecondColor);
    private final Animation alpha = new Animation(600L, 0.0f, Easing.QUAD_OUT);
    private final ThemeManager themeManager;
    private LivingEntity currentTarget;
    private LivingEntity lastTarget;
    private UUID randomColorsTargetUuid;
    private ColorRGBA randomPrimaryColor = Theme.DARK.getColor();
    private ColorRGBA randomSecondaryColor = Theme.DARK.getSecondColor();
    private ColorRGBA randomNextPrimaryColor = this.randomPrimaryColor;
    private ColorRGBA randomNextSecondaryColor = this.randomSecondaryColor;
    private Vec3d lastKnownCenter;
    private float lastKnownHeight = 1.8f;
    private float lastKnownWidth = 0.6f;
    private long lastTime = System.currentTimeMillis();
    private long lastSeenAt = 0L;
    private static final long DAMAGE_FLASH_DURATION = 350L;
    private static final long RANDOM_COLOR_TRANSITION_MS = 500L;
    private long randomColorTransitionStartedAt;
    private float damageFlashIntensity;
    private float circleMoving;
    private float circlePrevMoving;
    private float circleVerticalTime;
    private float circlePrevVerticalTime;
    private float circleImpactProgress;
    private int circlePrevHurtTime;
    private LivingEntity circleImpactTarget;
    private Vec3d lunarOcclusionFrom;
    private Vec3d lunarOcclusionTo;
    private boolean lunarOcclusionValue;
    private long lunarOcclusionUpdatedAt;
    private static final long LUNAR_OCCLUSION_CACHE_MS = 80L;
    private static final long SOUL_HISTORY_DURATION_MS = 1000L;
    private final CopyOnWriteArrayList<PositionEntry> targetPositionHistory = new CopyOnWriteArrayList();
    private static final Vector3f[] BLUME_CRYSTAL_VERTICES = new Vector3f[]{new Vector3f(0.0f, 1.5f, 0.0f), new Vector3f(0.0f, -1.5f, 0.0f), new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(-1.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.0f, 0.0f, -1.0f)};
    private static final int[][] BLUME_CRYSTAL_FACES = new int[][]{{0, 2, 4}, {0, 4, 3}, {0, 3, 5}, {0, 5, 2}, {1, 4, 2}, {1, 3, 4}, {1, 5, 3}, {1, 2, 5}};

    private TargetEsp() {
        this.themeManager = VurstVisual.getInstance().getThemeManager();
        this.type.getValues().remove(this.modeCrystalsNew);
        this.type.getValues().remove(this.modeSouls);
        if (this.type.is(this.modeSouls) || this.type.is(this.modeCrystalsNew)) {
            this.modeRhombus.select();
        }
    }

    @Override
    public JsonObject save() {
        JsonObject object = super.save();
        JsonObject modeProfiles = new JsonObject();
        this.saveModeProfile(modeProfiles, "rhombus", this.sizeRhombus, this.speedRhombus);
        this.saveModeProfile(modeProfiles, "ring", this.sizeRing, this.speedRing);
        this.saveModeProfile(modeProfiles, "ring2", this.sizeRing2, this.ring2Speed);
        this.saveModeProfile(modeProfiles, "ghosts", this.sizeGhosts, this.speedGhosts, this.ghostCount);
        this.saveModeProfile(modeProfiles, "vex", this.sizeVex, this.speedVex);
        this.saveModeProfile(modeProfiles, "crystals", this.sizeCrystals, this.speedCrystals);
        this.saveModeProfile(modeProfiles, "skeleton", this.sizeSkeleton, this.speedSkeleton);
        this.saveModeProfile(modeProfiles, "marker", this.sizeMarker, this.speedMarker);
        this.saveModeProfile(modeProfiles, "chains", this.sizeChains, this.speedChains);
        this.saveModeProfile(modeProfiles, "circle", this.circleSize, this.circleSpeed, this.circleBloom, this.circleBloomSize, this.circleRedOnImpact, this.circleImpactFadeIn, this.circleImpactFadeOut, this.circleImpactIntensity);
        this.saveModeProfile(modeProfiles, "garland", this.sizeGarland, this.speedGarland);
        this.saveModeProfile(modeProfiles, "super_rhombus", this.sizeSuperRhombus, this.speedSuperRhombus);
        this.saveModeProfile(modeProfiles, "flow", this.sizeFlow, this.speedFlow);
        this.saveModeProfile(modeProfiles, "marker_v2", this.sizeMarkerV2, this.speedMarkerV2);
        this.saveModeProfile(modeProfiles, "vertushka", this.sizeVertushka, this.speedVertushka);
        object.add("modeProfiles", modeProfiles);
        return object;
    }

    @Override
    public void load(JsonObject object) {
        super.load(object);
        if (object == null || !object.has("modeProfiles") || !object.get("modeProfiles").isJsonObject()) {
            return;
        }
        JsonObject modeProfiles = object.getAsJsonObject("modeProfiles");
        this.loadModeProfile(modeProfiles, "rhombus", this.sizeRhombus, this.speedRhombus);
        this.loadModeProfile(modeProfiles, "ring", this.sizeRing, this.speedRing);
        this.loadModeProfile(modeProfiles, "ring2", this.sizeRing2, this.ring2Speed);
        this.loadModeProfile(modeProfiles, "ghosts", this.sizeGhosts, this.speedGhosts, this.ghostCount);
        this.loadModeProfile(modeProfiles, "vex", this.sizeVex, this.speedVex);
        this.loadModeProfile(modeProfiles, "crystals", this.sizeCrystals, this.speedCrystals);
        this.loadModeProfile(modeProfiles, "skeleton", this.sizeSkeleton, this.speedSkeleton);
        this.loadModeProfile(modeProfiles, "marker", this.sizeMarker, this.speedMarker);
        this.loadModeProfile(modeProfiles, "chains", this.sizeChains, this.speedChains);
        this.loadModeProfile(modeProfiles, "circle", this.circleSize, this.circleSpeed, this.circleBloom, this.circleBloomSize, this.circleRedOnImpact, this.circleImpactFadeIn, this.circleImpactFadeOut, this.circleImpactIntensity);
        this.loadModeProfile(modeProfiles, "garland", this.sizeGarland, this.speedGarland);
        this.loadModeProfile(modeProfiles, "super_rhombus", this.sizeSuperRhombus, this.speedSuperRhombus);
        this.loadModeProfile(modeProfiles, "flow", this.sizeFlow, this.speedFlow);
        this.loadModeProfile(modeProfiles, "marker_v2", this.sizeMarkerV2, this.speedMarkerV2);
        this.loadModeProfile(modeProfiles, "vertushka", this.sizeVertushka, this.speedVertushka);
    }

    private void saveModeProfile(JsonObject modeProfiles, String key, Setting ... settings) {
        JsonObject profile = new JsonObject();
        for (Setting setting : settings) {
            setting.safe(profile);
        }
        modeProfiles.add(key, profile);
    }

    private void loadModeProfile(JsonObject modeProfiles, String key, Setting ... settings) {
        if (!modeProfiles.has(key) || !modeProfiles.get(key).isJsonObject()) {
            return;
        }
        JsonObject profile = modeProfiles.getAsJsonObject(key);
        for (Setting setting : settings) {
            if (!profile.has(setting.getName())) continue;
            setting.load(profile);
        }
    }

    @EventTarget
    public void onUpdate(EventUpdate eventUpdate) {
        if (TargetEsp.mc.player == null || TargetEsp.mc.world == null) {
            return;
        }
        if (this.hasVisionBlockingEffect()) {
            this.alpha.update(false);
            if (this.alpha.getValue() <= 0.0f) {
                this.lastKnownCenter = null;
                this.currentTarget = null;
            }
            return;
        }
        LivingEntity candidate = this.findCrosshairTarget();
        long now = System.currentTimeMillis();
        if (candidate != null) {
            this.currentTarget = candidate;
            this.lastTarget = candidate;
            this.lastSeenAt = now;
        }
        if (this.currentTarget != null && (!this.currentTarget.isAlive() || this.isInvisibleTarget(this.currentTarget))) {
            this.currentTarget = null;
            this.lastKnownCenter = null;
        }
        boolean keepVisible = false;
        if (this.currentTarget != null) {
            float hideDelaySeconds = TargetHud.INSTANCE.getHideDelaySeconds();
            keepVisible = this.shouldKeepVisible(candidate != null, now, hideDelaySeconds);
        }
        if (keepVisible && this.currentTarget != null) {
            this.lastKnownCenter = this.getEntityCenter(this.currentTarget);
            this.lastKnownHeight = this.currentTarget.getHeight();
            this.lastKnownWidth = this.currentTarget.getWidth();
        }
        this.refreshRandomColors(this.currentTarget != null ? this.currentTarget : this.lastTarget);
        this.updateDamageFlashState(this.currentTarget != null ? this.currentTarget : this.lastTarget);
        if (this.type.is(this.modeCircle)) {
            this.updateCircleState(this.currentTarget != null ? this.currentTarget : this.lastTarget);
        } else {
            this.resetCircleImpact();
        }
        if (this.type.is(this.modeSouls)) {
            this.updateTargetPositionHistory(keepVisible ? this.currentTarget : null);
        } else if (!this.targetPositionHistory.isEmpty()) {
            this.targetPositionHistory.clear();
        }
        this.alpha.update(keepVisible);
        if (!keepVisible && this.alpha.getValue() <= 0.0f) {
            this.lastKnownCenter = null;
            this.currentTarget = null;
            this.lastTarget = null;
            this.resetRandomColorCycle();
        }
    }

    private boolean shouldKeepVisible(boolean hasCandidate, long now, float hideDelaySeconds) {
        if (hasCandidate) {
            return true;
        }
        float hideDelayMs = hideDelaySeconds * 1000.0f;
        return hideDelayMs > 0.0f && (float)(now - this.lastSeenAt) < hideDelayMs;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventTarget
    public void onRender2D(EventRender2D event) {
        if (TargetEsp.mc.player == null || TargetEsp.mc.world == null) {
            return;
        }
        try {
            if (TargetEsp.mc.currentScreen != null) {
                return;
            }
            if (this.hasVisionBlockingEffect()) {
                return;
            }
            boolean isTextureMode = this.isTextureMode();
            if (!isTextureMode) {
                return;
            }
            float hudVisibility = TargetHud.INSTANCE.isEnabled() ? TargetHud.INSTANCE.getVisibilityScale() : 1.0f;
            float alphaValue = this.alpha.getValue() * hudVisibility;
            if (alphaValue <= 0.0f) {
                return;
            }
            Vec3d center = this.getRenderCenter();
            if (center == null) {
                return;
            }
            if (this.isOccludedForCurrentMode(TargetEsp.mc.player.getCameraPosVec(1.0f), center)) {
                return;
            }
            Vec3d screen = ProjectionUtil.worldSpaceToScreenSpace(center);
            if (screen.z <= 0.0 || screen.z >= 1.0) {
                return;
            }
            float size = (float)this.getScale(center, this.getTextureModeSize());
            float angle = (float)(Math.sin((double)System.currentTimeMillis() / 1000.0 * (double)this.getTextureModeSpeed()) * 360.0);
            Gradient tinted = this.getTextureGradient(alphaValue, System.currentTimeMillis());
            CustomDrawContext ctx = event.getContext();
            MatrixStack matrices = ctx.getMatrices();
            matrices.push();
            matrices.translate(screen.x, screen.y, 0.0);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle));
            matrices.translate(-screen.x, -screen.y, 0.0);
            Identifier tex = this.getTextureModeTexture();
            DrawUtil.drawTexture(matrices, tex, (float)screen.x - size / 2.0f, (float)screen.y - size / 2.0f, size, size, tinted);
            matrices.pop();
        }
        finally {
            this.resetRenderStateAfterEsp();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @EventTarget
    public void onRender3D(EventRender3D event) {
        if (TargetEsp.mc.player == null || TargetEsp.mc.world == null) {
            return;
        }
        try {
            if (TargetEsp.mc.currentScreen != null) {
                return;
            }
            if (this.hasVisionBlockingEffect()) {
                return;
            }
            if (this.isTextureMode()) {
                return;
            }
            float hudVisibility = TargetHud.INSTANCE.isEnabled() ? TargetHud.INSTANCE.getVisibilityScale() : 1.0f;
            float alphaValue = this.alpha.getValue() * hudVisibility;
            if (alphaValue <= 0.0f) {
                return;
            }
            Vec3d center = this.getRenderCenter();
            if (center == null) {
                return;
            }
            if (this.isOccludedForCurrentMode(TargetEsp.mc.player.getCameraPosVec(1.0f), center)) {
                return;
            }
            if (this.type.is(this.modeRing)) {
                this.renderRing(event.getMatrix(), center, alphaValue);
            } else if (this.type.is(this.modeRing2)) {
                this.renderRing2(event, alphaValue);
            } else if (this.type.is(this.modeGhosts)) {
                this.renderGhosts(event, center, alphaValue);
            } else if (this.type.is(this.modeSouls)) {
                this.renderSouls(event, center, alphaValue);
            } else if (this.type.is(this.modeCrystals)) {
                this.renderCrystals(event, alphaValue);
            } else if (this.type.is(this.modeCrystalsNew)) {
                this.renderCrystalsBlumeStyle(event, alphaValue);
            } else if (this.type.is(this.modeSkeleton)) {
                this.renderSkeleton(this.currentTarget, alphaValue);
            } else if (this.type.is(this.modeChains)) {
                this.renderChains(event, alphaValue);
            } else if (this.type.is(this.modeCircle)) {
                this.renderCircle(event, alphaValue);
            } else if (this.type.is(this.modeGarland)) {
                this.renderGarland(event, alphaValue);
            }
        }
        finally {
            this.resetRenderStateAfterEsp();
        }
    }

    private void resetRenderStateAfterEsp() {
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (int)0);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        RenderSystem.colorMask((boolean)true, (boolean)true, (boolean)true, (boolean)true);
        RenderSystem.disableScissor();
        if (mc != null && mc.getWindow() != null) {
            RenderSystem.viewport((int)0, (int)0, (int)mc.getWindow().getFramebufferWidth(), (int)mc.getWindow().getFramebufferHeight());
        }
        RenderSystem.lineWidth((float)1.0f);
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderRing(MatrixStack matrices, Vec3d center, float alphaValue) {
        LivingEntity targetEntity;
        float alpha = MathHelper.clamp((float)alphaValue, (float)0.0f, (float)1.0f);
        if (alpha <= 0.0f) {
            return;
        }
        LivingEntity entity = targetEntity = this.currentTarget != null ? this.currentTarget : this.lastTarget;
        if (targetEntity == null || !targetEntity.isAlive()) {
            return;
        }
        Camera camera = TargetEsp.mc.getEntityRenderDispatcher().camera;
        Vec3d renderPos = MathUtil.interpolate((Entity)targetEntity).subtract(camera.getPos());
        float scale = Math.max(0.35f, this.sizeRing.getCurrent() / 9.0f);
        float radius = targetEntity.getWidth() * 0.85f * scale;
        float height = targetEntity.getHeight();
        double duration = 2000.0;
        double elapsed = (double)System.currentTimeMillis() % duration;
        boolean side = elapsed > duration / 2.0;
        double progress = elapsed / (duration / 2.0);
        progress = side ? progress - 1.0 : 1.0 - progress;
        progress = MathHelper.clamp((float)((float)progress), (float)0.0f, (float)1.0f);
        progress = progress < 0.5 ? 2.0 * progress * progress : 1.0 - Math.pow(-2.0 * progress + 2.0, 2.0) / 2.0;
        double eased = (double)(height / 2.0f) * (progress > 0.5 ? 1.0 - progress : progress) * (side ? -1.0 : 1.0);
        double y0 = renderPos.y + (double)height * progress;
        double y1 = y0 + eased * 1.5;
        int rgb = this.applyDamageFlash(this.getThemeColor(0).getRGB()) & 0xFFFFFF;
        int glowColor = TargetEsp.clamp255(145.0f * alpha) << 24 | rgb;
        int coreColor = TargetEsp.clamp255(17.1f * alpha) << 24 | rgb;
        RenderSystem.enableBlend();
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.lineWidth((float)Math.max(1.0f, 1.5f * scale));
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder band = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 360; ++i) {
            double angle = Math.toRadians(i);
            float x = (float)(Math.cos(angle) * (double)radius);
            float z = (float)(Math.sin(angle) * (double)radius);
            band.vertex(matrix, (float)(renderPos.x + (double)x), (float)y0, (float)(renderPos.z + (double)z)).color(glowColor);
            band.vertex(matrix, (float)(renderPos.x + (double)x), (float)y1, (float)(renderPos.z + (double)z)).color(coreColor);
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)band.end());
        BufferBuilder outline = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 360; ++i) {
            double angle = Math.toRadians(i);
            float x = (float)(Math.cos(angle) * (double)radius);
            float z = (float)(Math.sin(angle) * (double)radius);
            outline.vertex(matrix, (float)(renderPos.x + (double)x), (float)y0, (float)(renderPos.z + (double)z)).color(coreColor);
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)outline.end());
        RenderSystem.lineWidth((float)1.0f);
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask((boolean)true);
        RenderSystem.disableBlend();
    }

    private void renderRing2(EventRender3D event, float alphaValue) {
        LivingEntity targetEntity;
        float alpha = Math.max(alphaValue, 0.0f);
        if (alpha <= 0.0f) {
            return;
        }
        LivingEntity entity = targetEntity = this.currentTarget != null ? this.currentTarget : this.lastTarget;
        if (targetEntity == null) {
            return;
        }
        Camera camera = TargetEsp.mc.getEntityRenderDispatcher().camera;
        Vec3d renderPos = MathUtil.interpolate((Entity)targetEntity).subtract(camera.getPos());
        float scale = this.sizeRing2.getCurrent() / 9.0f;
        float entityWidth = targetEntity.getWidth() * 0.9f * scale;
        float entityHeight = targetEntity.getHeight();
        float animationAlpha = this.easeOutCubic(alpha);
        double step = (double)System.currentTimeMillis() / 1000.0 * (double)this.ring2Speed.getCurrent();
        double headY = this.absSinAnimation(step) * (double)entityHeight;
        double tailBaseY = this.absSinAnimation(step - 0.4) * (double)entityHeight;
        float headSize = 0.12f * scale;
        float tailSize = 0.08f * scale;
        int totalPoints = 138;
        int tailSegments = 16;
        long now = System.currentTimeMillis();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (Identifier)GLOW_TEX);
        MatrixStack matrices = event.getMatrix();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        for (int i = 0; i < totalPoints; ++i) {
            double angleRadians = Math.PI * 2 * (double)i / (double)totalPoints;
            float xOffset = (float)(Math.cos(angleRadians) * (double)entityWidth);
            float zOffset = (float)(Math.sin(angleRadians) * (double)entityWidth);
            int baseColor = this.getThemeColorAngle(i * (360 / totalPoints), now);
            int headColor = this.applyAlpha(baseColor, animationAlpha * 0.9f);
            Vec3d headPos = new Vec3d(renderPos.x + (double)xOffset, renderPos.y + headY, renderPos.z + (double)zOffset);
            this.drawBillboard(buffer, matrices, camera, headPos, headSize, headColor);
            for (int t = 1; t <= tailSegments; ++t) {
                float tailProgress = (float)t / (float)(tailSegments + 1);
                double currentTailY = headY + (tailBaseY - headY) * (double)tailProgress;
                float tailAlpha = animationAlpha * (1.0f - tailProgress) * 0.6f;
                if (tailAlpha <= 0.0f) continue;
                int tailColor = this.applyAlpha(baseColor, tailAlpha);
                Vec3d tailPos = new Vec3d(renderPos.x + (double)xOffset, renderPos.y + currentTailY, renderPos.z + (double)zOffset);
                this.drawBillboard(buffer, matrices, camera, tailPos, tailSize, tailColor);
            }
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void drawBillboard(BufferBuilder buffer, MatrixStack matrices, Camera camera, Vec3d pos, float size, int color) {
        if (size <= 0.0f) {
            return;
        }
        matrices.push();
        matrices.translate(pos.x, pos.y, pos.z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float half = size / 2.0f;
        buffer.vertex(matrix, -half, -half, 0.0f).texture(0.0f, 1.0f).color(color);
        buffer.vertex(matrix, half, -half, 0.0f).texture(1.0f, 1.0f).color(color);
        buffer.vertex(matrix, half, half, 0.0f).texture(1.0f, 0.0f).color(color);
        buffer.vertex(matrix, -half, half, 0.0f).texture(0.0f, 0.0f).color(color);
        matrices.pop();
    }

    private void renderGhosts(EventRender3D event, Vec3d center, float alphaValue) {
        int color;
        int baseColor;
        int alpha;
        double c;
        double s;
        double angle;
        int i;
        LivingEntity entityToRender;
        float anim = MathHelper.clamp((float)alphaValue, (float)0.0f, (float)1.0f);
        if (anim <= 0.0f) {
            return;
        }
        LivingEntity entity = entityToRender = this.currentTarget != null ? this.currentTarget : this.lastTarget;
        if (entityToRender == null || !entityToRender.isAlive()) {
            return;
        }
        MatrixStack matrices = event.getMatrix();
        float scaleFactor = this.sizeGhosts.getCurrent() / 9.0f;
        double radius = Math.max(0.45, (double)entityToRender.getWidth() * 0.95) * (double)scaleFactor;
        float speed = 27.0f / Math.max(0.1f, this.speedGhosts.getCurrent());
        float spriteSize = 0.32f * scaleFactor;
        double distance = 15.0;
        int trailLength = Math.max(1, Math.round(this.ghostCount.getCurrent()));
        int maxAlpha = 255;
        int alphaFactor = 15;
        float partialTicks = event.getPartialTicks();
        double ix = MathHelper.lerp((double)partialTicks, (double)entityToRender.prevX, (double)entityToRender.getX());
        double iy = MathHelper.lerp((double)partialTicks, (double)entityToRender.prevY, (double)entityToRender.getY());
        double iz = MathHelper.lerp((double)partialTicks, (double)entityToRender.prevZ, (double)entityToRender.getZ());
        Vec3d camPos = TargetEsp.mc.getEntityRenderDispatcher().camera.getPos();
        Vec3d anchor = new Vec3d(ix, iy + (double)entityToRender.getHeight() * 0.5 + 0.05, iz);
        long now = System.currentTimeMillis();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (Identifier)GLOW_TEX);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask((boolean)false);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        for (i = 0; i < trailLength; ++i) {
            angle = 0.15 * ((double)(now - this.lastTime) - (double)i * distance) / (double)speed;
            s = Math.sin(angle) * radius;
            c = Math.cos(angle) * radius;
            alpha = TargetEsp.clamp255((float)MathHelper.clamp((int)(maxAlpha - i * alphaFactor), (int)0, (int)maxAlpha) * anim);
            if (alpha <= 0) continue;
            baseColor = this.getThemeColorAngle(i * 360 / Math.max(1, trailLength), now);
            color = alpha << 24 | baseColor & 0xFFFFFF;
            this.drawGhost(buffer, matrices, camPos, anchor.add(s, c, -c), spriteSize, color);
        }
        for (i = 0; i < trailLength; ++i) {
            angle = 0.15 * ((double)(now - this.lastTime) - (double)i * distance) / (double)speed;
            s = Math.sin(angle) * radius;
            c = Math.cos(angle) * radius;
            alpha = TargetEsp.clamp255((float)MathHelper.clamp((int)(maxAlpha - i * alphaFactor), (int)0, (int)maxAlpha) * anim);
            if (alpha <= 0) continue;
            baseColor = this.getThemeColorAngle(i * 360 / Math.max(1, trailLength), now);
            color = alpha << 24 | baseColor & 0xFFFFFF;
            this.drawGhost(buffer, matrices, camPos, anchor.add(-s, s, -c), spriteSize, color);
        }
        for (i = 0; i < trailLength; ++i) {
            angle = 0.15 * ((double)(now - this.lastTime) - (double)i * distance) / (double)speed;
            s = Math.sin(angle) * radius;
            c = Math.cos(angle) * radius;
            alpha = TargetEsp.clamp255((float)MathHelper.clamp((int)(maxAlpha - i * alphaFactor), (int)0, (int)maxAlpha) * anim);
            if (alpha <= 0) continue;
            baseColor = this.getThemeColorAngle(i * 360 / Math.max(1, trailLength), now);
            color = alpha << 24 | baseColor & 0xFFFFFF;
            this.drawGhost(buffer, matrices, camPos, anchor.add(-s, -s, c), spriteSize, color);
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthMask((boolean)true);
    }

    private void renderSouls(EventRender3D event, Vec3d center, float alphaValue) {
        LivingEntity entityToRender;
        float anim = MathHelper.clamp((float)alphaValue, (float)0.0f, (float)1.0f);
        if (anim <= 0.0f) {
            return;
        }
        LivingEntity entity = entityToRender = this.currentTarget != null ? this.currentTarget : this.lastTarget;
        if (entityToRender == null || !entityToRender.isAlive()) {
            return;
        }
        MatrixStack matrices = event.getMatrix();
        Vec3d camPos = TargetEsp.mc.getEntityRenderDispatcher().camera.getPos();
        long now = System.currentTimeMillis();
        float speed = Math.max(0.1f, this.speedGhosts.getCurrent());
        float scaleFactor = this.sizeGhosts.getCurrent() / 9.0f;
        float spriteSize = 0.23f * scaleFactor;
        float orbitRadius = Math.max(0.25f, entityToRender.getWidth() * 0.45f) * scaleFactor;
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (Identifier)GLOW_TEX);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask((boolean)false);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        int index = 0;
        for (PositionEntry entry : this.targetPositionHistory) {
            float age = MathHelper.clamp((float)((float)(now - entry.timestamp) / 1000.0f), (float)0.0f, (float)1.0f);
            float soulAlpha = (1.0f - age) * anim;
            if (soulAlpha <= 0.02f) {
                ++index;
                continue;
            }
            double wave = (double)now / 120.0 * (double)speed + (double)index * 0.75;
            Vec3d pos = entry.position.add(Math.cos(wave) * (double)orbitRadius, Math.sin(wave * 1.7) * 0.08 * (double)scaleFactor, Math.sin(wave) * (double)orbitRadius);
            int baseColor = this.getThemeColorAngleRaw(index * 31, now);
            int color = this.applyAlpha(this.applyDamageFlash(baseColor), soulAlpha);
            float localSize = spriteSize * (1.0f - age * 0.35f);
            this.drawGhost(buffer, matrices, camPos, pos, Math.max(0.04f, localSize), color);
            ++index;
        }
        if (this.targetPositionHistory.isEmpty() && center != null) {
            for (int i = 0; i < 3; ++i) {
                double angle = (double)now / 90.0 * (double)speed + (double)i * 2.0943951023931953;
                Vec3d pos = center.add(Math.cos(angle) * (double)orbitRadius, 0.15 + Math.sin(angle * 1.2) * 0.12, Math.sin(angle) * (double)orbitRadius);
                int baseColor = this.getThemeColorAngleRaw(i * 90, now);
                int color = this.applyAlpha(this.applyDamageFlash(baseColor), anim * 0.75f);
                this.drawGhost(buffer, matrices, camPos, pos, spriteSize, color);
            }
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthMask((boolean)true);
    }

    private void renderCrystals(EventRender3D event, float alphaValue) {
        LivingEntity entityToRender;
        float alpha = alphaValue;
        if (alphaValue <= 0.0f) {
            return;
        }
        LivingEntity entity = entityToRender = this.currentTarget != null ? this.currentTarget : this.lastTarget;
        if (entityToRender != null) {
            float easedAnim = this.easeOutCubic(alphaValue);
            float tickDelta = mc.getRenderTickCounter().getTickDelta(true);
            Camera camera = TargetEsp.mc.getEntityRenderDispatcher().camera;
            Vec3d targetPos = MathUtil.interpolate((Entity)entityToRender);
            Vec3d cameraPos = camera.getPos();
            Vec3d renderPos = targetPos.subtract(cameraPos);
            float crystalScaleFactor = this.sizeCrystals.getCurrent() / 9.0f;
            float time = ((float)TargetEsp.mc.player.age + tickDelta) * 6.0f * Math.max(0.1f, this.speedCrystals.getCurrent());
            float entityHeight = entityToRender.getHeight();
            float entityWidth = entityToRender.getWidth();
            float halfWidth = entityWidth * 0.5f;
            MatrixStack matrices = event.getMatrix();
            matrices.push();
            matrices.translate(renderPos.x, renderPos.y, renderPos.z);
            RenderSystem.enableBlend();
            RenderSystem.disableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask((boolean)false);
            int crystalCount = 14;
            long now = System.currentTimeMillis();
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
            RenderSystem.setShaderTexture((int)0, (Identifier)GLOW_TEX);
            RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
            BufferBuilder glowBuffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            for (int i = 0; i < crystalCount; ++i) {
                float seed1 = (float)Math.sin((float)i * 1.7f + 0.3f) * 0.5f + 0.5f;
                float seed2 = (float)Math.cos((float)i * 2.3f + 0.7f) * 0.5f + 0.5f;
                float seed3 = (float)Math.sin((float)i * 3.1f + 1.1f) * 0.5f + 0.5f;
                float angleOffset = (float)i * (360.0f / (float)crystalCount) + seed1 * 12.0f;
                float angle = time + angleOffset;
                float radius = (halfWidth + 0.25f + seed3 * 0.15f) * crystalScaleFactor;
                float x = radius * (float)Math.cos(Math.toRadians(angle));
                float z = radius * (float)Math.sin(Math.toRadians(angle));
                float y = seed2 * entityHeight;
                float crystalScale = 0.18f * easedAnim * crystalScaleFactor;
                int color = this.getThemeColorAngle(i * 26, now);
                this.drawCrystalB(glowBuffer, matrices, camera, x, y, z, crystalScale * 3.6f, color, alpha * 0.3f);
            }
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)glowBuffer.end());
            RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
            for (int i = 0; i < crystalCount; ++i) {
                float seed1 = (float)Math.sin((float)i * 1.7f + 0.3f) * 0.5f + 0.5f;
                float seed2 = (float)Math.cos((float)i * 2.3f + 0.7f) * 0.5f + 0.5f;
                float seed3 = (float)Math.sin((float)i * 3.1f + 1.1f) * 0.5f + 0.5f;
                float angleOffset = (float)i * (360.0f / (float)crystalCount) + seed1 * 12.0f;
                float angle = time + angleOffset;
                float radius = (halfWidth + 0.25f + seed3 * 0.15f) * crystalScaleFactor;
                float x = radius * (float)Math.cos(Math.toRadians(angle));
                float z = radius * (float)Math.sin(Math.toRadians(angle));
                float y = seed2 * entityHeight;
                float crystalScale = 0.18f * easedAnim * crystalScaleFactor;
                int color = this.getThemeColorAngle(i * 26, now);
                this.drawCrystalH(buffer, matrices, x, y, z, crystalScale, angle, color, alpha * 0.7f);
            }
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
            matrices.pop();
            RenderSystem.enableCull();
            RenderSystem.depthMask((boolean)true);
            RenderSystem.enableDepthTest();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        }
    }

    private void renderCrystalsBlumeStyle(EventRender3D event, float alphaValue) {
        LivingEntity entityToRender;
        float anim = MathHelper.clamp((float)alphaValue, (float)0.0f, (float)1.0f);
        if (anim <= 0.0f) {
            return;
        }
        LivingEntity entity = entityToRender = this.currentTarget != null ? this.currentTarget : this.lastTarget;
        if (entityToRender == null || !entityToRender.isAlive()) {
            return;
        }
        MatrixStack matrices = event.getMatrix();
        float tickDelta = event.getPartialTicks();
        Camera camera = TargetEsp.mc.getEntityRenderDispatcher().camera;
        Vec3d targetPos = new Vec3d(MathHelper.lerp((double)tickDelta, (double)entityToRender.prevX, (double)entityToRender.getX()), MathHelper.lerp((double)tickDelta, (double)entityToRender.prevY, (double)entityToRender.getY()), MathHelper.lerp((double)tickDelta, (double)entityToRender.prevZ, (double)entityToRender.getZ()));
        Vec3d renderPos = targetPos.subtract(camera.getPos());
        float scaleFactor = this.sizeCrystals.getCurrent() / 9.0f;
        float speed = Math.max(0.1f, this.speedCrystals.getCurrent());
        float height = entityToRender.getHeight();
        float halfWidth = entityToRender.getWidth() * 0.5f;
        long now = System.currentTimeMillis();
        matrices.push();
        matrices.translate(renderPos.x, renderPos.y, renderPos.z);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (Identifier)GLOW_TEX);
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
        BufferBuilder glowBuffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        int crystalCount = 24;
        double time = (double)System.nanoTime() / 1.0E9 * (double)speed;
        for (int i = 0; i < crystalCount; ++i) {
            float angle = (float)(time * 90.0 + (double)i * (360.0 / (double)crystalCount));
            float radius = (halfWidth + 0.24f + (float)Math.sin((float)i * 1.9f) * 0.08f) * scaleFactor;
            float x = (float)Math.cos(Math.toRadians(angle)) * radius;
            float z = (float)Math.sin(Math.toRadians(angle)) * radius;
            float y = (float)(i % 8) / 8.0f * height + (float)Math.sin(time + (double)i) * 0.08f * scaleFactor;
            int color = this.getThemeColorAngle(i * 17, now);
            float glowSize = 0.22f * scaleFactor * (0.75f + 0.25f * (float)Math.sin(time * 1.3 + (double)i));
            this.drawCrystalB(glowBuffer, matrices, camera, x, y, z, glowSize * 3.0f, color, anim * 0.35f);
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)glowBuffer.end());
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        BufferBuilder crystalBuffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
        for (int i = 0; i < crystalCount; ++i) {
            float angle = (float)(time * 90.0 + (double)i * (360.0 / (double)crystalCount));
            float radius = (halfWidth + 0.24f + (float)Math.sin((float)i * 1.9f) * 0.08f) * scaleFactor;
            float x = (float)Math.cos(Math.toRadians(angle)) * radius;
            float z = (float)Math.sin(Math.toRadians(angle)) * radius;
            float y = (float)(i % 8) / 8.0f * height + (float)Math.sin(time + (double)i) * 0.08f * scaleFactor;
            float crystalScale = 0.09f * scaleFactor * (0.85f + 0.25f * (float)Math.sin(time * 1.7 + (double)i));
            matrices.push();
            matrices.translate(x, y, z);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-angle));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f + (float)Math.sin(time + (double)i) * 22.0f));
            matrices.scale(crystalScale, crystalScale, crystalScale);
            Matrix4f matrix = matrices.peek().getPositionMatrix();
            int color = this.applyAlpha(this.getThemeColorAngle(i * 17, now), anim * 0.75f);
            this.drawCrystalMesh(crystalBuffer, matrix, color);
            matrices.pop();
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)crystalBuffer.end());
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        matrices.pop();
    }

    private void renderSkeleton(LivingEntity entity, float alphaValue) {
        if (!(entity instanceof PlayerEntity)) {
            return;
        }
        PlayerEntity player = (PlayerEntity)entity;
        Vec3d pos = MathUtil.interpolate((Entity)player);
        float height = player.getHeight();
        float width = player.getWidth();
        float sneakingOffset = player.isSneaking() ? height * 0.12f : 0.0f;
        Vec3d head = pos.add(0.0, (double)(height * 0.9f - sneakingOffset), 0.0);
        Vec3d neck = pos.add(0.0, (double)(height * 0.75f - sneakingOffset), 0.0);
        Vec3d body = pos.add(0.0, (double)(height * 0.55f - sneakingOffset), 0.0);
        Vec3d pelvis = pos.add(0.0, (double)(height * 0.4f - sneakingOffset), 0.0);
        float yawRad = (float)Math.toRadians(-player.getYaw() + 90.0f);
        float swing = (float)Math.sin((double)System.currentTimeMillis() / 350.0 * (double)Math.max(0.1f, this.speedSkeleton.getCurrent()) + (double)player.getId()) * 0.35f;
        float shoulderOffset = width * 0.45f;
        float hipOffset = width * 0.25f;
        float armLength = height * 0.25f;
        float legLength = height * 0.3f;
        Vec3d rightShoulder = neck.add(Math.sin(yawRad) * (double)shoulderOffset, (double)(-height * 0.05f), Math.cos(yawRad) * (double)shoulderOffset);
        Vec3d leftShoulder = neck.add(-Math.sin(yawRad) * (double)shoulderOffset, (double)(-height * 0.05f), -Math.cos(yawRad) * (double)shoulderOffset);
        Vec3d rightElbow = rightShoulder.add(Math.sin((double)yawRad + 1.5707963267948966) * (double)swing * 0.25, (double)(-armLength * 0.5f), Math.cos((double)yawRad + 1.5707963267948966) * (double)swing * 0.25);
        Vec3d rightHand = rightElbow.add(Math.sin((double)yawRad + 1.5707963267948966) * (double)swing * 0.25, (double)(-armLength * 0.5f), Math.cos((double)yawRad + 1.5707963267948966) * (double)swing * 0.25);
        Vec3d leftElbow = leftShoulder.add(-Math.sin((double)yawRad + 1.5707963267948966) * (double)swing * 0.25, (double)(-armLength * 0.5f), -Math.cos((double)yawRad + 1.5707963267948966) * (double)swing * 0.25);
        Vec3d leftHand = leftElbow.add(-Math.sin((double)yawRad + 1.5707963267948966) * (double)swing * 0.25, (double)(-armLength * 0.5f), -Math.cos((double)yawRad + 1.5707963267948966) * (double)swing * 0.25);
        Vec3d rightHip = pelvis.add(Math.sin(yawRad) * (double)hipOffset, 0.0, Math.cos(yawRad) * (double)hipOffset);
        Vec3d leftHip = pelvis.add(-Math.sin(yawRad) * (double)hipOffset, 0.0, -Math.cos(yawRad) * (double)hipOffset);
        Vec3d rightKnee = rightHip.add(Math.sin((double)yawRad + 1.5707963267948966) * (double)swing * (double)0.2f, (double)(-legLength * 0.5f), Math.cos((double)yawRad + 1.5707963267948966) * (double)swing * (double)0.2f);
        Vec3d rightFoot = rightKnee.add(Math.sin((double)yawRad + 1.5707963267948966) * (double)swing * (double)0.2f, (double)(-legLength * 0.5f), Math.cos((double)yawRad + 1.5707963267948966) * (double)swing * (double)0.2f);
        Vec3d leftKnee = leftHip.add(-Math.sin((double)yawRad + 1.5707963267948966) * (double)swing * (double)0.2f, (double)(-legLength * 0.5f), -Math.cos((double)yawRad + 1.5707963267948966) * (double)swing * (double)0.2f);
        Vec3d leftFoot = leftKnee.add(-Math.sin((double)yawRad + 1.5707963267948966) * (double)swing * (double)0.2f, (double)(-legLength * 0.5f), -Math.cos((double)yawRad + 1.5707963267948966) * (double)swing * (double)0.2f);
        ColorRGBA base = this.getPrimaryColor();
        int color = this.applyDamageFlash(base.withAlpha(TargetEsp.clamp255(220.0f * alphaValue)).getRGB());
        float lineWidth = Math.max(0.6f, 1.2f * (this.sizeSkeleton.getCurrent() / 9.0f));
        boolean depth = false;
        Render3DUtil.drawLine(head, neck, color, lineWidth, depth);
        Render3DUtil.drawLine(neck, body, color, lineWidth, depth);
        Render3DUtil.drawLine(body, pelvis, color, lineWidth, depth);
        Render3DUtil.drawLine(rightShoulder, rightElbow, color, lineWidth, depth);
        Render3DUtil.drawLine(rightElbow, rightHand, color, lineWidth, depth);
        Render3DUtil.drawLine(leftShoulder, leftElbow, color, lineWidth, depth);
        Render3DUtil.drawLine(leftElbow, leftHand, color, lineWidth, depth);
        Render3DUtil.drawLine(rightHip, rightKnee, color, lineWidth, depth);
        Render3DUtil.drawLine(rightKnee, rightFoot, color, lineWidth, depth);
        Render3DUtil.drawLine(leftHip, leftKnee, color, lineWidth, depth);
        Render3DUtil.drawLine(leftKnee, leftFoot, color, lineWidth, depth);
        Render3DUtil.drawLine(rightShoulder, leftShoulder, color, lineWidth, depth);
        Render3DUtil.drawLine(rightHip, leftHip, color, lineWidth, depth);
    }

    private void renderChains(EventRender3D event, float alphaValue) {
        LivingEntity entity;
        LivingEntity entity = entity = this.currentTarget != null ? this.currentTarget : this.lastTarget;
        if (entity == null || !entity.isAlive()) {
            return;
        }
        MatrixStack matrices = event.getMatrix();
        float partialTicks = event.getPartialTicks();
        double ix = MathHelper.lerp((double)partialTicks, (double)entity.prevX, (double)entity.getX());
        double iy = MathHelper.lerp((double)partialTicks, (double)entity.prevY, (double)entity.getY());
        double iz = MathHelper.lerp((double)partialTicks, (double)entity.prevZ, (double)entity.getZ());
        Vec3d camPos = TargetEsp.mc.getEntityRenderDispatcher().camera.getPos();
        double entX = ix - camPos.x;
        double entY = iy - camPos.y - 0.5;
        double entZ = iz - camPos.z;
        float movingValue = this.getMovingValue(this.speedChains.getCurrent());
        float gradusX = (float)(20.0 * Math.min(1.0 + Math.sin(Math.toRadians(movingValue)), 1.0));
        float gradusZ = (float)(20.0 * (Math.min(1.0 + Math.sin(Math.toRadians(movingValue)), 2.0) - 1.0));
        float width = entity.getWidth() * 1.5f;
        float hitProgress = EntityDamageTracker.getDamageFlashIntensity(entity, 350L);
        int baseColor = this.getThemeColor(0).getRGB();
        int blendedColor = hitProgress > 0.0f ? this.lerpColor(baseColor, -65536, hitProgress) : baseColor;
        int alphaVal = TargetEsp.clamp255(255.0f * alphaValue);
        int linksStep = 18;
        int totalAngle = 720;
        float chainSize = 4.0f;
        float down = 1.0f;
        RenderSystem.enableBlend();
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (Identifier)CHAIN_TEX);
        RenderSystem.texParameter((int)3553, (int)10242, (int)10497);
        RenderSystem.texParameter((int)3553, (int)10243, (int)10497);
        for (int chain = 0; chain < 2; ++chain) {
            float val = 1.2f - 0.5f * (chain == 0 ? 1.0f : 0.9f);
            matrices.push();
            matrices.translate(entX, entY + (double)(entity.getHeight() / 2.0f), entZ);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(chain == 0 ? gradusX : -gradusX));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(chain == 0 ? gradusZ : -gradusZ));
            Matrix4f matrix = matrices.peek().getPositionMatrix();
            int modif = linksStep / 2;
            int color = alphaVal << 24 | blendedColor & 0xFFFFFF;
            BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            for (int i = 0; i < totalAngle; i += modif) {
                float prevSin = (float)((double)((chain == 0 ? gradusX : -gradusX) / 100.0f) + Math.sin(Math.toRadians((float)(i - modif) + movingValue * 0.5f)) * (double)width * (double)val);
                float prevCos = (float)((double)((chain == 0 ? -gradusZ : gradusZ) / 100.0f) + Math.cos(Math.toRadians((float)(i - modif) + movingValue * 0.5f)) * (double)width * (double)val);
                float sin = (float)((double)((chain == 0 ? gradusX : -gradusX) / 100.0f) + Math.sin(Math.toRadians((float)i + movingValue * 0.5f)) * (double)width * (double)val);
                float cos = (float)((double)((chain == 0 ? -gradusZ : gradusZ) / 100.0f) + Math.cos(Math.toRadians((float)i + movingValue * 0.5f)) * (double)width * (double)val);
                float u0 = 0.0027777778f * (float)(i - modif) * chainSize;
                float u1 = 0.0027777778f * (float)i * chainSize;
                builder.vertex(matrix, prevSin, 0.0f, prevCos).texture(u0, 0.0f).color(color);
                builder.vertex(matrix, sin, 0.0f, cos).texture(u1, 0.0f).color(color);
                builder.vertex(matrix, sin, down, cos).texture(u1, 0.99f).color(color);
                builder.vertex(matrix, prevSin, down, prevCos).texture(u0, 0.99f).color(color);
            }
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)builder.end());
            matrices.pop();
        }
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
    }

    private void renderCircle(EventRender3D event, float alphaValue) {
        LivingEntity entity;
        LivingEntity entity = entity = this.currentTarget != null ? this.currentTarget : this.lastTarget;
        if (entity == null || !entity.isAlive()) {
            return;
        }
        float alphaPC = MathHelper.clamp((float)alphaValue, (float)0.0f, (float)1.0f);
        if (alphaPC <= 0.0f) {
            return;
        }
        Camera camera = TargetEsp.mc.getEntityRenderDispatcher().camera;
        Vec3d vec = MathUtil.interpolate((Entity)entity);
        Vec3d camPos = camera.getPos();
        double x = vec.x - camPos.x;
        double y = vec.y - camPos.y;
        double z = vec.z - camPos.z;
        float width = entity.getWidth() * 1.45f + (1.0f - alphaPC) / 2.5f;
        float baseVal = Math.max(0.5f, 0.8f - 0.1f * this.circleImpactProgress - 0.1f * alphaPC);
        float movingAngle = MathUtil.interpolate(this.circlePrevMoving, this.circleMoving);
        int step = 3;
        float size = 0.4f * this.circleSize.getCurrent();
        float bigSize = (0.5f + this.circleBloomSize.getCurrent()) * this.circleSize.getCurrent();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
        RenderSystem.setShaderTexture((int)0, (Identifier)GLOW_TEX);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        MatrixStack matrixStack = event.getMatrix();
        long now = System.currentTimeMillis();
        for (int i = 0; i < 360; i += step) {
            if ((int)((float)i / 45.0f) % 2 == 0) continue;
            double rad = Math.toRadians((float)i + movingAngle);
            float sin = (float)(x + Math.sin(rad) * (double)width * (double)baseVal);
            float cos = (float)(z + Math.cos(rad) * (double)width * (double)baseVal);
            float interpolatedVerticalTime = MathUtil.interpolate(this.circlePrevVerticalTime, this.circleVerticalTime);
            double radAngle = Math.toRadians(interpolatedVerticalTime);
            float waveValue = (float)((1.0 - Math.cos(radAngle)) / 2.0);
            float yPos = (float)(y + (double)(entity.getHeight() * waveValue));
            matrixStack.push();
            matrixStack.translate(sin, yPos, cos);
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            Matrix4f matrix = matrixStack.peek().getPositionMatrix();
            int alpha = TargetEsp.clamp255(alphaPC * 255.0f);
            int rawColor = this.getThemeColorAngleRaw(i * 3, now);
            int baseColor = alpha << 24 | rawColor & 0xFFFFFF;
            if (this.circleRedOnImpact.isEnabled() && this.circleImpactProgress > 0.0f) {
                int impactColor = alpha << 24 | 0xFF2020;
                baseColor = this.lerpColor(baseColor, impactColor, MathHelper.clamp((float)this.circleImpactProgress, (float)0.0f, (float)1.0f));
            }
            int finalColor = this.applyDamageFlash(baseColor);
            if (this.circleBloom.isEnabled()) {
                int bloomAlpha = TargetEsp.clamp255(alphaPC * 255.0f * 0.1f);
                int bloomColor = bloomAlpha << 24 | finalColor & 0xFFFFFF;
                BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                buffer.vertex(matrix, -bigSize / 2.0f, bigSize / 2.0f, -size / 2.0f).texture(0.0f, 1.0f).color(bloomColor);
                buffer.vertex(matrix, bigSize / 2.0f, bigSize / 2.0f, -size / 2.0f).texture(1.0f, 1.0f).color(bloomColor);
                buffer.vertex(matrix, bigSize / 2.0f, -bigSize / 2.0f, -size / 2.0f).texture(1.0f, 0.0f).color(bloomColor);
                buffer.vertex(matrix, -bigSize / 2.0f, -bigSize / 2.0f, -size / 2.0f).texture(0.0f, 0.0f).color(bloomColor);
                BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
            }
            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            buffer.vertex(matrix, -size / 2.0f, size / 2.0f, -size / 2.0f).texture(0.0f, 1.0f).color(finalColor);
            buffer.vertex(matrix, size / 2.0f, size / 2.0f, -size / 2.0f).texture(1.0f, 1.0f).color(finalColor);
            buffer.vertex(matrix, size / 2.0f, -size / 2.0f, -size / 2.0f).texture(1.0f, 0.0f).color(finalColor);
            buffer.vertex(matrix, -size / 2.0f, -size / 2.0f, -size / 2.0f).texture(0.0f, 0.0f).color(finalColor);
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
            matrixStack.pop();
        }
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    private void renderGarland(EventRender3D event, float alphaValue) {
        LivingEntity entityToRender;
        float anim = MathHelper.clamp((float)alphaValue, (float)0.0f, (float)1.0f);
        if (anim <= 0.0f) {
            return;
        }
        LivingEntity entity = entityToRender = this.currentTarget != null ? this.currentTarget : this.lastTarget;
        if (entityToRender == null || !entityToRender.isAlive()) {
            return;
        }
        MatrixStack ms = event.getMatrix();
        Camera camera = TargetEsp.mc.getEntityRenderDispatcher().camera;
        Vec3d targetPos = MathUtil.interpolate((Entity)entityToRender);
        double renderX = targetPos.x - camera.getPos().x;
        double renderY = targetPos.y - camera.getPos().y;
        double renderZ = targetPos.z - camera.getPos().z;
        float height = entityToRender.getHeight();
        float width = entityToRender.getWidth();
        float garlandScale = this.sizeGarland.getCurrent() / 9.0f;
        float radius = width * 1.2f * garlandScale;
        float time = (float)System.currentTimeMillis() * Math.max(0.1f, this.speedGarland.getCurrent()) % 4000.0f / 4000.0f;
        float offset = time * 360.0f;
        int lightsCount = 30;
        int spirals = 3;
        int wireColor = -16041205;
        ms.push();
        ms.translate(renderX, renderY, renderZ);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        for (int i = 0; i <= lightsCount; ++i) {
            float progress = (float)i / (float)lightsCount;
            float angle = (float)Math.toRadians(offset + progress * 360.0f * (float)spirals);
            float currentRadius = radius * (1.0f - progress * 0.6f);
            float x = (float)Math.cos(angle) * currentRadius;
            float z = (float)Math.sin(angle) * currentRadius;
            float y = progress * height;
            int color = ColorUtil.multAlpha(wireColor, anim);
            buffer.vertex(ms.peek().getPositionMatrix(), x, y, z).color(color);
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (Identifier)GLOW_TEX);
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE);
        BufferBuilder bulbBuffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        float pitch = camera.getPitch();
        float yaw = camera.getYaw();
        float baseSize = 0.15f * this.easeOutCubic(anim) * Math.max(0.35f, garlandScale);
        for (int i = 0; i <= lightsCount; ++i) {
            float progress = (float)i / (float)lightsCount;
            float angle = (float)Math.toRadians(offset + progress * 360.0f * (float)spirals);
            float currentRadius = radius * (1.0f - progress * 0.6f);
            float x = (float)Math.cos(angle) * currentRadius;
            float z = (float)Math.sin(angle) * currentRadius;
            float y = progress * height;
            float twinkle = (float)Math.sin((double)System.currentTimeMillis() / 100.0 * (double)Math.max(0.1f, this.speedGarland.getCurrent()) + (double)i) * 0.2f + 0.8f;
            float localAlpha = MathHelper.clamp((float)(anim * twinkle), (float)0.0f, (float)1.0f);
            int color = this.getFestiveColor(i);
            int finalColor = ColorUtil.multAlpha(color, localAlpha);
            this.drawBillboard(bulbBuffer, ms, x, y, z, baseSize, yaw, pitch, finalColor);
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)bulbBuffer.end());
        RenderSystem.depthMask((boolean)true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        ms.pop();
    }

    private void drawBillboard(BufferBuilder buffer, MatrixStack ms, float x, float y, float z, float scale, float yaw, float pitch, int color) {
        ms.push();
        ms.translate(x, y, z);
        ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw));
        ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch));
        Matrix4f matrix = ms.peek().getPositionMatrix();
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        int a = color >> 24 & 0xFF;
        buffer.vertex(matrix, -scale, -scale, 0.0f).texture(0.0f, 0.0f).color(r, g, b, a);
        buffer.vertex(matrix, -scale, scale, 0.0f).texture(0.0f, 1.0f).color(r, g, b, a);
        buffer.vertex(matrix, scale, scale, 0.0f).texture(1.0f, 1.0f).color(r, g, b, a);
        buffer.vertex(matrix, scale, -scale, 0.0f).texture(1.0f, 0.0f).color(r, g, b, a);
        ms.pop();
    }

    private int getFestiveColor(int index) {
        return switch (index % 4) {
            case 0 -> -65536;
            case 1 -> -10496;
            case 2 -> -16711936;
            case 3 -> -16728065;
            default -> -1;
        };
    }

    private float getMovingValue(float speedMul) {
        double speed = Math.max(0.1f, speedMul);
        double time = (double)System.currentTimeMillis() * speed;
        return (float)(time % 3600.0 * 0.1);
    }

    private float easeOutCubic(float value) {
        float t = MathHelper.clamp((float)value, (float)0.0f, (float)1.0f);
        float inv = 1.0f - t;
        return 1.0f - inv * inv * inv;
    }

    private int applyAlpha(int color, float alpha) {
        int a = TargetEsp.clamp255(255.0f * MathHelper.clamp((float)alpha, (float)0.0f, (float)1.0f));
        return a << 24 | color & 0xFFFFFF;
    }

    private int lerpColor(int from, int to, float progress) {
        float t = MathHelper.clamp((float)progress, (float)0.0f, (float)1.0f);
        int a1 = from >> 24 & 0xFF;
        int r1 = from >> 16 & 0xFF;
        int g1 = from >> 8 & 0xFF;
        int b1 = from & 0xFF;
        int a2 = to >> 24 & 0xFF;
        int r2 = to >> 16 & 0xFF;
        int g2 = to >> 8 & 0xFF;
        int b2 = to & 0xFF;
        int a = MathHelper.lerp((float)t, (int)a1, (int)a2);
        int r = MathHelper.lerp((float)t, (int)r1, (int)r2);
        int g = MathHelper.lerp((float)t, (int)g1, (int)g2);
        int b = MathHelper.lerp((float)t, (int)b1, (int)b2);
        return a << 24 | r << 16 | g << 8 | b;
    }

    private void updateDamageFlashState(LivingEntity target) {
        if (!this.damageRed.isEnabled() || target == null || !target.isAlive()) {
            this.damageFlashIntensity = 0.0f;
            return;
        }
        this.damageFlashIntensity = EntityDamageTracker.getDamageFlashIntensity(target, 350L);
    }

    private int applyDamageFlash(int color) {
        if (!this.damageRed.isEnabled() || this.damageFlashIntensity < 0.05f) {
            return color;
        }
        int alpha = color >> 24 & 0xFF;
        int red = color >> 16 & 0xFF;
        int green = color >> 8 & 0xFF;
        int blue = color & 0xFF;
        int finalRed = MathHelper.lerp((float)this.damageFlashIntensity, (int)red, (int)255);
        int finalGreen = MathHelper.lerp((float)this.damageFlashIntensity, (int)green, (int)50);
        int finalBlue = MathHelper.lerp((float)this.damageFlashIntensity, (int)blue, (int)50);
        return alpha << 24 | finalRed << 16 | finalGreen << 8 | finalBlue;
    }

    private void updateCircleState(LivingEntity target) {
        if (target == null) {
            this.resetCircleImpact();
            return;
        }
        if (target != this.circleImpactTarget) {
            this.circleImpactTarget = target;
            this.circleImpactProgress = 0.0f;
            this.circlePrevHurtTime = 0;
        }
        this.circlePrevMoving = this.circleMoving;
        this.circleMoving += this.circleSpeed.getCurrent();
        this.circlePrevVerticalTime = this.circleVerticalTime;
        this.circleVerticalTime += this.circleSpeed.getCurrent();
        this.updateCircleImpact(target);
    }

    private void updateCircleImpact(LivingEntity target) {
        if (!this.circleRedOnImpact.isEnabled() || target == null) {
            this.circleImpactProgress = 0.0f;
            this.circlePrevHurtTime = 0;
            return;
        }
        float fadeInSpeed = this.circleImpactFadeIn.getCurrent();
        float fadeOutSpeed = this.circleImpactFadeOut.getCurrent();
        float maxIntensity = this.circleImpactIntensity.getCurrent();
        long damageAge = EntityDamageTracker.getDamageAge(target);
        boolean justDamaged = damageAge <= 75L;
        boolean recentlyDamaged = damageAge <= 350L;
        int currentHurtTime = target.hurtTime;
        this.circleImpactProgress = justDamaged ? Math.min(maxIntensity, this.circleImpactProgress + fadeInSpeed) : (recentlyDamaged || currentHurtTime > 0 ? Math.min(maxIntensity, this.circleImpactProgress + fadeInSpeed * 0.5f) : Math.max(0.0f, this.circleImpactProgress - fadeOutSpeed));
        this.circlePrevHurtTime = currentHurtTime;
    }

    private void resetCircleImpact() {
        this.circleImpactProgress = 0.0f;
        this.circlePrevHurtTime = 0;
        this.circleImpactTarget = null;
    }

    private int getThemeColorAngle(int offsetAngle, long currentTime) {
        if (this.randomColors.isEnabled() || this.customTheme.isEnabled()) {
            return this.applyDamageFlash(this.getAnimatedPairColor(offsetAngle, currentTime).getRGB());
        }
        float timeFactor = (float)(currentTime % 3000L) / 3000.0f;
        int colorAngle = (int)(timeFactor * 360.0f) + offsetAngle;
        int color = this.themeManager.getClientColor(colorAngle % 360).getRGB();
        return this.applyDamageFlash(color);
    }

    private int getThemeColorAngleRaw(int offsetAngle, long currentTime) {
        if (this.randomColors.isEnabled() || this.customTheme.isEnabled()) {
            return this.getAnimatedPairColor(offsetAngle, currentTime).getRGB();
        }
        float timeFactor = (float)(currentTime % 3000L) / 3000.0f;
        int colorAngle = (int)(timeFactor * 360.0f) + offsetAngle;
        return this.themeManager.getClientColor(colorAngle % 360).getRGB();
    }

    private double absSinAnimation(double value) {
        return Math.abs(Math.sin(value));
    }

    private void drawCrystalB(BufferBuilder buffer, MatrixStack matrices, Camera camera, float x, float y, float z, float size, int color, float alpha) {
        if (alpha <= 0.0f || size <= 0.0f) {
            return;
        }
        int finalColor = this.applyAlpha(color, alpha);
        this.drawBillboard(buffer, matrices, camera, new Vec3d((double)x, (double)y, (double)z), size, finalColor);
    }

    private void drawCrystalH(BufferBuilder buffer, MatrixStack matrices, float x, float y, float z, float scale, float yaw, int color, float alpha) {
        if (alpha <= 0.0f || scale <= 0.0f) {
            return;
        }
        matrices.push();
        matrices.translate(x, y, z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw + 90.0f));
        matrices.scale(scale, scale, scale);
        MatrixStack.Entry entry = matrices.peek();
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        int a = (int)(180.0f * alpha);
        int rL = Math.min(255, (int)((float)r * 1.3f));
        int gL = Math.min(255, (int)((float)g * 1.3f));
        int bL = Math.min(255, (int)((float)b * 1.3f));
        int rD = (int)((float)r * 0.6f);
        int gD = (int)((float)g * 0.6f);
        int bD = (int)((float)b * 0.6f);
        float w = 0.5f;
        float h = 1.0f;
        this.drawTriangle(buffer, entry, 0.0f, 0.0f, h, -w, 0.0f, 0.0f, 0.0f, w, 0.0f, rL, gL, bL, a);
        this.drawTriangle(buffer, entry, 0.0f, 0.0f, h, 0.0f, w, 0.0f, w, 0.0f, 0.0f, rL, gL, bL, a);
        this.drawTriangle(buffer, entry, 0.0f, 0.0f, h, w, 0.0f, 0.0f, 0.0f, -w, 0.0f, r, g, b, a);
        this.drawTriangle(buffer, entry, 0.0f, 0.0f, h, 0.0f, -w, 0.0f, -w, 0.0f, 0.0f, r, g, b, a);
        this.drawTriangle(buffer, entry, 0.0f, 0.0f, -h, 0.0f, w, 0.0f, -w, 0.0f, 0.0f, rD, gD, bD, a);
        this.drawTriangle(buffer, entry, 0.0f, 0.0f, -h, w, 0.0f, 0.0f, 0.0f, w, 0.0f, rD, gD, bD, a);
        this.drawTriangle(buffer, entry, 0.0f, 0.0f, -h, 0.0f, -w, 0.0f, w, 0.0f, 0.0f, rD, gD, bD, a);
        this.drawTriangle(buffer, entry, 0.0f, 0.0f, -h, -w, 0.0f, 0.0f, 0.0f, -w, 0.0f, rD, gD, bD, a);
        matrices.pop();
    }

    private void drawTriangle(BufferBuilder buffer, MatrixStack.Entry entry, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, int r, int g, int b, int a) {
        Matrix4f matrix = entry.getPositionMatrix();
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a);
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a);
        buffer.vertex(matrix, x3, y3, z3).color(r, g, b, a);
    }

    private void addTriangle(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, int color) {
        buffer.vertex(matrix, x1, y1, z1).color(color);
        buffer.vertex(matrix, x2, y2, z2).color(color);
        buffer.vertex(matrix, x3, y3, z3).color(color);
    }

    private void drawCrystalMesh(BufferBuilder buffer, Matrix4f matrix, int color) {
        for (int[] face : BLUME_CRYSTAL_FACES) {
            Vector3f v1 = BLUME_CRYSTAL_VERTICES[face[0]];
            Vector3f v2 = BLUME_CRYSTAL_VERTICES[face[1]];
            Vector3f v3 = BLUME_CRYSTAL_VERTICES[face[2]];
            this.addTriangle(buffer, matrix, v1.x, v1.y, v1.z, v2.x, v2.y, v2.z, v3.x, v3.y, v3.z, color);
        }
    }

    private void updateTargetPositionHistory(LivingEntity target) {
        if (target == null || !target.isAlive()) {
            if (!this.targetPositionHistory.isEmpty()) {
                this.targetPositionHistory.clear();
            }
            return;
        }
        long now = System.currentTimeMillis();
        Vec3d currentPos = MathUtil.interpolate((Entity)target).add(0.0, (double)target.getHeight() * 0.55, 0.0);
        if (this.targetPositionHistory.isEmpty() || currentPos.squaredDistanceTo(this.targetPositionHistory.getFirst().position) > 0.0025) {
            this.targetPositionHistory.addFirst(new PositionEntry(currentPos));
        }
        this.targetPositionHistory.removeIf(entry -> now - entry.timestamp > 1000L);
        while (this.targetPositionHistory.size() > 160) {
            this.targetPositionHistory.remove(this.targetPositionHistory.size() - 1);
        }
    }

    private void drawGhost(BufferBuilder buffer, MatrixStack matrices, Vec3d camPos, Vec3d pos, float size, int color) {
        matrices.push();
        matrices.translate(pos.x - camPos.x, pos.y - camPos.y, pos.z - camPos.z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-TargetEsp.mc.getEntityRenderDispatcher().camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(TargetEsp.mc.getEntityRenderDispatcher().camera.getPitch()));
        matrices.scale(-size, -size, size);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        buffer.vertex(matrix, -0.5f, -0.5f, 0.0f).texture(0.0f, 1.0f).color(color);
        buffer.vertex(matrix, 0.5f, -0.5f, 0.0f).texture(1.0f, 1.0f).color(color);
        buffer.vertex(matrix, 0.5f, 0.5f, 0.0f).texture(1.0f, 0.0f).color(color);
        buffer.vertex(matrix, -0.5f, 0.5f, 0.0f).texture(0.0f, 0.0f).color(color);
        matrices.pop();
    }

    private LivingEntity findCrosshairTarget() {
        PlayerEntity player;
        if (TargetEsp.mc.crosshairTarget == null || TargetEsp.mc.crosshairTarget.getType() != HitResult.Type.ENTITY) {
            return null;
        }
        EntityHitResult hit = (EntityHitResult)TargetEsp.mc.crosshairTarget;
        Entity entity = hit.getEntity();
        if (!(entity instanceof PlayerEntity) || !(player = (PlayerEntity)entity).isAlive()) {
            return null;
        }
        if (this.isInvisibleTarget((LivingEntity)player)) {
            return null;
        }
        Vec3d center = this.getEntityCenter((LivingEntity)player);
        if (this.isOccludedForCurrentMode(TargetEsp.mc.player.getCameraPosVec(1.0f), center)) {
            return null;
        }
        return player;
    }

    private boolean hasVisionBlockingEffect() {
        if (TargetEsp.mc.player == null) {
            return false;
        }
        if (FullBright.INSTANCE.isEnabled()) {
            return false;
        }
        if (TargetEsp.mc.player.hasStatusEffect(StatusEffects.DARKNESS)) {
            return false;
        }
        return TargetEsp.mc.player.hasStatusEffect(StatusEffects.BLINDNESS);
    }

    private Vec3d getEntityCenter(LivingEntity ent) {
        Vec3d pos = MathUtil.interpolate((Entity)ent);
        return pos.add(0.0, (double)ent.getHeight() * 0.5, 0.0);
    }

    private Vec3d getRenderCenter() {
        if (this.currentTarget != null && this.currentTarget.isAlive()) {
            this.lastKnownHeight = this.currentTarget.getHeight();
            this.lastKnownWidth = this.currentTarget.getWidth();
            this.lastKnownCenter = this.getEntityCenter(this.currentTarget);
            return this.lastKnownCenter;
        }
        return this.lastKnownCenter;
    }

    private boolean isInvisibleTarget(LivingEntity entity) {
        if (!(entity instanceof PlayerEntity)) {
            return false;
        }
        PlayerEntity player = (PlayerEntity)entity;
        return player.isInvisible() || player.hasStatusEffect(StatusEffects.INVISIBILITY);
    }

    private boolean isOccluded(Vec3d from, Vec3d to) {
        BlockHitResult hit = TargetEsp.mc.world.raycast(new RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)TargetEsp.mc.player));
        return hit.getType() != HitResult.Type.MISS;
    }

    private boolean isTextureMode() {
        return this.type.is(this.modeRhombus) || this.type.is(this.modeVex) || this.type.is(this.modeMarker) || this.type.is(this.modeSuperRhombus) || this.type.is(this.modeFlow) || this.type.is(this.modeMarkerV2) || this.type.is(this.modeVertushka);
    }

    private boolean isOccludedForCurrentMode(Vec3d from, Vec3d to) {
        if (LunarCompat.isLunar() && this.isTextureMode()) {
            boolean needUpdate;
            long now = System.currentTimeMillis();
            boolean bl = needUpdate = now - this.lunarOcclusionUpdatedAt >= 80L;
            if (!needUpdate && this.lunarOcclusionFrom != null && this.lunarOcclusionTo != null) {
                needUpdate = this.lunarOcclusionFrom.squaredDistanceTo(from) > 0.01 || this.lunarOcclusionTo.squaredDistanceTo(to) > 0.01;
            } else if (!needUpdate) {
                needUpdate = true;
            }
            if (needUpdate) {
                this.lunarOcclusionValue = this.isOccluded(from, to);
                this.lunarOcclusionFrom = from;
                this.lunarOcclusionTo = to;
                this.lunarOcclusionUpdatedAt = now;
            }
            return this.lunarOcclusionValue;
        }
        return this.isOccluded(from, to);
    }

    private boolean isOccludedByPlayer(Vec3d from, Vec3d to) {
        for (PlayerEntity player : TargetEsp.mc.world.getPlayers()) {
            if (player == TargetEsp.mc.player || !player.isAlive() || player.isSpectator() || !player.getBoundingBox().raycast(from, to).isPresent()) continue;
            return true;
        }
        return false;
    }

    private static int clamp255(double value) {
        if (value < 0.0) {
            return 0;
        }
        if (value > 255.0) {
            return 255;
        }
        return (int)Math.round(value);
    }

    private ColorRGBA getPrimaryColor() {
        return this.getPrimaryColor(System.currentTimeMillis());
    }

    private ColorRGBA getPrimaryColor(long currentTime) {
        if (this.randomColors.isEnabled()) {
            return ColorRGBA.lerp(this.randomPrimaryColor, this.randomNextPrimaryColor, this.getRandomColorBlend(currentTime));
        }
        return this.customTheme.isEnabled() ? this.primaryColor.getColor() : this.themeManager.getCurrentTheme().getColor();
    }

    private ColorRGBA getSecondaryColor() {
        return this.getSecondaryColor(System.currentTimeMillis());
    }

    private ColorRGBA getSecondaryColor(long currentTime) {
        if (this.randomColors.isEnabled()) {
            return ColorRGBA.lerp(this.randomSecondaryColor, this.randomNextSecondaryColor, this.getRandomColorBlend(currentTime));
        }
        return this.customTheme.isEnabled() ? this.secondaryColor.getColor() : this.themeManager.getCurrentTheme().getSecondColor();
    }

    private ColorRGBA getThemeColor(int index) {
        if (this.randomColors.isEnabled() || this.customTheme.isEnabled()) {
            return this.getPrimaryColor();
        }
        return this.themeManager.getCurrentTheme().getColor();
    }

    private Gradient getTextureGradient(float alphaValue, long now) {
        int alpha = TargetEsp.clamp255(255.0f * alphaValue);
        ColorRGBA left = new ColorRGBA(this.applyDamageFlash(this.getAnimatedPairColor(0, now).withAlpha(alpha).getRGB()));
        ColorRGBA right = new ColorRGBA(this.applyDamageFlash(this.getAnimatedPairColor(180, now).withAlpha(alpha).getRGB()));
        return Gradient.of(left, left, right, right);
    }

    private ColorRGBA getAnimatedPairColor(int offsetAngle, long currentTime) {
        float timeFactor = (float)(currentTime % 3000L) / 3000.0f;
        float angle = (float)Math.toRadians(timeFactor * 360.0f + (float)offsetAngle);
        float blend = (float)((Math.sin(angle) + 1.0) * 0.5);
        return ColorRGBA.lerp(this.getPrimaryColor(currentTime), this.getSecondaryColor(currentTime), blend);
    }

    private void refreshRandomColors(LivingEntity target) {
        if (!this.randomColors.isEnabled()) {
            this.resetRandomColorCycle();
            return;
        }
        if (target == null) {
            return;
        }
        long now = System.currentTimeMillis();
        UUID uuid = target.getUuid();
        if (!uuid.equals(this.randomColorsTargetUuid) || this.randomColorTransitionStartedAt <= 0L) {
            this.initializeRandomColorCycle(uuid, now);
            return;
        }
        while (now - this.randomColorTransitionStartedAt >= 500L) {
            this.randomPrimaryColor = this.randomNextPrimaryColor;
            this.randomSecondaryColor = this.randomNextSecondaryColor;
            this.randomNextPrimaryColor = this.createDistinctRandomEspColor(this.randomPrimaryColor);
            this.randomNextSecondaryColor = this.createDistinctRandomEspColor(this.randomNextPrimaryColor);
            this.randomColorTransitionStartedAt += 500L;
        }
    }

    private float getRandomColorBlend(long currentTime) {
        if (this.randomColorTransitionStartedAt <= 0L) {
            return 0.0f;
        }
        return MathHelper.clamp((float)((float)(currentTime - this.randomColorTransitionStartedAt) / 500.0f), (float)0.0f, (float)1.0f);
    }

    private void initializeRandomColorCycle(UUID targetUuid, long now) {
        this.randomColorsTargetUuid = targetUuid;
        this.randomPrimaryColor = this.createRandomEspColor();
        this.randomSecondaryColor = this.createDistinctRandomEspColor(this.randomPrimaryColor);
        this.randomNextPrimaryColor = this.createDistinctRandomEspColor(this.randomPrimaryColor);
        this.randomNextSecondaryColor = this.createDistinctRandomEspColor(this.randomNextPrimaryColor);
        this.randomColorTransitionStartedAt = now;
    }

    private void resetRandomColorCycle() {
        this.randomColorsTargetUuid = null;
        this.randomPrimaryColor = Theme.DARK.getColor();
        this.randomSecondaryColor = Theme.DARK.getSecondColor();
        this.randomNextPrimaryColor = this.randomPrimaryColor;
        this.randomNextSecondaryColor = this.randomSecondaryColor;
        this.randomColorTransitionStartedAt = 0L;
    }

    private ColorRGBA createRandomEspColor() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        float hue = random.nextFloat();
        float saturation = random.nextFloat(0.82f, 1.0f);
        float brightness = random.nextFloat(0.92f, 1.0f);
        return ColorRGBA.fromHSB(hue, saturation, brightness).withAlpha(255);
    }

    private ColorRGBA createDistinctRandomEspColor(ColorRGBA base) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        float hueShift = random.nextFloat(0.28f, 0.55f);
        float shiftedHue = (base.getHue() + hueShift) % 1.0f;
        float saturation = MathHelper.clamp((float)(base.getSaturation() + random.nextFloat(-0.05f, 0.1f)), (float)0.82f, (float)1.0f);
        float brightness = MathHelper.clamp((float)(base.getBrightness() + random.nextFloat(-0.03f, 0.06f)), (float)0.92f, (float)1.0f);
        ColorRGBA candidate = ColorRGBA.fromHSB(shiftedHue, saturation, brightness).withAlpha(255);
        for (int i = 0; i < 6; ++i) {
            if (candidate.difference(base) >= 0.45f) {
                return candidate;
            }
            candidate = this.createRandomEspColor();
        }
        return candidate;
    }

    private float getTextureModeSize() {
        if (this.type.is(this.modeVex)) {
            return this.sizeVex.getCurrent();
        }
        if (this.type.is(this.modeMarker)) {
            return this.sizeMarker.getCurrent();
        }
        if (this.type.is(this.modeSuperRhombus)) {
            return this.sizeSuperRhombus.getCurrent();
        }
        if (this.type.is(this.modeFlow)) {
            return this.sizeFlow.getCurrent();
        }
        if (this.type.is(this.modeMarkerV2)) {
            return this.sizeMarkerV2.getCurrent();
        }
        if (this.type.is(this.modeVertushka)) {
            return this.sizeVertushka.getCurrent();
        }
        return this.sizeRhombus.getCurrent();
    }

    private float getTextureModeSpeed() {
        if (this.type.is(this.modeVex)) {
            return Math.max(0.1f, this.speedVex.getCurrent());
        }
        if (this.type.is(this.modeMarker)) {
            return Math.max(0.1f, this.speedMarker.getCurrent());
        }
        if (this.type.is(this.modeSuperRhombus)) {
            return Math.max(0.1f, this.speedSuperRhombus.getCurrent());
        }
        if (this.type.is(this.modeFlow)) {
            return Math.max(0.1f, this.speedFlow.getCurrent());
        }
        if (this.type.is(this.modeMarkerV2)) {
            return Math.max(0.1f, this.speedMarkerV2.getCurrent());
        }
        if (this.type.is(this.modeVertushka)) {
            return Math.max(0.1f, this.speedVertushka.getCurrent());
        }
        return Math.max(0.1f, this.speedRhombus.getCurrent());
    }

    private Identifier getTextureModeTexture() {
        if (this.type.is(this.modeVex)) {
            return VEX_TEX;
        }
        if (this.type.is(this.modeMarker)) {
            return MARKER_TEX;
        }
        if (this.type.is(this.modeSuperRhombus)) {
            return SUPER_RHOMBUS_TEX;
        }
        if (this.type.is(this.modeFlow)) {
            return FLOW_TEX;
        }
        if (this.type.is(this.modeMarkerV2)) {
            return MARKER_V2_TEX;
        }
        if (this.type.is(this.modeVertushka)) {
            return VERTUSHKA_TEX;
        }
        return RHOMBUS_TEX;
    }

    private double getScale(Vec3d position, double size) {
        Vec3d cam = TargetEsp.mc.getEntityRenderDispatcher().camera.getPos();
        double distance = Math.max(1.75, cam.distanceTo(position));
        double rawScale = Math.max(10.0, 1000.0 / distance) * (size / 30.0);
        if (mc == null || mc.getWindow() == null) {
            return Math.min(rawScale, 180.0);
        }
        double screenLimit = (double)Math.min(mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight()) * 0.32;
        return MathHelper.clamp((double)rawScale, (double)10.0, (double)Math.max(96.0, screenLimit));
    }

    private static final class PositionEntry {
        private final Vec3d position;
        private final long timestamp;

        private PositionEntry(Vec3d position) {
            this.position = position;
            this.timestamp = System.currentTimeMillis();
        }
    }
}

