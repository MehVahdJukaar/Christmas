package net.mehvahdjukaar.snowyspirit.common.wreath;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mehvahdjukaar.moonlight.api.client.util.RenderUtil;
import net.mehvahdjukaar.snowyspirit.reg.ModRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class ClientEvents {

    public static void tickEvent() {
        ClientLevel level = Minecraft.getInstance().level;
        //Minecraft.getInstance().setScreen(new ModConfigSelectScreen(Minecraft.getInstance().screen));
        if (level != null) {
            WreathSavedData.get(level).refreshClientBlocksVisuals(level);
        }
    }

    public static void renderWreaths(PoseStack poseStack) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.player.level();
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        WreathSavedData wreathData = WreathSavedData.get(level);
        if (wreathData != null) {
            float dist = mc.gameRenderer.getRenderDistance();
            dist *= dist;

            poseStack.pushPose();
            poseStack.mulPose(new Quaternionf(camera.rotation()).invert());

            MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
            RenderSystem.enableDepthTest();

            for (var entry : wreathData.getWreathBlocks().entrySet()) {
                BlockPos pos = entry.getKey();

                if (mc.player.distanceToSqr(Vec3.atCenterOf(pos)) < dist) {

                    poseStack.pushPose();
                    poseStack.translate(pos.getX() - cameraPos.x(), pos.getY() - cameraPos.y(), pos.getZ() - cameraPos.z());

                    WreathSavedData.Data data = entry.getValue();
                    Direction dir = data.getDirection();

                    BlockState state = ModRegistry.WREATH.get().defaultBlockState();
                    BlockRenderDispatcher blockRenderer = mc.getBlockRenderer();
                    poseStack.translate(0.5, 0.5, 0.5);

                    var dim = data.getDimensions();

                    if (dim != null) {
                        poseStack.pushPose();
                        poseStack.mulPose(Axis.YP.rotationDegrees(-dir.toYRot()));
                        poseStack.translate(-0.5, -0.5, -0.5 + dim.getSecond());
                        RenderUtil.renderBlock(0, poseStack, bufferSource, state, level, pos, blockRenderer);
                        poseStack.popPose();

                        poseStack.pushPose();
                        poseStack.mulPose(Axis.YP.rotationDegrees(-dir.getOpposite().toYRot()));
                        poseStack.translate(-0.5, -0.5, -0.5 + dim.getFirst());
                        RenderUtil.renderBlock(0, poseStack, bufferSource, state, level, pos, blockRenderer);
                        poseStack.popPose();
                    }

                    //render stuff
                    poseStack.popPose();
                }
            }
            RenderSystem.disableDepthTest();
            bufferSource.endBatch();
            poseStack.popPose();
        }
    }
}

