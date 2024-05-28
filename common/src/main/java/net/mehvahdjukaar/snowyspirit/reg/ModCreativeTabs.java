package net.mehvahdjukaar.snowyspirit.reg;

import net.mehvahdjukaar.moonlight.api.misc.RegSupplier;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.snowyspirit.SnowySpirit;
import net.mehvahdjukaar.snowyspirit.configs.CommonConfigs;
import net.mehvahdjukaar.snowyspirit.integration.supp.SuppCompat;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ModCreativeTabs {

    public static final RegSupplier<CreativeModeTab> MOD_TAB = !CommonConfigs.MOD_TAB.get() ? null :
            RegHelper.registerCreativeModeTab(SnowySpirit.res(SnowySpirit.MOD_ID), builder ->
                    builder.title(Component.translatable("itemGroup.snowyspirit")).icon(
                            () -> ModRegistry.SLED_ITEMS.get(WoodTypeRegistry.OAK_TYPE).getDefaultInstance()));


    public static void init() {
        RegHelper.addItemsToTabsRegistration(ModCreativeTabs::registerItemsToTabs);
    }

    public static void registerItemsToTabs(RegHelper.ItemToTabEvent e) {
        after(e, ItemTags.MUSIC_DISCS, CreativeModeTabs.TOOLS_AND_UTILITIES,
                ModRegistry.WINTER_DISC_NAME,
                ModRegistry.WINTER_DISC);
        before(e, Items.HONEY_BOTTLE, CreativeModeTabs.FOOD_AND_DRINKS,
                ModRegistry.EGGNOG_NAME,
                ModRegistry.EGGNOG);

        if (SnowySpirit.SUPPLEMENTARIES_INSTALLED) {
            after(e, SuppCompat::isCandy, CreativeModeTabs.FOOD_AND_DRINKS,
                    ModRegistry.GINGER_NAME,
                    ModRegistry.GINGERBREAD_COOKIE);
            after(e, SuppCompat::isCandy, CreativeModeTabs.FOOD_AND_DRINKS,
                    ModRegistry.CANDY_CANE_NAME,
                    ModRegistry.CANDY_CANE);

            after(e, SuppCompat::isGlobe, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                    ModRegistry.SNOW_GLOBE_NAME,
                    ModRegistry.SNOW_GLOBE);
        } else {
            before(e, Items.ROTTEN_FLESH, CreativeModeTabs.FOOD_AND_DRINKS,
                    ModRegistry.GINGER_NAME,
                    ModRegistry.GINGERBREAD_COOKIE);
            before(e, Items.ROTTEN_FLESH, CreativeModeTabs.FOOD_AND_DRINKS,
                    ModRegistry.CANDY_CANE_NAME,
                    ModRegistry.CANDY_CANE);

            after(e, Items.BELL, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                    ModRegistry.SNOW_GLOBE_NAME,
                    ModRegistry.SNOW_GLOBE);
        }
        after(e, ItemTags.BOATS, CreativeModeTabs.TOOLS_AND_UTILITIES,
                ModRegistry.SLED_NAME,
                ModRegistry.SLED_ITEMS.values().stream()
                        .map(i -> (Supplier<Item>) i::asItem).toArray(Supplier[]::new));
        before(e, ItemTags.BANNERS, CreativeModeTabs.COLORED_BLOCKS,
                ModRegistry.GLOW_LIGHTS_NAME,
                ModRegistry.GLOW_LIGHTS_ITEMS.values().toArray(Supplier[]::new));

        before(e, ItemTags.BANNERS, CreativeModeTabs.COLORED_BLOCKS,
                ModRegistry.GUMDROP_NAME,
                ModRegistry.GUMDROPS_BUTTONS.values().toArray(Supplier[]::new));


        before(e, ItemTags.BANNERS, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModRegistry.GLOW_LIGHTS_NAME,
                ModRegistry.GLOW_LIGHTS_ITEMS.values().toArray(Supplier[]::new));

        before(e, ItemTags.BANNERS, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModRegistry.GUMDROP_NAME,
                ModRegistry.GUMDROPS_BUTTONS.values().toArray(Supplier[]::new));

        before(e, Items.GLOW_BERRIES, CreativeModeTabs.NATURAL_BLOCKS,
                ModRegistry.GINGER_NAME,
                ModRegistry.GINGER_FLOWER);

        before(e, Items.WHEAT, CreativeModeTabs.INGREDIENTS,
                ModRegistry.GINGER_NAME,
                ModRegistry.GINGER);

        after(e, Items.SMALL_DRIPLEAF, CreativeModeTabs.NATURAL_BLOCKS,
                ModRegistry.GINGER_NAME,
                ModRegistry.GINGER_WILD);

        add(e, CreativeModeTabs.BUILDING_BLOCKS,
                ModRegistry.GINGER_NAME,
                ModRegistry.GINGERBREAD_FROSTED_BLOCK,
                ModRegistry.GINGERBREAD_BLOCK,
                ModRegistry.GINGERBREAD_STAIRS,
                ModRegistry.GINGERBREAD_SLAB,
                ModRegistry.GINGERBREAD_DOOR,
                ModRegistry.GINGERBREAD_TRAPDOOR);

        add(e, CreativeModeTabs.BUILDING_BLOCKS,
                ModRegistry.CANDY_CANE_NAME,
                ModRegistry.CANDY_CANE_BLOCK);

        before(e, Items.BOOKSHELF, CreativeModeTabs.FUNCTIONAL_BLOCKS,
                ModRegistry.WREATH_NAME,
                ModRegistry.WREATH);
    }


    private static void after(RegHelper.ItemToTabEvent event, TagKey<Item> target,
                              ResourceKey<CreativeModeTab>  tab, String key, Supplier<?>... items) {
        after(event, i -> i.is(target), tab, key, items);
    }

    private static void after(RegHelper.ItemToTabEvent event, Item target,
                              ResourceKey<CreativeModeTab>  tab, String key, Supplier<?>... items) {
        after(event, i -> i.is(target), tab, key, items);
    }

    private static void after(RegHelper.ItemToTabEvent event, Predicate<ItemStack> targetPred,

                              ResourceKey<CreativeModeTab> tab, String key, Supplier<?>... items) {
        if (CommonConfigs.isEnabled(key)) {
            if (MOD_TAB != null) tab = MOD_TAB.getHolder().unwrapKey().get();
            ItemLike[] entries = Arrays.stream(items).map((s -> (ItemLike) (s.get()))).toArray(ItemLike[]::new);
            event.addAfter(tab, targetPred, entries);
        }
    }

    private static void before(RegHelper.ItemToTabEvent event, TagKey<Item> target,
                               ResourceKey<CreativeModeTab>  tab, String key, Supplier<?>... items) {
        before(event, i -> i.is(target), tab, key, items);
    }

    private static void before(RegHelper.ItemToTabEvent event, Item target, ResourceKey<CreativeModeTab> tab, String key, Supplier<?>... items) {
        before(event, i -> i.is(target), tab, key, items);
    }

    private static void before(RegHelper.ItemToTabEvent event, Predicate<ItemStack> targetPred,
                               ResourceKey<CreativeModeTab>  tab, String key, Supplier<?>... items) {
        if (CommonConfigs.isEnabled(key)) {
            if (MOD_TAB != null) tab = MOD_TAB.getHolder().unwrapKey().get();
            ItemLike[] entries = Arrays.stream(items).map(s -> (ItemLike) s.get()).toArray(ItemLike[]::new);
            event.addBefore(tab, targetPred, entries);
        }
    }

    private static void add(RegHelper.ItemToTabEvent event,
                            ResourceKey<CreativeModeTab>  tab, String key, Supplier<?>... items) {
        if (CommonConfigs.isEnabled(key)) {
            if (MOD_TAB != null) tab = MOD_TAB.getHolder().unwrapKey().get();
            ItemLike[] entries = Arrays.stream(items).map((s -> (ItemLike) (s.get()))).toArray(ItemLike[]::new);
            event.add(tab, entries);
        }
    }
}
