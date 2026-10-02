package net.slraura.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.slraura.client.aura.AuraPalette;
import net.slraura.client.aura.HunterAuraClient;

public final class BlueSmokeParticle extends TextureSheetParticle {
    private double originX;
    private double originZ;
    private final float riseSpeed;
    private final float baseSize;
    private final float driftPhase;
    private final float driftAmp;
    private final float spin;
    private final boolean follow;
    private final UUID followId;
    private final float locRight;
    private final float locFwd;
    private float locY;
    private final float peakAlpha;
    private Player cachedOwner;

    private BlueSmokeParticle(ClientLevel level, double x, double y, double z, double packedColor, double riseD, double sizeMulD, SpriteSet sprites) {
        super(level, x, y, z, 0.0, 0.0, 0.0);
        this.originX = x;
        this.originZ = z;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.friction = 1.0F;
        this.riseSpeed = (float) Math.max(0.0, riseD);
        float sizeMul = (float) (sizeMulD > 0.0 ? sizeMulD : 1.0);
        AuraPalette palette = paletteFromFlag(packedColor);
        float[] smoke = new float[3];
        palette.pickSmoke(ThreadLocalRandom.current(), smoke);
        this.rCol = smoke[0];
        this.gCol = smoke[1];
        this.bCol = smoke[2];
        float peak = palette == AuraPalette.WHITE ? 0.78F : 0.56F;
        this.pickSprite(sprites);
        Player owner = HunterAuraClient.resolveSmokeOwner(level, packedColor, x, y, z);
        this.cachedOwner = owner;
        if (owner != null) {
            this.follow = true;
            this.followId = owner.getUUID();
            float yaw = owner.yBodyRot * (float) (Math.PI / 180.0);
            double lookX = -Math.sin((double) yaw);
            double lookZ = Math.cos((double) yaw);
            double rightX = Math.cos((double) yaw);
            double rightZ = Math.sin((double) yaw);
            double rx = x - owner.getX();
            double rz = z - owner.getZ();
            this.locRight = (float) (rx * rightX + rz * rightZ);
            this.locFwd = (float) (rx * lookX + rz * lookZ);
            this.locY = (float) (y - owner.getY());
        } else {
            this.follow = false;
            this.followId = null;
            this.locRight = this.locFwd = this.locY = 0.0F;
        }
        float transparency = owner != null ? HunterAuraClient.smokeTransparencyFor(owner) : 1.0F;
        this.peakAlpha = Mth.clamp(peak * transparency, 0.0F, 1.0F);
        this.alpha = this.peakAlpha;
        this.baseSize = (0.55F + this.random.nextFloat() * 0.35F) * sizeMul;
        this.quadSize = this.baseSize * 0.6F;
        this.lifetime = 40 + this.random.nextInt(24);
        this.driftPhase = this.random.nextFloat() * 6.28F;
        this.driftAmp = 0.04F + this.random.nextFloat() * 0.08F;
        this.spin = (this.random.nextFloat() - 0.5F) * 0.06F;
        this.oRoll = this.random.nextFloat() * 6.28F;
        this.roll = this.oRoll;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.oRoll = this.roll;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        float t = (float) this.age / (float) this.lifetime;
        double swayX = Math.cos((double) this.driftPhase + (double) this.age * 0.06) * (double) this.driftAmp;
        double swayZ = Math.sin((double) this.driftPhase + (double) this.age * 0.06) * (double) this.driftAmp;
        if (this.follow) {
            Player owner = this.cachedOwner;
            if (owner == null || !owner.isAlive()) {
                if (Minecraft.getInstance().level != null && this.followId != null) {
                    owner = Minecraft.getInstance().level.getPlayerByUUID(this.followId);
                    this.cachedOwner = owner;
                }
            }
            if (owner != null) {
                this.locY = this.locY + this.riseSpeed;
                float yaw = owner.yBodyRot * (float) (Math.PI / 180.0);
                double lookX = -Math.sin((double) yaw);
                double lookZ = Math.cos((double) yaw);
                double rightX = Math.cos((double) yaw);
                double rightZ = Math.sin((double) yaw);
                this.x = owner.getX() + rightX * (double) this.locRight + lookX * (double) this.locFwd + swayX;
                this.z = owner.getZ() + rightZ * (double) this.locRight + lookZ * (double) this.locFwd + swayZ;
                this.y = owner.getY() + (double) this.locY;
            }
        } else {
            this.x = this.originX + swayX;
            this.z = this.originZ + swayZ;
            this.y = this.y + (double) this.riseSpeed;
        }

        this.roll = this.roll + this.spin;
        float fade = t < 0.15F ? t / 0.15F : 1.0F - (t - 0.15F) / 0.85F;
        this.alpha = Mth.clamp(fade * this.peakAlpha, 0.0F, 1.0F);
        this.quadSize = this.baseSize * (0.55F + 0.55F * t);
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && this.followId != null && this.followId.equals(mc.player.getUUID())
                && mc.options.getCameraType().isFirstPerson()) {
            return;
        }
        super.render(buffer, camera, partialTicks);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 15728880;
    }

    private static AuraPalette paletteFromFlag(double flag) {
        int packed = (int) Math.round(flag);
        int id = Math.floorMod(packed, 8);
        AuraPalette[] all = AuraPalette.values();
        return id >= 0 && id < all.length ? all[id] : AuraPalette.BLUE;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return new BlueSmokeParticle(level, x, y, z, xd, yd, zd, this.sprites);
        }
    }
}
