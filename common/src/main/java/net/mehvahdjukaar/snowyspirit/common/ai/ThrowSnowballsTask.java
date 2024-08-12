package net.mehvahdjukaar.snowyspirit.common.ai;

import com.google.common.collect.ImmutableMap;
import net.mehvahdjukaar.snowyspirit.SnowySpirit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ThrowSnowballsTask extends Behavior<Villager> {
    private int cooldownBetweenAttacks;
    private int eggs;
    private final int maxRange;
    private int duration = 20 * 20;

    //cooldown between tasks
    private int cooldown = 0;

    //TODO: check this code. there was some lazy work here and it works a bit wonky

    public ThrowSnowballsTask(int range) {
        super(ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_PRESENT));
        this.maxRange = range;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel pLevel, Villager pOwner) {
        if (cooldown-- > 0) return false;
        if(!SnowySpirit.isChristmasSeason(pOwner.level)) return false;
        LivingEntity livingentity = this.getLookTarget(pOwner);

        return BehaviorUtils.canSee(pOwner, livingentity) && livingentity.distanceToSqr(pOwner.getX(), pOwner.getY(), pOwner.getZ()) < maxRange * maxRange;
    }

    @Override
    protected boolean canStillUse(ServerLevel pLevel, Villager pEntity, long pGameTime) {
        if (duration <= 0) return false;
        return this.eggs == 0 || this.checkExtraStartConditions(pLevel, pEntity);
    }

    @Override
    protected void start(ServerLevel pLevel, Villager pEntity, long pGameTime) {

        LivingEntity livingentity = this.getLookTarget(pEntity);
        pEntity.getBrain().setMemory(MemoryModuleType.INTERACTION_TARGET, livingentity);
        //BehaviorUtils.lookAtEntity(pEntity, livingentity);
        displayAsHeldItem(pEntity, new ItemStack(Items.SNOWBALL));
        if (eggs == 0) {
            this.duration = 20 * 20;
            this.eggs = pLevel.random.nextInt(3) + 1;
            this.cooldownBetweenAttacks = 35 + pLevel.random.nextInt(30);
        }
    }

    @Override
    protected void tick(ServerLevel pLevel, Villager pOwner, long pGameTime) {
        LivingEntity target = pOwner.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).orElse(null);
        if (target == null) return;
        BehaviorUtils.lookAtEntity(pOwner, target);
        if (this.cooldownBetweenAttacks-- == 0) {
            this.cooldownBetweenAttacks = 20 + pLevel.random.nextInt(30);
            this.eggs--;

            //this is always server side
            Snowball egg = new Snowball(pLevel, pOwner);

            double d0 = target.getY() - 0.5;
            double d1 = target.getX() - pOwner.getX();
            double d2 = d0 - egg.getY();
            double d3 = target.getZ() - pOwner.getZ();
            double distFactor = Math.sqrt(d1 * d1 + d3 * d3) * (double) 0.2F;
            egg.shoot(d1, (d2 + distFactor) * 0.5, d3, 1.1F, 8);
            pLevel.playSound(null, pOwner.getX(), pOwner.getY(), pOwner.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));
            pLevel.addFreshEntity(egg);

        }
        if (eggs <= 0) {
            //goes in cooldown when it shot its snowballs
            this.cooldown = 20 * (10 + pLevel.random.nextInt(15)) + pLevel.random.nextInt(20);
        }
        this.duration--;
    }

    public static void clearInteractionTarget(Villager pOwner) {
        pOwner.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
        displayAsHeldItem(pOwner, ItemStack.EMPTY);
    }


    private static void displayAsHeldItem(Villager self, ItemStack p_182372_) {
        self.setItemSlot(EquipmentSlot.MAINHAND, p_182372_);
        self.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    @Override
    protected void stop(ServerLevel pLevel, Villager pEntity, long pGameTime) {
        super.stop(pLevel, pEntity, pGameTime);
        clearInteractionTarget(pEntity);
    }

    
    private LivingEntity getLookTarget(LivingEntity pMob) {
        var v = pMob.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).orElse(null);
        if (v instanceof EntityTracker entityTracker) {
            Entity e = entityTracker.getEntity();
            if (e instanceof LivingEntity entity) return entity;
        }
        return null;
    }

}