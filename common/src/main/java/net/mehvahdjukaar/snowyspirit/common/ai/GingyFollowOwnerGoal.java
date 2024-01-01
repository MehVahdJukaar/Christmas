package net.mehvahdjukaar.snowyspirit.common.ai;

import net.mehvahdjukaar.snowyspirit.common.entity.GingyEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

import java.util.EnumSet;

public class GingyFollowOwnerGoal extends Goal {
    public static final int TELEPORT_WHEN_DISTANCE_IS = 12;
    private static final int MIN_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 2;
    private static final int MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 3;
    private static final int MAX_VERTICAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 1;
    private final GingyEntity tamable;
    private LivingEntity owner;
    private final LevelReader level;
    private final double speedModifier;
    private final PathNavigation navigation;
    private int timeToRecalcPath;
    private final float stopDistance;
    private final float startDistance;
    private float oldWaterCost;

    public GingyFollowOwnerGoal(GingyEntity tamableAnimal, double speedMod, float startDistance, float g, boolean bl) {
        this.tamable = tamableAnimal;
        this.level = tamableAnimal.level();
        this.speedModifier = speedMod;
        this.navigation = tamableAnimal.getNavigation();
        this.startDistance = startDistance;
        this.stopDistance = g;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        if (!(tamableAnimal.getNavigation() instanceof GroundPathNavigation) && !(tamableAnimal.getNavigation() instanceof FlyingPathNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
        }
    }

    public boolean canUse() {
        LivingEntity livingEntity = this.tamable.getOwner();
        if (livingEntity == null) {
            return false;
        } else if (livingEntity.isSpectator()) {
            return false;
        } else if (this.unableToMove()) {
            return false;
        } else if (this.tamable.distanceToSqr(livingEntity) < (double)(this.startDistance * this.startDistance)) {
            return false;
        } else {
            this.owner = livingEntity;
            return true;
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (this.navigation.isDone()) {
            return false;
        } else if (this.unableToMove()) {
            return false;
        } else {
            return this.tamable.distanceToSqr(this.owner) > (double)(this.stopDistance * this.stopDistance);
        }
    }

    private boolean unableToMove() {
        return this.tamable.isOrderedToSit() || this.tamable.isPassenger() || this.tamable.isLeashed();
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.tamable.getPathfindingMalus(BlockPathTypes.WATER);
        this.tamable.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.tamable.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
    }

    @Override
    public void tick() {
        this.tamable.getLookControl().setLookAt(this.owner, 10.0F, (float)this.tamable.getMaxHeadXRot());
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            boolean canTeleport = false;
            if (this.tamable.distanceToSqr(this.owner) >= 144.0 && canTeleport) {
                this.teleportToOwner();
            } else {
                this.navigation.moveTo(this.owner, this.speedModifier);
            }

        }
    }

    private void teleportToOwner() {
        BlockPos blockPos = this.owner.blockPosition();

        for(int i = 0; i < 10; ++i) {
            int j = this.randomIntInclusive(-3, 3);
            int k = this.randomIntInclusive(-1, 1);
            int l = this.randomIntInclusive(-3, 3);
            boolean bl = this.maybeTeleportTo(blockPos.getX() + j, blockPos.getY() + k, blockPos.getZ() + l);
            if (bl) {
                return;
            }
        }

    }

    private boolean maybeTeleportTo(int x, int y, int z) {
        if (Math.abs(x - this.owner.getX()) < 2.0 && Math.abs(z - this.owner.getZ()) < 2.0) {
            return false;
        } else if (!this.canTeleportTo(new BlockPos(x, y, z))) {
            return false;
        } else {
            this.tamable.moveTo(x + 0.5, y, z + 0.5, this.tamable.getYRot(), this.tamable.getXRot());
            this.navigation.stop();
            return true;
        }
    }

    private boolean canTeleportTo(BlockPos pos) {
        BlockPathTypes blockPathTypes = WalkNodeEvaluator.getBlockPathTypeStatic(this.level, pos.mutable());
        if (blockPathTypes != BlockPathTypes.WALKABLE) {
            return false;
        } else {
            BlockState blockState = this.level.getBlockState(pos.below());
            if (blockState.getBlock() instanceof LeavesBlock) {
                return false;
            } else {
                BlockPos blockPos = pos.subtract(this.tamable.blockPosition());
                return this.level.noCollision(this.tamable, this.tamable.getBoundingBox().move(blockPos));
            }
        }
    }

    private int randomIntInclusive(int min, int max) {
        return this.tamable.getRandom().nextInt(max - min + 1) + min;
    }
}
