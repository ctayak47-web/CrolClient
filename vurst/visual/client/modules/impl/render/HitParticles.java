
package vurst.visual.client.modules.impl.render;

import com.darkmagician6.eventapi.EventTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.ShaderProgramKeys;
import net.minecraft.ShaderProgramKey;
import net.minecraft.Entity;
import net.minecraft.LivingEntity;
import net.minecraft.PlayerEntity;
import net.minecraft.ItemStack;
import net.minecraft.BlockPos;
import net.minecraft.HitResult;
import net.minecraft.Vec3d;
import net.minecraft.BlockState;
import net.minecraft.BufferRenderer;
import net.minecraft.BufferBuilder;
import net.minecraft.Tessellator;
import net.minecraft.VertexFormats;
import net.minecraft.VertexFormat;
import net.minecraft.Identifier;
import net.minecraft.MathHelper;
import net.minecraft.EntityHitResult;
import net.minecraft.RotationAxis;
import net.minecraft.BuiltBuffer;
import org.joml.Matrix4f;
import vurst.visual.VurstVisual;
import vurst.visual.base.events.impl.player.EventUpdate;
import vurst.visual.base.events.impl.render.EventRender3D;
import vurst.visual.base.theme.Theme;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.BooleanSetting;
import vurst.visual.client.modules.api.setting.impl.ColorSetting;
import vurst.visual.client.modules.api.setting.impl.ModeSetting;
import vurst.visual.client.modules.api.setting.impl.MultiBooleanSetting;
import vurst.visual.client.modules.api.setting.impl.NumberSetting;
import vurst.visual.utility.math.MathUtil;
import vurst.visual.utility.render.display.base.color.ColorRGBA;

@ModuleAnnotation(name="Particle", category=Category.RENDER, description="Частицы при ударе.")
public final class HitParticles
extends Module {
    public static final HitParticles INSTANCE = new HitParticles();
    private static final double GRAVITY = 0.03;
    private static final double DRAG = 0.98;
    private static final double HAND_HEIGHT_FACTOR = 0.65;
    private static final double HAND_SIDE_OFFSET = 0.35;
    private static final double HAND_FORWARD_OFFSET = 0.15;
    private static final double HAND_BURST_SCALE = 0.4;
    private final BooleanSetting hitEnabled = new BooleanSetting("От ударов", true);
    private final ModeSetting targetMode = new ModeSetting("Цели", "Игроки", "Мобы", "Все");
    private static final ParticleType HEART = new ParticleType("Сердечки", VurstVisual.id("hud/particles/heart.png"));
    private static final ParticleType ZALUPA = new ParticleType("Залупа", VurstVisual.id("hud/particles/thor.png"));
    private static final ParticleType LIGHTNING = new ParticleType("Молния", VurstVisual.id("hud/particles/lightning.png"));
    private static final ParticleType SNOW = new ParticleType("Снежинки", VurstVisual.id("hud/particles/snowflake.png"));
    private static final ParticleType ORB = new ParticleType("Орбизы", VurstVisual.id("hud/particles/orb.png"));
    private static final ParticleType CROWN = new ParticleType("Короны", VurstVisual.id("hud/particles/crown.png"));
    private static final ParticleType DOLLAR = new ParticleType("Доллар", VurstVisual.id("hud/particles/dollar.png"));
    private static final ParticleType SKULL = new ParticleType("Скелеты", VurstVisual.id("hud/particles/skull.png"));
    private static final ParticleType STAR = new ParticleType("Звезда", VurstVisual.id("hud/particles/star.png"));
    private static final ParticleType FIREFLY = new ParticleType("Огоньки", VurstVisual.id("hud/particles/firefly.png"));
    private static final List<ParticleType> TYPES = List.of(HEART, ZALUPA, LIGHTNING, SNOW, ORB, CROWN, DOLLAR, SKULL, STAR, FIREFLY);
    private final ModeSetting physics = new ModeSetting("Физика", "Реалистичная", "Без коллизий", "Без физики");
    private final MultiBooleanSetting hitTypes = new MultiBooleanSetting("Типы (удар)", MultiBooleanSetting.Value.of("Сердечки", true), MultiBooleanSetting.Value.of("Залупа", false), MultiBooleanSetting.Value.of("Молния", false), MultiBooleanSetting.Value.of("Снежинки", false), MultiBooleanSetting.Value.of("Орбизы", false), MultiBooleanSetting.Value.of("Короны", false), MultiBooleanSetting.Value.of("Доллар", false), MultiBooleanSetting.Value.of("Скелеты", false), MultiBooleanSetting.Value.of("Звезда", false), MultiBooleanSetting.Value.of("Огоньки", false));
    private final NumberSetting count = new NumberSetting("Количество", 20.0f, 1.0f, 200.0f, 1.0f);
    private final NumberSetting size = new NumberSetting("Размер", 0.2f, 0.2f, 4.0f, 0.1f);
    private final NumberSetting lifetime = new NumberSetting("Время жизни", 1500.0f, 200.0f, 8000.0f, 50.0f);
    private final NumberSetting spread = new NumberSetting("Сила разлёта", 0.25f, 0.05f, 1.5f, 0.05f);
    private final ColorSetting color = new ColorSetting("Цвет", Theme.DARK.getColor(), Theme.DARK::getColor);
    private final List<Particle> particles = new ArrayList<Particle>();
    private boolean lastAttackPressed;
    private Object lastWorld;

    private HitParticles() {
    }

    @Override
    public void onEnable() {
        this.particles.clear();
        this.lastAttackPressed = false;
        this.lastWorld = null;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.particles.clear();
        this.lastWorld = null;
        super.onDisable();
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        LivingEntity target;
        if (HitParticles.mc.player == null || HitParticles.mc.world == null) {
            this.particles.clear();
            this.lastWorld = null;
            this.lastAttackPressed = false;
            return;
        }
        if (this.lastWorld != HitParticles.mc.world) {
            this.particles.clear();
            this.lastWorld = HitParticles.mc.world;
        }
        boolean currentAttack = HitParticles.mc.options.attackKey.isPressed();
        if (this.hitEnabled.isEnabled() && currentAttack && !this.lastAttackPressed && (target = this.getTarget()) != null) {
            List<ParticleType> enabledTypes = this.getEnabledTypes(this.hitTypes);
            this.spawnBurst(this.getHitPosition(target), enabledTypes);
            if (target instanceof PlayerEntity) {
                PlayerEntity playerTarget = (PlayerEntity)target;
                this.spawnHandBursts(playerTarget, enabledTypes);
            }
        }
        this.lastAttackPressed = currentAttack;
        long now = System.currentTimeMillis();
        long lifeTimeMs = Math.max(1L, (long)this.lifetime.getCurrent());
        for (int i = this.particles.size() - 1; i >= 0; --i) {
            Particle particle = this.particles.get(i);
            this.tickParticle(particle);
            if (now - particle.spawnTime < lifeTimeMs) continue;
            this.particles.remove(i);
        }
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        if (HitParticles.mc.player == null || HitParticles.mc.world == null || this.particles.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        long lifeTimeMs = Math.max(1L, (long)this.lifetime.getCurrent());
        LinkedHashMap<Identifier, List> grouped = new LinkedHashMap<Identifier, List>();
        for (Particle particle : this.particles) {
            grouped.computeIfAbsent(particle.type.texture, key -> new ArrayList()).add(particle);
        }
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.enableDepthTest();
        for (Map.Entry entry : grouped.entrySet()) {
            RenderSystem.setShaderTexture((int)0, (Identifier)((Identifier)entry.getKey()));
            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            for (Particle particle : (List)entry.getValue()) {
                this.renderParticle(buffer, event, particle, lifeTimeMs, now);
            }
            BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        }
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void renderParticle(BufferBuilder buffer, EventRender3D event, Particle particle, long lifeTimeMs, long now) {
        long ageMs = now - particle.spawnTime;
        float progress = MathHelper.clamp((float)((float)ageMs / (float)lifeTimeMs), (float)0.0f, (float)1.0f);
        if (progress >= 1.0f) {
            return;
        }
        float alpha = 1.0f - progress;
        float scale = this.size.getCurrent() * (0.35f + (1.0f - progress) * 0.65f);
        if (scale <= 0.0f) {
            return;
        }
        float tickDelta = event.getPartialTicks();
        Vec3d camPos = HitParticles.mc.getEntityRenderDispatcher().camera.getPos();
        Vec3d pos = particle.renderPos(tickDelta);
        float rotation = particle.rotation + (float)ageMs * particle.rotationSpeed;
        ColorRGBA drawColor = this.color.getColor(alpha);
        int colorInt = drawColor.getRGB();
        event.getMatrix().push();
        event.getMatrix().translate(pos.x - camPos.x, pos.y - camPos.y, pos.z - camPos.z);
        event.getMatrix().multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-HitParticles.mc.getEntityRenderDispatcher().camera.getYaw()));
        event.getMatrix().multiply(RotationAxis.POSITIVE_X.rotationDegrees(HitParticles.mc.getEntityRenderDispatcher().camera.getPitch()));
        event.getMatrix().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotation));
        event.getMatrix().scale(-scale, -scale, scale);
        Matrix4f matrix = event.getMatrix().peek().getPositionMatrix();
        buffer.vertex(matrix, -0.5f, -0.5f, 0.0f).texture(0.0f, 1.0f).color(colorInt);
        buffer.vertex(matrix, 0.5f, -0.5f, 0.0f).texture(1.0f, 1.0f).color(colorInt);
        buffer.vertex(matrix, 0.5f, 0.5f, 0.0f).texture(1.0f, 0.0f).color(colorInt);
        buffer.vertex(matrix, -0.5f, 0.5f, 0.0f).texture(0.0f, 0.0f).color(colorInt);
        event.getMatrix().pop();
    }

    private void tickParticle(Particle particle) {
        particle.tick();
        String mode = this.physics.get();
        if ("Без физики".equals(mode)) {
            particle.position = particle.position.add(particle.velocity);
            particle.velocity = particle.velocity.multiply(0.99, 0.99, 0.99);
            return;
        }
        particle.velocity = particle.velocity.multiply(0.98, 0.98, 0.98).add(0.0, -0.03, 0.0);
        if ("Без коллизий".equals(mode)) {
            particle.position = particle.position.add(particle.velocity);
            return;
        }
        Vec3d next = particle.position.add(particle.velocity);
        BlockPos blockPos = BlockPos.ofFloored((double)next.x, (double)next.y, (double)next.z);
        BlockState blockState = HitParticles.mc.world.getBlockState(blockPos);
        if (!blockState.isAir()) {
            if (!HitParticles.mc.world.getBlockState(BlockPos.ofFloored((double)(particle.position.x + particle.velocity.x), (double)particle.position.y, (double)particle.position.z)).isAir()) {
                particle.velocity = new Vec3d(0.0, particle.velocity.y, particle.velocity.z);
            }
            if (!HitParticles.mc.world.getBlockState(BlockPos.ofFloored((double)particle.position.x, (double)(particle.position.y + particle.velocity.y), (double)particle.position.z)).isAir()) {
                particle.velocity = new Vec3d(particle.velocity.x, -particle.velocity.y * 0.6, particle.velocity.z);
            }
            if (!HitParticles.mc.world.getBlockState(BlockPos.ofFloored((double)particle.position.x, (double)particle.position.y, (double)(particle.position.z + particle.velocity.z))).isAir()) {
                particle.velocity = new Vec3d(particle.velocity.x, particle.velocity.y, 0.0);
            }
            particle.position = particle.position.add(particle.velocity);
        } else {
            particle.position = next;
        }
    }

    private void spawnBurst(Vec3d origin, List<ParticleType> types) {
        int amount = Math.max(1, Math.round(this.count.getCurrent()));
        double power = this.spread.getCurrent();
        this.spawnBurst(origin, types, amount, power);
    }

    private void spawnBurst(Vec3d origin, List<ParticleType> types, int amount, double power) {
        if (origin == null || types.isEmpty() || amount <= 0) {
            return;
        }
        for (int i = 0; i < amount; ++i) {
            ParticleType type = types.get(ThreadLocalRandom.current().nextInt(types.size()));
            Vec3d pos = origin.add(MathUtil.getRandom(-0.2, 0.2), MathUtil.getRandom(-0.1, 0.2), MathUtil.getRandom(-0.2, 0.2));
            Vec3d velocity = this.randomVelocity(power);
            this.particles.add(new Particle(type, pos, velocity));
        }
    }

    private Vec3d randomVelocity(double power) {
        String mode = this.physics.get();
        double x = MathUtil.getRandom(-power, power);
        double z = MathUtil.getRandom(-power, power);
        double y = "Без физики".equals(mode) ? MathUtil.getRandom(power * 0.6, power * 1.2) : MathUtil.getRandom(power * 0.2, power);
        return new Vec3d(x, y, z);
    }

    private void spawnHandBursts(PlayerEntity player, List<ParticleType> types) {
        if (types.isEmpty()) {
            return;
        }
        int amount = Math.max(1, Math.round(this.count.getCurrent() * 0.4f));
        double power = this.spread.getCurrent();
        this.spawnBurst(this.getHandPosition(player, true), types, amount, power);
        this.spawnBurst(this.getHandPosition(player, false), types, amount, power);
    }

    private Vec3d getHandPosition(PlayerEntity player, boolean leftHand) {
        float yawRad = (float)Math.toRadians(player.getYaw());
        double sin = MathHelper.sin((float)yawRad);
        double cos = MathHelper.cos((float)yawRad);
        Vec3d base = new Vec3d(player.getX(), player.getY() + (double)player.getHeight() * 0.65, player.getZ());
        Vec3d forward = new Vec3d(-sin, 0.0, cos).multiply(0.15);
        Vec3d side = new Vec3d(cos, 0.0, sin).multiply(0.35);
        if (leftHand) {
            side = side.multiply(-1.0);
        }
        return base.add(forward).add(side);
    }

    private List<ParticleType> getEnabledTypes(MultiBooleanSetting setting) {
        ArrayList<ParticleType> selected = new ArrayList<ParticleType>();
        for (ParticleType type : TYPES) {
            if (!setting.isEnable(type.name)) continue;
            selected.add(type);
        }
        return selected;
    }

    private LivingEntity getTarget() {
        LivingEntity living;
        if (HitParticles.mc.crosshairTarget == null || HitParticles.mc.crosshairTarget.getType() != HitResult.Type.ENTITY) {
            return null;
        }
        EntityHitResult entityHit = (EntityHitResult)HitParticles.mc.crosshairTarget;
        Entity entity = entityHit.getEntity();
        if (entity instanceof LivingEntity && (living = (LivingEntity)entity).isAlive() && this.isTargetAllowed((Entity)living)) {
            return living;
        }
        return null;
    }

    private Vec3d getHitPosition(LivingEntity target) {
        HitResult ItemStackParticleEffect = HitParticles.mc.crosshairTarget;
        if (ItemStackParticleEffect instanceof EntityHitResult) {
            EntityHitResult entityHit = (EntityHitResult)ItemStackParticleEffect;
            return entityHit.getPos();
        }
        return new Vec3d(target.getX(), target.getY() + (double)target.getHeight() / 2.0, target.getZ());
    }

    private boolean isTargetAllowed(Entity entity) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity)entity;
            if (this.isNakedInvisiblePlayer(player)) {
                return false;
            }
            return this.targetMode.is("Игроки") || this.targetMode.is("Все");
        }
        if (entity instanceof LivingEntity) {
            return this.targetMode.is("Мобы") || this.targetMode.is("Все");
        }
        return false;
    }

    private boolean isNakedInvisiblePlayer(PlayerEntity player) {
        if (!player.isInvisible()) {
            return false;
        }
        for (ItemStack stack : player.getArmorItems()) {
            if (stack.isEmpty()) continue;
            return false;
        }
        return true;
    }

    private static final class Particle {
        private final ParticleType type;
        private final long spawnTime;
        private final float rotation;
        private final float rotationSpeed;
        private Vec3d position;
        private Vec3d prevPosition;
        private Vec3d velocity;

        private Particle(ParticleType type, Vec3d position, Vec3d velocity) {
            this.type = type;
            this.position = position;
            this.prevPosition = position;
            this.velocity = velocity;
            this.spawnTime = System.currentTimeMillis();
            this.rotation = ThreadLocalRandom.current().nextFloat() * 360.0f;
            this.rotationSpeed = MathUtil.random(-0.15, 0.15);
        }

        private void tick() {
            this.prevPosition = this.position;
        }

        private Vec3d renderPos(float tickDelta) {
            return new Vec3d(MathHelper.lerp((double)tickDelta, (double)this.prevPosition.x, (double)this.position.x), MathHelper.lerp((double)tickDelta, (double)this.prevPosition.y, (double)this.position.y), MathHelper.lerp((double)tickDelta, (double)this.prevPosition.z, (double)this.position.z));
        }
    }

    private static final class ParticleType {
        private final String name;
        private final Identifier texture;

        private ParticleType(String name, Identifier texture) {
            this.name = name;
            this.texture = texture;
        }
    }
}

