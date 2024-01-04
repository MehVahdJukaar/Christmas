package net.mehvahdjukaar.snowyspirit.common.entity;

import net.mehvahdjukaar.moonlight.api.entity.IControllableVehicle;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.snowyspirit.SnowySpirit;
import net.mehvahdjukaar.snowyspirit.common.ai.GingyFollowOwnerGoal;
import net.mehvahdjukaar.snowyspirit.common.ai.GingySitWhenOrderedToGoal;
import net.mehvahdjukaar.snowyspirit.reg.ModRegistry;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class GingyEntity extends AbstractGolem implements OwnableEntity {
    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(GingyEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(GingyEntity.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<Integer> DATA_COLOR_ID = SynchedEntityData.defineId(GingyEntity.class, EntityDataSerializers.INT);


    public GingyEntity(EntityType<? extends AbstractGolem> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new GingySitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(6, new GingyFollowOwnerGoal(this, 1.0, 7.0F, 3.5F, false));
        this.goalSelector.addGoal(11, new WaterAvoidingRandomStrollGoal(this, 0.8, 1.0000001E-5F));
        this.goalSelector.addGoal(12, new LookAtPlayerGoal(this, Player.class, 10.0F));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte) 0);
        this.entityData.define(DATA_OWNERUUID_ID, Optional.empty());
        this.entityData.define(DATA_COLOR_ID, DyeColor.WHITE.ordinal());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        DyeColor d = getColor();
        compound.putInt("Color", d.getId());
        if (this.getOwnerUUID() != null) {
            compound.putUUID("Owner", this.getOwnerUUID());
        }
        compound.putBoolean("Sitting", this.isOrderedToSit());
        BodyIntegrity bodyIntegrity = this.getBodyIntegrity();
        if (bodyIntegrity != BodyIntegrity.FULL) {
            compound.putInt("Bites", bodyIntegrity.ordinal());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        UUID uUID;
        if (compound.hasUUID("Owner")) {
            uUID = compound.getUUID("Owner");
            this.setOwnerUUID(uUID);
        }
        if (compound.contains("Color", 99)) {
            this.setColor(DyeColor.byId(compound.getInt("Color")));
        }
        this.setOrderedToSit(compound.getBoolean("Sitting"));

        if (compound.contains("Bites", 99)) {
            this.setBodyIntegrity(BodyIntegrity.values()[(compound.getInt("Bites"))]);
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        this.setColor(DyeColor.values()[level.getRandom().nextInt(DyeColor.values().length)]);
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return super.getStandingEyeHeight(pose, dimensions);
    }

    @Override
    public boolean isSensitiveToWater() {
        return true;
    }

    public DyeColor getColor() {
        return DyeColor.byId(this.entityData.get(DATA_COLOR_ID));
    }

    public void setColor(DyeColor collarColor) {
        this.entityData.set(DATA_COLOR_ID, collarColor.getId());
    }

    public boolean isForwardDeathAnim() {
        return ((this.entityData.get(DATA_FLAGS_ID) & 0b10000000) >> 7) == 1;
    }

    public void setForwardDeathAnim(boolean forward) {
        byte b = this.entityData.get(DATA_FLAGS_ID);
        this.entityData.set(DATA_FLAGS_ID, (byte) ((b & 0b01111111) | ((forward ? 1 : 0) << 7)));
    }

    public BodyIntegrity getBodyIntegrity() {
        return BodyIntegrity.values()[(this.entityData.get(DATA_FLAGS_ID) & 0b00001110) >> 1];
    }

    public boolean increaseIntegrity() {
        BodyIntegrity b = getBodyIntegrity();
        BodyIntegrity[] values = BodyIntegrity.values();
        int ind = b.ordinal();
        if (ind > 0) {
            setBodyIntegrity(values[ind - 1]);
            return true;
        }
        return false;
    }

    public boolean decreaseIntegrity() {
        BodyIntegrity b = getBodyIntegrity();
        BodyIntegrity[] values = BodyIntegrity.values();
        int ind = b.ordinal();
        if (ind < values.length - 1) {
            setBodyIntegrity(values[ind + 1]);
            return true;
        }
        return false;
    }


    public void setBodyIntegrity(BodyIntegrity bodyIntegrity) {
        byte b = this.entityData.get(DATA_FLAGS_ID);
        int ind = bodyIntegrity.ordinal();
        this.entityData.set(DATA_FLAGS_ID, (byte) ((b & 0b11110001) | (ind << 1)));
    }

    public boolean isOrderedToSit() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    public void setOrderedToSit(boolean sitting) {
        byte b = this.entityData.get(DATA_FLAGS_ID);
        if (sitting) {
            this.entityData.set(DATA_FLAGS_ID, (byte) (b | 1));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte) (b & -2));
        }
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNERUUID_ID).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable(uuid));
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !this.isOwnedBy(target) && super.canAttack(target);
    }

    public boolean isOwnedBy(LivingEntity entity) {
        return entity == this.getOwner();
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        LivingEntity livingEntity = this.getOwner();
        if (entity == livingEntity) {
            return true;
        }
        if (livingEntity != null) {
            return livingEntity.isAlliedTo(entity);
        }
        return super.isAlliedTo(entity);
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide && this.level().getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES) && this.getOwner() instanceof ServerPlayer) {
            this.getOwner().sendSystemMessage(this.getCombatTracker().getDeathMessage());
        }

        super.die(damageSource);
    }

    @Override
    protected int decreaseAirSupply(int currentAir) {
        return currentAir;
    }

    public boolean isFood(ItemStack stack) {
        return stack.is(ModRegistry.GINGERBREAD_COOKIE.get());
    }

    protected void usePlayerItem(Player player, InteractionHand hand, ItemStack stack) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }



    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        Item item = itemStack.getItem();
        Level level = level();


        if (this.isOwnedBy(player)) {
            if (item.isEdible() && this.isFood(itemStack) && this.getHealth() < this.getMaxHealth()) {
                this.usePlayerItem(player, hand, itemStack);
                this.heal(item.getFoodProperties().getNutrition());
                return InteractionResult.sidedSuccess(level.isClientSide);
            }

            if (player.isSecondaryUseActive()) {
                this.setOrderedToSit(!this.isOrderedToSit());
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        if(this.getBbHeight()>2){
            player.startRiding(this);
            return InteractionResult.sidedSuccess(level.isClientSide);

        }else {
            if (itemStack.is(Items.MILK_BUCKET)) {
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                    Utils.swapItem(player, hand, itemStack, Items.MILK_BUCKET.getCraftingRemainingItem().getDefaultInstance());
                }
                this.addEffect(new MobEffectInstance(MobEffects.POISON, 900));
                if (player.isCreative() || !this.isInvulnerable()) {
                    var oldMov = this.getDeltaMovement();
                    this.hurt(this.damageSources().playerAttack(player), Float.MAX_VALUE);
                    this.setDeltaMovement(oldMov);
                    setForwardDeathAnim(true);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            } else if (itemStack.is(ModRegistry.GINGERBREAD_COOKIE.get())) {
                this.increaseIntegrity();
                this.playSound(ModRegistry.GINGERBREAD_BLOCK.get()
                        .getSoundType(ModRegistry.GINGERBREAD_BLOCK.get().defaultBlockState()).getPlaceSound(), 1, 0.2f);
                itemStack.shrink(1);
                return InteractionResult.sidedSuccess(level.isClientSide);
            } else if (player.canEat(player.isCreative())) {
                if (player instanceof ServerPlayer sp) {
                    SnowySpirit.giveAdvancement(sp, "husbandry/eat_gingerbread_golem");
                }
                if (!this.decreaseIntegrity()) {
                    this.discard();
                }
                player.playSound(player.getEatingSound(ModRegistry.GINGERBREAD_COOKIE.get().getDefaultInstance()));
                player.getFoodData().eat(1, 0.1F);
                level.gameEvent(player, GameEvent.EAT, this.blockPosition());
                for (int j = 0; j < 15; j++) {
                    level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, ModRegistry.GINGERBREAD_FROSTED_BLOCK.get().defaultBlockState()),
                            this.getRandomX(1), this.getRandomY() + 0.2, this.getRandomZ(1.0), 0, 0, 0);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        InteractionResult interactionResult = super.mobInteract(player, hand);
        if (interactionResult.consumesAction()) {
            this.setPersistenceRequired();
        }
        return interactionResult;
    }

    @Override
    public int getMaxHeadYRot() {
        return 30;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25);
    }


    public enum BodyIntegrity {
        FULL,
        NO_LEFT_HAND,
        NO_HANDS,
        NO_HEAD,
        NO_BODY
    }
}
