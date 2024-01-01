package net.mehvahdjukaar.snowyspirit.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.snowyspirit.SnowySpirit;
import net.mehvahdjukaar.snowyspirit.common.entity.GingyEntity;
import net.mehvahdjukaar.snowyspirit.reg.ClientRegistry;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.DyeColor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GingyEntityRenderer extends HumanoidMobRenderer<GingyEntity, GingyModel> {

    private static final Map<DyeColor, ResourceLocation> GINGY_TEXTURES =
            Arrays.stream(DyeColor.values()).collect(Collectors.toMap(Function.identity(), d ->
                    SnowySpirit.res("textures/entity/gingerbread_golem/" + d.getName() + ".png")));

    public GingyEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new GingyModel(context.bakeLayer(ClientRegistry.GINGY_MODEL)), 0.25f);
    }

    @Override
    protected float getFlipDegrees(GingyEntity livingEntity) {
        return 0;
    }

    @Override
    public ResourceLocation getTextureLocation(GingyEntity entity) {
        return GINGY_TEXTURES.get(entity.getColor());
    }

    @Override
    protected void setupRotations(GingyEntity entity, PoseStack matrixStack, float ageInTicks, float rotationYaw, float partialTicks) {
        super.setupRotations(entity, matrixStack, ageInTicks, rotationYaw, partialTicks);

        if (entity.deathTime > 0) {
            float f = ((float) entity.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;
            f = Mth.sqrt(f);
            if (f > 1.0F) {
                f = 1.0F;
            }
            int deg = entity.isForwardDeathAnim() ? -90 : 90;
            matrixStack.mulPose(Axis.XP.rotationDegrees(f * deg));
        }

        float period = 2.5f * Mth.PI;
        float limbSwingAmount = 0.0F;
        float walkAnim = 0.0F;
        boolean orderedToSit = entity.isOrderedToSit();
        if (orderedToSit) {
            matrixStack.translate(0, -0.25, 0);
        }
        if (!orderedToSit && entity.isAlive()) {
            limbSwingAmount = entity.walkAnimation.speed(partialTicks);
            walkAnim = entity.walkAnimation.position(partialTicks);
            if (entity.isBaby()) {
                walkAnim *= 3.0F;
            }
            // cap limb swing
            // this looks wonky at higher values and can grow a lot, so we just use for smooth in and out of anim
            float maxLimbSwing = 1;
            if (limbSwingAmount > maxLimbSwing) {
                limbSwingAmount = maxLimbSwing;
            }
        }

        if (limbSwingAmount > 0.001) {
            float angle = walkAnim * (Mth.TWO_PI / period);
            int sideSwayPower = 20;
            matrixStack.mulPose(Axis.ZP.rotationDegrees(Mth.cos(angle) * sideSwayPower * limbSwingAmount));
        }
    }
}
