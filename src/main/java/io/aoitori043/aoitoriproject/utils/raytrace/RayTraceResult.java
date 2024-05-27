package io.aoitori043.aoitoriproject.utils.raytrace;

import org.apache.commons.lang.Validate;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * @Author: natsumi
 * @CreateTime: 2023-12-23  00:37
 * @Description: ?
 */
public class RayTraceResult {
    private final Vector hitPosition;
    private final Block hitBlock;
    private final BlockFace hitBlockFace;
    private final Entity hitEntity;

    @Override
    public String toString() {
        return new StringJoiner(", ", RayTraceResult.class.getSimpleName() + "[", "]")
                .add("hitPosition=" + hitPosition)
                .add("hitBlock=" + hitBlock)
                .add("hitBlockFace=" + hitBlockFace)
                .add("hitEntity=" + hitEntity)
                .toString();
    }

    private RayTraceResult(@NotNull Vector hitPosition, Block hitBlock, BlockFace hitBlockFace, Entity hitEntity) {
        Validate.notNull(hitPosition, "Hit position is null!");
        this.hitPosition = hitPosition.clone();
        this.hitBlock = hitBlock;
        this.hitBlockFace = hitBlockFace;
        this.hitEntity = hitEntity;
    }

    public RayTraceResult(@NotNull Vector hitPosition) {
        this(hitPosition, (Block)null, (BlockFace)null, (Entity)null);
    }

    public RayTraceResult(@NotNull Vector hitPosition, BlockFace hitBlockFace) {
        this(hitPosition, (Block)null, hitBlockFace, (Entity)null);
    }

    public RayTraceResult(@NotNull Vector hitPosition, Block hitBlock, BlockFace hitBlockFace) {
        this(hitPosition, hitBlock, hitBlockFace, (Entity)null);
    }

    public RayTraceResult(@NotNull Vector hitPosition,  Entity hitEntity) {
        this(hitPosition, (Block)null, (BlockFace)null, hitEntity);
    }

    public RayTraceResult(@NotNull Vector hitPosition,  Entity hitEntity,  BlockFace hitBlockFace) {
        this(hitPosition, (Block)null, hitBlockFace, hitEntity);
    }

    @NotNull
    public Vector getHitPosition() {
        return this.hitPosition.clone();
    }

    public Block getHitBlock() {
        return this.hitBlock;
    }

    public BlockFace getHitBlockFace() {
        return this.hitBlockFace;
    }

    public Entity getHitEntity() {
        return this.hitEntity;
    }

    public int hashCode() {
        boolean prime = true;
        int result = 1;
        result = 31 * result + this.hitPosition.hashCode();
        result = 31 * result + (this.hitBlock == null ? 0 : this.hitBlock.hashCode());
        result = 31 * result + (this.hitBlockFace == null ? 0 : this.hitBlockFace.hashCode());
        result = 31 * result + (this.hitEntity == null ? 0 : this.hitEntity.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof RayTraceResult)) {
            return false;
        } else {
            RayTraceResult other = (RayTraceResult)obj;
            if (!this.hitPosition.equals(other.hitPosition)) {
                return false;
            } else if (!Objects.equals(this.hitBlock, other.hitBlock)) {
                return false;
            } else if (!Objects.equals(this.hitBlockFace, other.hitBlockFace)) {
                return false;
            } else {
                return Objects.equals(this.hitEntity, other.hitEntity);
            }
        }
    }

}