
package vurst.visual.client.modules.impl.utility;

import com.darkmagician6.eventapi.EventTarget;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.minecraft.Entity;
import net.minecraft.LivingEntity;
import net.minecraft.TntEntity;
import net.minecraft.Items;
import net.minecraft.BlockPos;
import net.minecraft.Vec3i;
import net.minecraft.HitResult;
import net.minecraft.Vec3d;
import net.minecraft.MathHelper;
import net.minecraft.BlockHitResult;
import vurst.visual.base.events.impl.player.EventUpdate;
import vurst.visual.base.events.impl.render.EventRender2D;
import vurst.visual.base.font.Font;
import vurst.visual.base.font.Fonts;
import vurst.visual.client.modules.api.Category;
import vurst.visual.client.modules.api.Module;
import vurst.visual.client.modules.api.ModuleAnnotation;
import vurst.visual.utility.math.ProjectionUtil;
import vurst.visual.utility.render.display.base.BorderRadius;
import vurst.visual.utility.render.display.base.CustomDrawContext;
import vurst.visual.utility.render.display.base.color.ColorRGBA;

@ModuleAnnotation(name="TNT Timer", category=Category.MOVEMENT, description="Показывает время до взрыва TNT.")
public final class TNTTimer
extends Module {
    public static final TNTTimer INSTANCE = new TNTTimer();
    private static final long OWN_PLACE_WINDOW_MS = 1200L;
    private static final long PLACE_CAPTURE_COOLDOWN_MS = 140L;
    private static final double PLACE_MATCH_RADIUS_SQ = 4.0;
    private final Deque<PlacementAttempt> placementAttempts = new ArrayDeque<PlacementAttempt>();
    private final Map<UUID, Long> trackedTnt = new HashMap<UUID, Long>();
    private long lastPlacementCaptureMs;

    private TNTTimer() {
    }

    @Override
    public void onDisable() {
        this.placementAttempts.clear();
        this.trackedTnt.clear();
        this.lastPlacementCaptureMs = 0L;
        super.onDisable();
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (TNTTimer.mc.player == null || TNTTimer.mc.world == null) {
            return;
        }
        long now = System.currentTimeMillis();
        this.cleanupPlacementAttempts(now);
        this.cleanupTracked(now);
        if (!TNTTimer.mc.options.useKey.isPressed() || !this.isHoldingTnt()) {
            return;
        }
        if (now - this.lastPlacementCaptureMs < 140L) {
            return;
        }
        Vec3d placementPos = this.resolvePlacementPos();
        if (placementPos != null) {
            this.placementAttempts.addLast(new PlacementAttempt(placementPos, now));
            this.lastPlacementCaptureMs = now;
        }
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        if (TNTTimer.mc.player == null || TNTTimer.mc.world == null) {
            return;
        }
        long now = System.currentTimeMillis();
        this.cleanupPlacementAttempts(now);
        this.cleanupTracked(now);
        CustomDrawContext ctx = event.getContext();
        Font font = Fonts.MEDIUM.getFont(7.0f);
        for (Entity entity : TNTTimer.mc.world.getEntities()) {
            TntEntity tnt;
            if (!(entity instanceof TntEntity) || !this.isOwnTnt(tnt = (TntEntity)entity, now)) continue;
            Vec3d pos = this.getInterpolatedPos((Entity)tnt, event.getTickDelta()).add(0.0, (double)tnt.getHeight() + 0.5, 0.0);
            Vec3d screen = ProjectionUtil.worldSpaceToScreenSpace(pos);
            if (screen.z <= 0.0 || screen.z >= 1.0) continue;
            float seconds = Math.max(0.0f, (float)tnt.getFuse() / 20.0f);
            String text = String.format(Locale.US, "%.1fs", Float.valueOf(seconds));
            float textWidth = font.width(text);
            float textHeight = font.height();
            float x = (float)screen.x - textWidth / 2.0f;
            float y = (float)screen.y;
            ctx.drawRoundedRect(x - 3.0f, y - 1.0f, textWidth + 6.0f, textHeight + 3.0f, BorderRadius.all(2.0f), new ColorRGBA(0, 0, 0, 120));
            ctx.drawText(font, text, x, y, ColorRGBA.RED);
        }
    }

    private Vec3d getInterpolatedPos(Entity entity, float tickDelta) {
        return new Vec3d(MathHelper.lerp((double)tickDelta, (double)entity.prevX, (double)entity.getX()), MathHelper.lerp((double)tickDelta, (double)entity.prevY, (double)entity.getY()), MathHelper.lerp((double)tickDelta, (double)entity.prevZ, (double)entity.getZ()));
    }

    private boolean isOwnTnt(TntEntity tnt, long now) {
        Long trackedUntil = this.trackedTnt.get(tnt.getUuid());
        if (trackedUntil != null && trackedUntil >= now) {
            return true;
        }
        LivingEntity owner = tnt.getOwner();
        if (owner != null && owner.getUuid().equals(TNTTimer.mc.player.getUuid())) {
            this.trackTnt(tnt, now);
            return true;
        }
        for (PlacementAttempt attempt : this.placementAttempts) {
            if (!(tnt.getPos().squaredDistanceTo(attempt.pos) <= 4.0)) continue;
            this.trackTnt(tnt, now);
            return true;
        }
        return false;
    }

    private void trackTnt(TntEntity tnt, long now) {
        long ttlMs = Math.max(2000L, (long)tnt.getFuse() * 50L + 2000L);
        this.trackedTnt.put(tnt.getUuid(), now + ttlMs);
    }

    private boolean isHoldingTnt() {
        return TNTTimer.mc.player != null && (TNTTimer.mc.player.getMainHandStack().isOf(Items.TNT) || TNTTimer.mc.player.getOffHandStack().isOf(Items.TNT));
    }

    private Vec3d resolvePlacementPos() {
        HitResult ItemStackParticleEffect = TNTTimer.mc.crosshairTarget;
        if (!(ItemStackParticleEffect instanceof BlockHitResult)) {
            return null;
        }
        BlockHitResult blockHitResult = (BlockHitResult)ItemStackParticleEffect;
        BlockPos placePos = blockHitResult.getBlockPos().offset(blockHitResult.getSide());
        return Vec3d.ofCenter((Vec3i)placePos);
    }

    private void cleanupPlacementAttempts(long now) {
        while (!this.placementAttempts.isEmpty() && now - this.placementAttempts.peekFirst().time > 1200L) {
            this.placementAttempts.removeFirst();
        }
    }

    private void cleanupTracked(long now) {
        this.trackedTnt.entrySet().removeIf(entry -> (Long)entry.getValue() < now);
    }

    private static final class PlacementAttempt {
        private final Vec3d pos;
        private final long time;

        private PlacementAttempt(Vec3d pos, long time) {
            this.pos = pos;
            this.time = time;
        }
    }
}

