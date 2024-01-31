package net.mehvahdjukaar.snowyspirit.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class MongoEntity extends GingyEntity implements PlayerRideableJumping {

    public MongoEntity(EntityType<? extends AbstractGolem> entityType, Level level) {
        super(entityType, level);
        this.setMaxUpStep(2);
    }

    @Override
    public boolean canJump() {
        return true;

    }

    @Override
    public void handleStartJump(int jumpPower) {

    }

    @Override
    public void handleStopJump() {

    }

    @Override
    public void onPlayerJump(int jumpPower) {
        float f = 0.01f;
        double d = jumpPower * (double) this.getBlockJumpFactor() * f;
        double e = d + (double) this.getJumpBoostPower();
        Vec3 vec32 = this.getDeltaMovement();
        this.setDeltaMovement(vec32.x, e, vec32.z);
        //this.setIsJumping(true);
        this.hasImpulse = true;
        //  if (vec3.z > 0.0) {
        float g = Mth.sin(this.getYRot() * 0.017453292F);
        float h = Mth.cos(this.getYRot() * 0.017453292F);
        this.setDeltaMovement(this.getDeltaMovement().add((double) (-0.4F * g * f), 0.0, (0.4F * h * f)));
        // }
    }

    public boolean isPushable() {
        return !this.isVehicle();
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
        //return super.causeFallDamage(fallDistance, multiplier, source);
    }


    @Nullable
    public LivingEntity getControllingPassenger() {
        var v = this.getFirstPassenger();
        return v instanceof LivingEntity le ? le : null;
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 vec3) {
        float f = player.xxa * 0.5F;
        float f1 = player.zza;
        if (f1 <= 0.0F) {
            f1 *= 0.25F;
        }
        return new Vec3(f, 0.0, f1);
    }

    @Override
    protected void tickRidden(Player player, Vec3 vec3) {
        super.tickRidden(player, vec3);
        this.setYRot(player.getYRot());
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        return 0.1f;
    }

    @Override
    public double getPassengersRidingOffset() {
        return this.getBbHeight() * 0.95;
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction callback) {
        if (level().isClientSide) {
            float scale = 10;
            float period = 2.5f * Mth.PI * scale;
            float limbSwingAmount = 0.0F;
            float walkAnim = 0.0F;
            if (this.isAlive()) {
                limbSwingAmount = this.walkAnimation.speed(0);
                walkAnim = this.walkAnimation.position(0);
                // cap limb swing
                // this looks wonky at higher values and can grow a lot, so we just use for smooth in and out of anim
                float maxLimbSwing = 1;
                if (limbSwingAmount > maxLimbSwing) {
                    limbSwingAmount = maxLimbSwing;
                }
            }
            //body v
            Vec3 v = new Vec3(0, 13 / 2f, 0);

            float angle = walkAnim * (Mth.TWO_PI / period);
            float sideSwayPower = 20 / scale;

            float cos = Mth.cos(angle);

            float walkCycleClamped = cos * Mth.clamp(limbSwingAmount, 0, 0.4f);

            v = v.zRot(Mth.DEG_TO_RAD * (cos * sideSwayPower * limbSwingAmount));
            //head v
            Vec3 hv = new Vec3(0, 5 / 2f + 2, 0.5);
            hv = hv.zRot(+walkCycleClamped * 0.3f);
            v = v.add(hv);

            v = v.yRot(-this.getYRot() * Mth.DEG_TO_RAD);

            callback.accept(passenger, this.getX() + v.x, this.getY() + v.y - 0.4, this.getZ() + v.z);
        } else super.positionRider(passenger, callback);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isAlive() && this.horizontalCollision && this.isControlledByLocalInstance()) {
            boolean bl = false;
            AABB aABB = this.getBoundingBox().inflate(1);
            Iterator<BlockPos> var8 = BlockPos.betweenClosed(Mth.floor(aABB.minX), Mth.floor(aABB.minY), Mth.floor(aABB.minZ), Mth.floor(aABB.maxX), Mth.floor(aABB.maxY), Mth.floor(aABB.maxZ)).iterator();

            label60:
            while (true) {
                BlockPos blockPos;
                Block block;
                do {
                    if (!var8.hasNext()) {
                        if (!bl && this.onGround()) {
                            this.jumpFromGround();
                        }
                        break label60;
                    }

                    blockPos = var8.next();
                    BlockState blockState = this.level().getBlockState(blockPos);
                    block = blockState.getBlock();
                } while (!(block instanceof LeavesBlock));

                bl = this.level().destroyBlock(blockPos, true, this) || bl;
            }
        }
    }

    @Override
    public int getMaxHeadXRot() {
        return 0;
    }

    public static AttributeSupplier.Builder createGiantAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 50.0)
                .add(Attributes.MOVEMENT_SPEED, 0.5);
    }


}
