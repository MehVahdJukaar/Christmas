package net.mehvahdjukaar.snowyspirit.reg;

import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.snowyspirit.SnowySpirit;
import net.mehvahdjukaar.snowyspirit.client.SledSoundInstance;
import net.mehvahdjukaar.snowyspirit.common.entity.SledEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.JukeboxSong;

import java.util.function.Supplier;

public class ModSounds {

    public static void init(){

    }
    public static final ResourceKey<JukeboxSong> WINTER_DISC_JUKEBOX = ResourceKey.create(Registries.JUKEBOX_SONG,
            SnowySpirit.res("a_carol"));

    public static final Supplier<SoundEvent> WINTER_MUSIC = RegHelper.registerSound(SnowySpirit.res("music_disc.a_carol"));
    public static final Supplier<SoundEvent> SLED_SOUND = RegHelper.registerSound(SnowySpirit.res("entity.sled"));
    public static final Supplier<SoundEvent> SLED_SOUND_SNOW = RegHelper.registerSound(SnowySpirit.res("entity.sled_snow"));


}
