package net.slraura.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.slraura.client.aura.AuraPalette;

public final class AuraConfig {
    public enum ColorMode {
        AUTO,
        BLUE,
        PURPLE,
        RED,
        WHITE,
        YELLOW,
        BLOOD,
        CRIMSON;

        public AuraPalette toPalette() {
            return switch (this) {
                case PURPLE -> AuraPalette.PURPLE;
                case RED -> AuraPalette.RED;
                case WHITE -> AuraPalette.WHITE;
                case YELLOW -> AuraPalette.YELLOW;
                case BLOOD -> AuraPalette.BLOOD;
                case CRIMSON -> AuraPalette.CRIMSON;
                default -> AuraPalette.BLUE;
            };
        }
    }

    public enum SmokeColorMode {
        MATCH_AURA,
        BLUE,
        PURPLE,
        RED,
        WHITE,
        YELLOW,
        BLOOD,
        CRIMSON;

        public AuraPalette toPalette(AuraPalette aura) {
            return switch (this) {
                case MATCH_AURA -> aura;
                case PURPLE -> AuraPalette.PURPLE;
                case RED -> AuraPalette.RED;
                case WHITE -> AuraPalette.WHITE;
                case YELLOW -> AuraPalette.YELLOW;
                case BLOOD -> AuraPalette.BLOOD;
                case CRIMSON -> AuraPalette.CRIMSON;
                default -> AuraPalette.BLUE;
            };
        }
    }

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue OUTLINE_THICKNESS;

    public static final ModConfigSpec.BooleanValue FLAMES_ENABLED;
    public static final ModConfigSpec.EnumValue<ColorMode> FORCE_COLOR;
    public static final ModConfigSpec.IntValue FLAME_COUNT;
    public static final ModConfigSpec.DoubleValue FLAME_LENGTH;
    public static final ModConfigSpec.IntValue FLAME_ANGLE;

    public static final ModConfigSpec.EnumValue<SmokeColorMode> SMOKE_COLOR;
    public static final ModConfigSpec.DoubleValue SMOKE_SPEED;
    public static final ModConfigSpec.DoubleValue SMOKE_SIZE;
    public static final ModConfigSpec.DoubleValue SMOKE_TRANSPARENCY;

    public static final ModConfigSpec.DoubleValue EYE_OFFSET_UP;
    public static final ModConfigSpec.DoubleValue LEFT_EYE_OFFSET_X;
    public static final ModConfigSpec.DoubleValue RIGHT_EYE_OFFSET_X;

    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("flames");
        FLAMES_ENABLED = BUILDER
                .comment("Show the swirling flames around the aura.")
                .define("enableFlames", true);
        FORCE_COLOR = BUILDER
                .comment("Force the aura color. AUTO uses your Solo Leveling vessel color.")
                .defineEnum("forceColor", ColorMode.AUTO);
        FLAME_COUNT = BUILDER
                .comment("How many swirling flames stay around the body. Default 20.")
                .defineInRange("flameCount", 20, 4, 60);
        FLAME_LENGTH = BUILDER
                .comment("Flame length multiplier. 1.0 is the original length.")
                .defineInRange("flameLength", 1.0, 0.4, 3.0);
        FLAME_ANGLE = BUILDER
                .comment("Flame angle in degrees. 0 is straight up, 90 is outward. Default 55.")
                .defineInRange("flameAngle", 55, 0, 90);
        BUILDER.pop();

        BUILDER.push("outline");
        OUTLINE_THICKNESS = BUILDER
                .comment("Glow outline thickness. 2.0 is normal Minecraft, higher is thicker.")
                .defineInRange("thickness", 5.0, 2.0, 12.0);
        BUILDER.pop();

        BUILDER.push("smoke");
        SMOKE_COLOR = BUILDER
                .comment("Smoke color. MATCH_AURA uses the same color as the flames.")
                .defineEnum("smokeColor", SmokeColorMode.MATCH_AURA);
        SMOKE_SPEED = BUILDER
                .comment("How fast the smoke rises. 1.0 is the original speed.")
                .defineInRange("smokeSpeed", 1.0, 0.2, 4.0);
        SMOKE_SIZE = BUILDER
                .comment("Smoke puff size. 1.0 is the original size.")
                .defineInRange("smokeSize", 1.0, 0.3, 3.0);
        SMOKE_TRANSPARENCY = BUILDER
                .comment("Smoke opacity. 1.0 is original, lower is more see-through.")
                .defineInRange("smokeTransparency", 1.0, 0.1, 1.0);
        BUILDER.pop();

        BUILDER.push("eyes");
        EYE_OFFSET_UP = BUILDER
                .comment("Move both star eyes up (positive) or down (negative). 0 is the original position.")
                .defineInRange("offsetUp", 0.0, -8.0, 8.0);
        LEFT_EYE_OFFSET_X = BUILDER
                .comment("Move the left eye left (negative) or right (positive). 0 is the original position.")
                .defineInRange("leftOffsetX", 0.0, -8.0, 8.0);
        RIGHT_EYE_OFFSET_X = BUILDER
                .comment("Move the right eye left (negative) or right (positive). 0 is the original position.")
                .defineInRange("rightOffsetX", 0.0, -8.0, 8.0);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private AuraConfig() {
    }
}
