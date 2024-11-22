package net.mehvahdjukaar.snowyspirit.common.wreath;

import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.snowyspirit.common.network.ClientBoundSyncWreathMessage;
import net.mehvahdjukaar.snowyspirit.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class WreathHelper {

    public static boolean placeWreathOnDoor(BlockPos pos, Level level) {
        var c = WreathSavedData.get(level);

        if (c != null) {
            BlockState door = level.getBlockState(pos);

            if (door.getBlock() instanceof DoorBlock) {
                boolean lower = door.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER;
                BlockPos p = lower ? pos.above() : pos;
                if (!c.hasWreath(p)) {

                    if (level instanceof ServerLevel) {
                        BlockState state = ModRegistry.WREATH.get().defaultBlockState();

                        c.refreshWreathVisual(p, level);
                        SoundType soundtype = state.getSoundType();
                        level.playSound(null, p, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                        //send packet to clients
                        NetworkHelper.sendToAllClientPlayers(new ClientBoundSyncWreathMessage(p, true));

                    }
                    return true;
                }
            }
        }
        return false;
    }

}
