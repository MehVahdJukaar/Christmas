package net.mehvahdjukaar.snowyspirit.mixins;

import net.mehvahdjukaar.snowyspirit.common.entity.SledEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;



@Mixin(LivingEntity.class)
public abstract class EntityMixin extends Entity{

    protected EntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "canFreeze()Z", at = @At("RETURN"), cancellable = true)
    private void canFreeze(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            if (this.isPassenger()) {
                Entity v = this.getVehicle();
                if (v instanceof SledEntity sled && sled.isComfy()) {
                    cir.setReturnValue(false);
                }
            }
        }
    }
}
