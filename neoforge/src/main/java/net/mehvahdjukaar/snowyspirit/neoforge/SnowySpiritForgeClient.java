package net.mehvahdjukaar.snowyspirit.neoforge;

import net.mehvahdjukaar.snowyspirit.SnowySpirit;
import net.mehvahdjukaar.snowyspirit.common.wreath.ClientEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.TickEvent;

@Mod.EventBusSubscriber(modid = SnowySpirit.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SnowySpiritForgeClient {


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void tickEvent(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ClientEvents.tickEvent();
        }
    }

    @SuppressWarnings("ConstantConditions")
    @SubscribeEvent
    public static void renderWreaths(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
            ClientEvents.renderWreaths(event.getPoseStack());
        }
    }


}