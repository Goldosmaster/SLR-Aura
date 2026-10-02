package net.slraura;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.slraura.config.AuraConfig;
import net.slraura.network.ModNetwork;
import net.slraura.particle.ModParticles;

@Mod(SLRAuraMod.MODID)
public class SLRAuraMod {
    public static final String MODID = "slr_aura";

    public SLRAuraMod(IEventBus modEventBus, ModContainer container) {
        ModParticles.register(modEventBus);
        modEventBus.addListener(ModNetwork::register);
        container.registerConfig(ModConfig.Type.CLIENT, AuraConfig.SPEC);
    }
}
