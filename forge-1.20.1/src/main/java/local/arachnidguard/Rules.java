package local.arachnidguard;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public final class Rules {
    private Rules() {}
    public static final List<String> DEFAULT_ENTITIES = List.of(
        "minecraft:spider", "minecraft:cave_spider",
        "alexsmobs:fly", "alexsmobs:leafcutter_ant", "twilightforest:mosquito_swarm", "blue_skies:nyctofly",
        "alexsmobs:centipede_head", "alexsmobs:centipede_body", "alexsmobs:centipede_tail",
        "alexsmobs:cockroach", "alexsmobs:cockroach_egg", "alexsmobs:tarantula_hawk",
        "alexsmobs:crimson_mosquito", "alexsmobs:warped_mosco", "alexsmobs:enderiophage",
        "blue_skies:venom_spider", "blue_skies:nested_spider", "blue_skies:arachnarch",
        "blue_skies:infested_swarmer", "cataclysm:endermaptera", "deeperdarker:sculk_centipede",
        "twilightforest:hedge_spider", "twilightforest:king_spider", "twilightforest:swarm_spider",
        "twilightforest:carminite_broodling", "twilightforest:fire_beetle",
        "twilightforest:pinch_beetle", "twilightforest:slime_beetle", "twilightforest:towerwood_borer",
        "hybrid_aquatic:spider_crab", "hybrid_aquatic:giant_isopod",
        "dealt_force_skills:gizmo_spiderling", "dealt_force_skills:gizmo_spider_nest_trap");
    public static final List<String> DEFAULT_BLOCKS = List.of(
        "minecraft:cobweb", "biomesoplenty:hanging_cobweb", "biomesoplenty:hanging_cobweb_strand",
        "biomesoplenty:stringy_cobweb", "biomesoplenty:webbing", "biomesoplenty:spider_egg",
        "blue_skies:spider_webbing", "blue_skies:spider_nest",
        "chipped:broken_cobweb", "chipped:cobweb_arch", "chipped:cobweb_floor",
        "chipped:dense_cobweb", "chipped:hanging_broken_cobweb", "chipped:hanging_cobweb",
        "chipped:scattered_cobweb", "chipped:stretched_cobweb", "chipped:stringy_cobweb", "chipped:thick_cobweb_floor");
    public static final List<String> DEFAULT_WORDS = List.of("spider", "scorpion", "centipede", "millipede", "arachnid", "arachnarch", "tarantula");
    public record Snapshot(Set<String> entities, Set<String> allowed, List<String> words,
                           Set<String> blocks, boolean removeBlocks, boolean removeSpawners) {}
    public static volatile Snapshot current = new Snapshot(Set.copyOf(DEFAULT_ENTITIES), Set.of(), DEFAULT_WORDS,
        Set.copyOf(DEFAULT_BLOCKS), true, true);
    private static final Set<String> observedSpiderTypes = ConcurrentHashMap.newKeySet();
    private static final java.util.Map<net.minecraft.world.level.block.Block, Boolean> BLOCK_CACHE = new ConcurrentHashMap<>();
    public static void clearBlockCache() { BLOCK_CACHE.clear(); }
    public static boolean blocksEntityId(String value) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) return false;
        String full = id.toString();
        Snapshot rules = current;
        if (rules.allowed.contains(full)) return false;
        if (rules.entities.contains(full) || observedSpiderTypes.contains(full)) return true;
        // Preserve vanilla bees, silverfish and endermites.
        return !id.getNamespace().equals("minecraft") && rules.words.stream().anyMatch(id.getPath()::contains);
    }
    public static boolean blocksEntity(Entity entity) {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (id == null || current.allowed.contains(id.toString())) return false;
        if (entity instanceof Spider) observedSpiderTypes.add(id.toString());
        return blocksEntityId(id.toString());
    }
    public static boolean blocksState(BlockState state) {
        return current.removeBlocks && BLOCK_CACHE.computeIfAbsent(state.getBlock(), block -> current.blocks.contains(blockId(state)));
    }
    public static String blockId(BlockState state) {
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        return id == null ? "" : id.toString();
    }
    public static boolean spawnerState(BlockState state) { return blockId(state).contains("spawner"); }
}
