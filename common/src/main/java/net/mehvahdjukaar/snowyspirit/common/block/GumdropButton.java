package net.mehvahdjukaar.snowyspirit.common.block;

import net.mehvahdjukaar.snowyspirit.SnowySpirit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class GumdropButton extends DirectionalBlock {

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    protected static final VoxelShape DOWN_AABB = Block.box(5.0D, 12.0D, 5.0D, 11.0D, 16.0D, 11.0D);
    protected static final VoxelShape UP_AABB = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 4.0D, 11.0D);
    protected static final VoxelShape NORTH_AABB = Block.box(5.0D, 5.0D, 12.0D, 11.0D, 11.0D, 16.0D);
    protected static final VoxelShape SOUTH_AABB = Block.box(5.0D, 5.0D, 0.0D, 11.0D, 11.0D, 4.0D);
    protected static final VoxelShape WEST_AABB = Block.box(12.0D, 5.0D, 5.0D, 16.0D, 11.0D, 11.0D);
    protected static final VoxelShape EAST_AABB = Block.box(0.0D, 5.0D, 5.0D, 4.0D, 11.0D, 11.0D);

    protected static final VoxelShape PRESSED_DOWN_AABB = Block.box(5.0D, 14.0D, 5.0D, 11.0D, 16.0D, 11.0D);
    protected static final VoxelShape PRESSED_UP_AABB = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 2.0D, 11.0D);
    protected static final VoxelShape PRESSED_NORTH_AABB = Block.box(5.0D, 5.0D, 14.0D, 11.0D, 11.0D, 16.0D);
    protected static final VoxelShape PRESSED_SOUTH_AABB = Block.box(5.0D, 5.0D, 0.0D, 11.0D, 11.0D, 2.0D);
    protected static final VoxelShape PRESSED_WEST_AABB = Block.box(14.0D, 5.0D, 5.0D, 16.0D, 11.0D, 11.0D);
    protected static final VoxelShape PRESSED_EAST_AABB = Block.box(0.0D, 5.0D, 5.0D, 2.0D, 11.0D, 11.0D);

    public final DyeColor color;

    public GumdropButton(DyeColor color) {
        super(Properties.of(Material.CLAY, MaterialColor.byId(color.getId() + 14))
                .sound(SoundType.SLIME_BLOCK).noCollission());
        this.color = color;
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH).setValue(POWERED, false));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,  LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockPos relative = pos.above().relative(state.getValue(FACING).getOpposite());
        BlockState pumpkin = level.getBlockState(relative);
        if(pumpkin.getBlock() instanceof CarvedPumpkinBlock){
            SnowySpirit.trySpawningGingy(pumpkin, level, relative, placer);
        }
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        Direction dir = pState.getValue(FACING).getOpposite();
        BlockPos blockpos = pPos.relative(dir);
        return canSupportCenter(pLevel, blockpos, dir);
    }

    @Override
    
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        Direction direction = pContext.getClickedFace();
        BlockState blockstate = this.defaultBlockState().setValue(FACING, direction);
        if (blockstate.canSurvive(pContext.getLevel(), pContext.getClickedPos())) {
            return blockstate;
        }

        return null;
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        return pState.getValue(FACING).getOpposite() == pFacing && !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    private int getPressDuration() {
        return 40;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        boolean powered = pState.getValue(POWERED);

        return switch (pState.getValue(FACING)) {
            case EAST -> powered ? PRESSED_EAST_AABB : EAST_AABB;
            case WEST -> powered ? PRESSED_WEST_AABB : WEST_AABB;
            case SOUTH -> powered ? PRESSED_SOUTH_AABB : SOUTH_AABB;
            case NORTH -> powered ? PRESSED_NORTH_AABB : NORTH_AABB;
            case DOWN -> powered ? PRESSED_DOWN_AABB : DOWN_AABB;
            case UP -> powered ? PRESSED_UP_AABB : UP_AABB;
        };
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pState.getValue(POWERED)) {
            return InteractionResult.CONSUME;
        } else {
            this.press(pState, pLevel, pPos);
            this.playSound(pPlayer, pLevel, pPos, true);
            pLevel.gameEvent(pPlayer, GameEvent.BLOCK_ACTIVATE, pPos);
            return InteractionResult.sidedSuccess(pLevel.isClientSide);
        }
    }

    public void press(BlockState pState, Level pLevel, BlockPos pPos) {
        pLevel.setBlock(pPos, pState.setValue(POWERED, true), 3);
        this.updateNeighbours(pState, pLevel, pPos);
        pLevel.scheduleTick(pPos, this, this.getPressDuration());
    }

    protected void playSound( Player pPlayer, LevelAccessor pLevel, BlockPos pPos, boolean pHitByArrow) {
        pLevel.playSound(pHitByArrow ? pPlayer : null, pPos, this.getSound(pHitByArrow), SoundSource.BLOCKS, 0.3F, pHitByArrow ? 0.6F : 0.5F);
    }

    protected SoundEvent getSound(boolean pIsOn) {
        return pIsOn ? SoundEvents.SLIME_BLOCK_BREAK : SoundEvents.SLIME_BLOCK_STEP;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (!pIsMoving && !pState.is(pNewState.getBlock())) {
            if (pState.getValue(POWERED)) {
                this.updateNeighbours(pState, pLevel, pPos);
            }
            super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        }
    }

    @Override
    public int getSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
        return pBlockState.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
        return pBlockState.getValue(POWERED) && pBlockState.getValue(FACING) == pSide ? 15 : 0;
    }

    @Override
    public boolean isSignalSource(BlockState pState) {
        return true;
    }

    @Override
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRand) {
        if (pState.getValue(POWERED)) {
            this.checkPressed(pState, pLevel, pPos);
        }
    }

    @Override
    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
        if (!pLevel.isClientSide && !pState.getValue(POWERED)) {
            this.checkPressed(pState, pLevel, pPos);
        }
    }

    private void checkPressed(BlockState pState, Level pLevel, BlockPos pPos) {
        List<? extends Entity> list = pLevel.getEntitiesOfClass(Entity.class, pState.getShape(pLevel, pPos).bounds().move(pPos))
                .stream().filter(e -> (e instanceof AbstractArrow || e instanceof LivingEntity)).toList();

        boolean flag = !list.isEmpty();
        boolean flag1 = pState.getValue(POWERED);
        if (flag != flag1) {
            pLevel.setBlock(pPos, pState.setValue(POWERED, flag), 3);
            this.updateNeighbours(pState, pLevel, pPos);
            this.playSound(null, pLevel, pPos, flag);
            pLevel.gameEvent(list.stream().findFirst().orElse(null), flag ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pPos);
        }

        if (flag) {
            pLevel.scheduleTick(new BlockPos(pPos), this, this.getPressDuration());
        }
    }

    private void updateNeighbours(BlockState pState, Level pLevel, BlockPos pPos) {
        pLevel.updateNeighborsAt(pPos, this);
        pLevel.updateNeighborsAt(pPos.relative(pState.getValue(FACING).getOpposite()), this);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, POWERED);
    }

/*
    public void updateEntityAfterFallOn(BlockGetter pLevel, Entity pEntity) {
        if (!pEntity.isSuppressingBounce()) {
            BlockState state = pLevel.getBlockState(pEntity.blockPosition());
            if (state.is(this) && !state.getValue(POWERED)) {
                this.bounceUp(pEntity);
                return;
            }
        }
        super.updateEntityAfterFallOn(pLevel, pEntity);
    }

    private void bounceUp(Entity pEntity) {
        Vec3 vec3 = pEntity.getDeltaMovement();
        if (vec3.y < 0.0D) {
            double d0 = pEntity instanceof LivingEntity ? 1.0D : 0.8D;
            pEntity.setDeltaMovement(vec3.x, -vec3.y * (double) 0.5F * d0, vec3.z);
        }

    }
    */

}