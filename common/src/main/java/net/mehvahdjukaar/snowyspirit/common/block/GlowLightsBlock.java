package net.mehvahdjukaar.snowyspirit.common.block;

import net.mehvahdjukaar.moonlight.api.block.IColored;
import net.mehvahdjukaar.moonlight.api.block.WaterBlock;
import net.mehvahdjukaar.moonlight.api.platform.ForgeHelper;
import net.mehvahdjukaar.snowyspirit.dynamicpack.ClientDynamicResourcesHandler;
import net.mehvahdjukaar.snowyspirit.reg.ModRegistry;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class GlowLightsBlock extends WaterBlock implements EntityBlock, IColored {
    public final DyeColor color;

    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    private static final Map<Direction, BooleanProperty> DIR_MAP = Util.make(() -> {
        Map<Direction, BooleanProperty> m = new EnumMap<>(Direction.class);
        m.put(Direction.UP, UP);
        m.put(Direction.DOWN, DOWN);
        m.put(Direction.WEST, WEST);
        m.put(Direction.SOUTH, SOUTH);
        m.put(Direction.EAST, EAST);
        m.put(Direction.NORTH, NORTH);
        return m;
    });

    public GlowLightsBlock(DyeColor color) {
        super(Properties.ofFullCopy(Blocks.OAK_LEAVES)
                .lightLevel(s -> 6));
        this.color = color;
    }

    public static boolean hasSide(BlockState state, Direction direction) {
        if (direction == null) return true;
        return state.getValue(DIR_MAP.get(direction));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(UP);
        builder.add(DOWN);
        builder.add(EAST);
        builder.add(WEST);
        builder.add(SOUTH);
        builder.add(NORTH);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos)
                .setValue(DIR_MAP.get(direction),
                        !Block.isFaceFull(neighborState.getCollisionShape(level, neighborPos), direction.getOpposite()));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        for (var e : DIR_MAP.entrySet()) {
            Direction d = e.getKey();
            BlockPos p = pos.relative(d);
            state = state.setValue(e.getValue(), !Block.isFaceFull(level.getBlockState(p).getCollisionShape(level, p), d.getOpposite()));
        }
        return state;
    }

    @Nullable
    @Override
    public DyeColor getColor() {
        return color;
    }

    @Override
    public boolean supportsBlankColor() {
        return true;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        if (builder.getParameter(LootContextParams.BLOCK_ENTITY) instanceof GlowLightsBlockTile tile) {
            //checks again if the content itself can be mined
            BlockState heldState = tile.getHeldBlock();
            if (builder.getParameter(LootContextParams.THIS_ENTITY) instanceof ServerPlayer player) {
                if (!ForgeHelper.canHarvestBlock(heldState, builder.getLevel(), BlockPos.containing(builder.getParameter(LootContextParams.ORIGIN)), player)) {
                    return drops;
                }
            }
            List<ItemStack> newDrops = heldState.getDrops(builder);
            drops.addAll(newDrops);

        }
        return drops;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new GlowLightsBlockTile(pPos, pState);
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState pState, BlockGetter pReader, BlockPos pPos) {
        return Shapes.empty();
    }

    @Override
    public int getLightBlock(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return 1;
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        if (pLevel.isRainingAt(pPos.above())) {
            if (pRandom.nextInt(15) == 1) {
                BlockPos blockpos = pPos.below();
                BlockState blockstate = pLevel.getBlockState(blockpos);
                if (!blockstate.canOcclude() || !blockstate.isFaceSturdy(pLevel, blockpos, Direction.UP)) {
                    double d0 = pPos.getX() + pRandom.nextDouble();
                    double d1 = pPos.getY() - 0.05D;
                    double d2 = pPos.getZ() + pRandom.nextDouble();
                    pLevel.addParticle(ParticleTypes.DRIPPING_WATER, d0, d1, d2, 0.0D, 0.0D, 0.0D);
                }
            }
        }
        spawnParticlesOnBlockFaces(pLevel, pState, pPos, ModRegistry.GLOW_LIGHT_PARTICLE.get(), pRandom, this.color);
    }


    public void spawnParticlesOnBlockFaces(Level level, BlockState state, BlockPos pos, ParticleOptions particleOptions,
                                           RandomSource randomSource, DyeColor color) {
        Vec3 vec3 = Vec3.atCenterOf(pos);
        for (Direction direction : Direction.values()) {
            if (randomSource.nextFloat() < 0.15f && hasSide(state, direction)) {
                int i = direction.getStepX();
                int j = direction.getStepY();
                int k = direction.getStepZ();
                double d0 = vec3.x + (i == 0 ? Mth.nextDouble(level.random, -0.5D, 0.5D) : i * 0.6D);
                double d1 = vec3.y + (j == 0 ? Mth.nextDouble(level.random, -0.5D, 0.5D) : j * 0.6D);
                double d2 = vec3.z + (k == 0 ? Mth.nextDouble(level.random, -0.5D, 0.5D) : k * 0.6D);
                var c = ClientDynamicResourcesHandler.getGlowLightColor(color, randomSource);
                level.addParticle(particleOptions, d0, d1, d2, c[0], c[1], c[2]);
            }
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player pPlayer, InteractionHand pHand, BlockHitResult hitResult) {
        if (stack.getItem() instanceof ShearsItem) {
            var drops = this.shearAction(pPlayer, stack, level, pos, 0);
            drops.forEach(d -> {
                ItemEntity ent = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, d);
                ent.setDefaultPickUpDelay();
                level.addFreshEntity(ent);
                RandomSource r = level.random;
                ent.setDeltaMovement(ent.getDeltaMovement().add((r.nextFloat() - r.nextFloat()) * 0.1F, r.nextFloat() * 0.05F, (r.nextFloat() - r.nextFloat()) * 0.1F));
            });
            stack.hurtAndBreak(1, pPlayer, pHand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.useItemOn(stack, state, level, pos, pPlayer, pHand, hitResult);
    }

    private List<ItemStack> shearAction(@Nullable Player player, ItemStack item, Level world, BlockPos pos, int fortune) {
        if (world.getBlockEntity(pos) instanceof GlowLightsBlockTile tile) {
            if (!world.isClientSide()) {
                world.setBlockAndUpdate(pos, tile.getHeldBlock());
                return Collections.singletonList(ModRegistry.GLOW_LIGHTS_ITEMS.get(color).get().getDefaultInstance());
            }
        }
        return Collections.emptyList();
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return ModRegistry.GLOW_LIGHTS_ITEMS.get(this.color).get().getDefaultInstance();
    }

}
