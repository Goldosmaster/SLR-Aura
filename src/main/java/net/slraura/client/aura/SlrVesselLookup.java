package net.slraura.client.aura;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import net.minecraft.world.entity.Entity;

public final class SlrVesselLookup {
    private static boolean slrChecked;
    private static boolean slrPresent;
    private static Method vesselOf;
    private static Method vesselIdentity;
    private static Method vesselFallbackName;
    private static Object capToken;
    private static Method getCap1;
    private static Method getCap2;
    private static Field combatModeField;

    private SlrVesselLookup() {
    }

    public static boolean slrLoaded() {
        ensureInit();
        return slrPresent;
    }

    public static String identity(Entity entity) {
        ensureInit();
        if (slrPresent && entity != null && vesselOf != null) {
            try {
                if (vesselOf.invoke(null, entity) instanceof Optional<?> boxed && !boxed.isEmpty()) {
                    Object vessel = boxed.get();
                    if (vesselIdentity != null && vesselIdentity.invoke(vessel) instanceof String s && !s.isBlank()) {
                        return s;
                    }
                    if (vesselFallbackName != null) {
                        Object name = vesselFallbackName.invoke(vessel);
                        if (name instanceof String text) {
                            return text;
                        }
                    }
                    return "";
                }
                return "";
            } catch (Throwable ignored) {
                return "";
            }
        }
        return "";
    }

    public static AuraPalette palette(Entity entity) {
        return AuraPalette.fromVesselIdentity(identity(entity));
    }

    public static boolean isCombatMode(Entity entity) {
        ensureInit();
        if (slrPresent && entity != null && capToken != null && combatModeField != null) {
            try {
                Object vars = readVars(entity);
                return vars != null && combatModeField.getBoolean(vars);
            } catch (Throwable ignored) {
                return false;
            }
        }
        return false;
    }

    private static Object readVars(Entity entity) throws Exception {
        Object raw = null;
        if (getCap1 != null) {
            raw = getCap1.invoke(entity, capToken);
        } else if (getCap2 != null) {
            raw = getCap2.invoke(entity, capToken, null);
        }
        return raw instanceof Optional<?> opt ? opt.orElse(null) : raw;
    }

    private static void ensureInit() {
        if (slrChecked) {
            return;
        }
        slrChecked = true;
        try {
            Class<?> registry = Class.forName("net.solocraft.api.vessel.VesselRegistry");
            vesselOf = registry.getMethod("of", Entity.class);
            Class<?> vesselClass = Class.forName("net.solocraft.api.vessel.Vessel");
            vesselIdentity = vesselClass.getMethod("identity");
            vesselFallbackName = vesselClass.getMethod("fallbackName");
            Class<?> holder = Class.forName("net.solocraft.network.SololevelingModVariables");
            capToken = holder.getField("PLAYER_VARIABLES_CAPABILITY").get(null);
            Class<?> varsClass = Class.forName("net.solocraft.network.SololevelingModVariables$PlayerVariables");
            combatModeField = varsClass.getField("combatmode");
            try {
                getCap1 = Entity.class.getMethod("getCapability", capToken.getClass());
            } catch (NoSuchMethodException ignored) {
                getCap2 = Entity.class.getMethod("getCapability", capToken.getClass(), Object.class);
            }
            slrPresent = true;
        } catch (Throwable ignored) {
            slrPresent = false;
        }
    }
}
