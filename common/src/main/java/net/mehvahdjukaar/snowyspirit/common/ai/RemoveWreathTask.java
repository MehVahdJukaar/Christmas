package net.mehvahdjukaar.snowyspirit.common.ai;

import com.google.common.collect.ImmutableMap;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.snowyspirit.SnowySpirit;
import net.mehvahdjukaar.snowyspirit.common.network.ClientBoundSyncWreathMessage;
import net.mehvahdjukaar.snowyspirit.common.wreath.WreathSavedData;
import net.mehvahdjukaar.snowyspirit.reg.ModMemoryModules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class RemoveWreathTask extends Behavior<Villager> {
    private final float speedModifier;
    private int ticksSinceReached = 0;
    private int cooldown = 5 * 20;
    protected int lastBreakProgress = -1;

    public RemoveWreathTask(float speed) {
        super(ImmutableMap.of(
                        MemoryModuleType.INTERACTION_TARGET, MemoryStatus.VALUE_ABSENT,
                        ModMemoryModules.WREATH_POS.get(), MemoryStatus.VALUE_PRESENT,
                        MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
                270, 350);
        this.speedModifier = speed * 1.1f;

    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel pLevel, Villager pOwner) {
        if (SnowySpirit.isChristmasSeason(pLevel)) return false;
        if (cooldown-- > 0) return false;
        if (!PlatHelper.isMobGriefingOn(pLevel, pOwner)) {
            cooldown = 20 * 60;
            return false;
        }
        GlobalPos globalpos = pOwner.getBrain().getMemory(ModMemoryModules.WREATH_POS.get()).get();
        return globalpos.dimension() == pLevel.dimension() && !pOwner.isBaby();
    }

    @Override
    protected void start(ServerLevel pLevel, Villager pEntity, long pGameTime) {
        this.cooldown = 20 * (5 + pLevel.random.nextInt(20)) + pLevel.random.nextInt(20);
        this.ticksSinceReached = 0;
        this.lastBreakProgress = -1;
        GlobalPos globalpos = pEntity.getBrain().getMemory(ModMemoryModules.WREATH_POS.get()).get();

        pEntity.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
        pEntity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(globalpos.pos(), this.speedModifier, 1));
        PlaceWreathTask.displayAsHeldItem(pEntity, new ItemStack(Items.SHEARS));
    }

    @Override
    protected void stop(ServerLevel pLevel, Villager pEntity, long pGameTime) {
        super.stop(pLevel, pEntity, pGameTime);
        PlaceWreathTask.clearHeldItem(pEntity);
    }

    @Override
    protected boolean canStillUse(ServerLevel pLevel, Villager pEntity, long pGameTime) {
        return pEntity.getBrain().hasMemoryValue(ModMemoryModules.WREATH_POS.get());
    }

    @Override
    protected void tick(ServerLevel level, Villager pOwner, long pGameTime) {
        BlockPos pos = pOwner.getBrain().getMemory(ModMemoryModules.WREATH_POS.get()).get().pos();

        //hax
        pOwner.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
        pOwner.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pos, this.speedModifier, 2));

        pOwner.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(pos));
        if (pos.closerToCenterThan(pOwner.position(), 2.3)) {
            this.ticksSinceReached++;

            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof DoorBlock) {
                boolean lower = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER;
                var c = WreathSavedData.get(level);

                pos = lower ? pos.above() : pos;
                if (c != null && c.hasWreath(pos)) {
                    //breaking animation. same as fodder lol. might have the same issues
                    int k = (int) ((float) this.ticksSinceReached / (float) 10 * 10.0F);
                    if (k != this.lastBreakProgress) {
                        level.destroyBlockProgress(pOwner.getId(), pos, k);
                        this.lastBreakProgress = k;
                    }

                    if (ticksSinceReached > 10) {

                        pOwner.getBrain().eraseMemory(ModMemoryModules.WREATH_POS.get());
                        c.removeWreath(pos, level, true);
                        NetworkHelper.sendToAllClientPlayers(new ClientBoundSyncWreathMessage(pos, false));
                    }
                    return;
                }
            }
            pOwner.getBrain().eraseMemory(ModMemoryModules.WREATH_POS.get());

        }
    }
}