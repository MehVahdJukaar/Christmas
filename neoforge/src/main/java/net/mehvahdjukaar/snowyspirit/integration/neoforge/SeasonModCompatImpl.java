package net.mehvahdjukaar.snowyspirit.integration.forge;

import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigBuilder;
import net.minecraft.world.level.Level;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

//serene seasons
public class SeasonModCompatImpl {

    private static Supplier<List<String>> SEASONS_CONFIG;
    private static List<Season.SubSeason> VALID_WINTER_SEASONS = new ArrayList<>();


    //if winder AI should be on
    public static boolean isWinter(Level level) {
        var s = SeasonHelper.getSeasonState(level);
        if (s == null) return false;
        return (VALID_WINTER_SEASONS.contains(s.getSubSeason()));
    }


    public static void addConfig(ConfigBuilder builder) {
        SEASONS_CONFIG = builder.comment("Season in which the mod villager AI behaviors will be active")
                .define("winter_season_sub_seasons",
                        List.of(Season.SubSeason.MID_WINTER.toString(), Season.SubSeason.LATE_WINTER.toString()),
                        s -> Arrays.stream(Season.SubSeason.values()).anyMatch(d -> d.toString().equals(s)));
    }

    public static void refresh() {
        VALID_WINTER_SEASONS = SEASONS_CONFIG.get().stream().map(Season.SubSeason::valueOf).toList();
    }
}
