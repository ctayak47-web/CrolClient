
package crol.client.modules.impl.render;

import com.darkmagician6.eventapi.EventTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.ShaderProgramKeys;
import net.minecraft.ShaderProgramKey;
import net.minecraft.Entity;
import net.minecraft.LivingEntity;
import net.minecraft.PlayerEntity;
import net.minecraft.Box;
import net.minecraft.HitResult;
import net.minecraft.Vec3d;
import net.minecraft.BufferRenderer;
import net.minecraft.BufferBuilder;
import net.minecraft.Tessellator;
import net.minecraft.VertexFormats;
import net.minecraft.VertexFormat;
import net.minecraft.Identifier;
import net.minecraft.SoundEvent;
import net.minecraft.SoundCategory;
import net.minecraft.MathHelper;
import net.minecraft.RaycastContext;
import net.minecraft.BlockHitResult;
import net.minecraft.Camera;
import net.minecraft.MatrixStack;
import net.minecraft.PlayerListEntry;
import net.minecraft.RotationAxis;
import net.minecraft.BuiltBuffer;
import org.joml.Matrix4f;
import crol.client.CrolClient;
import crol.client.base.events.impl.player.EventAttack;
import crol.client.base.events.impl.player.EventUpdate;
import crol.client.base.events.impl.render.EventRender3D;
import crol.client.base.theme.Theme;
import crol.client.modules.api.Category;
import crol.client.modules.api.Module;
import crol.client.modules.api.ModuleAnnotation;
import crol.client.modules.api.setting.impl.BooleanSetting;
import crol.client.modules.api.setting.impl.ColorSetting;
import crol.client.modules.api.setting.impl.ModeSetting;
import crol.client.modules.api.setting.impl.NumberSetting;
import crol.client.utility.render.level.Render3DUtil;

@ModuleAnnotation(name="KillEffect", category=Category.RENDER, description="Эффекты при ваших убийствах.")
public final class KillEffects
extends Module {
    public static final KillEffects INSTANCE = new KillEffects();
    private static final Identifier SOUL_TEX = CrolClient.id("hud/skull.png");
    private static final SoundEvent KILL_SOUND = SoundEvent.of((Identifier)CrolClient.id("kill_effect.abmiss"));
    private static final long EFFECT_DURATION_MS = 3000L;
    private static final long KILL_WINDOW_MS = 3500L;
    private static final long ATTACK_MEMORY_MS = 10000L;
    private final ModeSetting effectType = new ModeSetting("Режим", "Могильный крест", "Уходящая душа");
    private final BooleanSetting playSound = new BooleanSetting("Воспроизводить звук", true);
    private final NumberSetting volume = new NumberSetting("Громкость", 100.0f, 0.0f, 100.0f, 1.0f, this.playSound::isEnabled);
    private final ColorSetting color = new ColorSetting("Цвет", Theme.DARK.getColor(), Theme.DARK::getColor);
    private final List<DeathEffect> activeEffects = new ArrayList<DeathEffect>();
    private final Set<Integer> processedDeaths = new HashSet<Integer>();
    private final Map<Integer, Long> attackedAt = new HashMap<Integer, Long>();
    private Object lastWorld;

    private KillEffects() {
    }

    @Override
    public void onDisable() {
        this.activeEffects.clear();
        this.processedDeaths.clear();
        this.attackedAt.clear();
        this.lastWorld = null;
        super.onDisable();
    }

    @EventTarget
    public void onAttack(EventAttack event) {
        PlayerEntity target;
        if (event.getAction() != EventAttack.Action.POST || KillEffects.mc.player == null || KillEffects.mc.world == null) {
            return;
        }
        Entity entity = event.getTarget();
        if (!(entity instanceof PlayerEntity && (target = (PlayerEntity)entity).isAlive() && this.isValidKillTarget(target))) {
            return;
        }
        this.attackedAt.put(target.getId(), System.currentTimeMillis());
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (KillEffects.mc.world == null || KillEffects.mc.player == null) {
            this.clearState();
            return;
        }
        if (this.lastWorld != KillEffects.mc.world) {
            this.clearState();
            this.lastWorld = KillEffects.mc.world;
        }
        long now = System.currentTimeMillis();
        this.attackedAt.entrySet().removeIf(entry -> now - (Long)entry.getValue() > 10000L);
        HashSet<Integer> activeIds = new HashSet<Integer>();
        for (PlayerEntity player : KillEffects.mc.world.getPlayers()) {
            boolean dead;
            if (!this.isValidKillTarget(player)) continue;
            int id2 = player.getId();
            activeIds.add(id2);
            boolean bl = dead = player.deathTime > 0 || !player.isAlive();
            if (dead) {
                if (!this.processedDeaths.add(id2) || !this.wasKilledByMe((LivingEntity)player, now) || !this.canSeeSpawnEffect(player)) continue;
                this.spawnEffect(player, now);
                this.playKillSound(player);
                continue;
            }
            this.processedDeaths.remove(id2);
        }
        this.processedDeaths.removeIf(id -> !activeIds.contains(id));
        this.attackedAt.keySet().removeIf(id -> !activeIds.contains(id));
        this.activeEffects.removeIf(effect -> now - effect.startTime > 3000L);
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        if (this.activeEffects.isEmpty() || KillEffects.mc.world == null || KillEffects.mc.player == null) {
            return;
        }
        long now = System.currentTimeMillis();
        MatrixStack matrices = event.getMatrix();
        this.renderCrossEffects(now);
        this.renderSoulEffects(matrices, now);
    }

    private void renderCrossEffects(long now) {
        for (DeathEffect effect : this.activeEffects) {
            float progress;
            float alpha;
            if (effect.type != EffectType.CROSS || !this.canSeeCrossEffect(effect) || (alpha = 1.0f - (progress = MathHelper.clamp((float)((float)(now - effect.startTime) / 3000.0f), (float)0.0f, (float)1.0f))) <= 0.01f) continue;
            int effectColor = this.applyAlpha(this.color.getColor().getRGB(), alpha * 0.6f);
            Vec3d base = effect.startPos;
            Render3DUtil.drawLine(base, base.add(0.0, 3.0, 0.0), effectColor, 5.0f, true);
            float yaw = (float)Math.toRadians(effect.yaw + 95.0f);
            float armLength = 1.0f;
            float yOffset = 2.3f;
            Vec3d start = base.add((double)(-armLength) * Math.sin(yaw), (double)yOffset, (double)armLength * Math.cos(yaw));
            Vec3d end = base.add((double)armLength * Math.sin(yaw), (double)yOffset, (double)(-armLength) * Math.cos(yaw));
            Render3DUtil.drawLine(start, end, effectColor, 5.0f, true);
        }
    }

    private void renderSoulEffects(MatrixStack matrices, long now) {
        Camera camera = KillEffects.mc.getEntityRenderDispatcher().camera;
        if (camera == null) {
            return;
        }
        Vec3d cameraPos = camera.getPos();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture((int)0, (Identifier)SOUL_TEX);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc((GlStateManager.SrcFactor)GlStateManager.SrcFactor.SRC_ALPHA, (GlStateManager.DstFactor)GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableCull();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.enableDepthTest();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        for (DeathEffect effect : this.activeEffects) {
            Vec3d soulPos;
            float progress;
            float alpha;
            if (effect.type != EffectType.SOUL || (alpha = 1.0f - (progress = MathHelper.clamp((float)((float)(now - effect.startTime) / 3000.0f), (float)0.0f, (float)1.0f))) <= 0.01f || !this.canSeeSoulEffect(soulPos = effect.startPos.add(0.0, (double)progress * 3.0, 0.0))) continue;
            float size = 0.8f + progress * 0.35f;
            int effectColor = this.applyAlpha(this.color.getColor().getRGB(), alpha);
            this.drawBillboard(buffer, matrices, camera, soulPos.subtract(cameraPos), size, effectColor);
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        RenderSystem.depthMask((boolean)true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }

    private void drawBillboard(BufferBuilder buffer, MatrixStack matrices, Camera camera, Vec3d pos, float size, int color) {
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

    private boolean wasKilledByMe(LivingEntity living, long now) {
        if (KillEffects.mc.player == null) {
            return false;
        }
        if (living.getAttacker() == KillEffects.mc.player) {
            return true;
        }
        Long attackedTime = this.attackedAt.get(living.getId());
        return attackedTime != null && now - attackedTime <= 3500L;
    }

    private void spawnEffect(PlayerEntity entity, long now) {
        EffectType type = this.effectType.is("Могильный крест") ? EffectType.CROSS : EffectType.SOUL;
        this.activeEffects.add(new DeathEffect(now, entity.getYaw(), entity.getPos(), entity.getBoundingBox(), type));
    }

    private void playKillSound(PlayerEntity entity) {
        if (!this.playSound.isEnabled() || KillEffects.mc.player == null || KillEffects.mc.world == null || entity == null) {
            return;
        }
        float soundVolume = MathHelper.clamp((float)(this.volume.getCurrent() / 100.0f), (float)0.0f, (float)1.0f);
        if (soundVolume <= 0.0f) {
            return;
        }
        KillEffects.mc.world.playSoundFromEntity((PlayerEntity)KillEffects.mc.player, (Entity)entity, KILL_SOUND, SoundCategory.BLOCKS, soundVolume, 1.0f);
    }

    private int applyAlpha(int color, float alpha) {
        int a = KillEffects.clamp255(255.0f * MathHelper.clamp((float)alpha, (float)0.0f, (float)1.0f));
        return a << 24 | color & 0xFFFFFF;
    }

    private static int clamp255(float value) {
        return MathHelper.clamp((int)((int)value), (int)0, (int)255);
    }

    private boolean isValidKillTarget(PlayerEntity player) {
        if (player == null || KillEffects.mc.player == null || mc.getNetworkHandler() == null) {
            return false;
        }
        if (player == KillEffects.mc.player || player.isSpectator()) {
            return false;
        }
        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
        return entry != null;
    }

    private boolean canSeeSpawnEffect(PlayerEntity player) {
        if (player == null) {
            return false;
        }
        Box box = player.getBoundingBox();
        if (box == null) {
            return false;
        }
        Vec3d center = box.getCenter();
        Vec3d torso = player.getPos().add(0.0, Math.min(1.2, (double)player.getHeight() * 0.65), 0.0);
        return this.canSeePoint(center) || this.canSeePoint(torso);
    }

    private boolean canSeeCrossEffect(DeathEffect effect) {
        if (effect == null) {
            return false;
        }
        Vec3d base = effect.startPos.add(0.0, 1.0, 0.0);
        Vec3d middle = effect.startPos.add(0.0, 2.2, 0.0);
        return this.canSeePoint(base) || this.canSeePoint(middle);
    }

    private boolean canSeeSoulEffect(Vec3d soulPos) {
        if (soulPos == null) {
            return false;
        }
        return this.canSeePoint(soulPos.add(0.0, 0.35, 0.0));
    }

    private boolean canSeePoint(Vec3d to) {
        if (KillEffects.mc.player == null || KillEffects.mc.world == null || to == null) {
            return false;
        }
        Vec3d from = KillEffects.mc.player.getCameraPosVec(1.0f);
        BlockHitResult hit = KillEffects.mc.world.raycast(new RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)KillEffects.mc.player));
        return hit.getType() == HitResult.Type.MISS;
    }

    private void clearState() {
        this.activeEffects.clear();
        this.processedDeaths.clear();
        this.attackedAt.clear();
    }

    private static final class DeathEffect {
        private final long startTime;
        private final float yaw;
        private final Vec3d startPos;
        private final Box visibilityBox;
        private final EffectType type;

        private DeathEffect(long startTime, float yaw, Vec3d startPos, Box visibilityBox, EffectType type) {
            this.startTime = startTime;
            this.yaw = yaw;
            this.startPos = startPos;
            this.visibilityBox = visibilityBox;
            this.type = type;
        }
    }

    private static enum EffectType {
        CROSS,
        SOUL;

    }
}

