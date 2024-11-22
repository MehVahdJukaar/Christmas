package net.mehvahdjukaar.snowyspirit.integration;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

import static net.mehvahdjukaar.snowyspirit.reg.ModRegistry.regWithItem;


public class FDCompat {

    public static void init() {
        RegHelper.addItemsToTabsRegistration(FDCompat::addItemsToTabs);

        PlatHelper.addCommonSetup(() -> RegHelper.registerBlockFlammability(GINGER_CRATE.get(),
                5, 20));
    }

    public static void addItemsToTabs(RegHelper.ItemToTabEvent event) {
        event.add(ResourceKey.create(Registries.CREATIVE_MODE_TAB,
                ResourceLocation.parse("farmersdelight:farmersdelight")), GINGER_CRATE.get());
    }

    public static final Supplier<Block> GINGER_CRATE = regWithItem(
            "ginger_crate", () ->
                    new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                            .strength(2.0F, 3.0F)
                            .sound(SoundType.WOOD))
    );
}
