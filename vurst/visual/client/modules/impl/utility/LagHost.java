
package vurst.visual.client.modules.impl.utility;

import com.darkmagician6.eventapi.EventTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ShaderProgramKeys;
import net.minecraft.ShaderProgramKey;
import net.minecraft.StatusEffects;
import net.minecraft.Entity;
import net.minecraft.ItemStack;
import net.minecraft.World;
import net.minecraft.HitResult;
import net.minecraft.Vec3d;
import net.minecraft.Packet;
import net.minecraft.EntityStatusS2CPacket;
import net.minecraft.BufferRenderer;
import net.minecraft.BufferBuilder;
import net.minecraft.Tessellator;
import net.minecraft.VertexFormats;
import net.minecraft.VertexFormat;
import net.minecraft.MathHelper;
import net.minecraft.RaycastContext;
import net.minecraft.BlockHitResult;
import net.minecraft.MatrixStack;
import net.minecraft.Perspective;
import net.minecraft.AbstractClientPlayerEntity;
import net.minecraft.RotationAxis;
import net.minecraft.BuiltBuffer;
import org.joml.Matrix4f;
import vurst.visual.base.events.impl.player.EventUpdate;
import vurst.visual.base.events.impl.render.EventRender3D;
import vurst.visual.base.events.impl.server.EventPacket;
import vurst.visual.base.theme.Theme;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.client.modules.api.setting.impl.BooleanSetting;
import vurst.visual.client.modules.api.setting.impl.ColorSetting;
import vurst.visual.client.modules.api.setting.impl.ModeSetting;
import vurst.visual.client.modules.api.setting.impl.NumberSetting;
import vurst.visual.utility.render.display.base.color.ColorRGBA;

@ModuleAnnotation(name="LagHost", category=Category.RENDER, description="След призраков при спринте/прыжке/ходьбе.")
public final class LagHost
extends Module {
    public static final LagHost INSTANCE = new LagHost();
    private static final byte TOTEM_STATUS = 35;
    private static final float UNIT = 0.0625f;
    private static final String TARGET_BOTH = "Оба";
    private static final String TARGET_SELF = "У меня";
    private static final String TARGET_PLAYERS = "У игроков";
    private final NumberSetting riseHeight = new NumberSetting("Высота подъема", 4.0f, 0.2f, 5.0f, 0.1f);
    private final NumberSetting fadeTime = new NumberSetting("Время исчезновения", 3.0f, 0.2f, 6.0f, 0.1f);
    private final NumberSetting opacity = new NumberSetting("Прозрачность", 0.75f, 0.05f, 1.0f, 0.05f);
    private final ColorSetting color = new ColorSetting("Цвет", Theme.DARK.getColor(), Theme.DARK::getColor);
    private final BooleanSetting onTotem = new BooleanSetting("При тотеме", true);
    private final BooleanSetting onJump = new BooleanSetting("При прыжке", false);
    private final BooleanSetting onWalk = new BooleanSetting("При ходьбе", false);
    private final NumberSetting walkInterval = new NumberSetting("Интервал ходьбы", 0.6f, 0.1f, 3.0f, 0.1f, this.onWalk::isEnabled);
    private final ModeSetting targets = new ModeSetting("Для кого", "Оба", "У меня", "У игроков");
    private final NumberSetting playersRadius = new NumberSetting("Радиус игроков", 8.0f, 1.0f, 64.0f, 1.0f, this::isPlayersRadiusVisible);
    private final List<Ghost> ghosts = new ArrayList<Ghost>();
    private final Map<UUID, PlayerTrack> otherTracks = new HashMap<UUID, PlayerTrack>();
    private boolean wasOnGround;
    private long lastWalkSpawnMs;
    private Object lastWorld;

    private LagHost() {
    }

    @Override
    public void onEnable() {
        this.resetState();
        this.lastWorld = LagHost.mc.world;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.resetState();
        super.onDisable();
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        AbstractClientPlayerEntity player;
        EntityStatusS2CPacket packet;
        if (!event.isReceive() || LagHost.mc.world == null || !this.onTotem.isEnabled()) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (!(packet instanceof EntityStatusS2CPacket) || (packet = (EntityStatusS2CPacket)packet).getStatus() != 35) {
            return;
        }
        Entity entity = packet.getEntity((World)LagHost.mc.world);
        if (!(entity instanceof AbstractClientPlayerEntity) || !this.shouldProcess(player = (AbstractClientPlayerEntity)entity)) {
            return;
        }
        this.addGhost(player, true);
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (LagHost.mc.player == null || LagHost.mc.world == null) {
            this.resetState();
            return;
        }
        if (this.lastWorld != LagHost.mc.world) {
            this.resetState();
            this.lastWorld = LagHost.mc.world;
        }
        long now = System.currentTimeMillis();
        long intervalMs = Math.max(1L, (long)(this.walkInterval.getCurrent() * 1000.0f));
        long fadeMs = this.getFadeMillis();
        this.ghosts.removeIf(ghost -> now - ghost.startTime >= fadeMs);
        if (this.shouldProcessSelf()) {
            boolean onGround = LagHost.mc.player.isOnGround();
            if (this.onJump.isEnabled()) {
                boolean jumpedThisTick;
                boolean bl = jumpedThisTick = this.wasOnGround && !onGround && LagHost.mc.player.getVelocity().y > 0.0;
                if (jumpedThisTick) {
                    this.addGhost((AbstractClientPlayerEntity)LagHost.mc.player, true);
                }
            }
            if (this.onWalk.isEnabled()) {
                if (this.onJump.isEnabled() && !onGround) {
                    this.lastWalkSpawnMs = 0L;
                } else {
                    boolean moving;
                    Vec3d velocity = LagHost.mc.player.getVelocity();
                    double horizontalSpeed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
                    boolean bl = moving = horizontalSpeed > 0.02;
                    if (moving) {
                        if (this.lastWalkSpawnMs == 0L) {
                            this.lastWalkSpawnMs = now;
                        }
                        if (now - this.lastWalkSpawnMs >= intervalMs) {
                            this.lastWalkSpawnMs = now;
                            this.addGhost((AbstractClientPlayerEntity)LagHost.mc.player, false);
                        }
                    } else {
                        this.lastWalkSpawnMs = 0L;
                    }
                }
            }
            this.wasOnGround = onGround;
        }
        if (this.shouldProcessOthers()) {
            this.updateOtherPlayers(now, intervalMs);
        } else {
            this.otherTracks.clear();
        }
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        if (this.ghosts.isEmpty() || LagHost.mc.world == null || LagHost.mc.player == null || LagHost.mc.options.getPerspective() == Perspective.FIRST_PERSON) {
            return;
        }
        long now = System.currentTimeMillis();
        long fadeMs = this.getFadeMillis();
        Vec3d cameraPos = LagHost.mc.getEntityRenderDispatcher().camera.getPos();
        ColorRGBA baseColor = this.color.getColor();
        RenderSystem.setShader((ShaderProgramKey)ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask((boolean)false);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        for (Ghost ghost : this.ghosts) {
            float progress;
            if (!this.shouldRenderGhost(ghost, cameraPos) || (progress = MathHelper.clamp((float)((float)(now - ghost.startTime) / (float)fadeMs), (float)0.0f, (float)1.0f)) >= 1.0f) continue;
            double motionY = ghost.rising ? (double)this.riseHeight.getCurrent() * LagHost.ease(progress) : 0.0;
            float alpha = MathHelper.clamp((float)((float)(LagHost.easeOutAlpha(progress) * (double)this.opacity.getCurrent())), (float)0.0f, (float)1.0f);
            if (alpha <= 0.01f) continue;
            int colorInt = baseColor.withAlpha((int)(255.0f * alpha)).getRGB();
            MatrixStack matrices = event.getMatrix();
            matrices.push();
            double renderX = ghost.position.x - cameraPos.x;
            double renderY = ghost.position.y - cameraPos.y + motionY;
            double renderZ = ghost.position.z - cameraPos.z;
            matrices.translate(renderX, renderY, renderZ);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - ghost.bodyYaw));
            float animatedWalkPhase = ghost.walkPhase + (float)(now - ghost.startTime) * 0.02f;
            this.renderFlatHumanoid(matrices, buffer, colorInt, ghost.sneaking, animatedWalkPhase, ghost.walkSpeed);
            matrices.pop();
        }
        BufferRenderer.drawWithGlobalProgram((BuiltBuffer)buffer.end());
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private void updateOtherPlayers(long now, long intervalMs) {
        if (LagHost.mc.player == null || LagHost.mc.world == null) {
            return;
        }
        double radius = this.playersRadius.getCurrent();
        double radiusSq = radius * radius;
        this.otherTracks.entrySet().removeIf(entry -> {
            UUID uuid = (UUID)entry.getKey();
            for (AbstractClientPlayerEntity player : LagHost.mc.world.getPlayers()) {
                if (player == LagHost.mc.player || player.isRemoved() || !player.getUuid().equals(uuid) || !(LagHost.mc.player.squaredDistanceTo((Entity)player) <= radiusSq) || this.isInvisibleOpponent(player)) continue;
                return false;
            }
            return true;
        });
        for (AbstractClientPlayerEntity player : LagHost.mc.world.getPlayers()) {
            if (player == LagHost.mc.player || player.isRemoved()) continue;
            if (this.isInvisibleOpponent(player)) {
                this.otherTracks.remove(player.getUuid());
                continue;
            }
            if (LagHost.mc.player.squaredDistanceTo((Entity)player) > radiusSq) continue;
            PlayerTrack track = this.otherTracks.computeIfAbsent(player.getUuid(), uuid -> new PlayerTrack(player.isOnGround(), player.getX(), player.getZ()));
            boolean onGround = player.isOnGround();
            if (this.onJump.isEnabled()) {
                boolean jumpedThisTick;
                boolean bl = jumpedThisTick = track.wasOnGround && !onGround && player.getVelocity().y > 0.0;
                if (jumpedThisTick) {
                    this.addGhost(player, true);
                }
            }
            if (this.onWalk.isEnabled()) {
                if (this.onJump.isEnabled() && !onGround) {
                    track.lastWalkSpawnMs = 0L;
                } else {
                    boolean moving;
                    double dz;
                    double dx = player.getX() - track.lastX;
                    double delta = Math.sqrt(dx * dx + (dz = player.getZ() - track.lastZ) * dz);
                    boolean bl = moving = delta > 0.003;
                    if (moving) {
                        if (track.lastWalkSpawnMs == 0L) {
                            track.lastWalkSpawnMs = now;
                        }
                        if (now - track.lastWalkSpawnMs >= intervalMs) {
                            track.lastWalkSpawnMs = now;
                            this.addGhost(player, false);
                        }
                    } else {
                        track.lastWalkSpawnMs = 0L;
                    }
                }
            }
            track.wasOnGround = onGround;
            track.lastX = player.getX();
            track.lastZ = player.getZ();
        }
    }

    private boolean shouldProcess(AbstractClientPlayerEntity player) {
        if (LagHost.mc.player == null) {
            return false;
        }
        if (player == LagHost.mc.player) {
            return this.shouldProcessSelf();
        }
        if (!this.shouldProcessOthers()) {
            return false;
        }
        if (this.isInvisibleOpponent(player)) {
            return false;
        }
        double radius = this.playersRadius.getCurrent();
        return LagHost.mc.player.squaredDistanceTo((Entity)player) <= radius * radius;
    }

    private boolean shouldProcessSelf() {
        return this.targets.is(TARGET_SELF) || this.targets.is(TARGET_BOTH);
    }

    private boolean shouldProcessOthers() {
        return this.targets.is(TARGET_PLAYERS) || this.targets.is(TARGET_BOTH);
    }

    private boolean isPlayersRadiusVisible() {
        return this.shouldProcessOthers();
    }

    private void addGhost(AbstractClientPlayerEntity player, boolean rising) {
        if (player == null || LagHost.mc.world == null) {
            return;
        }
        if (player != LagHost.mc.player && this.isInvisibleOpponent(player)) {
            return;
        }
        Vec3d position = player.getPos();
        float bodyYaw = player.getBodyYaw();
        boolean sneaking = player.isSneaking();
        float walkPhase = player.age;
        float horizontalSpeed = (float)player.getVelocity().horizontalLength();
        float walkSpeed = MathHelper.clamp((float)(horizontalSpeed * 8.0f), (float)0.0f, (float)1.0f);
        boolean selfGhost = player == LagHost.mc.player;
        this.ghosts.add(new Ghost(player.getUuid(), selfGhost, position, bodyYaw, sneaking, walkPhase, walkSpeed, rising, System.currentTimeMillis()));
    }

    private boolean isInvisibleOpponent(AbstractClientPlayerEntity player) {
        if (player == null || player == LagHost.mc.player) {
            return false;
        }
        if (player.isInvisible() || player.hasStatusEffect(StatusEffects.INVISIBILITY)) {
            return true;
        }
        return this.hasInvisArmor(player);
    }

    private boolean hasInvisArmor(AbstractClientPlayerEntity player) {
        for (ItemStack stack : player.getArmorItems()) {
            String lower;
            String armorName;
            if (stack == null || stack.isEmpty() || (armorName = stack.getName().getString()) == null || armorName.isEmpty() || !(lower = armorName.toLowerCase(Locale.ROOT)).contains("инвиз") && !lower.contains("invis")) continue;
            return true;
        }
        return false;
    }

    private long getFadeMillis() {
        return Math.max(1L, (long)(this.fadeTime.getCurrent() * 1000.0f));
    }

    private void renderFlatHumanoid(MatrixStack matrices, BufferBuilder buffer, int colorInt, boolean sneaking, float walkPhase, float walkSpeed) {
        matrices.push();
        matrices.scale(-1.0f, -1.0f, 1.0f);
        matrices.translate(0.0, -1.501, 0.0);
        if (sneaking) {
            matrices.translate(0.0, 0.2, 0.0);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(28.0f));
        }
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        LagHost.drawBox(buffer, matrix, -0.25f, 0.0f, -0.125f, 0.5f, 0.75f, 0.25f, colorInt);
        LagHost.drawBox(buffer, matrix, -0.25f, -0.5f, -0.25f, 0.5f, 0.5f, 0.5f, colorInt);
        float swing = MathHelper.sin((float)(walkPhase * 0.6662f)) * 1.4f * walkSpeed;
        this.renderArm(matrices, buffer, colorInt, true, -swing);
        this.renderArm(matrices, buffer, colorInt, false, swing);
        this.renderLeg(matrices, buffer, colorInt, true, swing);
        this.renderLeg(matrices, buffer, colorInt, false, -swing);
        matrices.pop();
    }

    private void renderArm(MatrixStack matrices, BufferBuilder buffer, int colorInt, boolean left, float pitchRad) {
        matrices.push();
        float pivotX = (left ? -6.0f : 6.0f) * 0.0625f;
        float pivotY = 0.125f;
        matrices.translate(pivotX, pivotY, 0.0f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotation(pitchRad));
        matrices.translate(-pivotX, -pivotY, 0.0f);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float boxX = (left ? -8.0f : 4.0f) * 0.0625f;
        LagHost.drawBox(buffer, matrix, boxX, -0.125f, -0.125f, 0.25f, 0.75f, 0.25f, colorInt);
        matrices.pop();
    }

    private void renderLeg(MatrixStack matrices, BufferBuilder buffer, int colorInt, boolean left, float pitchRad) {
        matrices.push();
        float pivotX = (left ? -2.0f : 2.0f) * 0.0625f;
        float pivotY = 0.75f;
        matrices.translate(pivotX, pivotY, 0.0f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotation(pitchRad));
        matrices.translate(-pivotX, -pivotY, 0.0f);
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float boxX = (left ? -4.0f : 0.0f) * 0.0625f;
        LagHost.drawBox(buffer, matrix, boxX, 0.75f, -0.125f, 0.25f, 0.75f, 0.25f, colorInt);
        matrices.pop();
    }

    private static void drawBox(BufferBuilder buffer, Matrix4f matrix, float x, float y, float z, float sx, float sy, float sz, int colorInt) {
        float x1 = x;
        float y1 = y;
        float z1 = z;
        float x2 = x + sx;
        float y2 = y + sy;
        float z2 = z + sz;
        buffer.vertex(matrix, x1, y1, z2).color(colorInt);
        buffer.vertex(matrix, x2, y1, z2).color(colorInt);
        buffer.vertex(matrix, x2, y2, z2).color(colorInt);
        buffer.vertex(matrix, x1, y2, z2).color(colorInt);
        buffer.vertex(matrix, x2, y1, z1).color(colorInt);
        buffer.vertex(matrix, x1, y1, z1).color(colorInt);
        buffer.vertex(matrix, x1, y2, z1).color(colorInt);
        buffer.vertex(matrix, x2, y2, z1).color(colorInt);
        buffer.vertex(matrix, x1, y1, z1).color(colorInt);
        buffer.vertex(matrix, x1, y1, z2).color(colorInt);
        buffer.vertex(matrix, x1, y2, z2).color(colorInt);
        buffer.vertex(matrix, x1, y2, z1).color(colorInt);
        buffer.vertex(matrix, x2, y1, z2).color(colorInt);
        buffer.vertex(matrix, x2, y1, z1).color(colorInt);
        buffer.vertex(matrix, x2, y2, z1).color(colorInt);
        buffer.vertex(matrix, x2, y2, z2).color(colorInt);
        buffer.vertex(matrix, x1, y2, z2).color(colorInt);
        buffer.vertex(matrix, x2, y2, z2).color(colorInt);
        buffer.vertex(matrix, x2, y2, z1).color(colorInt);
        buffer.vertex(matrix, x1, y2, z1).color(colorInt);
        buffer.vertex(matrix, x1, y1, z1).color(colorInt);
        buffer.vertex(matrix, x2, y1, z1).color(colorInt);
        buffer.vertex(matrix, x2, y1, z2).color(colorInt);
        buffer.vertex(matrix, x1, y1, z2).color(colorInt);
    }

    private static double ease(double t) {
        t = MathHelper.clamp((double)t, (double)0.0, (double)0.75);
        return 1.0 - Math.pow(1.0 - t, 3.0);
    }

    private static double easeOutAlpha(double t) {
        t = MathHelper.clamp((double)t, (double)0.0, (double)1.0);
        double invT = 1.0 - t;
        return 0.75 * Math.pow(invT, 3.0);
    }

    private void resetState() {
        this.ghosts.clear();
        this.otherTracks.clear();
        this.wasOnGround = false;
        this.lastWalkSpawnMs = 0L;
    }

    private boolean shouldRenderGhost(Ghost ghost, Vec3d cameraPos) {
        if (ghost.self) {
            return true;
        }
        AbstractClientPlayerEntity owner = null;
        for (AbstractClientPlayerEntity player : LagHost.mc.world.getPlayers()) {
            if (!player.getUuid().equals(ghost.ownerUuid)) continue;
            owner = player;
            break;
        }
        if (owner == null || owner.isRemoved() || !owner.isAlive()) {
            return false;
        }
        if (this.isInvisibleOpponent(owner)) {
            return false;
        }
        return !this.isOccludedByBlock(cameraPos, ghost.position.add(0.0, (double)owner.getHeight() * 0.5, 0.0));
    }

    private boolean isOccludedByBlock(Vec3d from, Vec3d to) {
        if (LagHost.mc.world == null || LagHost.mc.player == null) {
            return false;
        }
        BlockHitResult hit = LagHost.mc.world.raycast(new RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)LagHost.mc.player));
        return hit.getType() != HitResult.Type.MISS;
    }

    private static final class Ghost {
        private final UUID ownerUuid;
        private final boolean self;
        private final Vec3d position;
        private final float bodyYaw;
        private final boolean sneaking;
        private final float walkPhase;
        private final float walkSpeed;
        private final boolean rising;
        private final long startTime;

        private Ghost(UUID ownerUuid, boolean self, Vec3d position, float bodyYaw, boolean sneaking, float walkPhase, float walkSpeed, boolean rising, long startTime) {
            this.ownerUuid = ownerUuid;
            this.self = self;
            this.position = position;
            this.bodyYaw = bodyYaw;
            this.sneaking = sneaking;
            this.walkPhase = walkPhase;
            this.walkSpeed = walkSpeed;
            this.rising = rising;
            this.startTime = startTime;
        }
    }

    private static final class PlayerTrack {
        private boolean wasOnGround;
        private long lastWalkSpawnMs;
        private double lastX;
        private double lastZ;

        private PlayerTrack(boolean wasOnGround, double lastX, double lastZ) {
            this.wasOnGround = wasOnGround;
            this.lastX = lastX;
            this.lastZ = lastZ;
        }
    }
}

