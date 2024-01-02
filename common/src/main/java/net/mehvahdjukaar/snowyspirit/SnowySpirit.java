package net.mehvahdjukaar.snowyspirit;


import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.snowyspirit.common.block.GumdropButton;
import net.mehvahdjukaar.snowyspirit.common.entity.GingyEntity;
import net.mehvahdjukaar.snowyspirit.common.network.ModMessages;
import net.mehvahdjukaar.snowyspirit.configs.ClientConfigs;
import net.mehvahdjukaar.snowyspirit.configs.CommonConfigs;
import net.mehvahdjukaar.snowyspirit.dynamicpack.ClientDynamicResourcesHandler;
import net.mehvahdjukaar.snowyspirit.dynamicpack.ServerDynamicResourcesHandler;
import net.mehvahdjukaar.snowyspirit.integration.FDCompat;
import net.mehvahdjukaar.snowyspirit.integration.SeasonModCompat;
import net.mehvahdjukaar.snowyspirit.reg.*;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
import java.util.Date;


/**
 * Author: MehVahdJukaar
 */
public class SnowySpirit {
    public static final String MOD_ID = "snowyspirit";

    public static ResourceLocation res(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }

    public static final Logger LOGGER = LogManager.getLogger();

    public static final boolean SUPPLEMENTARIES_INSTALLED = PlatHelper.isModLoaded("supplementaries");
    public static final boolean FARMERSDELIGHT_INSTALLED = PlatHelper.isModLoaded("farmersdelight");
    public static final boolean SEASON_MOD_INSTALLED = PlatHelper.isModLoaded(PlatHelper.getPlatform().isForge() ? "sereneseasons" : "seasons");


    public static void commonInit() {

        //TODO: check wolf height
        // add item burntime
        // add block burn time

        CommonConfigs.init();

        ModMessages.init();

        RegHelper.registerSimpleRecipeCondition(SnowySpirit.res("flag"), CommonConfigs::isEnabled);

        ModSounds.init();
        ModRegistry.init();
        ModMemoryModules.init();
        ModCreativeTabs.init();
        if (FARMERSDELIGHT_INSTALLED) FDCompat.init();

        ServerDynamicResourcesHandler.INSTANCE.register();

        PlatHelper.addCommonSetup(ModSetup::setup);

        if (PlatHelper.getPhysicalSide().isClient()) {
            ClientConfigs.init();

            ClientDynamicResourcesHandler.INSTANCE.register();

            ClientRegistry.init();
            ClientHelper.addClientSetup(ClientRegistry::setup);
        }
    }
    //flute pacifies
    //hostile gingeerbread golems
    //mongo
    //sleds loose their chest
    //TODO: sync xRot, chest weight, tweak values
    //TODO: nerf sled acceleration without wolf to make wolf more relevant. can still be used for downhill descent
    //TODO: maybe make friction delend also on xRot to better handle slope descent

    public static boolean IS_CHRISTMAS_REAL_TIME;

    public static boolean USES_SEASON_MOD;

    public static void onConfigReload() {

        //refresh date after configs are loaded
        int startM = CommonConfigs.START_MONTH.get() - 1;
        int startD = CommonConfigs.START_DAY.get();

        int endM = CommonConfigs.END_MONTH.get() - 1;
        int endD = CommonConfigs.END_DAY.get();

        boolean inv = startM > endM;

        //pain
        Date start = new Date(0, startM, startD);
        Date end = new Date((inv ? 1 : 0), endM, endD);

        Date today = new Date(0, Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DATE));
        if (today.before(start) && inv) today = new Date(1, today.getMonth(), today.getDate());
        //TODO: rewrite properly
        //if seasonal use pumpkin placement time window
        IS_CHRISTMAS_REAL_TIME = after(today, start) && before(today, end);
        USES_SEASON_MOD = SEASON_MOD_INSTALLED && CommonConfigs.SEASONS_MOD_COMPAT.get();

        if (USES_SEASON_MOD) {
            SeasonModCompat.refresh();
        }

    }

    public static boolean before(Date obj, Date that) {
        int objYear = obj.getYear();
        int thatYear = that.getYear();
        int objMonth = obj.getMonth();
        int thatMonth = that.getMonth();
        int objDay = obj.getDate();
        int thatDay = that.getDate();

        if (objYear < thatYear) {
            return true;
        } else if (objYear > thatYear) {
            return false;
        } else {
            if (objMonth < thatMonth) {
                return true;
            } else if (objMonth > thatMonth) {
                return false;
            } else {
                return objDay < thatDay;
            }
        }
    }

    public static boolean after(Date obj, Date that) {
        return !before(obj, that) && !obj.equals(that);
    }

    public static boolean isChristmasSeason(Level level) {
        if (USES_SEASON_MOD) return SeasonModCompat.isWinter(level);
        return IS_CHRISTMAS_REAL_TIME;
    }

    public static void trySpawningGingy(BlockState pumpkinState, LevelAccessor level, BlockPos pumpkinPos, @Nullable Entity entity) {
        BlockPos below = pumpkinPos.below();
        if (level instanceof ServerLevel serverLevel && level.getBlockState(below).is(ModTags.GINGERBREADS)) {
            Direction dir = pumpkinState.getValue(CarvedPumpkinBlock.FACING);
            BlockPos button = below.relative(dir);
            BlockState state = level.getBlockState(button);
            if (state.getBlock() instanceof GumdropButton b && state.getValue(GumdropButton.FACING) == dir) {
                GingyEntity golem = ModRegistry.GINGERBREAD_GOLEM.get().create(serverLevel);
                if (golem != null) {
                    level.removeBlock(pumpkinPos, false);
                    level.removeBlock(button, false);
                    level.removeBlock(below, false);
                    golem.moveTo(pumpkinPos.getX() + 0.5, pumpkinPos.getY() + 0.05 - 1, pumpkinPos.getZ() + 0.5, dir.toYRot(), 0.0F);
                    if (entity instanceof ServerPlayer serverPlayer) {
                        CriteriaTriggers.SUMMONED_ENTITY.trigger(serverPlayer, golem);
                        golem.setOwnerUUID(serverPlayer.getUUID());
                        golem.setPersistenceRequired();
                    }
                    golem.setColor(b.color);
                    golem.setYHeadRot(dir.toYRot());

                    level.addFreshEntity(golem);

                }
            }
        }
    }

    @Deprecated(forRemoval = true)
    public static void giveAdvancement( ServerPlayer sp, String name) {
        Advancement advancement = sp.getServer().getAdvancements().getAdvancement(SnowySpirit.res(name));
        if (advancement != null) {
            if (!sp.getAdvancements().getOrStartProgress(advancement).isDone()) {
                sp.getAdvancements().award(advancement, "unlock");
            }
        }
    }
}
