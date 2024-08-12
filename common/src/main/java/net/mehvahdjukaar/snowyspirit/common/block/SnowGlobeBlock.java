package net.mehvahdjukaar.snowyspirit.common.block;

import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SnowGlobeBlock extends WaterBlock {

    public static final BooleanProperty SNOWING = BlockStateProperties.SNOWY;

    private static final VoxelShape SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 8.0D, 13.0D);

    public SnowGlobeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SNOWING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean snowy = canBeSnowy(context.getClickedPos(), context.getLevel());
        return super.getStateForPlacement(context).setValue(SNOWING, snowy);
    }

    private boolean canBeSnowy(BlockPos pos, Level level) {
        return level.getBiome(pos).value().coldEnoughToSnow(pos);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pState.getValue(SNOWING)) {
            return InteractionResult.CONSUME;
        } else {
            pLevel.setBlock(pPos, pState.setValue(SNOWING, true), 3);
            pLevel.scheduleTick(pPos, this, 50);
            return InteractionResult.sidedSuccess(pLevel.isClientSide);
        }
    }

    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRand) {
        if (pState.getValue(SNOWING) && !this.canBeSnowy(pPos, pLevel)) {
            pLevel.setBlock(pPos, pState.setValue(SNOWING, false), 3);
        }
    }
}
