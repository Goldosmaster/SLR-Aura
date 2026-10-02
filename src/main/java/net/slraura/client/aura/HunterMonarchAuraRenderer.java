package net.slraura.client.aura;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;

@EventBusSubscriber(modid = "slr_aura", value = Dist.CLIENT)
public final class HunterMonarchAuraRenderer {
    private static final String TEAM_PREFIX = "slraura_";
    private static final Map<UUID, ChatFormatting> GLOWING = new HashMap<>();
    private static final Map<UUID, String> SAVED_TEAMS = new HashMap<>();

    private HunterMonarchAuraRenderer() {
    }

    @SubscribeEvent
    public static void onClientTick(Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            GLOWING.clear();
            SAVED_TEAMS.clear();
            return;
        }

        Map<UUID, ChatFormatting> keep = new HashMap<>();
        for (Player player : mc.level.players()) {
            if (!HunterAuraClient.isAuraVisible(player) || HunterAuraClient.hideFromLocalCamera(player)) {
                continue;
            }
            ChatFormatting color = HunterAuraClient.palette(player).glow;
            ChatFormatting last = GLOWING.get(player.getUUID());
            if (last == null || last != color) {
                applyGlow(player, color);
            } else {
                player.setSharedFlag(6, true);
            }
            keep.put(player.getUUID(), color);
        }

        for (UUID id : new ArrayList<>(GLOWING.keySet())) {
            if (keep.containsKey(id)) {
                continue;
            }
            Player player = mc.level.getPlayerByUUID(id);
            if (player != null) {
                clearGlow(player);
            }
            SAVED_TEAMS.remove(id);
        }
        GLOWING.clear();
        GLOWING.putAll(keep);
    }

    private static void applyGlow(Player player, ChatFormatting color) {
        PlayerTeam prev = player.getTeam();
        String ourTeam = teamName(player.getUUID());
        if (prev == null || !ourTeam.equals(prev.getName())) {
            SAVED_TEAMS.put(player.getUUID(), prev != null ? prev.getName() : null);
        }

        Scoreboard board = player.level().getScoreboard();
        PlayerTeam team = board.getPlayerTeam(ourTeam);
        if (team == null) {
            team = board.addPlayerTeam(ourTeam);
        }
        team.setColor(color);
        board.addPlayerToTeam(player.getScoreboardName(), team);
        player.setSharedFlag(6, true);
    }

    private static void clearGlow(Player player) {
        player.setSharedFlag(6, false);
        Scoreboard board = player.level().getScoreboard();
        PlayerTeam team = board.getPlayerTeam(teamName(player.getUUID()));
        if (team != null && team.getPlayers().contains(player.getScoreboardName())) {
            board.removePlayerFromTeam(player.getScoreboardName(), team);
        }
        String saved = SAVED_TEAMS.remove(player.getUUID());
        if (saved != null) {
            PlayerTeam restore = board.getPlayerTeam(saved);
            if (restore != null) {
                board.addPlayerToTeam(player.getScoreboardName(), restore);
            }
        }
    }

    private static String teamName(UUID id) {
        return TEAM_PREFIX + id.toString().replace("-", "");
    }
}
