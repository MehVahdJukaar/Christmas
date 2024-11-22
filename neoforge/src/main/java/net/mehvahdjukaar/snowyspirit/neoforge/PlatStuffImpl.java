package net.mehvahdjukaar.snowyspirit.neoforge;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ItemAbilities;

public class PlatStuffImpl {
    public static boolean isShear(ItemStack stack) {
        return stack.canPerformAction(ItemAbilities.SHEARS_HARVEST);
    }
}
