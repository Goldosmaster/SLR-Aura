package net.slraura.client.aura;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.network.PacketDistributor;
import net.slraura.config.AuraConfig;
import net.slraura.network.AuraToggleC2SPayload;
import net.slraura.particle.ModParticles;

@EventBusSubscriber(modid = "slr_aura", value = Dist.CLIENT)
public final class HunterAuraClient {
    private static final String SLR_AURA_KEY = "key.sololeveling.ability_4";
    private static final Map<UUID, AuraState> STATES = new ConcurrentHashMap<>();
    private static boolean predictedOn;
    private static boolean bWasDown;

    private static int smokeInterval(Player player, LocalPlayer viewer) {
        if (isLocalPlayer(player)) {
            return 2;
        }
        double distSq = player.distanceToSqr(viewer);
        if (distSq < 64.0) {
            return 4;
        }
        if (distSq < 196.0) {
            return 3;
        }
        return 2;
    }

    private static boolean shouldEmitSmoke(Player player, ClientLevel level, LocalPlayer viewer) {
        int interval = smokeInterval(player, viewer);
        if (interval <= 1) {
            return true;
        }
        return Math.floorMod(level.getGameTime() + (long) player.getId(), interval) == 0L;
    }

    public record AuraState(boolean on, AuraPalette palette) {
    }

    private HunterAuraClient() {
    }

    public static void applySync(UUID player, boolean on, byte palette) {
        Minecraft mc = Minecraft.getInstance();
        if (on) {
            STATES.put(player, new AuraState(true, AuraPalette.fromOrdinal(palette)));
        } else {
            STATES.remove(player);
        }
        if (mc.player != null && player.equals(mc.player.getUUID())) {
            predictedOn = on;
        }
    }

    public static void clearAll() {
        STATES.clear();
        predictedOn = false;
        bWasDown = false;
    }

    public static boolean isAuraVisible(Player player) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && player == mc.player) {
            AuraState state = STATES.get(player.getUUID());
            return state != null ? state.on() : predictedOn;
        }
        AuraState state = STATES.get(player.getUUID());
        return state != null && state.on();
    }

    public static boolean hideFromLocalCamera(Player player) {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && player == mc.player && mc.options.getCameraType().isFirstPerson();
    }

    public static boolean isLocalPlayer(Player player) {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && player == mc.player;
    }

    public static boolean flamesEnabledFor(Player player) {
        return !isLocalPlayer(player) || AuraConfig.FLAMES_ENABLED.get();
    }

    public static int flameCountFor(Player player) {
        int base = isLocalPlayer(player) ? AuraConfig.FLAME_COUNT.get() : 12;
        if (isLocalPlayer(player)) {
            return base;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return base;
        }
        double distSq = player.distanceToSqr(mc.player);
        if (distSq < 64.0) {
            return Math.min(base, 8);
        }
        if (distSq < 196.0) {
            return Math.min(base, 10);
        }
        return base;
    }

    public static boolean useFullFlameQuality(Player player) {
        return isLocalPlayer(player);
    }

    public static float flameLengthFor(Player player) {
        return isLocalPlayer(player) ? AuraConfig.FLAME_LENGTH.get().floatValue() : 1.0F;
    }

    public static int flameAngleFor(Player player) {
        return isLocalPlayer(player) ? AuraConfig.FLAME_ANGLE.get() : 55;
    }

    public static double smokeSpeedFor(Player player) {
        return isLocalPlayer(player) ? AuraConfig.SMOKE_SPEED.get() : 1.0;
    }

    public static double smokeSizeFor(Player player) {
        return isLocalPlayer(player) ? AuraConfig.SMOKE_SIZE.get() : 1.0;
    }

    public static float smokeTransparencyFor(Player player) {
        return isLocalPlayer(player) ? AuraConfig.SMOKE_TRANSPARENCY.get().floatValue() : 1.0F;
    }

    public static AuraPalette palette(Player player) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && player == mc.player) {
            return resolveOwnPalette(player);
        }
        AuraState state = STATES.get(player.getUUID());
        if (state != null) {
            return state.palette();
        }
        return SlrVesselLookup.palette(player);
    }

    public static void resyncOwnColor() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer self = mc.player;
        if (self == null || !isAuraVisible(self)) {
            return;
        }
        PacketDistributor.sendToServer(new AuraToggleC2SPayload(true, (byte) resolveOwnPalette(self).ordinal()));
    }

    public static AuraPalette resolveOwnPalette(Player player) {
        var mode = AuraConfig.FORCE_COLOR.get();
        if (mode == AuraConfig.ColorMode.AUTO) {
            return SlrVesselLookup.palette(player);
        }
        return mode.toPalette();
    }

    public static AuraPalette smokePalette(Player player) {
        if (isLocalPlayer(player)) {
            return AuraConfig.SMOKE_COLOR.get().toPalette(palette(player));
        }
        return palette(player);
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clearAll();
    }

    @SubscribeEvent
    public static void onClientTick(Post event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        LocalPlayer self = mc.player;
        if (level != null && self != null && !mc.isPaused()) {
            boolean bDown = isKeyDown(mc, SLR_AURA_KEY) && mc.screen == null;
            boolean bRising = bDown && !bWasDown;
            bWasDown = bDown;
            if (bRising) {
                predictedOn = !isAuraVisible(self);
                PacketDistributor.sendToServer(new AuraToggleC2SPayload(predictedOn, (byte) resolveOwnPalette(self).ordinal()));
            }

            for (Player player : level.players()) {
                if (!(player.distanceToSqr(self) > 4096.0) && isAuraVisible(player) && !hideFromLocalCamera(player)
                        && ModParticles.BLUE_SMOKE.isBound() && shouldEmitSmoke(player, level, self)) {
                    emitAuraSmoke(level, player, self);
                }
            }
        }
    }

    private static boolean isKeyDown(Minecraft mc, String name) {
        for (KeyMapping mapping : mc.options.keyMappings) {
            if (name.equals(mapping.getName())) {
                return mapping.isDown();
            }
        }
        return false;
    }

    private static void emitAuraSmoke(ClientLevel level, Player player, LocalPlayer viewer) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double half = (double) player.getBbWidth() * 0.42;
        double height = (double) player.getBbHeight();
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        int paletteId = smokePalette(player).ordinal();
        double packedColor = (double) paletteId + (double) player.getId() * 8.0;
        float yaw = player.yBodyRot * (float) (Math.PI / 180.0);
        double lookX = -Math.sin((double) yaw);
        double lookZ = Math.cos((double) yaw);
        double rightX = Math.cos((double) yaw);
        double rightZ = Math.sin((double) yaw);
        boolean local = isLocalPlayer(player);
        boolean veryClose = !local && player.distanceToSqr(viewer) < 64.0;
        int count = local ? 3 + random.nextInt(3) : veryClose ? 1 + random.nextInt(2) : 2 + random.nextInt(2);
        double speed = smokeSpeedFor(player);
        double size = smokeSizeFor(player);

        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double radius = half * (0.9 + random.nextDouble() * 0.7);
            double body = 0.05 + random.nextDouble() * height;
            level.addAlwaysVisibleParticle(
                    (ParticleOptions) ModParticles.BLUE_SMOKE.get(),
                    true,
                    x + Math.cos(angle) * radius,
                    y + body,
                    z + Math.sin(angle) * radius,
                    packedColor,
                    (0.018 + random.nextDouble() * 0.022) * speed,
                    (0.9 + random.nextDouble() * 0.55) * size
            );
        }

        int extra = local ? 3 + random.nextInt(2) : veryClose ? 1 : 1 + random.nextInt(2);
        for (int i = 0; i < extra; i++) {
            boolean left = random.nextBoolean();
            double side = left ? -1.0 : 1.0;
            double along = half * (1.05 + random.nextDouble() * 0.35);
            double body = height * (0.48 + random.nextDouble() * 0.3);
            double back = (random.nextDouble() - 0.5) * 0.16;
            level.addAlwaysVisibleParticle(
                    (ParticleOptions) ModParticles.BLUE_SMOKE.get(),
                    true,
                    x + rightX * along * side + lookX * back,
                    y + body,
                    z + rightZ * along * side + lookZ * back,
                    packedColor,
                    (0.016 + random.nextDouble() * 0.02) * speed,
                    (1.05 + random.nextDouble() * 0.5) * size
            );
        }
    }

    public static Player resolveSmokeOwner(ClientLevel level, double packedColor, double x, double y, double z) {
        int packed = (int) Math.round(packedColor);
        int entityId = packed / 8;
        if (entityId != 0) {
            var entity = level.getEntity(entityId);
            if (entity instanceof Player player && isAuraVisible(player)) {
                return player;
            }
        }
        Player best = null;
        double bestDist = 2.25;
        for (Player player : level.players()) {
            if (!isAuraVisible(player)) {
                continue;
            }
            double d = player.distanceToSqr(x, y, z);
            if (d < bestDist) {
                bestDist = d;
                best = player;
            }
        }
        return best;
    }
}
