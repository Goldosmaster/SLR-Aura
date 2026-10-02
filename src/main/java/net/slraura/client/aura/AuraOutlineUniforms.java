package net.slraura.client.aura;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.PostChain;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.slraura.config.AuraConfig;

@EventBusSubscriber(modid = "slr_aura", value = Dist.CLIENT)
public final class AuraOutlineUniforms {
    private static float lastRadius = Float.NaN;

    private AuraOutlineUniforms() {
    }

    @SubscribeEvent
    public static void onClientTick(Post event) {
        apply();
    }

    public static void apply() {
        float radius = AuraConfig.OUTLINE_THICKNESS.get().floatValue();
        if (Float.isNaN(lastRadius) || radius != lastRadius) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.levelRenderer != null) {
                PostChain chain = getEntityEffect(mc.levelRenderer);
                if (chain != null) {
                    chain.setUniform("OutlineRadius", radius);
                }
            }
            lastRadius = radius;
        }
    }

    private static PostChain getEntityEffect(LevelRenderer renderer) {
        return renderer.entityEffect;
    }
}
