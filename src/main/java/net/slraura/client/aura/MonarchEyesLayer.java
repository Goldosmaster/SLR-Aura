package net.slraura.client.aura;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.slraura.config.AuraConfig;
import org.joml.Matrix4f;

public final class MonarchEyesLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation STAR = ResourceLocation.fromNamespaceAndPath("slr_aura", "textures/entity/monarch_star.png");

    public MonarchEyesLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(
            PoseStack ps,
            MultiBufferSource buf,
            int packedLight,
            AbstractClientPlayer player,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        if (!HunterAuraClient.isAuraVisible(player) || player.isInvisible()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (player == mc.player && mc.options.getCameraType().isFirstPerson()) {
            return;
        }
        PlayerModel<AbstractClientPlayer> model = this.getParentModel();
        if (!model.head.visible) {
            return;
        }
        ps.pushPose();
        model.head.translateAndRotate(ps);
        float p = 0.0625F;
        float eyeX = 2.0F * p;
        float eyeYOffset = 0.0F;
        float leftOffset = 0.0F;
        float rightOffset = 0.0F;
        if (HunterAuraClient.isLocalPlayer(player)) {
            eyeYOffset = AuraConfig.EYE_OFFSET_UP.get().floatValue() * p;
            leftOffset = AuraConfig.LEFT_EYE_OFFSET_X.get().floatValue() * p;
            rightOffset = AuraConfig.RIGHT_EYE_OFFSET_X.get().floatValue() * p;
        }
        float eyeY = -1.85F * p + eyeYOffset;
        float eyeZ = -4.08F * p;
        float leftX = eyeX - leftOffset;
        float rightX = -eyeX - rightOffset;
        float[] tint = HunterAuraClient.palette(player).eyeTint();
        VertexConsumer stars = buf.getBuffer(RenderType.entityTranslucentEmissive(STAR));
        drawStarQuad(ps, stars, leftX, eyeY, eyeZ, 0.072F, tint);
        drawStarQuad(ps, stars, rightX, eyeY, eyeZ, 0.072F, tint);
        ps.popPose();
    }

    private static void drawStarQuad(PoseStack ps, VertexConsumer buf, float cx, float cy, float cz, float half, float[] tint) {
        Pose pose = ps.last();
        Matrix4f mat = pose.pose();
        float x0 = cx - half;
        float x1 = cx + half;
        float y0 = cy - half;
        float y1 = cy + half;
        vert(buf, pose, mat, x0, y0, cz, 0.0F, 1.0F, tint);
        vert(buf, pose, mat, x1, y0, cz, 1.0F, 1.0F, tint);
        vert(buf, pose, mat, x1, y1, cz, 1.0F, 0.0F, tint);
        vert(buf, pose, mat, x0, y1, cz, 0.0F, 0.0F, tint);
    }

    private static void vert(VertexConsumer buf, Pose pose, Matrix4f mat, float x, float y, float z, float u, float v, float[] tint) {
        buf.addVertex(mat, x, y, z)
                .setColor(tint[0], tint[1], tint[2], 1.0F)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(pose, 0.0F, 0.0F, -1.0F);
    }
}
