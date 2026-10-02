package net.slraura.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.slraura.SLRAuraMod;
import net.slraura.client.aura.HunterAuraClient;

import java.util.UUID;

public record AuraSyncS2CPayload(UUID player, boolean on, byte palette) implements CustomPacketPayload {
    public static final Type<AuraSyncS2CPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SLRAuraMod.MODID, "sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AuraSyncS2CPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, AuraSyncS2CPayload::player,
            ByteBufCodecs.BOOL, AuraSyncS2CPayload::on,
            ByteBufCodecs.BYTE, AuraSyncS2CPayload::palette,
            AuraSyncS2CPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AuraSyncS2CPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> HunterAuraClient.applySync(payload.player(), payload.on(), payload.palette()));
    }
}
