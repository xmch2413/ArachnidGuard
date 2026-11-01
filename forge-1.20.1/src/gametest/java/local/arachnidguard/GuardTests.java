package local.arachnidguard;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;

@GameTestHolder(ArachnidGuard.ID)
@PrefixGameTestTemplate(false)
public final class GuardTests {
    private static BlockPos position(GameTestHelper h) { return h.absolutePos(new BlockPos(1, 1, 1)); }
    private static void check(GameTestHelper h, boolean result, String message) { h.assertTrue(result, message); }
    @GameTest(template = "empty3x3x3")
    public static void blocksAcrossEveryLoadedDimension(GameTestHelper h) {
        int dimensions = 0;
        for (var level : h.getLevel().getServer().getAllLevels()) {
            var spider = EntityType.SPIDER.create(level);
            spider.setPos(100000.5, 80, 100000.5);
            check(h, !level.addFreshEntity(spider), "Spider accepted far away in " + level.dimension().location());
            dimensions++;
        }
        check(h, dimensions >= 3, "Expected overworld, nether and end");
        ArachnidGuard.LOG.info("DIMENSION CHECK: distant spawning blocked in {} dimensions", dimensions);
        h.succeed();
    }
    @GameTest(template = "empty3x3x3", timeoutTicks = 200)
    public static void registeredModTargetsCannotEnterWorld(GameTestHelper h) {
        int checked = 0;
        for (var id : ForgeRegistries.ENTITY_TYPES.getKeys()) {
            if (id.getNamespace().equals("minecraft") || !Rules.blocksEntityId(id.toString())) continue;
            var entity = ForgeRegistries.ENTITY_TYPES.getValue(id).create(h.getLevel());
            check(h, entity != null, "Could not construct target entity: " + id);
            entity.setPos(Vec3.atCenterOf(position(h)));
            check(h, !h.getLevel().addFreshEntity(entity), "Mod entity entered world: " + id);
            checked++;
        }
        if (Boolean.getBoolean("arachnidguard.compat")) check(h, checked >= 20, "Content-mod fixtures missing");
        ArachnidGuard.LOG.info("COMPAT ENTITY CHECK: {} registered mod targets blocked", checked);
        h.succeed();
    }
    @GameTest(template = "empty3x3x3", timeoutTicks = 200)
    public static void registeredWebsRespectPlayerPlacement(GameTestHelper h) {
        var level = h.getLevel(); var p = position(h); int checked = 0;
        for (String value : Rules.DEFAULT_BLOCKS) {
            var id = new ResourceLocation(value);
            if (!ForgeRegistries.BLOCKS.containsKey(id)) continue;
            var block = ForgeRegistries.BLOCKS.getValue(id);
            for (var direction : Direction.values()) level.setBlock(p.relative(direction), Blocks.STONE.defaultBlockState(), 3);
            level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
            level.setBlock(p, block.defaultBlockState(), 3);
            check(h, level.getBlockState(p).isAir(), "Natural mod web survived: " + value);
            if (block.asItem() instanceof BlockItem item) {
                placeByHand(h, p, item);
                check(h, level.getBlockState(p).is(block), "Player mod web lost: " + value);
                ArachnidGuard.cleanupChunk(level, level.getChunkAt(p));
                check(h, level.getBlockState(p).is(block), "Cleanup lost player mod web: " + value);
            }
            checked++;
        }
        level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
        if (Boolean.getBoolean("arachnidguard.compat")) check(h, checked >= 15, "Web-mod fixtures missing");
        ArachnidGuard.LOG.info("COMPAT WEB CHECK: {} registered web/nest blocks checked", checked);
        h.succeed();
    }
    @GameTest(template = "empty3x3x3")
    public static void blocksActualSpiderInsertion(GameTestHelper h) {
        var level = h.getLevel();
        var spider = EntityType.SPIDER.create(level);
        spider.setPos(Vec3.atCenterOf(position(h)));
        check(h, !level.addFreshEntity(spider), "Spider entered the world");
        var cave = EntityType.CAVE_SPIDER.create(level);
        cave.setPos(Vec3.atCenterOf(position(h)));
        check(h, !level.addFreshEntity(cave), "Cave spider entered the world");
        h.succeed();
    }
    @GameTest(template = "empty3x3x3")
    public static void preservesOtherVanillaArthropods(GameTestHelper h) {
        for (var type : new EntityType<?>[]{EntityType.BEE, EntityType.SILVERFISH, EntityType.ENDERMITE}) {
            var entity = type.create(h.getLevel());
            entity.setPos(Vec3.atCenterOf(position(h)));
            check(h, h.getLevel().addFreshEntity(entity), "Non-target vanilla entity was blocked: " + type);
            entity.discard();
        }
        check(h, Rules.blocksEntityId("example:giant_scorpion"), "Modded scorpion not detected");
        check(h, Rules.blocksEntityId("twilightforest:carminite_broodling"), "Non-spider-named spider missing");
        check(h, !Rules.blocksEntityId("hybrid_aquatic:hermit_crab"), "Ordinary crab blocked");
        h.succeed();
    }
    @GameTest(template = "empty3x3x3")
    public static void filtersProtoChunkWorldGeneration(GameTestHelper h) {
        var level = h.getLevel();
        var proto = new ProtoChunk(new ChunkPos(0, 0), UpgradeData.EMPTY, level,
            level.registryAccess().registryOrThrow(Registries.BIOME), null);
        BlockPos p = new BlockPos(1, 70, 1);
        proto.setBlockState(p, Blocks.COBWEB.defaultBlockState(), false);
        check(h, proto.getBlockState(p).isAir(), "Worldgen web survived");
        proto.setBlockState(p, Blocks.STONE.defaultBlockState(), false);
        check(h, proto.getBlockState(p).is(Blocks.STONE), "Ordinary worldgen block lost");
        h.succeed();
    }
    @GameTest(template = "empty3x3x3")
    public static void filtersRuntimeNaturalWebs(GameTestHelper h) {
        var p = position(h);
        h.getLevel().setBlock(p, Blocks.COBWEB.defaultBlockState(), 3);
        check(h, h.getLevel().getBlockState(p).isAir(), "Runtime natural web survived");
        h.succeed();
    }
    private static void placeByHand(GameTestHelper h, BlockPos p, net.minecraft.world.item.Item item) {
        var level = h.getLevel();
        level.setBlock(p.below(), Blocks.STONE.defaultBlockState(), 3);
        var player = FakePlayerFactory.getMinecraft(level);
        player.setPos(Vec3.atCenterOf(p.offset(2, 0, 0)));
        var stack = new ItemStack(item);
        var hit = new BlockHitResult(Vec3.atCenterOf(p.below()).add(0, 0.5, 0), Direction.UP, p.below(), false);
        var result = ((BlockItem)item).place(new BlockPlaceContext(player, InteractionHand.MAIN_HAND, stack, hit));
        check(h, result.consumesAction(), "Player hand placement failed");
    }
    @GameTest(template = "empty3x3x3")
    public static void handPlacedWebSurvivesCleanupAndSave(GameTestHelper h) {
        var level = h.getLevel();
        var p = position(h);
        placeByHand(h, p, Items.COBWEB);
        check(h, level.getBlockState(p).is(Blocks.COBWEB), "Hand-placed web removed");
        var chunk = level.getChunkAt(p);
        ArachnidGuard.cleanupChunk(level, chunk);
        check(h, level.getBlockState(p).is(Blocks.COBWEB), "Cleanup removed protected web");
        var saved = ChunkSerializer.write(level, chunk);
        var restored = ChunkSerializer.read(level, level.getPoiManager(), chunk.getPos(), saved);
        check(h, restored instanceof ImposterProtoChunk, "Expected full chunk reload");
        var copy = ((ImposterProtoChunk)restored).getWrapped();
        check(h, PlayerPlacement.isProtected(copy, p, copy.getBlockState(p)), "Player provenance was not persisted");
        // A later natural web must not inherit protection from a broken player web.
        level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(p, Blocks.COBWEB.defaultBlockState(), 3);
        check(h, level.getBlockState(p).isAir(), "Broken web left stale protection");
        h.succeed();
    }
    private static SpawnerBlockEntity spawner(GameTestHelper h, BlockPos p, String id) {
        h.getLevel().setBlock(p, Blocks.SPAWNER.defaultBlockState(), 3);
        var spawner = (SpawnerBlockEntity)h.getLevel().getBlockEntity(p);
        var entity = new CompoundTag(); entity.putString("id", id);
        var data = new CompoundTag(); data.put("entity", entity);
        var nbt = new CompoundTag(); nbt.put("SpawnData", data); nbt.putShort("Delay", (short)0);
        spawner.load(nbt);
        return spawner;
    }
    @GameTest(template = "empty3x3x3")
    public static void removesOnlyTargetSpawners(GameTestHelper h) {
        var level = h.getLevel(); var p = position(h);
        spawner(h, p, "minecraft:cave_spider");
        ArachnidGuard.cleanupChunk(level, level.getChunkAt(p));
        check(h, level.getBlockState(p).isAir(), "Target spawner survived chunk cleanup");
        var zombie = spawner(h, p, "minecraft:zombie");
        ArachnidGuard.cleanupChunk(level, level.getChunkAt(p));
        zombie.getSpawner().serverTick(level, p);
        check(h, level.getBlockState(p).is(Blocks.SPAWNER), "Zombie spawner removed");
        var spider = spawner(h, p, "minecraft:spider");
        spider.getSpawner().serverTick(level, p);
        check(h, level.getBlockState(p).isAir(), "Late-created spider spawner survived its first tick");
        h.succeed();
    }
    @GameTest(template = "empty3x3x3")
    public static void preservesPlayerSpawner(GameTestHelper h) {
        var level = h.getLevel(); var p = position(h);
        placeByHand(h, p, Items.SPAWNER);
        var entity = new CompoundTag(); entity.putString("id", "minecraft:spider");
        var data = new CompoundTag(); data.put("entity", entity);
        var tag = new CompoundTag(); tag.put("SpawnData", data);
        var spawner = (SpawnerBlockEntity)level.getBlockEntity(p);
        spawner.load(tag);
        ArachnidGuard.cleanupChunk(level, level.getChunkAt(p));
        spawner.getSpawner().serverTick(level, p);
        check(h, level.getBlockState(p).is(Blocks.SPAWNER), "Player spawner removed");
        h.succeed();
    }
    @GameTest(template = "empty3x3x3")
    public static void detectsNestedAndMixedSpawnerData(GameTestHelper h) {
        var entity = new CompoundTag(); entity.putString("id", "alexsmobs:centipede_head");
        var data = new CompoundTag(); data.put("entity", entity);
        var weighted = new CompoundTag(); weighted.put("data", data); weighted.putInt("weight", 1);
        var potentials = new ListTag(); potentials.add(weighted);
        var spawner = new CompoundTag(); spawner.put("SpawnPotentials", potentials);
        var dynamic = new CompoundTag(); dynamic.put("Spawner", spawner);
        check(h, SpawnerRules.containsBlockedEntity(dynamic), "Nested mixed spawner escaped detection");
        var name = new CompoundTag(); name.putString("CustomName", "minecraft:spider");
        check(h, !SpawnerRules.containsBlockedEntity(name), "CustomName caused false positive");
        h.succeed();
    }
}
