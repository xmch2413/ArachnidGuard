package local.arachnidguard;
import java.util.HashSet;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class GuardConfig {
    private GuardConfig() {}
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();
    private static final ModConfigSpec.ConfigValue<java.util.List<? extends String>> ENTITIES = B
        .comment("Exact entity IDs blocked in ALL dimensions and ALL spawn sources. Other vanilla arthropods are preserved.")
        .defineListAllowEmpty("blockedEntities", Rules.DEFAULT_ENTITIES, o -> o instanceof String s && s.contains(":"));
    private static final ModConfigSpec.ConfigValue<java.util.List<? extends String>> ALLOWED = B
        .comment("Explicit exceptions, overriding all other entity rules.")
        .defineListAllowEmpty("allowedEntities", java.util.List.<String>of(), o -> o instanceof String s && s.contains(":"));
    private static final ModConfigSpec.ConfigValue<java.util.List<? extends String>> WORDS = B
        .comment("Name matching applies to MODDED entity IDs only. Empty list disables name matching.")
        .defineListAllowEmpty("moddedEntityKeywords", Rules.DEFAULT_WORDS, o -> o instanceof String s && !s.isBlank());
    private static final ModConfigSpec.ConfigValue<java.util.List<? extends String>> BLOCKS = B
        .comment("Natural web/egg/nest blocks. Normal player hand placement is preserved in chunk metadata.")
        .defineListAllowEmpty("naturalWebBlocks", Rules.DEFAULT_BLOCKS, o -> o instanceof String s && s.contains(":"));
    private static final ModConfigSpec.BooleanValue REMOVE_BLOCKS = B.define("removeNaturalWebs", true);
    private static final ModConfigSpec.BooleanValue REMOVE_SPAWNERS = B
        .comment("Remove natural spawners with any blocked entity in their NBT, including mixed SpawnPotentials.")
        .define("removeTargetSpawners", true);
    public static final ModConfigSpec SPEC = B.build();
    public static void bake() {
        Rules.current = new Rules.Snapshot(new HashSet<>(ENTITIES.get()), new HashSet<>(ALLOWED.get()),
            java.util.List.copyOf(WORDS.get()), new HashSet<>(BLOCKS.get()), REMOVE_BLOCKS.get(), REMOVE_SPAWNERS.get());
        Rules.clearBlockCache();
    }
}
