package net.mehvahdjukaar.snowyspirit.common.entity;

import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.moonlight.api.entity.IExtraClientSpawnData;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.snowyspirit.SnowySpirit;
import net.mehvahdjukaar.snowyspirit.integration.supp.SuppCompat;
import net.mehvahdjukaar.snowyspirit.reg.ModRegistry;
import net.mehvahdjukaar.snowyspirit.reg.ModTags;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;

public class ContainerHolderEntity extends Entity implements Container, MenuProvider, IExtraClientSpawnData {
    private static final EntityDataAccessor<Integer> DATA_ID_HURT = SynchedEntityData.defineId(ContainerHolderEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_ID_DAMAGE = SynchedEntityData.defineId(ContainerHolderEntity.class, EntityDataSerializers.FLOAT);

    private static final int CONTAINER_SIZE = 27;

    private ItemStack containerStack = ItemStack.EMPTY;

    //for client
    private BlockState displayState = Blocks.AIR.defaultBlockState();

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
            // sled.positionRider(this);
        }

    }

    public BlockState getDisplayState() {
        return displayState;
    }

    public void setContainerItem(ItemStack stack) {
        this.containerStack = stack;
        if (this.containerStack.getItem() instanceof BlockItem blockItem) {
            this.displayState = blockItem.getBlock().defaultBlockState();
        }
        if(isContainerWithNBT(stack) && stack.hasTag()){
            CompoundTag tag = stack.getTagElement("BlockEntityTag");
            if(tag != null) ContainerHelper.loadAllItems(tag, itemStacks);
        }
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return PlatformHelper.getEntitySpawnPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeItem(this.containerStack);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        this.setContainerItem(additionalData.readItem());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        this.containerStack = ItemStack.of(pCompound.getCompound("ContainerItem"));
        if (this.containerStack.getItem() instanceof BlockItem blockItem) {
            this.displayState = blockItem.getBlock().defaultBlockState();
        }

        this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (pCompound.contains("LootTable", 8)) {
            this.lootTable = new ResourceLocation(pCompound.getString("LootTable"));
            this.lootTableSeed = pCompound.getLong("LootTableSeed");
        } else {
            ContainerHelper.loadAllItems(pCompound, this.itemStacks);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        pCompound.put("ContainerItem", this.containerStack.save(new CompoundTag()));

        if (this.lootTable != null) {
            pCompound.putString("LootTable", this.lootTable.toString());
            if (this.lootTableSeed != 0L) {
                pCompound.putLong("LootTableSeed", this.lootTableSeed);
            }
        } else {
            ContainerHelper.saveAllItems(pCompound, this.itemStacks);
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_ID_HURT, 0);
        this.entityData.define(DATA_ID_DAMAGE, 0.0F);
    }

    @Override
    public double getMyRidingOffset() {
        return 0;
    }

    @Override
    protected float getEyeHeight(Pose pPose, EntityDimensions pSize) {
        return pSize.height * 0.5F;
    }

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
        if (!this.level.isClientSide && !this.isRemoved()) {
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
        if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            if (!this.level.isClientSide) {
                Entity entity = pSource.getDirectEntity();
                if (entity != null && entity.getType() == EntityType.PLAYER) {
                    PiglinAi.angerNearbyPiglins((Player) entity, true);
                }
            }
        }
        this.remove(Entity.RemovalReason.KILLED);
    }

    public void spawnDrops() {
        ItemStack stack = this.containerStack.copy();
        if (this.hasCustomName()) {
            stack.setHoverName(this.getCustomName());
        }
        //sacks and shulker. kind of ugly here
        if (isContainerWithNBT(this.containerStack)) {
            CompoundTag tag = new CompoundTag();
            ContainerHelper.saveAllItems(tag, this.itemStacks, false);
            stack.addTagElement("BlockEntityTag", tag);
        } else {
            Containers.dropContents(this.level, this, this);
        }
        this.spawnAtLocation(stack);
    }

    /**
     * Setups the entity to do the hurt animation. Only used by packets in multiplayer.
     */
    @Override
    public void animateHurt() {
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

            this.checkOutOfWorld();
            this.handleNetherPortal();


            // this.xRotO = v.xRotO;
            // this.yRotO = v.yRotO;
            //this.setYRot(v.getYRot());
            super.tick();
            // this.xRotO = this.getXRot();
            //this.yRotO = this.getYRot();
        } else {
            this.destroy(DamageSource.GENERIC);
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
        return this.containerStack.copy();
    }

    //container

    private NonNullList<ItemStack> itemStacks = NonNullList.withSize(36, ItemStack.EMPTY);
    
    private ResourceLocation lootTable;
    private long lootTableSeed;


    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.itemStacks) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the stack in the given slot.
     */
    @Override
    public ItemStack getItem(int pIndex) {
        this.unpackLootTable(null);
        return this.itemStacks.get(pIndex);
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     */
    @Override
    public ItemStack removeItem(int pIndex, int pCount) {
        this.unpackLootTable(null);
        return ContainerHelper.removeItem(this.itemStacks, pIndex, pCount);
    }

    /**
     * Removes a stack from the given slot and returns it.
     */
    @Override
    public ItemStack removeItemNoUpdate(int pIndex) {
        this.unpackLootTable(null);
        ItemStack itemstack = this.itemStacks.get(pIndex);
        if (itemstack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.itemStacks.set(pIndex, ItemStack.EMPTY);
            return itemstack;
        }
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     */
    @Override
    public void setItem(int pIndex, ItemStack pStack) {
        this.unpackLootTable(null);
        this.itemStacks.set(pIndex, pStack);
        if (!pStack.isEmpty() && pStack.getCount() > this.getMaxStackSize()) {
            pStack.setCount(this.getMaxStackSize());
        }
    }

    @Override
    public SlotAccess getSlot(final int pSlot) {
        return pSlot >= 0 && pSlot < this.getContainerSize() ? new SlotAccess() {
            public ItemStack get() {
                return ContainerHolderEntity.this.getItem(pSlot);
            }

            @Override
            public boolean set(ItemStack carried) {
                ContainerHolderEntity.this.setItem(pSlot, carried);
                return true;
            }
        } : super.getSlot(pSlot);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("message.snowyspirit.container_entity_name",
                this.containerStack.getItem().getDescription().getString());
    }

    /**
     * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
     * hasn't changed and skip it.
     */
    @Override
    public void setChanged() {
    }

    /**
     * Don't rename this method to canInteractWith due to conflicts with Container
     */
    @Override
    public boolean stillValid(Player pPlayer) {
        if (this.isRemoved()) {
            return false;
        } else {
            return pPlayer.distanceToSqr(this) <= 64.0D;
        }
    }

    @Override
    public void remove(Entity.RemovalReason pReason) {
        if (!this.level.isClientSide && pReason.shouldDestroy()) {
            if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                this.spawnDrops();
            }
        }
        super.remove(pReason);
    }

    @Override
    public InteractionResult interact(Player pPlayer, InteractionHand pHand) {
        InteractionResult ret = super.interact(pPlayer, pHand);
        if (ret.consumesAction()) return ret;
        pPlayer.openMenu(this);
        if (!pPlayer.level.isClientSide) {
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
        if (this.lootTable == null) {
            return AbstractContainerMenu.getRedstoneSignalFromContainer(this) / 15f;
        }
        return 0;
    }

    /**
     * Adds loot to the minecart's contents.
     */
    public void unpackLootTable( Player pPlayer) {
        if (this.lootTable != null && this.level.getServer() != null) {
            LootTable loottable = this.level.getServer().getLootTables().get(this.lootTable);
            if (pPlayer instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.GENERATE_LOOT.trigger( serverPlayer, this.lootTable);
            }

            this.lootTable = null;
            LootContext.Builder builder = (new LootContext.Builder((ServerLevel) this.level))
                    .withParameter(LootContextParams.ORIGIN, this.position()).withOptionalRandomSeed(this.lootTableSeed);

            if(PlatformHelper.getPlatform().isForge()) {
                // Forge: add this entity to loot context, however, currently Vanilla uses 'this' for the player creating the chests. So we take over 'killer_entity' for this.
                builder.withParameter(LootContextParams.KILLER_ENTITY, this);
            }
            if (pPlayer != null) {
                builder.withLuck(pPlayer.getLuck()).withParameter(LootContextParams.THIS_ENTITY, pPlayer);
            }

            loottable.fill(this, builder.create(LootContextParamSets.CHEST));
        }
    }

    @Override
    public void clearContent() {
        this.unpackLootTable(null);
        this.itemStacks.clear();
    }

    public void setLootTable(ResourceLocation pLootTable, long pLootTableSeed) {
        this.lootTable = pLootTable;
        this.lootTableSeed = pLootTableSeed;
    }

    
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
        if (this.lootTable != null && pPlayer.isSpectator()) {
            return null;
        } else {
            this.unpackLootTable(pInventory.player);
            return this.createMenu(pContainerId, pInventory);
        }
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    public AbstractContainerMenu createMenu(int id, Inventory pPlayerInventory) {
        if (isSack(containerStack.getItem())) {
            return SuppCompat.createSackMenu(id, pPlayerInventory, this);
        } else if (!isNormalContainer(containerStack)) {
            return new ShulkerBoxMenu(id, pPlayerInventory, this);
        }
        return ChestMenu.threeRows(id, pPlayerInventory, this);
    }


    public static boolean isValidContainer(ItemStack stack) {
        return isNormalContainer(stack) || isContainerWithNBT(stack);
    }

    private static boolean isNormalContainer(ItemStack stack) {
        return stack.is(ModTags.VALID_CONTAINERS);
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

}
