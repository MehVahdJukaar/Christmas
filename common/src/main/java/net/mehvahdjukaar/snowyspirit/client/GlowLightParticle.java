package net.mehvahdjukaar.snowyspirit.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.moonlight.core.client.MLRenderTypes;
import net.mehvahdjukaar.snowyspirit.configs.ClientConfigs;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class GlowLightParticle extends TextureSheetParticle {

    private final float scale;
    private float oldQuadSize;
    private final float deltaRot;
    protected final SpriteSet sprites;

    private GlowLightParticle(ClientLevel arg, double d, double e, double f, SpriteSet sprites) {
        super(arg, d, e, f);
        this.sprites = sprites;
        this.gravity = 0.0F;
        this.lifetime = 19 + this.random.nextInt(12);
        this.hasPhysics = false;
        this.alpha = 0;
        this.quadSize = 0;
        this.bbHeight = 0.2f;
        this.bbWidth = 0.2f;
        this.deltaRot = MthUtils.nextWeighted(this.random, 0.03f, 500);
        this.scale = 0.05f + MthUtils.nextWeighted(this.random, 0.15f, 1);
        this.roll = (float) (Math.PI * this.random.nextFloat());
    }


    @Override
    protected void renderRotatedQuad(VertexConsumer buffer, Quaternionf quaternionf,
                                     float x, float y, float z, float partialTicks) {
        this.setSprite(sprites.get(0, 3));

        int lightColor = this.getLightColor(partialTicks);
        float size = this.getQuadSize(partialTicks);

        int mode = ClientConfigs.PARTICLE_MODE.get() - 1;

        PoseStack poseStack = new PoseStack();
        poseStack.translate(x, y, z);
        poseStack.mulPose(quaternionf);
        Matrix4f matrix4f = poseStack.last().pose();

        if (mode == 0) {
            renderQuad(sprite, buffer, lightColor, matrix4f, size,
                    rCol, gCol, bCol, alpha * .4f);

            this.setSprite(sprites.get(2, 3));
            renderQuad(sprite, buffer, lightColor, matrix4f, size,
                    0.5f + rCol / 2f, 0.5f + gCol / 2f, 0.5f + bCol / 2f, alpha * 0.6f);
        } else if (mode == 1) {
            renderQuad(sprite, buffer, lightColor, matrix4f, size * 1.5f,
                    rCol, gCol, bCol, alpha * .3f);

            renderQuad(sprite, buffer, lightColor, matrix4f, size,
                    0.5f + rCol / 2f, 0.5f + gCol / 2f, 0.5f + bCol / 2f, alpha * .4f);

            this.setSprite(sprites.get(2, 3));
            renderQuad(sprite, buffer, lightColor, matrix4f, size,
                    1, 1, 1, alpha * 0.3f);
        } else if (mode == 2) {
            renderQuad(sprite, buffer, lightColor, matrix4f, size,
                    rCol, gCol, bCol, alpha * .8f);

            this.setSprite(sprites.get(2, 3));
            renderQuad(sprite, buffer, lightColor, matrix4f, size,
                    1, 1, 1, alpha * 0.3f);
        }


        this.setSprite(sprites.get(3, 3));
        renderQuad(sprite, buffer, lightColor, matrix4f, size,
                1, 1, 1, alpha * 0.5f);

    }

    private static void renderQuad(TextureAtlasSprite sprite, VertexConsumer buffer,
                                   int lightColor, Matrix4f pose, float size,
                                   float rCol, float gCol, float bCol, float alpha) {
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        u1 = u0 + (u1 - u0) * 7 / 8f;
        v1 = v0 + (v1 - v0) * 7 / 8f;

        buffer.addVertex(pose,  (1 * size),  (-1 * size), 0)
                .setUv(u1, v1)
                .setColor(rCol, gCol, bCol, alpha)
                .setLight(lightColor);
        buffer.addVertex(pose, (1 * size),  (1 * size), 0)
                .setUv(u1, v0)
                .setColor(rCol, gCol, bCol, alpha)
                .setLight(lightColor);
        buffer.addVertex(pose, (-1 * size),  (1 * size), 0)
                .setUv(u0, v0)
                .setColor(rCol, gCol, bCol, alpha)
                .setLight(lightColor);
        buffer.addVertex(pose, (-1 * size),  (-1 * size), 0)
                .setUv(u0, v1)
                .setColor(rCol, gCol, bCol, alpha)
                .setLight(lightColor);
    }

    @Override
    public ParticleRenderType getRenderType() {
         return MLRenderTypes.PARTICLE_ADDITIVE_TRANSLUCENCY_RENDER_TYPE;
    }

    @Override
    public float getQuadSize(float partialTicks) {
        return Mth.lerp(partialTicks, oldQuadSize, quadSize);
    }

    @Override
    public void tick() {
        super.tick();
        float sin = Mth.sin((float) (Math.PI * this.age / (this.lifetime)));
        this.alpha = (float) (Math.pow(sin, 0.2));
        this.oldQuadSize = this.quadSize;
        this.quadSize = (float) (this.scale * Math.pow(sin, 0.4));
        this.oRoll = this.roll;
        this.roll += this.deltaRot;
    }

    @Override
    protected int getLightColor(float partialTick) {

        int i = super.getLightColor(partialTick);
        int k = i >> 16 & 0xFF;
        int a = (int) (255 * Mth.sin((float) (Math.PI * this.age / this.lifetime)));

        return a | k << 16;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z, double reg, double green, double blue) {
            var p = new GlowLightParticle(level, x, y, z, sprites);
            p.setColor((float) reg, (float) green, (float) blue);
            return p;
        }
    }
}
