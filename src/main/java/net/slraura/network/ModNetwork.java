package net.slraura.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
    private ModNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("2");
        registrar.playToServer(AuraToggleC2SPayload.TYPE, AuraToggleC2SPayload.STREAM_CODEC, AuraToggleC2SPayload::handle);
        registrar.playToClient(AuraSyncS2CPayload.TYPE, AuraSyncS2CPayload.STREAM_CODEC, AuraSyncS2CPayload::handle);
    }
}
