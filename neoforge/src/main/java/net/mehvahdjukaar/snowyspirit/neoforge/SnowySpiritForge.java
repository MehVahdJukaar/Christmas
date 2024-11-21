package net.mehvahdjukaar.snowyspirit.neoforge;

import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.snowyspirit.SnowySpirit;
import net.mehvahdjukaar.snowyspirit.common.entity.ContainerHolderEntity;
import net.mehvahdjukaar.snowyspirit.common.wreath.ServerEvents;
import net.mehvahdjukaar.snowyspirit.integration.configured.ModConfigSelectScreen;
import net.mehvahdjukaar.snowyspirit.reg.ModRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.Nullable;

/**
 * Author: MehVahdJukaar
 */
@Mod(SnowySpirit.MOD_ID)
public class SnowySpiritForge {

    public SnowySpiritForge(IEventBus busEvent) {

        SnowySpirit.commonInit();

        if (PlatHelper.getPhysicalSide().isClient()) {
            ClientHelper.addClientSetup(() -> {
                if (ModList.get().isLoaded("configured")) {
                    //TODO: add back
                    ModConfigSelectScreen.registerConfigScreen(SnowySpirit.MOD_ID, ModConfigSelectScreen::new);
                }
            });
        }

        NeoForge.EVENT_BUS.register(this);
        PlatHelper.addCommonSetup(() -> {
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(Utils.getID(ModRegistry.GINGER.get()), ModRegistry.GINGER_POT);
        });

        busEvent.addListener(this::onRegisterCapability);

    }

    public void onRegisterCapability(RegisterCapabilitiesEvent event){
        event.registerEntity(Capabilities.ItemHandler.ENTITY,
                ModRegistry.CONTAINER_ENTITY.get(),
                (entity, object2) -> new InvWrapper(entity));
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        InteractionResult res = ServerEvents.onRightClickBlock(event.getEntity(), event.getLevel(), event.getItemStack(), event.getPos());
        if (res != InteractionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(res);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void tickEvent(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            ServerEvents.tickEvent(level);
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerEvents.onPlayerLogin(event.getEntity());
    }

    @SubscribeEvent
    public void onDimensionChanged(PlayerEvent.PlayerChangedDimensionEvent event) {
        ServerEvents.onDimensionChanged(event.getEntity());
    }
}
