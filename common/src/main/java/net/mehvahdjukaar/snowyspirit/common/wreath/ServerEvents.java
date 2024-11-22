package net.mehvahdjukaar.snowyspirit.common.wreath;

import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.mehvahdjukaar.snowyspirit.common.network.ClientBoundSyncAllWreaths;
import net.mehvahdjukaar.snowyspirit.reg.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ServerEvents {


    public static InteractionResult onRightClickBlock(Player player, Level level, ItemStack stack, BlockPos pos) {

        if (stack.is(ModRegistry.WREATH.get().asItem())) {

            if (WreathHelper.placeWreathOnDoor(pos, level)) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    public static void tickEvent(ServerLevel level) {
        WreathSavedData.get(level).updateAllBlocks(level);
    }

    public static void onPlayerLogin(Player player) {
        ServerLevel level = (ServerLevel) player.level();
        WreathSavedData data = WreathSavedData.get(level);
        if (data != null)
            NetworkHelper.sendToClientPlayer((ServerPlayer) player,
                    new ClientBoundSyncAllWreaths(data.getWreathBlocks().keySet()));
    }

    public static void onDimensionChanged(Player player) {
        ServerLevel level = (ServerLevel) player.level();
        WreathSavedData data = WreathSavedData.get(level);
        if (data != null)
            NetworkHelper.sendToClientPlayer((ServerPlayer) player,
                    new ClientBoundSyncAllWreaths(data.getWreathBlocks().keySet()));
    }


}
