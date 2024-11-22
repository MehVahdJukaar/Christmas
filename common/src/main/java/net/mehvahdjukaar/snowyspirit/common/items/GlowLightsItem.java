package net.mehvahdjukaar.snowyspirit.common.items;

import net.mehvahdjukaar.snowyspirit.common.block.GlowLightsBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class GlowLightsItem extends Item {

    private final Block block;

    public GlowLightsItem(Block pBlock) {
        super(new Item.Properties());
        this.block = pBlock;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && player.getAbilities().mayBuild) {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            BlockState clicked = level.getBlockState(pos);
            if (GlowLightsBlockTile.isValidBlock(clicked, pos, level)) {
                level.removeBlock(pos, false);
                BlockState glowLight = this.block.getStateForPlacement(new BlockPlaceContext(context));
                if(glowLight != null) {
                    level.setBlockAndUpdate(pos, glowLight);

                    if (level.getBlockEntity(pos) instanceof GlowLightsBlockTile tile) {

                        SoundEvent sound = SoundEvents.AMETHYST_CLUSTER_HIT;
                        tile.acceptBlock(clicked);
                        level.playSound(player, pos, sound, SoundSource.BLOCKS, (0.8f + 1.0F) / 2.0F,1 * 1.3F);
                        if (!player.isCreative() && !level.isClientSide()) {
                            context.getItemInHand().shrink(1);
                        }
                        return InteractionResult.sidedSuccess(level.isClientSide);
                    }
                    return InteractionResult.FAIL;
                }
            }
        }
        return super.useOn(context);
    }

}
