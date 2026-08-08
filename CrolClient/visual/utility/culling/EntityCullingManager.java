
package crol.client.utility.culling;

import com.google.gson.JsonObject;
import java.util.List;
import net.minecraft.Entity;
import net.minecraft.PlayerEntity;
import net.minecraft.BlockView;
import net.minecraft.World;
import net.minecraft.BlockPos;
import net.minecraft.Box;
import net.minecraft.HitResult;
import net.minecraft.Vec3d;
import net.minecraft.VoxelShape;
import net.minecraft.BlockState;
import net.minecraft.RaycastContext;
import net.minecraft.BlockHitResult;
import net.minecraft.Camera;
import net.minecraft.Frustum;
import net.minecraft.ClientWorld;
import net.minecraft.ClientPlayerEntity;
import crol.client.utility.culling.VisibilityCache;
import crol.client.utility.interfaces.IMinecraft;

public final class EntityCullingManager
implements IMinecraft {
    private static final EntityCullingManager INSTANCE = new EntityCullingManager();
    private static final double MIN_CHECK_DISTANCE = 6.0;
    private static final double PLAYER_NO_CULL_DISTANCE = 25.0;
    private static final double RAY_EPSILON = 0.05;
    private static final int MAX_RAYCAST_PASSES = 8;
    private final VisibilityCache visibilityCache = new VisibilityCache(4096);
    private boolean enabled = true;
    private int updateInterval = 10;
    private double maxCheckDistance = 128.0;
    private World cachedWorld;

    private EntityCullingManager() {
    }

    public static EntityCullingManager getInstance() {
        return INSTANCE;
    }

    public boolean shouldCull(Entity entity, Frustum frustum) {
        this.syncWorld();
        if (!this.enabled || !this.canCheck(entity, frustum)) {
            return false;
        }
        long currentTick = EntityCullingManager.mc.world.getTime();
        int entityId = entity.getId();
        Boolean cachedVisibility = this.visibilityCache.get(entityId, currentTick);
        if (cachedVisibility != null) {
            return cachedVisibility == false;
        }
        boolean visible = this.hasLineOfSight(entity);
        this.visibilityCache.put(entityId, visible, currentTick + (long)Math.max(1, this.updateInterval));
        return !visible;
    }

    public JsonObject save() {
        JsonObject object = new JsonObject();
        object.addProperty("enabled", this.enabled);
        object.addProperty("updateInterval", this.updateInterval);
        object.addProperty("maxCheckDistance", this.maxCheckDistance);
        return object;
    }

    public void load(JsonObject object) {
        if (object == null) {
            return;
        }
        if (object.has("enabled")) {
            this.enabled = object.get("enabled").getAsBoolean();
        }
        if (object.has("updateInterval")) {
            this.updateInterval = Math.max(1, object.get("updateInterval").getAsInt());
        }
        if (object.has("maxCheckDistance")) {
            this.maxCheckDistance = Math.max(6.0, object.get("maxCheckDistance").getAsDouble());
        }
        this.visibilityCache.clear();
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.visibilityCache.clear();
    }

    public int getUpdateInterval() {
        return this.updateInterval;
    }

    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = Math.max(1, updateInterval);
        this.visibilityCache.clear();
    }

    public double getMaxCheckDistance() {
        return this.maxCheckDistance;
    }

    public void setMaxCheckDistance(double maxCheckDistance) {
        this.maxCheckDistance = Math.max(6.0, maxCheckDistance);
        this.visibilityCache.clear();
    }

    private boolean canCheck(Entity entity, Frustum frustum) {
        Vec3d target;
        if (mc == null || EntityCullingManager.mc.world == null || EntityCullingManager.mc.player == null || entity == null) {
            return false;
        }
        if (entity.isRemoved()) {
            this.visibilityCache.remove(entity.getId());
            return false;
        }
        if (entity == mc.getCameraEntity()) {
            return false;
        }
        if (entity instanceof PlayerEntity && EntityCullingManager.mc.player.squaredDistanceTo(entity) <= 625.0) {
            return false;
        }
        Box boundingBox = entity.getBoundingBox();
        if (boundingBox == null) {
            return false;
        }
        if (frustum != null && !frustum.isVisible(boundingBox)) {
            return false;
        }
        Vec3d cameraPos = this.getCameraPos();
        double squaredDistance = cameraPos.squaredDistanceTo(target = boundingBox.getCenter());
        if (squaredDistance <= 36.0) {
            return false;
        }
        double maxDistance = this.maxCheckDistance;
        return squaredDistance <= maxDistance * maxDistance;
    }

    private boolean hasLineOfSight(Entity entity) {
        for (Vec3d sample : this.getVisibilitySamples(entity)) {
            if (!this.hasLineOfSight(sample)) continue;
            return true;
        }
        return false;
    }

    private boolean hasLineOfSight(Vec3d target) {
        Vec3d start = this.getCameraPos();
        Vec3d delta = target.subtract(start);
        if (delta.lengthSquared() <= 1.0E-6) {
            return true;
        }
        Vec3d advance = delta.normalize().multiply(0.05);
        Vec3d currentStart = start;
        for (int pass = 0; pass < 8; ++pass) {
            ClientPlayerEntity cameraEntity;
            ClientWorld NarrationMessageBuilder = EntityCullingManager.mc.world;
            Entity entity = mc.getCameraEntity();
            if (entity instanceof Entity) {
                Entity cameraEntity = entity;
                cameraEntity = cameraEntity;
            } else {
                cameraEntity = EntityCullingManager.mc.player;
            }
            BlockHitResult hit = NarrationMessageBuilder.raycast(new RaycastContext(currentStart, target, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)cameraEntity));
            if (hit.getType() == HitResult.Type.MISS) {
                return true;
            }
            if (!(hit instanceof BlockHitResult)) {
                return true;
            }
            BlockHitResult blockHit = hit;
            if (blockHit.getPos().squaredDistanceTo(target) <= 0.0025000000000000005) {
                return true;
            }
            BlockPos blockPos = blockHit.getBlockPos();
            BlockState state = EntityCullingManager.mc.world.getBlockState(blockPos);
            if (this.isOpaqueOccluder(state, blockPos)) {
                return false;
            }
            Vec3d nextStart = blockHit.getPos().add(advance);
            if (nextStart.squaredDistanceTo(currentStart) <= 1.0E-6) {
                return true;
            }
            if (nextStart.squaredDistanceTo(target) <= 0.0025000000000000005) {
                return true;
            }
            currentStart = nextStart;
        }
        return true;
    }

    private List<Vec3d> getVisibilitySamples(Entity entity) {
        Box box = entity.getBoundingBox();
        Vec3d center = box.getCenter();
        double widthInset = Math.min(box.getLengthX(), box.getLengthZ()) * 0.25;
        return List.of(center, new Vec3d(center.x, box.maxY - 0.12, center.z), new Vec3d(center.x, box.minY + 0.18, center.z), new Vec3d(center.x - widthInset, center.y, center.z), new Vec3d(center.x + widthInset, center.y, center.z), new Vec3d(center.x, center.y, center.z - widthInset), new Vec3d(center.x, center.y, center.z + widthInset));
    }

    private boolean isOpaqueOccluder(BlockState state, BlockPos blockPos) {
        if (state.isAir()) {
            return false;
        }
        if (!state.shouldBlockVision((BlockView)EntityCullingManager.mc.world, blockPos)) {
            return false;
        }
        VoxelShape collisionShape = state.getCollisionShape((BlockView)EntityCullingManager.mc.world, blockPos);
        if (collisionShape.isEmpty()) {
            return false;
        }
        if (!state.isOpaqueFullCube()) {
            return false;
        }
        List collisionBoxes = collisionShape.getBoundingBoxes();
        if (collisionBoxes.size() != 1) {
            return false;
        }
        Box box = (Box)collisionBoxes.getFirst();
        double epsilon = 0.001;
        return box.minX <= epsilon && box.minY <= epsilon && box.minZ <= epsilon && box.maxX >= 1.0 - epsilon && box.maxY >= 1.0 - epsilon && box.maxZ >= 1.0 - epsilon;
    }

    private Vec3d getCameraPos() {
        Camera camera;
        Camera camera = camera = EntityCullingManager.mc.gameRenderer != null ? EntityCullingManager.mc.gameRenderer.getCamera() : null;
        if (camera != null) {
            return camera.getPos();
        }
        return EntityCullingManager.mc.player.getCameraPosVec(1.0f);
    }

    private void syncWorld() {
        if (mc == null) {
            return;
        }
        if (this.cachedWorld != EntityCullingManager.mc.world) {
            this.cachedWorld = EntityCullingManager.mc.world;
            this.visibilityCache.clear();
        }
    }
}

