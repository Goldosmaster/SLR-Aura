package net.slraura.client;

import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin.Model;
import net.minecraft.core.particles.ParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.AddLayers;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.slraura.client.aura.MonarchEyesLayer;
import net.slraura.client.particle.BlueSmokeParticle;
import net.slraura.particle.ModParticles;

@EventBusSubscriber(modid = "slr_aura", value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {
    }

    @SubscribeEvent
    public static void onAddLayers(AddLayers event) {
        for (Model skin : event.getSkins()) {
            if (event.getSkin(skin) instanceof PlayerRenderer renderer) {
                renderer.addLayer(new MonarchEyesLayer(renderer));
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet((ParticleType) ModParticles.BLUE_SMOKE.get(), BlueSmokeParticle.Provider::new);
    }
}
