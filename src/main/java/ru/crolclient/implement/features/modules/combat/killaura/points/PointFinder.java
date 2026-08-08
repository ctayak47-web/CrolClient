package ru.crolclient.implement.features.modules.combat.killaura.points;

import ru.crolclient.implement.features.modules.combat.killaura.rotation.Angle;
import ru.crolclient.implement.features.modules.combat.killaura.rotation.AngleUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import ru.crolclient.common.QuickImports;

import java.security.SecureRandom;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public class PointFinder implements QuickImports {
    private Random random = new SecureRandom();
    @NonFinal
    private Vec3d offset = Vec3d.ZERO;

    public Vec3d computeVector(LivingEntity entity, float maxDistance, Angle initialAngle, Vec3d velocity) {
        List<Vec3d> candidatePoints = generateCandidatePoints(entity, maxDistance);
        Vec3d bestVector = findBestVector(candidatePoints, initialAngle);
        updateOffset(velocity);
        return (bestVector == null ? entity.getEyePos() : bestVector).add(offset);
    }

    private List<Vec3d> generateCandidatePoints(LivingEntity entity, float maxDistance) {
        Box entityBox = entity.getBoundingBox().expand(-0.2F);
        double minY = entityBox.minY + entityBox.getLengthY() * 0.6;
        double stepY = entityBox.getLengthY() * 0.1;

        double time = System.currentTimeMillis() / 1000.0;

        return Stream.iterate(minY, y -> y <= entityBox.maxY, y -> y + stepY)
                .map(y -> {
                    double offsetX = Math.sin(time + y) * 0.05;
                    double offsetZ = Math.cos(time + y) * 0.05;
                    return new Vec3d(
                            entityBox.getCenter().x + offsetX,
                            y,
                            entityBox.getCenter().z + offsetZ
                    );
                })
                .filter(point -> isWithinDistance(mc.player.getEyePos(), point, maxDistance))
                .collect(Collectors.toList());
    }

    private boolean isWithinDistance(Vec3d startPoint, Vec3d endPoint, float maxDistance) {
        return startPoint.distanceTo(endPoint) < maxDistance;
    }

    private Vec3d findBestVector(List<Vec3d> candidatePoints, Angle initialAngle) {
        Vec3d playerEyePos = mc.player.getEyePos();

        return candidatePoints.stream()
                .sorted(Comparator.comparing(point -> calculateRotationDifference(playerEyePos, point, initialAngle)))
                .findFirst()
                .orElse(null);
    }

    private double calculateRotationDifference(Vec3d startPoint, Vec3d endPoint, Angle initialAngle) {
        Angle targetAngle = AngleUtil.fromVec3d(endPoint.subtract(startPoint));
        Angle delta = AngleUtil.calculateDelta(initialAngle, targetAngle);
        return Math.hypot(delta.getYaw(), delta.getPitch());
    }

    private void updateOffset(Vec3d velocity) {
        double decay = 0.95;
        offset = offset.multiply(decay)
                .add(
                        random.nextGaussian() * 0.015,
                        random.nextGaussian() * 0.01,
                        random.nextGaussian() * 0.015
                )
                .multiply(velocity.multiply(0.5));
    }
}
