
package crol.client.utility.game.player.rotation;

import lombok.Generated;
import net.minecraft.Vec3d;
import net.minecraft.MathHelper;
import crol.client.utility.game.player.rotation.Rotation;
import crol.client.utility.interfaces.IMinecraft;

public final class RotationUtil
implements IMinecraft {
    public static Rotation getClientRotation() {
        return new Rotation(RotationUtil.mc.player.getYaw(), RotationUtil.mc.player.getPitch());
    }

    public static Rotation fromVec3d(Vec3d vector) {
        return new Rotation((float)MathHelper.wrapDegrees((double)(Math.toDegrees(Math.atan2(vector.z, vector.x)) - 90.0)), (float)MathHelper.wrapDegrees((double)Math.toDegrees(-Math.atan2(vector.y, Math.hypot(vector.x, vector.z)))));
    }

    public static Rotation calculateAngle(Vec3d to) {
        return RotationUtil.fromVec3d(to.subtract(RotationUtil.mc.player.getEyePos()));
    }

    @Generated
    private RotationUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

