package net.mehvahdjukaar.snowyspirit.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.mehvahdjukaar.snowyspirit.common.entity.SledEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import traben.entity_model_features.models.animation.EMFAnimation;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> {

    @Shadow
    protected M model;

    protected LivingEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }


    //animations for sled pullers
    //injects in all calls, so it works with optishit code. needs redirect
    @WrapOperation(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE",
                    target = "net/minecraft/world/entity/LivingEntity.isPassenger ()Z"))
    private boolean isPassenger(LivingEntity instance, Operation<Boolean> original) {
        Entity vehicle = instance.getVehicle();
        if (vehicle instanceof SledEntity sledEntity) {
            if (sledEntity.isMyPuller(instance)){
                model.riding = false;
                return false;
            }
        }
        return original.call(instance);
    }


}
