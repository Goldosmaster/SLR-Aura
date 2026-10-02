package net.slraura.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.slraura.SLRAuraMod;
import net.slraura.client.aura.AuraPalette;
import net.slraura.client.aura.SlrVesselLookup;
import net.slraura.network.AuraSyncS2CPayload;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = SLRAuraMod.MODID)
public final class AuraTracker {
    public record State(boolean on, byte palette) {
    }

    private static final Map<UUID, State> STATES = new ConcurrentHashMap<>();

    private AuraTracker() {
    }

    public static void toggleFromClient(Player player, boolean on, byte palette) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        byte resolved = palette;
        if (resolved < 0 || resolved >= AuraPalette.values().length) {
            resolved = (byte) SlrVesselLookup.palette(serverPlayer).ordinal();
        }
        State state = new State(on, resolved);
        if (on) {
            STATES.put(serverPlayer.getUUID(), state);
        } else {
            STATES.remove(serverPlayer.getUUID());
        }
        broadcast(serverPlayer, state);
    }

    public static State get(UUID id) {
        return STATES.get(id);
    }

    public static void broadcast(ServerPlayer player, State state) {
        AuraSyncS2CPayload payload = new AuraSyncS2CPayload(player.getUUID(), state.on(), state.palette());
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, payload);
    }

    private static void sendTo(ServerPlayer watcher, ServerPlayer target) {
        State state = STATES.get(target.getUUID());
        if (state == null) {
            return;
        }
        PacketDistributor.sendToPlayer(watcher, new AuraSyncS2CPayload(target.getUUID(), state.on(), state.palette()));
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            State self = STATES.get(player.getUUID());
            if (self != null) {
                broadcast(player, self);
            }
            if (player.serverLevel() != null) {
                for (ServerPlayer other : player.serverLevel().players()) {
                    if (other != player) {
                        sendTo(player, other);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof ServerPlayer tracked && event.getEntity() instanceof ServerPlayer watcher) {
            sendTo(watcher, tracked);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            State state = STATES.get(player.getUUID());
            if (state != null) {
                broadcast(player, state);
            }
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            STATES.remove(player.getUUID());
            broadcast(player, new State(false, (byte) AuraPalette.BLUE.ordinal()));
        }
    }
}
