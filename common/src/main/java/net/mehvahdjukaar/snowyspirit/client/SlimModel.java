package net.mehvahdjukaar.snowyspirit.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.snowyspirit.common.entity.SledEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

@Deprecated
public class SlimModel<T extends SledEntity> extends EntityModel<T> {

    private final ModelPart sled;

    public SlimModel(ModelPart root) {
        this.sled = root.getChild("sled");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition front = partdefinition.addOrReplaceChild("sled", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, 4.0F, 3.5F, 0.0F, 3.1416F, 0.0F));

        PartDefinition skiis = front.addOrReplaceChild("skiis", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 4.5F, 1.5708F, 1.5708F, 0.0F));

        PartDefinition slednew = skiis.addOrReplaceChild("slednew", CubeListBuilder.create().texOffs(0, 0).addBox(-16.5F, -10.0F, 8.0F, 33.0F, 2.0F, 2.0F)
                .texOffs(0, 8).addBox(-9.5F, -8.0F, 15.0F, 24.0F, 12.0F, 1.0F)
                .texOffs(0, 21).addBox(-10.5F, -10.0F, 14.0F, 26.0F, 2.0F, 2.0F)
                .texOffs(0, 25).addBox(-10.5F, 4.0F, 14.0F, 26.0F, 2.0F, 2.0F)
                .texOffs(0, 4).addBox(-16.5F, 4.0F, 8.0F, 33.0F, 2.0F, 2.0F), PartPose.offset(0.0F, 2.0F, -10.0F));

        PartDefinition supports = front.addOrReplaceChild("supports", CubeListBuilder.create().texOffs(28, 29).addBox(-6.0F, -5.0F, -2.5F, 12.0F, 1.0F, 2.0F)
                .texOffs(0, 29).addBox(-6.0F, -5.0F, 15.5F, 12.0F, 1.0F, 2.0F), PartPose.offset(0.0F, 2.0F, -5.5F));

        PartDefinition left = supports.addOrReplaceChild("left", CubeListBuilder.create().texOffs(104, 0).addBox(-8.0F, -5.0F, 24.5F, 2.0F, 3.0F, 2.0F)
                .texOffs(112, 0).addBox(-8.0F, -6.0F, 15.5F, 2.0F, 4.0F, 2.0F)
                .texOffs(120, 0).addBox(-8.0F, -6.0F, -2.5F, 2.0F, 4.0F, 2.0F), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition right = supports.addOrReplaceChild("right", CubeListBuilder.create().texOffs(104, 6).addBox(6.0F, -5.0F, 24.5F, 2.0F, 3.0F, 2.0F)
                .texOffs(112, 6).addBox(6.0F, -6.0F, 15.5F, 2.0F, 4.0F, 2.0F)
                .texOffs(120, 6).addBox(6.0F, -6.0F, -2.5F, 2.0F, 4.0F, 2.0F), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        sled.render(poseStack, buffer, packedLight, packedOverlay);
    }
}