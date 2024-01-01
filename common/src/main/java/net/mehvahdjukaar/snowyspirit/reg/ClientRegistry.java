package net.mehvahdjukaar.snowyspirit.reg;

import net.mehvahdjukaar.moonlight.api.client.model.NestedModelLoader;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.snowyspirit.SnowySpirit;
import net.mehvahdjukaar.snowyspirit.client.*;
import net.mehvahdjukaar.snowyspirit.common.block.GlowLightsBlockTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ClientRegistry {

    public static final ModelLayerLocation SLED_MODEL = loc("sled");
    public static final ModelLayerLocation SLED_MODEL_BAMBOO = loc("sled_bamboo");
    public static final ModelLayerLocation QUILT_MODEL = loc("quilt");
    public static final ModelLayerLocation GINGY_MODEL = loc("gingy");

    private static ModelLayerLocation loc(String name) {
        return new ModelLayerLocation(SnowySpirit.res(name), name);
    }

    public static void init() {
        ClientHelper.addModelLayerRegistration(ClientRegistry::registerModelLayers);
        ClientHelper.addEntityRenderersRegistration(ClientRegistry::registerEntityRenderers);
        ClientHelper.addModelLoaderRegistration(ClientRegistry::registerModelLoaders);
        ClientHelper.addBlockColorsRegistration(ClientRegistry::registerBlockColors);
        ClientHelper.addParticleRegistration(ClientRegistry::registerParticles);
    }

    private static void registerParticles(ClientHelper.ParticleEvent event) {
        event.register(ModRegistry.GLOW_LIGHT_PARTICLE.get(), GlowLightParticle.Provider::new);
    }


    public static void setup() {
        ClientHelper.registerRenderType(ModRegistry.GINGER_CROP.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.GINGER_WILD.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.SNOW_GLOBE.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.WREATH.get(), RenderType.cutout());
        ClientHelper.registerRenderType(ModRegistry.GINGER_POT.get(), RenderType.cutout());
        for (var v : ModRegistry.GUMDROPS_BUTTONS.values()) {
            ClientHelper.registerRenderType(v.get(), RenderType.translucent());
        }
        for (var v : ModRegistry.GLOW_LIGHTS_BLOCKS.values()) {
            //TODO: use forge emissive layer
            ClientHelper.registerRenderType(v.get(), RenderType.cutout());
        }

        ItemProperties.register(ModRegistry.GINGERBREAD_COOKIE.get(), ResourceLocation.parse("shape"),
                (stack, world, entity, s) -> (System.identityHashCode(stack) % 4)/3f);
    }

    private static void registerEntityRenderers(ClientHelper.EntityRendererEvent event) {
        event.register(ModRegistry.SLED.get(), SledEntityRenderer::new);
        event.register(ModRegistry.CONTAINER_ENTITY.get(), ContainerHolderEntityRenderer::new);
        event.register(ModRegistry.GINGERBREAD_GOLEM.get(), GingyEntityRenderer::new);
    }

    private static void registerModelLayers(ClientHelper.ModelLayerEvent event) {
        event.register(SLED_MODEL, SledModel::createMesh);
        event.register(SLED_MODEL_BAMBOO, SledModel::createBambooMesh);
        event.register(QUILT_MODEL, QuiltModel::createBodyLayer);
        event.register(GINGY_MODEL, GingyModel::createBodyLayer);
    }

    private static void registerModelLoaders(ClientHelper.ModelLoaderEvent event) {
        event.register(SnowySpirit.res("glow_lights"),  new NestedModelLoader("overlay", GlowLightsBakedModel::new));
    }

    private static void registerBlockColors(ClientHelper.BlockColorEvent event) {
        event.register(new MimicBlockColor(), ModRegistry.GLOW_LIGHTS_BLOCKS.values().stream()
                .map(Supplier::get).toArray(Block[]::new));

    }


    private static class MimicBlockColor implements BlockColor {

        @Override
        public int getColor(BlockState state, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos, int tint) {
            return col(state, world, pos, tint);
        }

        public static int col(BlockState state, BlockAndTintGetter level, BlockPos pos, int tint) {
            if (level != null && pos != null) {
                if (level.getBlockEntity(pos) instanceof GlowLightsBlockTile tile) {
                    BlockState mimic = tile.getHeldBlock();
                    if (mimic != null && !mimic.hasBlockEntity()) {
                        return Minecraft.getInstance().getBlockColors().getColor(mimic, level, pos, tint);
                    }
                }
            }
            return -1;
        }

        public static class NoParticle implements BlockColor {
            @Override
            public int getColor(BlockState state, @Nullable BlockAndTintGetter world, @Nullable BlockPos pos, int tint) {
                if (tint == 0) return -1;
                return col(state, world, pos, tint);
            }
        }
    }


}
