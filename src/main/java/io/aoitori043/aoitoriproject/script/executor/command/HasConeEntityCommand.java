package io.aoitori043.aoitoriproject.script.executor.command;

import io.aoitori043.aoitoriproject.script.PlayerDataAccessor;
import io.aoitori043.aoitoriproject.script.executor.AbstractCommand;
import io.aoitori043.aoitoriproject.script.parameter.Expression;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @Author: natsumi
 * @CreateTime: 2024-07-09  15:38
 * @Description: ?
 */
public class HasConeEntityCommand extends AbstractCommand {

    public Expression radius;
    public Expression fov;


    public void compile() {
        radius = new Expression(parameters[0]);
        fov = new Expression(parameters[1]);
    }

    public HasConeEntityCommand(int depth, String[] parameters) {
        super(depth, "hasConeEntity", parameters);
    }

    public static List<LivingEntity> getConeEntities(Player player, double radius, double fov) {
        List<Entity> nearbyEntities = player.getNearbyEntities(radius, radius, radius);
        return nearbyEntities.stream()
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .filter(livingEntity -> isEntityInCone(player, livingEntity, fov))
                .collect(Collectors.toList());
    }


    public static boolean isEntityInCone(Player player, LivingEntity target, double fov) {
        double angle = getAngleBetween(player, target.getLocation(), true);
        return angle >= 0 && angle < fov;
    }

    private static double getAngleBetween(LivingEntity viewer, Location targetLocation, boolean ignoreVertical) {
        if (!viewer.getWorld().equals(targetLocation.getWorld())) {
            return -999.0;
        }

        Location viewerEyeLocation = viewer.getEyeLocation();
        Vector toTarget = targetLocation.clone().subtract(viewerEyeLocation).toVector();
        Vector viewerDirection = viewer.getLocation().getDirection();

        if (ignoreVertical) {
            toTarget.setY(0);
            viewerDirection.setY(0);
        }

        return Math.toDegrees(viewerDirection.angle(toTarget));
    }

    public static boolean hasEntitiesInCone(Player player, double radius, double fov) {
        List<Entity> nearbyEntities = player.getNearbyEntities(radius, radius, radius);
        return nearbyEntities.stream()
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .anyMatch(livingEntity -> isEntityInCone(player, livingEntity, fov));
    }

    public static Location getLocationOutsideEntity(Entity entity, Location referenceLocation, double distance) {
        try {
            Method getHandleMethod = entity.getClass().getMethod("getHandle");
            getHandleMethod.setAccessible(true);
            Object nmsEntity = getHandleMethod.invoke(entity);
            Field boundingBoxField = net.minecraft.server.v1_12_R1.Entity.class.getDeclaredField("boundingBox");
            boundingBoxField.setAccessible(true);
            Object boundingBox = boundingBoxField.get(nmsEntity);
            Field minXField = boundingBox.getClass().getDeclaredField("a");
            Field minYField = boundingBox.getClass().getDeclaredField("b");
            Field minZField = boundingBox.getClass().getDeclaredField("c");
            Field maxXField = boundingBox.getClass().getDeclaredField("d");
            Field maxYField = boundingBox.getClass().getDeclaredField("e");
            Field maxZField = boundingBox.getClass().getDeclaredField("f");
            minXField.setAccessible(true);
            minYField.setAccessible(true);
            minZField.setAccessible(true);
            maxXField.setAccessible(true);
            maxYField.setAccessible(true);
            maxZField.setAccessible(true);
            double minX = minXField.getDouble(boundingBox);
            double minY = minYField.getDouble(boundingBox);
            double minZ = minZField.getDouble(boundingBox);
            double maxX = maxXField.getDouble(boundingBox);
            double maxY = maxYField.getDouble(boundingBox);
            double maxZ = maxZField.getDouble(boundingBox);
            double centerX = (minX + maxX) / 2.0;
            double centerY = (minY + maxY) / 2.0;
            double centerZ = (minZ + maxZ) / 2.0;
            Location centerLocation = new Location(entity.getWorld(), centerX, centerY, centerZ);
            Vector direction = referenceLocation.toVector().subtract(centerLocation.toVector()).normalize();
            Location edgeLocation = centerLocation.clone().add(direction.multiply(Math.max(maxX - minX, Math.max(maxY - minY, maxZ - minZ)) / 2));
            return edgeLocation.clone().add(direction.multiply(distance));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static double getEntityCollisionVolume(Entity entity) {
        try {
            Method getHandleMethod = entity.getClass().getMethod("getHandle");
            Object nmsEntity = getHandleMethod.invoke(entity);
            Field boundingBoxField = nmsEntity.getClass().getDeclaredField("boundingBox");
            Object boundingBox = boundingBoxField.get(nmsEntity);
            Field minXField = boundingBox.getClass().getDeclaredField("minX");
            Field minZField = boundingBox.getClass().getDeclaredField("minZ");
            Field maxXField = boundingBox.getClass().getDeclaredField("maxX");
            Field maxZField = boundingBox.getClass().getDeclaredField("maxZ");
            double minX = minXField.getDouble(boundingBox);
            double minZ = minZField.getDouble(boundingBox);
            double maxX = maxXField.getDouble(boundingBox);
            double maxZ = maxZField.getDouble(boundingBox);
            double length = maxX - minX;
            double width = maxZ - minZ;
            return length * width;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static Location getLocationAtDistanceFromEnd(Location start, Location end, double distance) {
        if (start.getWorld() != end.getWorld()) {
            throw new IllegalArgumentException("Locations must be in the same world");
        }
        Vector startToEnd = end.toVector().subtract(start.toVector());
        double totalDistance = startToEnd.length();
        double distanceFromStart = totalDistance - distance;
        Vector direction = startToEnd.normalize();
        Vector newLocationVector = start.toVector().add(direction.multiply(distanceFromStart));
        Location newLocation = new Location(start.getWorld(), newLocationVector.getX(), newLocationVector.getY(), newLocationVector.getZ());
        newLocation.setYaw(end.getYaw());
        newLocation.setPitch(end.getPitch());
        return newLocation;
    }

    @Override
    public NestedCommandWrapper execute(PlayerDataAccessor playerDataAccessor, PerformReturnContent performReturnContent, @NotNull ConcurrentHashMap<String, Object> variables) {
        double interpretRadius = Double.parseDouble(this.radius.interpret(playerDataAccessor, variables).toString());
        double interpretFov = Double.parseDouble(fov.interpret(playerDataAccessor, variables).toString());
        boolean b = hasEntitiesInCone(playerDataAccessor.getPlayer(), interpretRadius, interpretFov);
        NestedCommandWrapper nestedCommandWrapper = new NestedCommandWrapper();
        performReturnContent.setResult(b);
        return nestedCommandWrapper;
    }

}
