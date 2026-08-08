
package crol.client.utility.math;

import lombok.Generated;
import net.minecraft.Entity;
import net.minecraft.BlockPos;
import net.minecraft.Position;
import net.minecraft.Box;
import net.minecraft.Vec3d;
import net.minecraft.MathHelper;
import net.minecraft.Camera;
import net.minecraft.Frustum;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4d;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import crol.client.utility.game.player.rotation.Rotation;
import crol.client.utility.game.player.rotation.RotationUtil;
import crol.client.utility.interfaces.IMinecraft;
import crol.client.utility.math.MathUtil;
import crol.client.utility.render.level.Render3DUtil;

public final class ProjectionUtil
implements IMinecraft {
    @NotNull
    public static Vec3d worldSpaceToScreenSpace(Vec3d pos) {
        Vector3f delta = pos.subtract(ProjectionUtil.mc.getEntityRenderDispatcher().camera.getPos()).toVector3f();
        int[] viewport = new int[4];
        GL11.glGetIntegerv((int)2978, (int[])viewport);
        Vector3f target = new Vector3f();
        Vector4f transformedCoordinates = new Vector4f(delta.x, delta.y, delta.z, 1.0f).mul((Matrix4fc)Render3DUtil.getLastWorldSpaceMatrix());
        Matrix4f matrixProj = new Matrix4f((Matrix4fc)Render3DUtil.getLastProjMat());
        matrixProj.project(transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), viewport, target);
        return new Vec3d((double)target.x / mc.getWindow().getScaleFactor(), (double)((float)mc.getWindow().getHeight() - target.y) / mc.getWindow().getScaleFactor(), (double)target.z);
    }

    public static boolean canSee(Vec3d vec3d) {
        Camera camera = ProjectionUtil.mc.getEntityRenderDispatcher().camera;
        Rotation angle = RotationUtil.calculateAngle(vec3d);
        return Math.abs(MathHelper.wrapDegrees((float)(angle.getYaw() - camera.getYaw()))) < 90.0f && Math.abs(MathHelper.wrapDegrees((float)(angle.getPitch() - camera.getPitch()))) < 60.0f || ProjectionUtil.canSee(new Box(BlockPos.ofFloored((Position)vec3d)));
    }

    public static boolean canSee(Box box) {
        Frustum frustum = ProjectionUtil.mc.worldRenderer.frustum;
        return box != null && frustum != null && frustum.isVisible(box);
    }

    public static boolean canSee(Vector4d vec) {
        return vec == null || vec.x < 0.0 && vec.z < 1.0 || vec.y < 0.0 && vec.w < 1.0;
    }

    public static double centerX(Vector4d vec) {
        return vec.x + (vec.z - vec.x) / 2.0;
    }

    @NotNull
    public static Vec3d[] getVec3ds(Entity ent, Vec3d pos) {
        Box axisAlignedBB2 = ent.getBoundingBox();
        Box axisAlignedBB = new Box(axisAlignedBB2.minX - ent.getX() + pos.x - (double)0.1f, axisAlignedBB2.minY - ent.getY() + pos.y - (double)0.1f, axisAlignedBB2.minZ - ent.getZ() + pos.z - (double)0.1f, axisAlignedBB2.maxX - ent.getX() + pos.x + (double)0.1f, axisAlignedBB2.maxY - ent.getY() + pos.y + (double)0.1f, axisAlignedBB2.maxZ - ent.getZ() + pos.z + (double)0.1f);
        return new Vec3d[]{new Vec3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ), new Vec3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ), new Vec3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ), new Vec3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ)};
    }

    public static Vector4d getVector4D(Entity ent) {
        Vector4d position = null;
        for (Vec3d vector : ProjectionUtil.getVec3ds(ent, MathUtil.interpolate(ent))) {
            vector = ProjectionUtil.worldSpaceToScreenSpace(new Vec3d(vector.x, vector.y, vector.z));
            if (!(vector.z > 0.0) || !(vector.z < 1.0)) continue;
            if (position == null) {
                position = new Vector4d(vector.x, vector.y, vector.z, 0.0);
            }
            position.x = Math.min(vector.x, position.x);
            position.y = Math.min(vector.y, position.y);
            position.z = Math.max(vector.x, position.z);
            position.w = Math.max(vector.y, position.w);
        }
        return position;
    }

    @Generated
    private ProjectionUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

