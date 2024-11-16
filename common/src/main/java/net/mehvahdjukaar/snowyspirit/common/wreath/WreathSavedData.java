package net.mehvahdjukaar.snowyspirit.common.wreath;

import com.mojang.datafixers.util.Pair;
import net.mehvahdjukaar.snowyspirit.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WreathSavedData extends SavedData {
    private static final String FILE_ID = "wreaths";

    public static final SavedData.Factory<WreathSavedData> FACTORY = new SavedData.Factory<>(
            WreathSavedData::new,
            (compoundTag, registries) -> {
                var data = new WreathSavedData();
                data.load(compoundTag, registries);
                return data;
            },
            null);


    private static final WreathSavedData clientData = new WreathSavedData();

    public static WreathSavedData get(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return serverLevel.getDataStorage().computeIfAbsent(FACTORY, FILE_ID);
        }
        return clientData;
    }

    private final Map<BlockPos, Data> wreathBlocks = new HashMap<>();

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        int i = 0;
        for (BlockPos pos : wreathBlocks.keySet()) {
            NbtUtils.writeBlockPos(pos);
            tag.put(i + "", NbtUtils.writeBlockPos(pos));
            i++;
        }
        tag.putInt("Count", i);
        return tag;
    }

    public void load(CompoundTag total, HolderLookup.Provider reg) {
        for (int i = 0; i < total.getInt("Count"); i++) {
            BlockPos pos = NbtUtils.readBlockPos(total, i + "").orElseThrow();
            this.addWreath(pos);
        }
    }

    public Data addWreath(BlockPos pos) {
        return wreathBlocks.computeIfAbsent(pos, Data::new);
    }

    public void removeWreath(BlockPos p, Level level, boolean animationAndDrop) {
        wreathBlocks.remove(p);
        if (animationAndDrop) {
            ItemEntity itementity = new ItemEntity(level, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5,
                    ModRegistry.WREATH.get().asItem().getDefaultInstance());
            itementity.setDefaultPickUpDelay();
            level.addFreshEntity(itementity);
            level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, p, Block.getId(ModRegistry.WREATH.get().defaultBlockState()));
        }
    }

    public Map<BlockPos, Data> getWreathBlocks() {
        return wreathBlocks;
    }

    public boolean hasWreath(BlockPos pos) {
        return this.wreathBlocks.containsKey(pos);
    }

    public void refreshWreathVisual(BlockPos pos, Level level) {
        if (level.isLoaded(pos)) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof DoorBlock) {
                Direction dir = state.getValue(DoorBlock.FACING);
                boolean open = state.getValue(DoorBlock.OPEN);
                boolean hinge = state.getValue(DoorBlock.HINGE) == DoorHingeSide.RIGHT;
                Data data = this.addWreath(pos);
                data.face = dir;
                data.hinge = hinge;
                data.open = open;
                if (data.needsInitialization) {
                    this.calculateDoorDimensions(level, pos, state, data);
                    data.needsInitialization = false;
                }
            } else {
                this.removeWreath(pos, level, false);
            }
        }
    }

    private void calculateDoorDimensions(Level level, BlockPos pos, BlockState state, Data data) {
        state = state.setValue(DoorBlock.FACING, Direction.NORTH).setValue(DoorBlock.OPEN, Boolean.FALSE)
                .setValue(DoorBlock.HINGE, DoorHingeSide.RIGHT);
        VoxelShape shape = state.getShape(level, pos);
        AABB bounds = shape.bounds();
        if (bounds.maxX - bounds.minX >= 1) {
            double front = bounds.minZ - 1;
            double back = -bounds.maxZ;
            data.closedDimensions = Pair.of((float) front, (float) back);
        }
        state = state.setValue(DoorBlock.OPEN, Boolean.TRUE).setValue(DoorBlock.FACING, Direction.EAST);
        shape = state.getShape(level, pos);
        bounds = shape.bounds();
        if (bounds.maxX - bounds.minX >= 1) {
            double front = bounds.minZ - 1;
            double back = -bounds.maxZ;
            data.openDimensions = Pair.of((float) front, (float) back);
        }
    }

    public void refreshClientBlocksVisuals(Level level) {
        Set<BlockPos> positions = new HashSet<>(this.wreathBlocks.keySet());
        positions.forEach(p -> refreshWreathVisual(p, level));
    }

    public void updateAllBlocks(ServerLevel level) {
        Set<BlockPos> positions = new HashSet<>(this.wreathBlocks.keySet());
        positions.forEach(p -> {
            //prevents removing when not loaded
            if (level.isLoaded(p)) {
                BlockState state = level.getBlockState(p);
                if (!(state.getBlock() instanceof DoorBlock)) {
                    this.removeWreath(p, level, true);
                }
            }
        });
    }


    public static class Data {
        private Direction face = Direction.NORTH;
        private boolean open = true;
        private boolean hinge = true;

        private boolean needsInitialization = true;
        private Pair<Float, Float> openDimensions = null;
        private Pair<Float, Float> closedDimensions = null;

        public Data(BlockPos pos) {
        }

        public Direction getDirection() {
            if (this.open) {
                return this.hinge ? face.getCounterClockWise() : face.getClockWise();
            }
            return this.face;
        }

        public boolean isOpen() {
            return open;
        }

        public boolean isHinge() {
            return hinge;
        }

        public Pair<Float, Float> getDimensions() {
            return this.open ? openDimensions : closedDimensions;
        }
    }

}
