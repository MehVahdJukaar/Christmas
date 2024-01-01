package net.mehvahdjukaar.snowyspirit.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.snowyspirit.SnowySpirit;
import net.mehvahdjukaar.snowyspirit.common.wreath.ClientEvents;
import net.mehvahdjukaar.snowyspirit.common.wreath.ServerEvents;
import net.mehvahdjukaar.snowyspirit.reg.ClientRegistry;
import net.mehvahdjukaar.snowyspirit.reg.ModSetup;
import net.mehvahdjukaar.snowyspirit.reg.ModTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.GenerationStep;

public class SnowySpiritFabric implements ModInitializer {

    @Override
    public void onInitialize() {

        SnowySpirit.commonInit();

        PlatHelper.addCommonSetup(SnowySpiritFabric::commonSetup);

        if(PlatHelper.getPhysicalSide().isClient()){
            WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
                ClientEvents.renderWreaths(context.matrixStack());
            });

            ClientTickEvents.END_CLIENT_TICK.register(client -> ClientEvents.tickEvent());
        }

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> ServerEvents.onDimensionChanged(newPlayer));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> ServerEvents.onPlayerLogin(handler.player));

        ServerTickEvents.END_WORLD_TICK.register(ServerEvents::tickEvent);

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) ->
                ServerEvents.onRightClickBlock(player, world, player.getItemInHand(hand), hitResult.getBlockPos()));
    }


    private static void commonSetup() {
        BiomeModifications.addFeature(BiomeSelectors.tag(ModTags.HAS_GINGER),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                ResourceKey.create(Registries.PLACED_FEATURE, SnowySpirit.res("wild_ginger")));

        BiomeModifications.addFeature(BiomeSelectors.tag(ModTags.HAS_GINGER_DENSE),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                ResourceKey.create(Registries.PLACED_FEATURE, SnowySpirit.res("wild_ginger_dense")));
    }


}
