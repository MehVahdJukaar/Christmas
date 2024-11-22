package net.mehvahdjukaar.snowyspirit.fabric;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.world.item.ItemStack;

public class PlatStuffImpl {
    public static boolean isShear(ItemStack stack) {
        return stack.is(ConventionalItemTags.SHEAR_TOOLS);
    }
}
