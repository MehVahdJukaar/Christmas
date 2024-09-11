package net.mehvahdjukaar.snowyspirit.reg;

import net.mehvahdjukaar.snowyspirit.SnowySpirit;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

public class ModTags {

    public static final TagKey<Item> VALID_CONTAINERS = TagKey.create(Registries.ITEM, SnowySpirit.res("sled_container"));
    public static final TagKey<EntityType<?>> WOLVES = TagKey.create(Registries.ENTITY_TYPE, SnowySpirit.res("sled_pullers"));
    public static final TagKey<EntityType<?>> GINGY_TARGETS = TagKey.create(Registries.ENTITY_TYPE, SnowySpirit.res("gingy_targets"));
    public static final TagKey<Block> SLED_SNOW = TagKey.create(Registries.BLOCK,SnowySpirit.res("sled_snow"));
    public static final TagKey<Block> SLED_SAND = TagKey.create(Registries.BLOCK,SnowySpirit.res("sled_sand"));
    public static final TagKey<Block> ACCEPTS_GLOW_LIGHTS = TagKey.create(Registries.BLOCK,SnowySpirit.res("glow_lights_placeable"));
    public static final TagKey<Block> GINGERBREADS = TagKey.create(Registries.BLOCK,SnowySpirit.res("gingerbreads"));

    public static final TagKey<Biome> HAS_GINGER = TagKey.create(Registries.BIOME, SnowySpirit.res("has_wild_ginger"));
    public static final TagKey<Biome> HAS_GINGER_DENSE = TagKey.create(Registries.BIOME, SnowySpirit.res("has_wild_ginger_dense"));

}
