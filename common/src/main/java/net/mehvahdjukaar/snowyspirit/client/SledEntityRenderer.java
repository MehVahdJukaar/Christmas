package net.mehvahdjukaar.snowyspirit.client;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.snowyspirit.SnowySpirit;
import net.mehvahdjukaar.snowyspirit.common.entity.SledEntity;
import net.mehvahdjukaar.snowyspirit.reg.ClientRegistry;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.Map;
import java.util.stream.Stream;

public class SledEntityRenderer extends EntityRenderer<SledEntity> {

    private final Map<WoodType, ResourceLocation> textures;
    private final Map<DyeColor, ResourceLocation> quiltTextures;
    private final SledModel<SledEntity> model;
    private final SledModel<SledEntity> modelBamboo;
    private final QuiltModel<SledEntity> quiltModel;

    public SledEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.8F;
        this.model = new SledModel<>(context.bakeLayer(ClientRegistry.SLED_MODEL));
        this.modelBamboo = new SledModel<>(context.bakeLayer(ClientRegistry.SLED_MODEL_BAMBOO));
        this.quiltModel = new QuiltModel<>(context.bakeLayer(ClientRegistry.QUILT_MODEL));
        this.textures = WoodTypeRegistry.getTypes().stream().collect(ImmutableMap.toImmutableMap((e) -> e,
                (t) -> SnowySpirit.res("textures/entity/sled/" + t.getTexturePath() + ".png")));
        this.quiltTextures = Stream.of(DyeColor.values()).collect(ImmutableMap.toImmutableMap((e) -> e,
                (t) -> SnowySpirit.res("textures/entity/sled/quilt/" + t.getName() + ".png")));
    }

    @Override
    public void render(SledEntity sled, float yRot, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {

        double dy = Mth.lerp(partialTicks, sled.prevAdditionalY, sled.cachedAdditionalY);
        poseStack.pushPose();

        poseStack.translate(0.0D, 0.375D + dy, 0.0D);


        //same stuff that happens to yRot when its created
        float xRot = sled.getViewXRot(partialTicks);

        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yRot));
        poseStack.mulPose(Axis.XN.rotationDegrees(xRot));
        float hurtTme = (float) sled.getHurtTime() - partialTicks;
        float damage = sled.getDamage() - partialTicks;
        if (damage < 0.0F) {
            damage = 0.0F;
        }

        float zRot = 0;
        if (hurtTme > 0.0F) {
            zRot = Mth.sin(hurtTme) * hurtTme * damage / 10.0F * (float) sled.getHurtDir();
            poseStack.mulPose(Axis.ZP.rotationDegrees(zRot));
        }

        poseStack.pushPose();
        poseStack.translate(-0, 0.125, 0.4);
        poseStack.scale(0.75F, 0.75F, 0.75F);
        poseStack.translate(-0.5, 0, -0.5);

        poseStack.popPose();

        ResourceLocation resourcelocation = this.getTextureLocation(sled);

        poseStack.scale(-1.0F, -1.0F, 1.0F);

        VertexConsumer vertexconsumer = bufferSource.getBuffer(model.renderType(resourcelocation));
        var mod = resourcelocation.getPath().equals("textures/entity/sled/bamboo.png") ? modelBamboo : model;
        mod.renderToBuffer(poseStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        DyeColor color = sled.getSeatType();
        if (color != null) {
            vertexconsumer = bufferSource.getBuffer(model.renderType(this.quiltTextures.get(color)));
            quiltModel.renderToBuffer(poseStack, vertexconsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        }

        poseStack.popPose();

        this.renderLeash(sled, partialTicks, poseStack, bufferSource,
                (float) Math.toRadians(90 + yRot),
                (float) Math.toRadians(xRot),
                (float) Math.toRadians(zRot),
                dy);

        super.render(sled, yRot, partialTicks, poseStack, bufferSource, light);

        if (this.entityRenderDispatcher.shouldRenderHitBoxes()) {
            this.renderDebugHitbox(poseStack, bufferSource.getBuffer(RenderType.lines()), sled, partialTicks);
        }


    }

    @Override
    public ResourceLocation getTextureLocation(SledEntity sled) {
        return this.textures.get(sled.getWoodType());
    }

    private void renderDebugHitbox(PoseStack pMatrixStack, VertexConsumer pBuffer, SledEntity pEntity, float pPartialTicks) {
        AABB aabb = pEntity.getBoundingBox().move(lerpV(pPartialTicks, pEntity.prevProjectedPos, pEntity.projectedPos))
                .move(-pEntity.getX(), -pEntity.getY(), -pEntity.getZ());
        LevelRenderer.renderLineBox(pMatrixStack, pBuffer, aabb, 1.0F, 0, 0, 1.0F);

        if (pEntity.hasPuller()) {
            aabb = pEntity.pullerAABB.move(-pEntity.getX(), -pEntity.getY(), -pEntity.getZ());
            LevelRenderer.renderLineBox(pMatrixStack, pBuffer, aabb, 0, 1, 0, 1.0F);
        }

        Vec3 movement = lerpV(pPartialTicks, pEntity.prevDeltaMovement, pEntity.getDeltaMovement());

        PoseStack.Pose pose = pMatrixStack.last();
        float mult = 6;
        float eye = (pEntity.getEyeHeight() + 1 + pEntity.cachedAdditionalY);
        pBuffer.addVertex(pose, 0.0F, eye, 0.0F)
                .setColor(0, 255, 0, 255)
                .setNormal(pose, (float) movement.x, (float) movement.y, (float) movement.z);
        pBuffer.addVertex(pose, (float) (movement.x * mult), (float) (eye + movement.y * mult), (float) (movement.z * mult))
                .setColor(0, 255, 0, 255)
                .setNormal(pose, (float) movement.x, (float) movement.y, (float) movement.z);


        pBuffer.addVertex(pose, 0.0F, eye + 0.25f, 0.0F)
                .setColor(255, 0, 255, 255)
                .setNormal(pose, 0, 1, 0);
        pBuffer.addVertex(pose, 0, (float) (eye + 0.25f + pEntity.misalignedFrictionFactor), 0)
                .setColor(255, 0, 255, 255)
                .setNormal(pose, 0, 1, 0);


        if (pEntity.boost) {
            movement = movement.normalize().scale(-1);
            pBuffer.addVertex(pose, 0.0F, eye, 0.0F)
                    .setColor(255, 255, 0, 255)
                    .setNormal(pose, (float) movement.x, (float) movement.y, (float) movement.z);
            pBuffer.addVertex(pose, (float) (movement.x), (float) (eye + movement.y), (float) (movement.z))
                    .setColor(255, 255, 0, 255)
                    .setNormal(pose, (float) movement.x, (float) movement.y, (float) movement.z);
        }


    }

    public static Vec3 lerpV(float delta, Vec3 start, Vec3 end) {
        return new Vec3(
                Mth.lerp(delta, start.x, end.x),
                Mth.lerp(delta, start.y, end.y),
                Mth.lerp(delta, start.z, end.z));
    }


    private void renderLeash(SledEntity sled, float pPartialTicks, PoseStack poseStack, MultiBufferSource pBuffer,
                             float yRot, float xRot, float zRot, double addY) {
        Animal wolf = sled.getSledPuller();
        if (wolf != null) {
            boolean bear = Utils.getID(wolf.getType()).getPath().equals("grizzly_bear");
            Vec3 wolfPos = wolf.getRopeHoldPosition(pPartialTicks).add(0, wolf.isBaby() ? 0.1 : 0.25, 0);

            float bbw = wolf.getBbWidth() / (2.875f + (bear ? 1.5f : 0));
            Vec3 sledOffset = new Vec3(0.4125f, 0, 0.95f);
            Vec3 ropeOffset = new Vec3(bbw, 0, 0);

            //yaw
            float cos = Mth.cos(yRot);
            float sin = Mth.sin(yRot);

            //slope
            float pCos = Mth.cos(xRot);
            float pSin = Mth.sin(xRot);


            //wobble
            float wSin = Mth.sin(zRot);

            double sledX = Mth.lerp(pPartialTicks, sled.xo, sled.getX());
            double sledY = Mth.lerp(pPartialTicks, sled.yo, sled.getY());
            double sledZ = Mth.lerp(pPartialTicks, sled.zo, sled.getZ());

            BlockPos sledEyePos = BlockPos.containing(sled.getEyePosition(pPartialTicks));
            BlockPos wolfEyePos = BlockPos.containing(wolf.getEyePosition(pPartialTicks));

            for (int rope = -1; rope <= 1; rope += 2) {
                VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.leash());
                poseStack.pushPose();


                double wolfOffsetX = cos * ropeOffset.z + sin * ropeOffset.x * rope;
                double wolfOffsetZ = sin * ropeOffset.z - cos * ropeOffset.x * rope;
                double offsetX = (cos * sledOffset.z + sin * sledOffset.x * rope) * pCos;
                double offsetZ = (sin * sledOffset.z - cos * sledOffset.x * rope) * pCos;
                double offsetY = -pSin * sledOffset.length() + 0.25 + addY -
                        sledOffset.x * wSin * rope;
                double pX = sledX + offsetX;
                double pY = sledY + offsetY;
                double pZ = sledZ + offsetZ;
                poseStack.translate(offsetX, offsetY, offsetZ);

                float deltaX = (float) (wolfPos.x + wolfOffsetX - pX);
                float deltaY = (float) (wolfPos.y - pY);
                float deltaZ = (float) (wolfPos.z + wolfOffsetZ - pZ);
                float width = 0.025F;

                Matrix4f matrix4f = poseStack.last().pose();
                float f4 = Mth.invSqrt(deltaX * deltaX + deltaZ * deltaZ) * width / 2.0F;
                //id what these are for but something with angle
                float mathZ = deltaZ * f4;

                float mathX = deltaX * f4;

                int blockLight0 = this.getBlockLightLevel(sled, sledEyePos);
                Level level = wolf.level();
                int blockLight1 = wolf.isOnFire() ? 15 : level.getBrightness(LightLayer.BLOCK, wolfEyePos);
                int skyLight0 = level.getBrightness(LightLayer.SKY, sledEyePos);
                int skyLight1 = level.getBrightness(LightLayer.SKY, wolfEyePos);

                //each lead is composed of 2 strips
                int maxSegments = 12;
                for (int index = 0; index <= maxSegments; ++index) {
                    addVertexPair(vertexconsumer, matrix4f, deltaX, deltaY, deltaZ,
                            blockLight0, blockLight1, skyLight0, skyLight1,
                            0.025F, 0.025F, mathZ, mathX, index,
                            false, maxSegments);
                }

                for (int index = maxSegments; index >= 0; --index) {
                    addVertexPair(vertexconsumer, matrix4f, deltaX, deltaY, deltaZ,
                            blockLight0, blockLight1, skyLight0, skyLight1,
                            0.025F, 0.0F, mathZ, mathX, index,
                            true, maxSegments);
                }

                poseStack.popPose();

                if (pBuffer instanceof MultiBufferSource.BufferSource bu) {
                    bu.endBatch();
                }
            }
        }
    }

    //stolen from leash renderer
    private static void addVertexPair(VertexConsumer vertexConsumer, Matrix4f matrix4f,
                                      float startX, float startY, float startZ,
                                      int blockLight0, int blockLight1, int skyLight0, int skyLight1,
                                      float y0, float y1,
                                      float dx, float dz, int index,
                                      boolean flippedColors, int maxSegments) {
        float segment = index / (float) maxSegments;
        int i = (int) Mth.lerp(segment, blockLight0, blockLight1);
        int j = (int) Mth.lerp(segment, skyLight0, skyLight1);
        int light = LightTexture.pack(i, j);
        float darker = index % 2 == (flippedColors ? 1 : 0) ? 0.7F : 1.0F;
        //hardcoded colors 0.0
        float red = 0.5F * darker;
        float green = 0.4F * darker;
        float blue = 0.3F * darker;
        float sx = startX * segment;
        //parable here
        float sy = startY > 0.0F ? startY * segment * segment : startY - startY * (1.0F - segment) * (1.0F - segment);
        float sz = startZ * segment;
        vertexConsumer.addVertex(matrix4f, sx - dx, sy + y1, sz + dz)
                .setColor(red, green, blue, 1.0F).setLight(light);
        vertexConsumer.addVertex(matrix4f, sx + dx, sy + y0 - y1, sz - dz)
                .setColor(red, green, blue, 1.0F).setLight(light);
    }

}
