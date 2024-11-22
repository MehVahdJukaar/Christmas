package net.mehvahdjukaar.snowyspirit.common.block;

import net.mehvahdjukaar.moonlight.api.block.MimicBlockTile;
import net.mehvahdjukaar.snowyspirit.reg.ModRegistry;
import net.mehvahdjukaar.snowyspirit.reg.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;

public class GlowLightsBlockTile extends MimicBlockTile {

    public GlowLightsBlockTile(BlockPos pos, BlockState state) {
        super(ModRegistry.GLOW_LIGHTS_BLOCK_TILE.get(), pos, state);
        this.mimic = Blocks.OAK_LEAVES.defaultBlockState();
    }

    public static boolean isValidBlock(BlockState state, BlockPos pos, Level world) {
        if (state == null) return false;
        Block b = state.getBlock();
        if (b instanceof EntityBlock) {
            return false;
        }
        return state.is(ModTags.ACCEPTS_GLOW_LIGHTS) && Block.isShapeFullBlock(state.getCollisionShape(world, pos));
    }


    public void acceptBlock(BlockState state) {
        this.mimic = state;
        if (level.isClientSide) {
            this.requestModelReload();
        } else {
            this.setChanged();
        }
    }
}