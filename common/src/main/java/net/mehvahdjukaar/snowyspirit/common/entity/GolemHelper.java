package net.mehvahdjukaar.snowyspirit.common.entity;

import com.google.common.base.Suppliers;
import net.mehvahdjukaar.snowyspirit.common.block.GumdropButton;
import net.mehvahdjukaar.snowyspirit.reg.ModRegistry;
import net.mehvahdjukaar.snowyspirit.reg.ModTags;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class GolemHelper {

    public static void trySpawningGingy(BlockState pumpkinState, LevelAccessor level, BlockPos pumpkinPos, @Nullable Entity entity) {
        BlockPos below = pumpkinPos.below();
        if (level instanceof ServerLevel serverLevel && level.getBlockState(below).is(ModTags.GINGERBREADS)) {
            Direction dir = pumpkinState.getValue(CarvedPumpkinBlock.FACING);
            BlockPos button = below.relative(dir);
            BlockState state = level.getBlockState(button);
            if (state.getBlock() instanceof GumdropButton b && state.getValue(GumdropButton.FACING) == dir) {
                GingyEntity golem = ModRegistry.GINGERBREAD_GOLEM.get().create(serverLevel);
                if (golem != null) {
                    level.removeBlock(pumpkinPos, false);
                    level.removeBlock(button, false);
                    level.removeBlock(below, false);
                    golem.moveTo(pumpkinPos.getX() + 0.5, pumpkinPos.getY() + 0.05 - 1, pumpkinPos.getZ() + 0.5, dir.toYRot(), 0.0F);
                    if (entity instanceof ServerPlayer serverPlayer) {
                        CriteriaTriggers.SUMMONED_ENTITY.trigger(serverPlayer, golem);
                        golem.setOwnerUUID(serverPlayer.getUUID());
                        golem.setPersistenceRequired();
                    }
                    golem.setColor(b.color);
                    golem.setYHeadRot(dir.toYRot());

                    level.addFreshEntity(golem);

                }
            }
        }
    }

    public static void trySpawningMongo(BlockState buttonState, Level level, BlockPos buttonPos, @Nullable Entity entity) {
        BlockPos behind = buttonPos.relative(buttonState.getValue(GumdropButton.FACING).getOpposite());
        if (level.getBlockState(behind).is(ModTags.GINGERBREADS)) {
            var patternMatch = MONGO_PATTERN.get().find(level, behind);
            if (patternMatch != null) {
                MongoEntity giant = ModRegistry.GINGERBREAD_GIANT.get().create(level);
                // giant.setOwnerUUID(entity.getUUID());
                if (giant != null) {
                    giant.setColor(((GumdropButton)buttonState.getBlock()).color);

                    clearBlocks(level, patternMatch);

                    BlockPos bottom = patternMatch.getBlock(patternMatch.getWidth() / 2, patternMatch.getHeight(), 0).getPos();
                    giant.moveTo(bottom.getX() + 0.5, bottom.getY() + 0.05, bottom.getZ() + 0.5, 0.0F, 0.0F);
                    level.addFreshEntity(giant);

                    if (entity instanceof ServerPlayer serverPlayer)
                        CriteriaTriggers.SUMMONED_ENTITY.trigger(serverPlayer, giant);
                }
            }
        }

    }

    private static void clearBlocks(Level level, BlockPattern.BlockPatternMatch patternMatch) {
        Direction front = patternMatch.getForwards();
        for (int i = 0; i < patternMatch.getWidth(); ++i) {
            for (int j = 0; j < patternMatch.getHeight(); ++j) {
                BlockInWorld blockInWorld = patternMatch.getBlock(i, j, 0);
                if (blockInWorld.getState().is(ModTags.GINGERBREADS)) {
                    // level.blockUpdated(blockInWorld.getPos(), Blocks.AIR);

                    BlockPos inFront = blockInWorld.getPos().relative(front);
                    BlockState gum = level.getBlockState(inFront);
                    if (gum.getBlock() instanceof GumdropButton) {
                        level.setBlock(inFront, Blocks.AIR.defaultBlockState(), 2);
                        level.levelEvent(2001, inFront, Block.getId(gum));
                    }

                    level.setBlock(blockInWorld.getPos(), Blocks.AIR.defaultBlockState(), 2);
                    level.levelEvent(2001, blockInWorld.getPos(), Block.getId(blockInWorld.getState()));
                }
            }
        }
    }

    public static final Supplier<BlockPattern> MONGO_PATTERN = Suppliers.memoize(() ->
            BlockPatternBuilder.start().aisle(
                            "~~~###~~~",
                            "~~~###~~~",
                            "~~~###~~~",
                            "#########",
                            "#########",
                            "~~~###~~~",
                            "~~~###~~~",
                            "~~~###~~~",
                            "~~~###~~~",
                            "~~~###~~~",
                            "~~~###~~~")
                    .where('~', BlockInWorld.hasState(BlockBehaviour.BlockStateBase::canBeReplaced))
                    .where('#', BlockInWorld.hasState(s -> s.is(ModTags.GINGERBREADS))).build());
}
