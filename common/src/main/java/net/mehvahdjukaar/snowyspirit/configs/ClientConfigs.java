package net.mehvahdjukaar.snowyspirit.configs;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.snowyspirit.SnowySpirit;

import java.util.function.Supplier;

public class ClientConfigs {


    public static final ModConfigHolder SPEC;

    static {
        ConfigBuilder builder = ConfigBuilder.create(SnowySpirit.res("client"), ConfigType.CLIENT);

        builder.push("particles");
        PARTICLE_MODE = builder.comment("Glow light particle mode. I made 2 variants of this so choose the one you like the most")
                        .define("glow_particle_mode",1, 1, 3);
        SLED_SOUND_AMPLIFIER = builder.comment("Increases sled sound volume")
                        .define("sled_sound_volume", 1.2, 0, 20);
        builder.pop();

        SPEC = builder.build();
    }

    public static final Supplier<Integer> PARTICLE_MODE;
    public static final Supplier<Double> SLED_SOUND_AMPLIFIER;

    public static void init() {
    }
}