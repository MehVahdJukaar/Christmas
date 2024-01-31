package net.mehvahdjukaar.snowyspirit.reg;

import net.mehvahdjukaar.moonlight.api.misc.ModSoundType;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.snowyspirit.SnowySpirit;
import net.mehvahdjukaar.snowyspirit.client.SledSoundInstance;
import net.mehvahdjukaar.snowyspirit.common.entity.SledEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.block.SoundType;

import java.util.function.Supplier;

public class ModSounds {

    public static void init(){

    }
    public static final ResourceKey<JukeboxSong> WINTER_DISC_JUKEBOX = ResourceKey.create(Registries.JUKEBOX_SONG,
            SnowySpirit.res("a_carol"));

    public static final Supplier<SoundEvent> WINTER_MUSIC = RegHelper.registerSound(SnowySpirit.res("music_disc.a_carol"));
    public static final Supplier<SoundEvent> SLED_SOUND = RegHelper.registerSound(SnowySpirit.res("entity.sled"));
    public static final Supplier<SoundEvent> SLED_SOUND_SNOW = RegHelper.registerSound(SnowySpirit.res("entity.sled_snow"));
    public static final Supplier<SoundEvent> GINGERBREAD_BREAK = RegHelper.registerSound(SnowySpirit.res("block.gingerbread.break"));
    public static final Supplier<SoundEvent> GINGERBREAD_PLACE = RegHelper.registerSound(SnowySpirit.res("block.gingerbread.place"));
    public static final Supplier<SoundEvent> GINGERBREAD_HIT = RegHelper.registerSound(SnowySpirit.res("block.gingerbread.hit"));
    public static final Supplier<SoundEvent> GINGERBREAD_FALL = RegHelper.registerSound(SnowySpirit.res("block.gingerbread.fall"));
    public static final Supplier<SoundEvent> GINGERBREAD_STEP = RegHelper.registerSound(SnowySpirit.res("block.gingerbread.step"));


    public static final SoundType GINGERBREAD = new ModSoundType(
            1,1,GINGERBREAD_BREAK, GINGERBREAD_STEP,GINGERBREAD_PLACE, GINGERBREAD_HIT, GINGERBREAD_FALL);
}
