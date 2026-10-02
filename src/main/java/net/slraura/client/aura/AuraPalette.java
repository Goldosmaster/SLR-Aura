package net.slraura.client.aura;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.ChatFormatting;

public enum AuraPalette {
    BLUE(ChatFormatting.AQUA),
    PURPLE(ChatFormatting.LIGHT_PURPLE),
    RED(ChatFormatting.RED),
    WHITE(ChatFormatting.WHITE),
    YELLOW(ChatFormatting.YELLOW),
    BLOOD(ChatFormatting.DARK_RED),
    CRIMSON(ChatFormatting.RED);

    public final ChatFormatting glow;

    AuraPalette(ChatFormatting glow) {
        this.glow = glow;
    }

    public static AuraPalette fromOrdinal(int ordinal) {
        AuraPalette[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return BLUE;
        }
        return values[ordinal];
    }

    public void pickWisp(ThreadLocalRandom rng, float[] out) {
        float u = rng.nextFloat();
        switch (this) {
            case PURPLE:
                if (u < 0.4F) {
                    set(out, 0.18F, 0.02F, 0.42F, rng, 0.12F, 0.06F, 0.18F);
                } else if (u < 0.75F) {
                    set(out, 0.48F, 0.04F, 0.78F, rng, 0.18F, 0.08F, 0.18F);
                } else {
                    set(out, 0.85F, 0.2F, 1.0F, rng, 0.15F, 0.18F, 0.0F);
                }
                break;
            case RED:
                if (u < 0.4F) {
                    set(out, 0.45F, 0.02F, 0.02F, rng, 0.18F, 0.04F, 0.04F);
                } else if (u < 0.75F) {
                    set(out, 0.9F, 0.08F, 0.04F, rng, 0.1F, 0.1F, 0.06F);
                } else {
                    set(out, 1.0F, 0.35F, 0.1F, rng, 0.0F, 0.2F, 0.1F);
                }
                break;
            case BLOOD:
                if (u < 0.42F) {
                    set(out, 0.02F, 0.0F, 0.0F, rng, 0.07F, 0.015F, 0.015F);
                } else if (u < 0.78F) {
                    set(out, 0.38F, 0.0F, 0.02F, rng, 0.2F, 0.03F, 0.04F);
                } else {
                    set(out, 0.72F, 0.02F, 0.04F, rng, 0.18F, 0.04F, 0.04F);
                }
                break;
            case CRIMSON:
                if (u < 0.35F) {
                    set(out, 0.48F, 0.0F, 0.08F, rng, 0.14F, 0.03F, 0.06F);
                } else if (u < 0.75F) {
                    set(out, 0.82F, 0.04F, 0.12F, rng, 0.12F, 0.05F, 0.08F);
                } else {
                    set(out, 1.0F, 0.10F, 0.18F, rng, 0.0F, 0.08F, 0.08F);
                }
                break;
            case WHITE:
                if (u < 0.35F) {
                    set(out, 0.78F, 0.80F, 0.84F, rng, 0.08F, 0.08F, 0.08F);
                } else if (u < 0.75F) {
                    set(out, 0.92F, 0.93F, 0.96F, rng, 0.06F, 0.05F, 0.04F);
                } else {
                    set(out, 0.98F, 0.99F, 1.0F, rng, 0.02F, 0.01F, 0.0F);
                }
                break;
            case YELLOW:
                if (u < 0.4F) {
                    set(out, 0.55F, 0.35F, 0.02F, rng, 0.15F, 0.12F, 0.04F);
                } else if (u < 0.75F) {
                    set(out, 0.95F, 0.7F, 0.08F, rng, 0.05F, 0.15F, 0.08F);
                } else {
                    set(out, 1.0F, 0.92F, 0.35F, rng, 0.0F, 0.08F, 0.15F);
                }
                break;
            default:
                if (u < 0.4F) {
                    set(out, 0.05F, 0.2F, 0.7F, rng, 0.08F, 0.2F, 0.2F);
                } else if (u < 0.75F) {
                    set(out, 0.3F, 0.75F, 1.0F, rng, 0.15F, 0.15F, 0.0F);
                } else {
                    set(out, 0.55F, 0.9F, 1.0F, rng, 0.2F, 0.1F, 0.0F);
                }
        }
    }

    public void pickSmoke(ThreadLocalRandom rng, float[] out) {
        switch (this) {
            case PURPLE:
                set(out, 0.45F, 0.08F, 0.7F, rng, 0.25F, 0.1F, 0.25F);
                break;
            case RED:
                set(out, 0.7F, 0.1F, 0.06F, rng, 0.25F, 0.1F, 0.08F);
                break;
            case BLOOD:
                if (rng.nextBoolean()) {
                    set(out, 0.03F, 0.0F, 0.0F, rng, 0.06F, 0.02F, 0.02F);
                } else {
                    set(out, 0.32F, 0.0F, 0.02F, rng, 0.22F, 0.04F, 0.04F);
                }
                break;
            case CRIMSON:
                set(out, 0.78F, 0.05F, 0.12F, rng, 0.16F, 0.06F, 0.08F);
                break;
            case WHITE:
                set(out, 0.86F, 0.88F, 0.92F, rng, 0.1F, 0.08F, 0.06F);
                break;
            case YELLOW:
                set(out, 0.9F, 0.7F, 0.12F, rng, 0.1F, 0.15F, 0.1F);
                break;
            default:
                set(out, 0.28F, 0.52F, 0.85F, rng, 0.1F, 0.12F, 0.1F);
        }
    }

    public float[] eyeTint() {
        return switch (this) {
            case PURPLE -> new float[]{0.85F, 0.35F, 1.0F};
            case RED -> new float[]{1.0F, 0.25F, 0.18F};
            case WHITE -> new float[]{0.95F, 0.97F, 1.0F};
            case YELLOW -> new float[]{1.0F, 0.88F, 0.25F};
            case BLOOD -> new float[]{0.72F, 0.04F, 0.04F};
            case CRIMSON -> new float[]{1.0F, 0.10F, 0.18F};
            default -> new float[]{0.35F, 0.8F, 1.0F};
        };
    }

    public static AuraPalette fromVesselIdentity(String identity) {
        if (identity == null || identity.isBlank()) {
            return BLUE;
        }
        String key = identity.trim().toLowerCase().replace(' ', '_').replace('-', '_');
        return switch (key) {
            case "ashborn" -> PURPLE;
            case "antares" -> CRIMSON;
            case "rakan" -> BLOOD;
            case "baran", "sillad" -> WHITE;
            case "liu_zhigang", "thomas_andre", "sharpest_fragment", "adamant_fragment", "sharpest", "adamant" -> YELLOW;
            default -> {
                if (key.contains("ashborn")) {
                    yield PURPLE;
                }
                if (key.contains("antares")) {
                    yield CRIMSON;
                }
                if (key.contains("rakan")) {
                    yield BLOOD;
                }
                if (key.contains("baran") || key.contains("sillad")) {
                    yield WHITE;
                }
                if (key.contains("liu") || key.contains("zhigang") || key.contains("thomas")
                        || key.contains("andre") || key.contains("sharpest") || key.contains("adamant")) {
                    yield YELLOW;
                }
                yield BLUE;
            }
        };
    }

    private static void set(float[] out, float r, float g, float b, ThreadLocalRandom rng, float dr, float dg, float db) {
        out[0] = r + rng.nextFloat() * dr;
        out[1] = g + rng.nextFloat() * dg;
        out[2] = b + rng.nextFloat() * db;
    }
}
