package io.aoitori043.aoitoriproject.utils.raytrace;

import net.minecraft.server.v1_12_R1.AxisAlignedBB;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * @Author: natsumi
 * @CreateTime: 2023-11-18  17:11
 * @Description: ?
 */
public class MethodSupplement {

//    public static Collection<Entity> getNearbyEntities(World world, BoundingBox boundingBox, Predicate<Entity> filter) {
//        Validate.notNull(boundingBox, "Bounding box is null!");
//        AxisAlignedBB bb = new AxisAlignedBB(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(), boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
//        List<net.minecraft.server.v1_12_R1.Entity> entityList = ((CraftWorld)world).getHandle().getEntities((net.minecraft.server.v1_12_R1.Entity)null, bb, null);
//        List<Entity> bukkitEntityList = new ArrayList(entityList.size());
//        Iterator var7 = entityList.iterator();
//
//        while(true) {
//            CraftEntity bukkitEntity;
//            do {
//                if (!var7.hasNext()) {
//                    return bukkitEntityList;
//                }
//
//                net.minecraft.server.v1_12_R1.Entity entity = (net.minecraft.server.v1_12_R1.Entity)var7.next();
//                bukkitEntity = entity.getBukkitEntity();
//            } while(filter != null && !filter.test(bukkitEntity));
//
//            bukkitEntityList.add(bukkitEntity);
//        }
//    }

    public static RayTraceResult rayTrace(BoundingBox boundingBox,Vector start, Vector direction, double maxDistance) {
        Validate.notNull(start, "Start is null!");
        start.checkFinite();
        Validate.notNull(direction, "Direction is null!");
        direction.checkFinite();
        Validate.isTrue(direction.lengthSquared() > 0.0, "Direction's magnitude is 0!");
        if (maxDistance < 0.0) {
            return null;
        } else {
            double startX = start.getX();
            double startY = start.getY();
            double startZ = start.getZ();
            Vector dir = direction.clone().normalize();
            double dirX = dir.getX();
            double dirY = dir.getY();
            double dirZ = dir.getZ();
            double divX = 1.0 / dirX;
            double divY = 1.0 / dirY;
            double divZ = 1.0 / dirZ;
            double tMin;
            double tMax;
            BlockFace hitBlockFaceMin;
            BlockFace hitBlockFaceMax;
            if (dirX >= 0.0) {
                tMin = (boundingBox.getMinX() - startX) * divX;
                tMax = (boundingBox.getMaxX() - startX) * divX;
                hitBlockFaceMin = BlockFace.WEST;
                hitBlockFaceMax = BlockFace.EAST;
            } else {
                tMin = (boundingBox.getMaxX() - startX) * divX;
                tMax = (boundingBox.getMinX() - startX) * divX;
                hitBlockFaceMin = BlockFace.EAST;
                hitBlockFaceMax = BlockFace.WEST;
            }

            double tyMin;
            double tyMax;
            BlockFace hitBlockFaceYMin;
            BlockFace hitBlockFaceYMax;
            if (dirY >= 0.0) {
                tyMin = (boundingBox.getMinY() - startY) * divY;
                tyMax = (boundingBox.getMaxY() - startY) * divY;
                hitBlockFaceYMin = BlockFace.DOWN;
                hitBlockFaceYMax = BlockFace.UP;
            } else {
                tyMin = (boundingBox.getMaxY() - startY) * divY;
                tyMax = (boundingBox.getMinY() - startY) * divY;
                hitBlockFaceYMin = BlockFace.UP;
                hitBlockFaceYMax = BlockFace.DOWN;
            }

            if ((tMin > tyMax) || (tMax < tyMin)) {
                return null;
            } else {
                if (tyMin > tMin) {
                    tMin = tyMin;
                    hitBlockFaceMin = hitBlockFaceYMin;
                }

                if (tyMax < tMax) {
                    tMax = tyMax;
                    hitBlockFaceMax = hitBlockFaceYMax;
                }

                double tzMin;
                double tzMax;
                BlockFace hitBlockFaceZMin;
                BlockFace hitBlockFaceZMax;
                if (dirZ >= 0.0) {
                    tzMin = (boundingBox.getMinZ() - startZ) * divZ;
                    tzMax = (boundingBox.getMaxZ() - startZ) * divZ;
                    hitBlockFaceZMin = BlockFace.NORTH;
                    hitBlockFaceZMax = BlockFace.SOUTH;
                } else {
                    tzMin = (boundingBox.getMaxZ() - startZ) * divZ;
                    tzMax = (boundingBox.getMinZ() - startZ) * divZ;
                    hitBlockFaceZMin = BlockFace.SOUTH;
                    hitBlockFaceZMax = BlockFace.NORTH;
                }

                if ((tMin > tzMax) || (tMax < tzMin)) {
                    return null;
                } else {
                    if (tzMin > tMin) {
                        tMin = tzMin;
                        hitBlockFaceMin = hitBlockFaceZMin;
                    }

                    if (tzMax < tMax) {
                        tMax = tzMax;
                        hitBlockFaceMax = hitBlockFaceZMax;
                    }

                    if (tMax < 0.0) {
                        return null;
                    } else if (tMin > maxDistance) {
                        return null;
                    } else {
                        double t;
                        BlockFace hitBlockFace;
                        if (tMin < 0.0) {
                            t = tMax;
                            hitBlockFace = hitBlockFaceMax;
                        } else {
                            t = tMin;
                            hitBlockFace = hitBlockFaceMin;
                        }

                        Vector hitPosition = dir.multiply(t).add(start);
                        return new RayTraceResult(hitPosition, hitBlockFace);
                    }
                }
            }
        }
    }

    public static BoundingBox getBoundingBox(Entity entity) {
        AxisAlignedBB bb = ((CraftEntity)entity).getHandle().getBoundingBox();
        return new BoundingBox(bb.a, bb.b, bb.c, bb.d, bb.e, bb.f);
    }

    public static Collection<Entity> getNearbyEntities(World world, BoundingBox boundingBox, Predicate<Entity> filter) {
        Validate.notNull(boundingBox, "Bounding box is null!");
        AxisAlignedBB bb = new AxisAlignedBB(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(), boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
        List<net.minecraft.server.v1_12_R1.Entity> entityList = ((CraftWorld)world).getHandle().getEntities(null, bb, null);
        List<Entity> bukkitEntityList = new ArrayList(entityList.size());
        for (net.minecraft.server.v1_12_R1.Entity entity : entityList) {
            if(filter != null && !filter.test(entity.getBukkitEntity())){
                bukkitEntityList.add(entity.getBukkitEntity());
            }
        }
        return bukkitEntityList;
    }

    public static RayTraceResult rayTraceEntities(LivingEntity originEntity,Location start, Vector direction, double maxDistance, double raySize) {
        Validate.notNull(start, "Start location is null!");
//        Validate.isTrue(this.equals(start.getWorld()), "Start location is from different world!");
        start.checkFinite();
        Validate.notNull(direction, "Direction is null!");
        direction.checkFinite();
        Validate.isTrue(direction.lengthSquared() > 0.0, "Direction's magnitude is 0!");
        if (maxDistance < 0.0) {
            return null;
        } else {
            Vector startPos = start.toVector();
            Vector dir = direction.clone().normalize().multiply(maxDistance);

            BoundingBox aabb = BoundingBox.of(startPos, startPos).expandDirectional(dir).expand(raySize);
            Collection<Entity> entities = getNearbyEntities(start.getWorld(),aabb,s -> s==originEntity);
            Entity nearestHitEntity = null;
            RayTraceResult nearestHitResult = null;
            double nearestDistanceSq = Double.MAX_VALUE;

            for (Entity entity : entities) {
                if(!(entity instanceof LivingEntity) || entity instanceof Player){
                    continue;
                }
                BoundingBox boundingBox = getBoundingBox(entity).expand(raySize);
                RayTraceResult hitResult = boundingBox.rayTrace(startPos, direction, maxDistance);
                if (hitResult != null) {
                    double distanceSq = startPos.distanceSquared(hitResult.getHitPosition());
                    if (distanceSq < nearestDistanceSq) {
                        nearestHitEntity = entity;
                        nearestHitResult = hitResult;
                        nearestDistanceSq = distanceSq;
                    }
                }
            }

            return nearestHitEntity == null ? null : new RayTraceResult(nearestHitResult.getHitPosition(), nearestHitEntity, nearestHitResult.getHitBlockFace());
        }
    }

    public static Entity getRayTraceEntity(LivingEntity entity,double maxDistance,double raySize){
        RayTraceResult rayTraceResult = rayTraceEntities(entity,entity.getLocation(), entity.getEyeLocation().getDirection(), maxDistance, raySize);
        if(rayTraceResult == null || rayTraceResult.getHitEntity()==null){
            return null;
        }
        return rayTraceResult.getHitEntity();
    }


}
