package net.mehvahdjukaar.snowyspirit.mixins;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.snowyspirit.SnowySpirit;
import net.mehvahdjukaar.snowyspirit.common.entity.ContainerHolderEntity;
import net.mehvahdjukaar.snowyspirit.common.entity.SledEntity;
import net.mehvahdjukaar.snowyspirit.configs.CommonConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.structures.IglooPieces;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IglooPieces.IglooPiece.class)
public abstract class IglooPiecesMixin extends TemplateStructurePiece {

    protected IglooPiecesMixin(StructurePieceType structurePieceType, int i, StructureTemplateManager structureTemplateManager, ResourceLocation resourceLocation, String string, StructurePlaceSettings structurePlaceSettings, BlockPos blockPos) {
        super(structurePieceType, i, structureTemplateManager, resourceLocation, string, structurePlaceSettings, blockPos);
    }

    @Shadow
    private static StructurePlaceSettings makeSettings(Rotation pRotation, ResourceLocation pLocation) {
        return null;
    }


    @Inject(method = "postProcess", at = @At("RETURN"))
    private void addSleds(WorldGenLevel worldGenLevel, StructureManager structureManager,
                          ChunkGenerator chunkGenerator, RandomSource pRandom, BoundingBox pBox,
                          ChunkPos chunkPos, BlockPos pos, CallbackInfo ci) {

        if (pRandom.nextFloat() > 0.3 && CommonConfigs.SLEDS.get()) {

            ResourceLocation resourcelocation = ResourceLocation.parse(this.templateName);
            if (resourcelocation.equals(ResourceLocation.parse("igloo/top"))) {


                StructurePlaceSettings structureplacesettings = makeSettings(this.placeSettings.getRotation(), resourcelocation);
                BlockPos p = this.templatePosition.offset(StructureTemplate.calculateRelativePosition(structureplacesettings,
                        new BlockPos(6, 0, 0)));
                int y = worldGenLevel.getHeight(Heightmap.Types.WORLD_SURFACE_WG, p.getX(), p.getZ());
                BlockPos blockPos = new BlockPos(p.getX(), y, p.getZ());
                if (pBox.isInside(blockPos)) {
                    Level level = worldGenLevel.getLevel();
                    //bukkit doesn't like spawn calls from another thread...
                    var server = level.getServer();
                    if (server == null) return;
                    server.executeIfPossible(() -> {
                        SledEntity sledEntity = new SledEntity(level, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ());
                        Item chest = PlatHelper.isModLoaded("quark") ?
                                BuiltInRegistries.ITEM.get(ResourceLocation.parse("quark:spruce_chest")) : Items.CHEST;
                        ContainerHolderEntity c = sledEntity.tryAddingChest(chest.getDefaultInstance());
                        if (c != null) {
                            c.setLootTable(SnowySpirit.res("chests/igloo_sled"), level.random.nextLong());
                        }
                        level.addFreshEntity(sledEntity);
                    });
                }
            }
        }
    }
}
