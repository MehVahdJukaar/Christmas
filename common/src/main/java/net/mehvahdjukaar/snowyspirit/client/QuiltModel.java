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

public class QuiltModel<T extends SledEntity> extends EntityModel<T> {

    private final ModelPart quilt;

    public QuiltModel(ModelPart root) {
        this.quilt = root.getChild("quilt");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition quilt = partdefinition.addOrReplaceChild("quilt", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-7.0F, -11.5F, -3.0F, 14.0F, 20.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        quilt.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}