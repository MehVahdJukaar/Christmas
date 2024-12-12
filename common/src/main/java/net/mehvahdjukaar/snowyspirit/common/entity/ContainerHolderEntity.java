package net.mehvahdjukaar.snowyspirit.common.entity;

import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.snowyspirit.SnowySpirit;
import net.mehvahdjukaar.snowyspirit.integration.supp.SuppCompat;
import net.mehvahdjukaar.snowyspirit.reg.ModRegistry;
import net.mehvahdjukaar.snowyspirit.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

public class ContainerHolderEntity extends Entity implements Container, MenuProvider {
    private static final EntityDataAccessor<Integer> DATA_ID_HURT = SynchedEntityData.defineId(ContainerHolderEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_ID_DAMAGE = SynchedEntityData.defineId(ContainerHolderEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<ItemStack> DATA_CONTAINER = SynchedEntityData.defineId(ContainerHolderEntity.class, EntityDataSerializers.ITEM_STACK);

    //for client
    private BlockState displayState = Blocks.AIR.defaultBlockState();

    private BaseContainerBlockEntity innerBlockEntity;

    public ContainerHolderEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.blocksBuilding = true;
    }

    protected ContainerHolderEntity(Level level, Entity sled, ItemStack containerStack) {
        this(ModRegistry.CONTAINER_ENTITY.get(), level);
        this.setContainerItem(containerStack);
        this.setPos(sled.position());
        if (this.startRiding(sled)) {
            //this causes issues
            sled.positionRider(this);
        }
    }

    public BlockState getDisplayState() {
        return displayState;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> dataAccessor) {
        super.onSyncedDataUpdated(dataAccessor);
        if (dataAccessor.equals(DATA_CONTAINER)) {
            ItemStack stack = this.entityData.get(DATA_CONTAINER);

            if (stack.getItem() instanceof BlockItem blockItem) {
                this.displayState = blockItem.getBlock().defaultBlockState();
            }
            if (stack.getItem() instanceof BlockItem bi) {
                Block block = bi.getBlock();

                if (block instanceof EntityBlock eb && eb.newBlockEntity(BlockPos.ZERO, block.defaultBlockState()) instanceof BaseContainerBlockEntity c) {
                    innerBlockEntity = c;
                    innerBlockEntity.setLevel(level());
                }
            }
            if (innerBlockEntity == null) {
                throw new IllegalStateException("block {} does not provide a valid container block entity");
            }
            if (isContainerWithNBT(stack) && stack.hasFoil()) {
                CompoundTag tag = stack.get(ModRegistry.CONTAINER_BLOCK_ENTITY_TAG.get());
                if (tag != null) innerBlockEntity.loadWithComponents(tag, registryAccess());
            }
        }
    }

    public void setContainerItem(ItemStack stack) {
        this.entityData.set(DATA_CONTAINER, stack);
    }

    public ItemStack getContainerItem() {
        return this.entityData.get(DATA_CONTAINER);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setContainerItem(ItemStack.OPTIONAL_CODEC.decode(NbtOps.INSTANCE, tag.getCompound("ContainerItem")).getOrThrow().getFirst());
        if (innerBlockEntity == null) {
            int aaa = 1;
        } else innerBlockEntity.loadWithComponents(tag, this.registryAccess());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.put("ContainerItem", this.getContainerItem().save(this.registryAccess(), new CompoundTag()));
        tag.merge(innerBlockEntity.saveWithoutMetadata(this.registryAccess()));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ID_HURT, 0);
        builder.define(DATA_ID_DAMAGE, 0.0F);
        builder.define(DATA_CONTAINER, ItemStack.EMPTY);
    }
/* No longer needed, default fallback is to VEHICLE, (Entity#getVehicleAttachmentPoint)
    @Override
    public float getMyRidingOffset(Entity entity) {
        return 0;
    }
  */

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (!this.level().isClientSide && !this.isRemoved()) {
            if (this.isInvulnerableTo(pSource)) {
                return false;
            } else {
                this.setHurtTime(10);
                this.markHurt();
                this.setDamage(this.getDamage() + pAmount * 10.0F);
                this.gameEvent(GameEvent.ENTITY_DAMAGE, pSource.getEntity());
                boolean flag = pSource.getEntity() instanceof Player player && player.getAbilities().instabuild;
                if (flag || this.getDamage() > 15.0F) {
                    this.ejectPassengers();
                    if (flag && !this.hasCustomName()) {
                        this.discard();
                    } else {
                        this.destroy(pSource);
                    }
                }
            }
        }
        return true;
    }

    public void destroy(DamageSource pSource) {
        Level level = this.level();
        if (level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            if (!level.isClientSide) {
                Entity entity = pSource.getDirectEntity();
                if (entity != null && entity.getType() == EntityType.PLAYER) {
                    PiglinAi.angerNearbyPiglins((Player) entity, true);
                }
            }
        }
        this.remove(Entity.RemovalReason.KILLED);
    }

    public void spawnDrops() {
        ItemStack stack = this.getContainerItem().copy();
        if (this.hasCustomName()) {
            stack.set(DataComponents.CUSTOM_NAME, this.getCustomName());
        }
        //sacks and shulker. kind of ugly here
        if (isContainerWithNBT(this.getContainerItem())) {
            stack.set(ModRegistry.CONTAINER_BLOCK_ENTITY_TAG.get(), innerBlockEntity.saveWithoutMetadata(this.registryAccess()));
        } else {
            Containers.dropContents(this.level(), this, innerBlockEntity);
        }
        this.spawnAtLocation(stack);
    }

    /**
     * Setups the entity to do the hurt animation. Only used by packets in multiplayer.
     */
    @Override
    public void animateHurt(float hurtYaw) {
        this.setHurtTime(10);
        this.setDamage(this.getDamage() + this.getDamage() * 10.0F);
    }

    /**
     * Returns true if other Entities should be prevented from moving through this Entity.
     */
    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void tick() {

        Entity v = this.getVehicle();
        if (v != null) {

            if (this.getHurtTime() > 0) {
                this.setHurtTime(this.getHurtTime() - 1);
            }

            if (this.getDamage() > 0.0F) {
                this.setDamage(this.getDamage() - 1.0F);
            }

            this.checkBelowWorld();
            this.handlePortal();

            super.tick();

            // this.xRotO = v.xRotO;
            // this.yRotO = v.yRotO;
            //this.setYRot(v.getYRot());
            // this.xRotO = this.getXRot();
            //this.yRotO = this.getYRot();
        } else {
             if(!level().isClientSide)  this.destroy(this.damageSources().generic());
        }
    }

    /**
     * Sets the current amount of damage the minecart has taken. Decreases over time. The cart breaks when this is over
     * 40.
     */
    public void setDamage(float pDamage) {
        this.entityData.set(DATA_ID_DAMAGE, pDamage);
    }

    /**
     * Gets the current amount of damage the minecart has taken. Decreases over time. The cart breaks when this is over
     * 40.
     */
    public float getDamage() {
        return this.entityData.get(DATA_ID_DAMAGE);
    }

    /**
     * Sets the rolling amplitude the cart rolls while being attacked.
     */
    public void setHurtTime(int pRollingAmplitude) {
        this.entityData.set(DATA_ID_HURT, pRollingAmplitude);
    }

    /**
     * Gets the rolling amplitude the cart rolls while being attacked.
     */
    public int getHurtTime() {
        return this.entityData.get(DATA_ID_HURT);
    }

    //@Override
    @PlatformOnly(PlatformOnly.FORGE)
    public ItemStack getPickedResult(HitResult target) {
        return this.getContainerItem().copy();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("message.snowyspirit.container_entity_name",
                this.getContainerItem().getItem().getDescription().getString());
    }

    @Override
    public void remove(Entity.RemovalReason pReason) {
        if (!this.level().isClientSide && pReason.shouldDestroy()) {
            if (this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                this.spawnDrops();
            }
        }
        super.remove(pReason);
    }

    @Override
    public InteractionResult interact(Player pPlayer, InteractionHand pHand) {
        InteractionResult ret = super.interact(pPlayer, pHand);
        if (ret.consumesAction()) return ret;
        if (!pPlayer.level().isClientSide) {
            PlatHelper.openCustomMenu((ServerPlayer) pPlayer, this, b -> {
                //just for sack
                b.writeBoolean(false);
                b.writeVarInt(this.getId());
                b.writeInt(this.getContainerSize());
            });
            this.gameEvent(GameEvent.CONTAINER_OPEN, pPlayer);
            PiglinAi.angerNearbyPiglins(pPlayer, true);
            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.SUCCESS;
        }
    }

    //TODO: apply slow

    /**
     * from 0 to 1. How much should it slow down the sled
     */
    public float getWeightFromItems() {
        return AbstractContainerMenu.getRedstoneSignalFromContainer(innerBlockEntity) / 15f;
    }

    public static boolean isValidContainer(ItemStack stack) {
        return isNormalContainer(stack) || isContainerWithNBT(stack);
    }

    private static boolean isNormalContainer(ItemStack stack) {
        return stack.is(ModTags.VALID_CONTAINERS) && stack.getItem() instanceof BlockItem;
    }

    private static boolean isContainerWithNBT(ItemStack stack) {
        Item i = stack.getItem();
        return isShulkerBox(i) || isSack(i);
    }

    private static boolean isShulkerBox(Item i) {
        return i instanceof BlockItem bi && (bi.getBlock() instanceof ShulkerBoxBlock);
    }

    private static boolean isSack(Item i) {
        return SnowySpirit.SUPPLEMENTARIES_INSTALLED && SuppCompat.isSack(i);
    }


    //just delegates to inner container
    @Override
    public int getContainerSize() {
        return innerBlockEntity.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return innerBlockEntity.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return innerBlockEntity.getItem(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return innerBlockEntity.removeItem(slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return innerBlockEntity.removeItemNoUpdate(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        innerBlockEntity.setItem(slot, stack);
    }

    @Override
    public void setChanged() {
    }

    //just for this..
    @Override
    public boolean stillValid(Player player) {
        if (this.isRemoved()) {
            return false;
        } else {
            return player.distanceToSqr(this) <= 64.0D;
        }
    }

    @Override
    public void clearContent() {
        innerBlockEntity.clearContent();
    }

    public void setLootTable(ResourceLocation res, long seed) {
        if (innerBlockEntity instanceof RandomizableContainerBlockEntity r) {
            r.setLootTable(ResourceKey.create(Registries.LOOT_TABLE, res), seed);
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory pPlayerInventory, Player player) {
        //hardcoded since we need to pass this, not the tile...
        if (isSack(getContainerItem().getItem())) {
            return SuppCompat.createSackMenu(id, pPlayerInventory, this);
        } else if (!isNormalContainer(getContainerItem())) {
            return new ShulkerBoxMenu(id, pPlayerInventory, this);
        }
        return ChestMenu.threeRows(id, pPlayerInventory, this);

    }
}
