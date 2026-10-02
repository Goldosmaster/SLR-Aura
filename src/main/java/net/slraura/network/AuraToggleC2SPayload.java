package net.slraura.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.slraura.SLRAuraMod;
import net.slraura.server.AuraTracker;

public record AuraToggleC2SPayload(boolean on, byte palette) implements CustomPacketPayload {
    public static final Type<AuraToggleC2SPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SLRAuraMod.MODID, "toggle"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AuraToggleC2SPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, AuraToggleC2SPayload::on,
            ByteBufCodecs.BYTE, AuraToggleC2SPayload::palette,
            AuraToggleC2SPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AuraToggleC2SPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> AuraTracker.toggleFromClient(context.player(), payload.on(), payload.palette()));
    }
}
