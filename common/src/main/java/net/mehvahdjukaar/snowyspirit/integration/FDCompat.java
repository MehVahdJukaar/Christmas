package net.mehvahdjukaar.snowyspirit.integration;

import dev.architectury.injectables.annotations.ExpectPlatform;


public class FDCompat {

    public static void init() {
        RegHelper.addItemsToTabsRegistration(FDCompatImpl::addItemsToTabs);
    }

    public static void addItemsToTabs(RegHelper.ItemToTabEvent event){
        //event.add(FarmersDelight.CREATIVE_TAB, GINGER_CRATE.get());
    }

    public static final Supplier<Block> GINGER_CRATE = regWithItem(
            "ginger_crate", () ->
                    new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                            .strength(2.0F, 3.0F)
                            .sound(SoundType.WOOD))
    );
}
