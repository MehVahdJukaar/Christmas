package net.mehvahdjukaar.snowyspirit.wreath_stuff.capabilities;

import net.mehvahdjukaar.snowyspirit.SnowySpirit;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.event.AttachCapabilitiesEvent;

import javax.annotation.Nullable;

public class ModCapabilities {

    public static final Capability<WreathCapability> WREATH_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(WreathCapability.class);
    }

    public static void attachCapabilities(AttachCapabilitiesEvent<Level> event) {
        WreathCapability wreathProvider = new WreathCapability();
        event.addCapability(SnowySpirit.res("wreath"), wreathProvider);
        event.addListener(wreathProvider::invalidate);
    }

    @SuppressWarnings("ConstantConditions")
    
    public static <T> T get(ICapabilityProvider provider,Capability<T> cap){
        return provider.getCapability(cap).orElse(null);
    }

}