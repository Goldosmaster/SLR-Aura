package net.slraura.client.aura;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import net.slraura.config.AuraConfig;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = "slr_aura", value = Dist.CLIENT)
public final class HunterAuraFlameRenderer {
    private static final List<FlameWisp> AURA_WISPS = new ArrayList<>();
    private static final Map<UUID, Integer> WISP_COUNTS = new HashMap<>();
    private static final int CURVE_SAMPLES_FULL = 16;
    private static final int CURVE_SAMPLES_LITE = 10;
    private static final float BASE_WIDTH = 0.1F;
    private static final float GLOW_WIDTH_MULT = 3.0F;

    private HunterAuraFlameRenderer() {
    }

    @SubscribeEvent
    public static void onClientTick(Post event) {
        if (!AuraConfig.FLAMES_ENABLED.get()) {
            AURA_WISPS.removeIf(w -> {
                if (Minecraft.getInstance().player == null) {
                    return true;
                }
                return w.owner != null && w.owner.equals(Minecraft.getInstance().player.getUUID());
            });
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null && !mc.isPaused()) {
            tickList(AURA_WISPS);
            pruneStaleWisps(mc);
            for (Player player : mc.level.players()) {
                if (!(player.distanceToSqr(mc.player) > 4096.0) && HunterAuraClient.isAuraVisible(player)
                        && HunterAuraClient.flamesEnabledFor(player)) {
                    maintainAuraWisps(player);
                }
            }
        }
    }

    private static void pruneStaleWisps(Minecraft mc) {
        AURA_WISPS.removeIf(w -> {
            if (w.owner == null) {
                return true;
            }
            Player owner = mc.level.getPlayerByUUID(w.owner);
            if (owner == null || !HunterAuraClient.isAuraVisible(owner)
                    || owner.distanceToSqr(mc.player) > 4096.0) {
                decrementWispCount(w.owner);
                return true;
            }
            return false;
        });
    }

    private static void decrementWispCount(UUID owner) {
        WISP_COUNTS.computeIfPresent(owner, (id, count) -> count <= 1 ? null : count - 1);
    }

    private static void incrementWispCount(UUID owner) {
        WISP_COUNTS.merge(owner, 1, Integer::sum);
    }

    private static void maintainAuraWisps(Player player) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        UUID id = player.getUUID();
        int owned = WISP_COUNTS.getOrDefault(id, 0);
        int target = HunterAuraClient.flameCountFor(player);
        if (owned < target) {
            int spawn = owned < Math.max(1, target * 12 / 20) ? 2 : 1;
            double half = (double) player.getBbWidth() * 0.42;
            double height = (double) player.getBbHeight();
            double px = player.getX();
            double py = player.getY();
            double pz = player.getZ();
            float yaw = player.yBodyRot * (float) (Math.PI / 180.0);
            double lookX = -Math.sin((double) yaw);
            double lookZ = Math.cos((double) yaw);
            double rightX = Math.cos((double) yaw);
            double rightZ = Math.sin((double) yaw);
            for (int s = 0; s < spawn; s++) {
                spawnOneAuraWisp(player, rng, half, height, px, py, pz, lookX, lookZ, rightX, rightZ);
            }
        }
    }

    private static void spawnOneAuraWisp(
            Player player,
            ThreadLocalRandom rng,
            double half,
            double height,
            double px,
            double py,
            double pz,
            double lookX,
            double lookZ,
            double rightX,
            double rightZ
    ) {
        double pick = rng.nextDouble();
        double ox;
        double oz;
        double bodyY;
        float leanX;
        float leanZ;
        if (pick < 0.16) {
            double along = half * (1.05 + rng.nextDouble() * 0.25);
            bodyY = height * (0.4 + rng.nextDouble() * 0.38);
            ox = px - rightX * along;
            oz = pz - rightZ * along;
            leanX = (float) (-rightX);
            leanZ = (float) (-rightZ);
        } else if (pick < 0.32) {
            double along = half * (1.05 + rng.nextDouble() * 0.25);
            bodyY = height * (0.4 + rng.nextDouble() * 0.38);
            ox = px + rightX * along;
            oz = pz + rightZ * along;
            leanX = (float) rightX;
            leanZ = (float) rightZ;
        } else if (pick < 0.48) {
            double along = half * (0.75 + rng.nextDouble() * 0.3);
            double across = (rng.nextDouble() - 0.5) * 2.0 * half * 1.15;
            bodyY = height * (0.28 + rng.nextDouble() * 0.52);
            ox = px + lookX * along + rightX * across;
            oz = pz + lookZ * along + rightZ * across;
            leanX = (float) lookX;
            leanZ = (float) lookZ;
        } else if (pick < 0.64) {
            double along = half * (0.75 + rng.nextDouble() * 0.3);
            double across = (rng.nextDouble() - 0.5) * 2.0 * half * 1.15;
            bodyY = height * (0.28 + rng.nextDouble() * 0.52);
            ox = px - lookX * along + rightX * across;
            oz = pz - lookZ * along + rightZ * across;
            leanX = (float) (-lookX);
            leanZ = (float) (-lookZ);
        } else if (pick < 0.76) {
            double along = half * (0.55 + rng.nextDouble() * 0.3);
            double across = (rng.nextDouble() - 0.5) * 2.0 * half * 0.95;
            bodyY = height * (0.04 + rng.nextDouble() * 0.28);
            ox = px + lookX * along + rightX * across;
            oz = pz + lookZ * along + rightZ * across;
            leanX = (float) lookX;
            leanZ = (float) lookZ;
        } else if (pick < 0.88) {
            double along = half * (0.55 + rng.nextDouble() * 0.3);
            double across = (rng.nextDouble() - 0.5) * 2.0 * half * 0.95;
            bodyY = height * (0.04 + rng.nextDouble() * 0.28);
            ox = px - lookX * along + rightX * across;
            oz = pz - lookZ * along + rightZ * across;
            leanX = (float) (-lookX);
            leanZ = (float) (-lookZ);
        } else {
            boolean leftLeg = rng.nextBoolean();
            double side = leftLeg ? -1.0 : 1.0;
            double along = half * (0.8 + rng.nextDouble() * 0.35);
            bodyY = height * (0.04 + rng.nextDouble() * 0.28);
            ox = px + rightX * along * side;
            oz = pz + rightZ * along * side;
            leanX = (float) (rightX * side);
            leanZ = (float) (rightZ * side);
        }

        Vec3 origin = new Vec3(ox, py + bodyY, oz);
        float angleRad = (float) Math.toRadians(HunterAuraClient.flameAngleFor(player));
        float outward = (float) Math.sin((double) angleRad);
        float up = (float) Math.cos((double) angleRad);
        float dirX = leanX * outward;
        float dirZ = leanZ * outward;
        float dirY = up;
        float wispLen = (0.58F + rng.nextFloat() * 0.4F) * HunterAuraClient.flameLengthFor(player);
        float swirlFreq = 1.15F + rng.nextFloat() * 1.5F;
        float swirlRad = 0.07F + rng.nextFloat() * 0.11F;
        float phase = rng.nextFloat() * 6.28F;
        float rotSpeed = 0.05F + rng.nextFloat() * 0.08F;
        int lifetime = 18 + rng.nextInt(14);
        float widthMul = 0.7F + rng.nextFloat() * 0.45F;
        float[] col = new float[3];
        AuraPalette palette = HunterAuraClient.palette(player);
        palette.pickWisp(rng, col);
        AURA_WISPS.add(new FlameWisp(
                origin, wispLen, swirlFreq, swirlRad, phase, rotSpeed, lifetime, widthMul,
                col[0], col[1], col[2], palette, dirX, dirY, dirZ, true, player, lookX, lookZ, rightX, rightZ
        ));
        incrementWispCount(player.getUUID());
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != Stage.AFTER_TRANSLUCENT_BLOCKS || AURA_WISPS.isEmpty()) {
            return;
        }
        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        PoseStack ps = event.getPoseStack();
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        BufferSource bufSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer buf = bufSource.getBuffer(RenderType.lightning());
        ps.pushPose();
        Matrix4f mat = ps.last().pose();
        renderList(AURA_WISPS, mat, buf, camPos, partial);
        ps.popPose();
        bufSource.endBatch(RenderType.lightning());
    }

    public static void clearWisps() {
        AURA_WISPS.clear();
        WISP_COUNTS.clear();
    }

    private static void tickList(List<FlameWisp> list) {
        Iterator<FlameWisp> it = list.iterator();
        while (it.hasNext()) {
            FlameWisp w = it.next();
            w.age++;
            if (w.age >= w.lifetime) {
                decrementWispCount(w.owner);
                it.remove();
            }
        }
    }

    private static void renderList(List<FlameWisp> list, Matrix4f mat, VertexConsumer buf, Vec3 camPos, float partial) {
        Minecraft mc = Minecraft.getInstance();
        UUID localId = mc.player != null ? mc.player.getUUID() : null;
        boolean firstPerson = mc.options.getCameraType().isFirstPerson();
        Map<UUID, Player> owners = new HashMap<>();
        for (FlameWisp w : list) {
            if (firstPerson && localId != null && localId.equals(w.owner)) {
                continue;
            }
            float smoothAge = (float) w.age + partial;
            float life01 = smoothAge / (float) w.lifetime;
            float fade;
            if (life01 < 0.15F) {
                fade = life01 / 0.15F;
            } else if (life01 > 0.7F) {
                fade = (1.0F - life01) / 0.3F;
            } else {
                fade = 1.0F;
            }
            if (fade >= 0.01F) {
                Player owner = null;
                if (w.owner != null) {
                    owner = owners.computeIfAbsent(w.owner, id -> mc.level != null ? mc.level.getPlayerByUUID(id) : null);
                }
                renderWisp(w, mat, buf, camPos, smoothAge, fade, partial, owner);
            }
        }
    }

    private static void renderWisp(
            FlameWisp w,
            Matrix4f mat,
            VertexConsumer buf,
            Vec3 cam,
            float smoothAge,
            float masterAlpha,
            float partial,
            Player owner
    ) {
        Vec3 origin = w.origin;
        float dirX = w.dirX;
        float dirY = w.dirY;
        float dirZ = w.dirZ;
        if (w.attached && owner != null) {
            float yaw = Mth.rotLerp(partial, owner.yBodyRotO, owner.yBodyRot) * (float) (Math.PI / 180.0);
            double lookX = -Math.sin((double) yaw);
            double lookZ = Math.cos((double) yaw);
            double rightX = Math.cos((double) yaw);
            double rightZ = Math.sin((double) yaw);
            double px = Mth.lerp((double) partial, owner.xo, owner.getX());
            double py = Mth.lerp((double) partial, owner.yo, owner.getY());
            double pz = Mth.lerp((double) partial, owner.zo, owner.getZ());
            origin = new Vec3(
                    px + rightX * (double) w.localRight + lookX * (double) w.localFwd,
                    py + (double) w.localY,
                    pz + rightZ * (double) w.localRight + lookZ * (double) w.localFwd
            );
            dirX = (float) (rightX * (double) w.localDirRight + lookX * (double) w.localDirFwd);
            dirZ = (float) (rightZ * (double) w.localDirRight + lookZ * (double) w.localDirFwd);
            dirY = w.localDirY;
        }

        boolean fullQuality = owner != null && HunterAuraClient.useFullFlameQuality(owner);
        int samples = fullQuality ? CURVE_SAMPLES_FULL : CURVE_SAMPLES_LITE;
        Vec3[] pts = new Vec3[samples];
        Vec3 mainDir = new Vec3((double) dirX, (double) dirY, (double) dirZ).normalize();
        Vec3 up = Math.abs(mainDir.y) < 0.9 ? new Vec3(0.0, 1.0, 0.0) : new Vec3(1.0, 0.0, 0.0);
        Vec3 side = mainDir.cross(up).normalize();
        Vec3 perp = mainDir.cross(side).normalize();
        int last = samples - 1;
        for (int i = 0; i < samples; i++) {
            float t = (float) i / (float) last;
            float dist = t * w.length;
            float spread = t * t;
            float theta = t * w.swirlFreq * (float) (Math.PI * 2) + w.phase + smoothAge * w.rotSpeed;
            float swirlS = (float) (Math.cos((double) theta) * (double) w.swirlRadius * (double) spread);
            float swirlP = (float) (Math.sin((double) theta) * (double) w.swirlRadius * (double) spread);
            float wave = (float) (Math.sin((double) t * 3.5 + (double) w.phase * 2.0 + (double) smoothAge * 0.12) * 0.05 * (double) t);
            pts[i] = new Vec3(
                    origin.x + mainDir.x * (double) dist + side.x * (double) swirlS + perp.x * (double) swirlP + side.x * (double) wave,
                    origin.y + mainDir.y * (double) dist + side.y * (double) swirlS + perp.y * (double) swirlP + side.y * (double) wave,
                    origin.z + mainDir.z * (double) dist + side.z * (double) swirlS + perp.z * (double) swirlP + side.z * (double) wave
            );
        }
        if (fullQuality) {
            float glowR;
            float glowG;
            float glowB;
            float glowA;
            switch (w.palette) {
                case WHITE -> {
                    glowR = w.colR * 0.82F;
                    glowG = w.colG * 0.82F;
                    glowB = w.colB * 0.82F;
                    glowA = 0.5F;
                }
                case BLOOD -> {
                    glowR = w.colR * 0.22F;
                    glowG = w.colG * 0.02F;
                    glowB = w.colB * 0.02F;
                    glowA = 0.48F;
                }
                case CRIMSON, RED -> {
                    glowR = w.colR * 0.55F;
                    glowG = w.colG * 0.08F;
                    glowB = w.colB * 0.12F;
                    glowA = 0.4F;
                }
                default -> {
                    glowR = w.colR * 0.35F;
                    glowG = w.colG * 0.45F;
                    glowB = w.colB * 0.55F;
                    glowA = 0.35F;
                }
            }
            drawRibbon(pts, mat, buf, cam, masterAlpha, BASE_WIDTH * w.widthMul * GLOW_WIDTH_MULT, glowR, glowG, glowB, glowA);
        }
        drawRibbon(pts, mat, buf, cam, masterAlpha, BASE_WIDTH * w.widthMul, w.colR, w.colG, w.colB, fullQuality ? 0.85F : 0.95F);
    }

    private static void drawRibbon(
            Vec3[] pts,
            Matrix4f mat,
            VertexConsumer buf,
            Vec3 cam,
            float masterAlpha,
            float baseW,
            float r,
            float g,
            float b,
            float alphaScale
    ) {
        for (int i = 0; i < pts.length - 1; i++) {
            Vec3 a = pts[i];
            Vec3 bPt = pts[i + 1];
            float t = (float) i / (float) (pts.length - 1);
            float baseFactor = Math.min(1.0F, t * 6.0F);
            float tipFactor = 1.0F - t * t;
            float halfW = baseW * baseFactor * tipFactor * 0.5F;
            if (halfW < 0.002F) {
                continue;
            }
            float segAlpha = masterAlpha * alphaScale * tipFactor;
            if (segAlpha < 0.01F) {
                continue;
            }
            Vec3 seg = bPt.subtract(a);
            Vec3 mid = a.add(bPt).scale(0.5);
            Vec3 toCam = cam.subtract(mid);
            Vec3 perp = seg.cross(toCam);
            double pLen = perp.length();
            if (pLen < 1.0E-6) {
                continue;
            }
            perp = perp.scale((double) halfW / pLen);
            float ax = (float) (a.x - cam.x);
            float ay = (float) (a.y - cam.y);
            float az = (float) (a.z - cam.z);
            float bx = (float) (bPt.x - cam.x);
            float by = (float) (bPt.y - cam.y);
            float bz = (float) (bPt.z - cam.z);
            float px = (float) perp.x;
            float py = (float) perp.y;
            float pz = (float) perp.z;
            buf.addVertex(mat, ax + px, ay + py, az + pz).setColor(r, g, b, segAlpha);
            buf.addVertex(mat, bx + px, by + py, bz + pz).setColor(r, g, b, segAlpha);
            buf.addVertex(mat, bx - px, by - py, bz - pz).setColor(r, g, b, segAlpha);
            buf.addVertex(mat, ax - px, ay - py, az - pz).setColor(r, g, b, segAlpha);
        }
    }

    private static final class FlameWisp {
        final Vec3 origin;
        final float length;
        final float swirlFreq;
        final float swirlRadius;
        final float phase;
        final float rotSpeed;
        final int lifetime;
        final float widthMul;
        final float colR;
        final float colG;
        final float colB;
        final AuraPalette palette;
        final float dirX;
        final float dirY;
        final float dirZ;
        final boolean attached;
        final UUID owner;
        final float localRight;
        final float localY;
        final float localFwd;
        final float localDirRight;
        final float localDirY;
        final float localDirFwd;
        int age;

        FlameWisp(
                Vec3 origin,
                float length,
                float swirlFreq,
                float swirlRadius,
                float phase,
                float rotSpeed,
                int lifetime,
                float widthMul,
                float colR,
                float colG,
                float colB,
                AuraPalette palette,
                float dirX,
                float dirY,
                float dirZ,
                boolean attached,
                Player player,
                double lookX,
                double lookZ,
                double rightX,
                double rightZ
        ) {
            this.origin = origin;
            this.length = length;
            this.swirlFreq = swirlFreq;
            this.swirlRadius = swirlRadius;
            this.phase = phase;
            this.rotSpeed = rotSpeed;
            this.lifetime = lifetime;
            this.widthMul = widthMul;
            this.colR = colR;
            this.colG = colG;
            this.colB = colB;
            this.palette = palette;
            this.dirX = dirX;
            this.dirY = dirY;
            this.dirZ = dirZ;
            this.attached = attached;
            this.owner = attached && player != null ? player.getUUID() : null;
            if (attached && player != null) {
                double rx = origin.x - player.getX();
                double ry = origin.y - player.getY();
                double rz = origin.z - player.getZ();
                this.localRight = (float) (rx * rightX + rz * rightZ);
                this.localFwd = (float) (rx * lookX + rz * lookZ);
                this.localY = (float) ry;
                this.localDirRight = (float) ((double) dirX * rightX + (double) dirZ * rightZ);
                this.localDirFwd = (float) ((double) dirX * lookX + (double) dirZ * lookZ);
                this.localDirY = dirY;
            } else {
                this.localRight = this.localFwd = this.localY = 0.0F;
                this.localDirRight = this.localDirFwd = this.localDirY = 0.0F;
            }
        }
    }
}
