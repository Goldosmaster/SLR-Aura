package net.slraura.client;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.slraura.SLRAuraMod;
import net.slraura.client.aura.AuraOutlineUniforms;
import net.slraura.client.aura.HunterAuraClient;
import net.slraura.client.aura.HunterAuraFlameRenderer;
import net.slraura.config.AuraConfig;

@Mod(value = SLRAuraMod.MODID, dist = Dist.CLIENT)
public class SLRAuraClient {
    public SLRAuraClient(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(this::onConfigReload);
    }

    private void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() != AuraConfig.SPEC) {
            return;
        }
        Minecraft.getInstance().execute(() -> {
            HunterAuraFlameRenderer.clearWisps();
            AuraOutlineUniforms.apply();
            HunterAuraClient.resyncOwnColor();
        });
    }
}
