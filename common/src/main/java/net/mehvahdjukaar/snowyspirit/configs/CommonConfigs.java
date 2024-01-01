package net.mehvahdjukaar.snowyspirit.configs;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.api.platform.configs.ModConfigHolder;
import net.mehvahdjukaar.snowyspirit.SnowySpirit;
import net.mehvahdjukaar.snowyspirit.integration.SeasonModCompat;
import net.mehvahdjukaar.snowyspirit.reg.ModRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class CommonConfigs {

    public static final ModConfigHolder SPEC;

    private static final Map<String, Supplier<Boolean>> FEATURE_TOGGLES = new HashMap<>();

    public static void init() {
    }




    static{
        ConfigBuilder builder = ConfigBuilder.create(SnowySpirit.res("common"), ConfigType.COMMON);


        builder.push("snowy_season");

        builder.push("snow_season_start");
        START_MONTH = builder.comment("Day from which villagers will start placing wreaths, presents and throwing snowballs")
                .define("month", 12, 1, 12);
        START_DAY = builder.comment("Day from which villagers will  start placing wreaths, presents and throwing snowballs")
                .define("day", 20, 1, 31);
        builder.pop();
        builder.push("snow_season_end");
        END_MONTH = builder.comment("Day from which villagers will start removing placed wreaths")
                .define("month", 1, 1, 12);
        END_DAY = builder.comment("Day from which villagers will start removing placed wreaths")
                .define("day", 30, 1, 31);
        builder.pop();

        builder.push("season_mod_compat");

        SEASONS_MOD_COMPAT = builder.comment("Enables compatibility with Serene Seasons (Forge) or Fabric Seasons (Fabric). Only takes effect if the mod is installed. Will make snowy season only active during certain seasons. Note that this will override previous time window settings")
                .define("enabled", SnowySpirit.SEASON_MOD_INSTALLED);
        if (SnowySpirit.SEASON_MOD_INSTALLED) SeasonModCompat.addConfig(builder);
        builder.pop();
        builder.pop();

        builder.push("misc");
        DEBUG_RESOURCES = builder.comment("Save generated resources to disk in a 'debug' folder in your game directory. Mainly for debug purposes but can be used to generate assets in all wood types for your mods :0")
                .define("debug_save_dynamic_pack", false);
        CUSTOM_CONFIGURED_SCREEN = builder.comment("Enables custom Configured config screen")
                .define("custom_configured_screen", true);
        MOD_TAB = builder.define("mod_creative_tab", false);
        builder.pop();

        builder.push("blocks_and_items");
        GUMDROPS = feature(builder, ModRegistry.GUMDROP_NAME);
        GLOW_LIGHTS = feature(builder, ModRegistry.GLOW_LIGHTS_NAME);
        CANDY_CANE = feature(builder, ModRegistry.CANDY_CANE_NAME);
        GINGER = feature(builder, ModRegistry.GINGER_NAME);
        EGGNOG = feature(builder, ModRegistry.EGGNOG_NAME);
        WREATH = feature(builder, ModRegistry.WREATH_NAME);
        SNOW_GLOBE = feature(builder, ModRegistry.SNOW_GLOBE_NAME);
        GINGERBREAD_MAN = feature(builder.comment("Only make sense to turn off with both ginger and gumdrops off"), ModRegistry.GINGERBREAD_GOLEM_NAME);
        builder.pop();


        builder.push(ModRegistry.SLED_NAME);
        SLEDS = feature(builder);

        builder.push("physics").comment("Advanced settings. Use to alter sled physics");


        builder.push("friction").comment("Controls how sleds slide along other blocks." +
                "In other words its the inverse of friction"+
                "This number affect how the sled speed is multiplied each tick. A value of 1 will make it keep its velocity forever"+
                "Increasing these values will effectively increase the sled top speed when on these blocks");
        SAND_FRICTION = builder.define("sand_slipperiness", 0.83d, 0, 1);
        SNOW_FRICTION = builder.define("snow_slipperiness",0.985d, 0,1);
        ICE_FRICTION_MULTIPLIER = builder.comment("Used to decrease ice friction (for sleds)")
                .define("ice_slipperiness_mult", 0.97, 0, 1);
        SLOPE_FRICTION_INCREASE = builder.comment("Factor that will be added to a block slipperiness when the sled is considered on a slope (angled down) " +
                "This is also scaled by the slope angle, applying the full number at 45 degrees. " +
                        "This config alters how fast sleds go down slopes")
                        .define("slope_slipperiness_increment", 0.06,0,1);

        ROTATION_FRICTION = builder.comment("Multiply angular velocity by this number each tick. " +
                "Can be thought of as rotational drag")
                        .define("rotation_slipperiness", 0.92, 0,1);
        ROTATION_FRICTION_ON_W = builder.comment("Same as above but applied when you are pressing W. " +
                "Lower number makes it harder to steer when accelerating forward")
                        .define("rotation_slipperiness_on_forward_acceleration", 0.75, 0, 1);
        builder.pop();

        builder.push("steering").comment("Controls the forces applied each tick when moving a sled" +
                "Also effectively indirectly alters the sled max speed");
        FORWARD_ACCELERATION = builder.comment("Intensity of velocity increase applied when pressing forward")
                        .define("forward_acceleration", 0.015, 0,1);
        FORWARD_ACCELERATION_WOLF = builder.comment("Same as above but only when sled has a wolf")
                .define("forward_acceleration_with_wolf", 0.017, 0,1);
        FORWARD_ACCELERATION_WHEN_NOT_ON_SNOW = builder.comment("Acceleration when not on snow. Note that this is noticeably higher since its the higher frictions of those blocks that dont allow the sled to move fast")
                .define("forward_acceleration_when_not_on_snow", 0.037, 0, 1);
        BACKWARDS_ACCELERATION = builder.define("backwards_acceleration", 0.005, 0,1);
        SIDE_ACCELERATION = builder.define("backwards_acceleration", 0.005, 0,1);

        STEER_FACTOR = builder.comment("Affects the intensity of side steering")
                        .define("steer_factor",0.042, 0, 1);
        STEER_FACTOR_WOLF = builder.comment("Same as above but when a wolf is active")
                .define("steer_factor_with_wolf",0.042+0.025, 0, 1);
        builder.pop();


        builder.pop();

        MAX_SLED_PULLER_SIZE = builder.comment("Max allowed size of a sled entity puller. Allowing bigger ones could cause issue as hitbox will clash with sled itself")
                        .define("max_sled_puller_size", 1.25, 0.1, 3);

        builder.pop();


        builder.onChange(SnowySpirit::onConfigReload);
        SPEC = builder.build();
        //load early
        SPEC.forceLoad();
    }


    public static final Supplier<Integer> START_DAY;
    public static final Supplier<Integer> START_MONTH;

    public static final Supplier<Integer> END_DAY;
    public static final Supplier<Integer> END_MONTH;

    public static final Supplier<Boolean> SEASONS_MOD_COMPAT;

    public static final Supplier<Boolean> MOD_TAB;

    public static final Supplier<Boolean> CUSTOM_CONFIGURED_SCREEN;

    public static final Supplier<Boolean> DEBUG_RESOURCES;

    public static final Supplier<Double> SAND_FRICTION;
    public static final Supplier<Double> SNOW_FRICTION;
    public static final Supplier<Double> ICE_FRICTION_MULTIPLIER;
    public static final Supplier<Double> SLOPE_FRICTION_INCREASE;
    public static final Supplier<Double> ROTATION_FRICTION;
    public static final Supplier<Double> ROTATION_FRICTION_ON_W;

    public static final Supplier<Double> FORWARD_ACCELERATION;
    public static final Supplier<Double> FORWARD_ACCELERATION_WOLF;
    public static final Supplier<Double> FORWARD_ACCELERATION_WHEN_NOT_ON_SNOW;
    public static final Supplier<Double> BACKWARDS_ACCELERATION;
    public static final Supplier<Double> SIDE_ACCELERATION;

    public static final Supplier<Double> STEER_FACTOR;
    public static final Supplier<Double> STEER_FACTOR_WOLF;

    public static final Supplier<Double> MAX_SLED_PULLER_SIZE;

    //registry stuff
    public static final Supplier<Boolean> SLEDS;
    public static final Supplier<Boolean> GUMDROPS;
    public static final Supplier<Boolean> GLOW_LIGHTS;
    public static final Supplier<Boolean> CANDY_CANE;
    public static final Supplier<Boolean> GINGER;
    public static final Supplier<Boolean> EGGNOG;
    public static final Supplier<Boolean> WREATH;
    public static final Supplier<Boolean> SNOW_GLOBE;
    public static final Supplier<Boolean> GINGERBREAD_MAN;


    private static Supplier<Boolean> feature(ConfigBuilder builder) {
        return feature(builder, "enabled", builder.currentCategory(), true);
    }

    private static Supplier<Boolean> feature(ConfigBuilder builder, String name) {
        return feature(builder, name, name, true);
    }

    private static Supplier<Boolean> feature(ConfigBuilder builder, String name, String key, boolean value) {
        var config = builder.gameRestart().define(name, value);
        FEATURE_TOGGLES.put(key, config);
        return config;
    }

    public static boolean isEnabled(String key) {
        if(key.contains("candy_cane"))return CANDY_CANE.get();
        else if(key.contains("sled"))return SLEDS.get();
        else if(key.contains("gumdrop"))return GUMDROPS.get();
        else if(key.contains("glow_light"))return GLOW_LIGHTS.get();
        else if(key.contains("ginger"))return GINGER.get();
        return FEATURE_TOGGLES.getOrDefault(key, () -> true).get();
    }

}
