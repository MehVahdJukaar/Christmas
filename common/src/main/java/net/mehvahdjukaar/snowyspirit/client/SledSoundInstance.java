package net.mehvahdjukaar.snowyspirit.client;

import net.mehvahdjukaar.snowyspirit.common.entity.SledEntity;
import net.mehvahdjukaar.snowyspirit.configs.ClientConfigs;
import net.mehvahdjukaar.snowyspirit.reg.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

public class SledSoundInstance extends AbstractTickableSoundInstance {
    public static final int DELAY = 20;
    public static final float CUTOFF_SPEED = 0.05f;
    public static final int SPEED_DIVIDER = 25;
    private final SledEntity sled;
    private int time;
    private int fastTime;
    private final boolean isSnow;
    private int ticksOnSnow = 0;

    public SledSoundInstance(SledEntity sledEntity, boolean isSnow) {
        super(isSnow ? ModSounds.SLED_SOUND_SNOW.get() : ModSounds.SLED_SOUND.get(), SoundSource.PLAYERS, sledEntity.level.getRandom());
        this.sled = sledEntity;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0F;
        this.isSnow = isSnow;
    }

    @Override
    public boolean canPlaySound() {
        return !this.sled.isSilent();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    public void tick() {
        ++this.time;
        if (!this.sled.isRemoved()) {
            this.x = (float) this.sled.getX();
            this.y = (float) this.sled.getY();
            this.z = (float) this.sled.getZ();
            float f = (float) this.sled.getDeltaMovement().lengthSqr();
            if (f > CUTOFF_SPEED) {
                fastTime++;
            } else {
                fastTime = 0;
                this.volume = 0;
                return;
            }
            if ( f >= 1.0E-7D) {
                this.volume = Mth.clamp(f / SPEED_DIVIDER, 0.0F, 1.1F);
            } else {
                this.volume = 0.0F;
            }


            if (this.time < DELAY) {
                this.volume *= (float) (this.fastTime - DELAY) / DELAY;
            }

            if (this.isSnow) {
                if (!this.sled.getCurrentStatus().onSnow()) {
                    this.volume = 0;
                    this.ticksOnSnow = 0;
                } else {
                    this.ticksOnSnow++;
                    if (this.ticksOnSnow < 5)
                        this.volume *= (float) (this.ticksOnSnow - 5) / 5;
                    this.volume = Math.min(0.8f, volume); //so it never gets pitch shifted
                }
            }

            float f1 = 0.8F;
            if (this.volume > 0.8F) {
                this.pitch = 1.0F + (this.volume - 0.8F);
            } else {
                this.pitch = 1.0F;
            }

            //amplify a bit since itwas too quiet
            this.volume*= ClientConfigs.SLED_SOUND_AMPLIFIER.get();

        } else {
            this.stop();
        }
    }

    public static void playAt(SledEntity sledEntity) {
        Minecraft.getInstance().getSoundManager().play(new SledSoundInstance(sledEntity, false));
        Minecraft.getInstance().getSoundManager().play(new SledSoundInstance(sledEntity,true));
    }
}