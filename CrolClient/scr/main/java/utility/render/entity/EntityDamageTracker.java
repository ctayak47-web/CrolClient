
package crol.client.utility.render.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.LivingEntity;

public final class EntityDamageTracker {
    private static final Map<Integer, DamageState> STATES = new ConcurrentHashMap<Integer, DamageState>();
    private static final long STALE_STATE_MS = 10000L;
    private static final float HEALTH_EPSILON = 0.001f;
    private static long lastCleanupAt;

    private EntityDamageTracker() {
    }

    public static boolean isRecentlyDamaged(LivingEntity entity, long durationMs) {
        return EntityDamageTracker.getDamageAge(entity) <= durationMs;
    }

    public static void markDamaged(LivingEntity entity) {
        if (entity == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (!entity.isAlive()) {
            STATES.remove(entity.getId());
            return;
        }
        DamageState state = STATES.computeIfAbsent(entity.getId(), id -> new DamageState());
        state.lastDamageAt = now;
        state.lastSeenAt = now;
        state.lastHealth = entity.getHealth() + entity.getAbsorptionAmount();
        state.lastHurtTime = entity.hurtTime;
        EntityDamageTracker.cleanup(now);
    }

    public static float getDamageFlashIntensity(LivingEntity entity, long durationMs) {
        if (entity == null || durationMs <= 0L) {
            return 0.0f;
        }
        long age = EntityDamageTracker.getDamageAge(entity);
        if (age > durationMs) {
            return 0.0f;
        }
        float progress = (float)age / (float)durationMs;
        float inverse = 1.0f - progress;
        return inverse * inverse * inverse;
    }

    public static long getDamageAge(LivingEntity entity) {
        if (entity == null) {
            return Long.MAX_VALUE;
        }
        long now = System.currentTimeMillis();
        if (!entity.isAlive()) {
            STATES.remove(entity.getId());
            return Long.MAX_VALUE;
        }
        DamageState state = STATES.computeIfAbsent(entity.getId(), id -> new DamageState());
        float currentHealth = entity.getHealth() + entity.getAbsorptionAmount();
        int currentHurtTime = entity.hurtTime;
        if (!Float.isNaN(state.lastHealth) && currentHealth + 0.001f < state.lastHealth) {
            state.lastDamageAt = now;
        }
        if (currentHurtTime > state.lastHurtTime || currentHurtTime > 0 && state.lastHurtTime <= 0) {
            state.lastDamageAt = now;
        }
        state.lastHealth = currentHealth;
        state.lastHurtTime = currentHurtTime;
        state.lastSeenAt = now;
        EntityDamageTracker.cleanup(now);
        return state.lastDamageAt > 0L ? now - state.lastDamageAt : Long.MAX_VALUE;
    }

    private static void cleanup(long now) {
        if (now - lastCleanupAt < 1000L) {
            return;
        }
        lastCleanupAt = now;
        STATES.entrySet().removeIf(entry -> now - ((DamageState)entry.getValue()).lastSeenAt > 10000L);
    }

    private static final class DamageState {
        private float lastHealth = Float.NaN;
        private int lastHurtTime;
        private long lastDamageAt;
        private long lastSeenAt;

        private DamageState() {
        }
    }
}

