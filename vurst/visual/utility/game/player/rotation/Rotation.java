
package vurst.visual.utility.game.player.rotation;

import lombok.Generated;
import net.minecraft.Vec3d;
import net.minecraft.MinecraftClient;
import net.minecraft.MathHelper;
import vurst.visual.utility.game.player.rotation.RotationDelta;

public class Rotation {
    private final float yaw;
    private final float pitch;
    private boolean isNormalized;
    public static final Rotation ZERO = new Rotation(0.0f, 0.0f);

    public Rotation(float yaw, float pitch) {
        this(yaw, pitch, false);
    }

    public Rotation(float yaw, float pitch, boolean isNormalized) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.isNormalized = isNormalized;
    }

    public static Rotation lookingAt(Vec3d point, Vec3d from) {
        return Rotation.fromRotationVec(point.subtract(from));
    }

    public static Rotation fromRotationVec(Vec3d lookVec) {
        double diffX = lookVec.x;
        double diffY = lookVec.y;
        double diffZ = lookVec.z;
        return new Rotation((float)MathHelper.wrapDegrees((double)(Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0)), (float)MathHelper.wrapDegrees((double)(-Math.toDegrees(Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ))))));
    }

    public float angleTo(Rotation other) {
        return Math.min(this.rotationDeltaTo(other).length(), 180.0f);
    }

    public RotationDelta rotationDeltaTo(Rotation other) {
        return new RotationDelta(this.angleDifference(other.yaw, this.yaw), this.angleDifference(other.pitch, this.pitch));
    }

    private float angleDifference(float a, float b) {
        return MathHelper.wrapDegrees((float)(a - b));
    }

    public boolean approximatelyEquals(Rotation other, float tolerance) {
        return this.angleTo(other) <= tolerance;
    }

    public boolean isNormalized() {
        return this.isNormalized;
    }

    public Vec3d getDirectionVector() {
        return Vec3d.fromPolar((float)this.pitch, (float)this.yaw);
    }

    public final Vec3d toVector() {
        float f = this.pitch * ((float)Math.PI / 180);
        float g = -this.yaw * ((float)Math.PI / 180);
        float h = MathHelper.cos((float)g);
        float i = MathHelper.sin((float)g);
        float j = MathHelper.cos((float)f);
        float k = MathHelper.sin((float)f);
        return new Vec3d((double)(i * j), (double)(-k), (double)(h * j));
    }

    public Rotation towardsLinear(Rotation other, float horizontalFactor, float verticalFactor) {
        RotationDelta diff = this.rotationDeltaTo(other);
        float rotationDifference = diff.length();
        float straightLineYaw = Math.abs(diff.getDeltaYaw() / rotationDifference) * horizontalFactor;
        float straightLinePitch = Math.abs(diff.getDeltaPitch() / rotationDifference) * verticalFactor;
        float limitedYaw = MathHelper.clamp((float)diff.getDeltaYaw(), (float)(-straightLineYaw), (float)straightLineYaw);
        float limitedPitch = MathHelper.clamp((float)diff.getDeltaPitch(), (float)(-straightLinePitch), (float)straightLinePitch);
        return new Rotation(this.yaw + limitedYaw, this.pitch + limitedPitch);
    }

    public boolean check() {
        return Float.isInfinite(this.yaw) || Float.isNaN(this.yaw) || Float.isInfinite(this.pitch) || Float.isNaN(this.pitch);
    }

    public static float gcd() {
        double f = (Double)MinecraftClient.getInstance().options.getMouseSensitivity().getValue() * (double)0.6f + (double)0.2f;
        return (float)(f * f * f * 8.0 * (double)0.15f);
    }

    public Rotation normalize(Rotation currentRotation) {
        if (this.isNormalized || this.equals(currentRotation)) {
            return this;
        }
        RotationDelta rotationDelta = currentRotation.rotationDeltaTo(this);
        double gcd = Rotation.gcd();
        int targetX = (int)((double)rotationDelta.getDeltaYaw() / gcd);
        int targetY = (int)((double)rotationDelta.getDeltaPitch() / gcd);
        return new Rotation((float)((double)currentRotation.getYaw() + (double)targetX * gcd), (float)((double)currentRotation.getPitch() + (double)targetY * gcd), true);
    }

    public Rotation add(RotationDelta diff) {
        return new Rotation(this.yaw + diff.getDeltaYaw(), this.pitch + diff.getDeltaPitch());
    }

    public boolean equals(Object obj) {
        if (obj instanceof Rotation) {
            Rotation o2 = (Rotation)obj;
            return o2.yaw == this.yaw && o2.pitch == this.pitch;
        }
        return false;
    }

    @Generated
    public float getYaw() {
        return this.yaw;
    }

    @Generated
    public float getPitch() {
        return this.pitch;
    }
}

