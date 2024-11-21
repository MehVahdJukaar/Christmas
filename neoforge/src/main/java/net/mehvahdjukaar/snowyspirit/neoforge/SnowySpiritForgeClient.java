package net.mehvahdjukaar.snowyspirit.neoforge;

import net.mehvahdjukaar.snowyspirit.SnowySpirit;
import net.mehvahdjukaar.snowyspirit.common.wreath.ClientEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = SnowySpirit.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class SnowySpiritForgeClient {


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void tickEvent(ClientTickEvent.Post event) {
        ClientEvents.tickEvent();
    }

    @SuppressWarnings("ConstantConditions")
    @SubscribeEvent
    public static void renderWreaths(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
            ClientEvents.renderWreaths(event.getPoseStack());
        }
    }


}