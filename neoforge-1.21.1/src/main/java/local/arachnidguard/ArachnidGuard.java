package local.arachnidguard;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.bus.api.IEventBus;
import net.minecraft.core.registries.BuiltInRegistries;
import org.slf4j.Logger;

@Mod(ArachnidGuard.ID)
public final class ArachnidGuard {
    public static final String ID = "arachnidguard";
    public static final Logger LOG = LogUtils.getLogger();
    public static final AtomicLong BLOCKED_ENTITIES = new AtomicLong();
    public static final AtomicLong REMOVED_BLOCKS = new AtomicLong();
    private static final Map<ServerLevel, Set<Long>> PENDING = new ConcurrentHashMap<>();
    public ArachnidGuard(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, GuardConfig.SPEC);
        modBus.addListener(this::configChanged);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::entityJoin);
        NeoForge.EVENT_BUS.addListener(this::chunkLoaded);
        NeoForge.EVENT_BUS.addListener(this::chunkUnloaded);
        NeoForge.EVENT_BUS.addListener(this::levelTick);
        NeoForge.EVENT_BUS.addListener(this::commands);
        NeoForge.EVENT_BUS.addListener(this::serverStopped);
    }
    private void configChanged(ModConfigEvent event) {
        if (event.getConfig().getSpec() == GuardConfig.SPEC) GuardConfig.bake();
    }
    private void entityJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && Rules.blocksEntity(event.getEntity())) {
            event.setCanceled(true);
            BLOCKED_ENTITIES.incrementAndGet();
        }
    }
    private void chunkLoaded(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level && event.getChunk() instanceof LevelChunk chunk)
            PENDING.computeIfAbsent(level, unused -> ConcurrentHashMap.newKeySet()).add(chunk.getPos().toLong());
    }
    private void chunkUnloaded(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            Set<Long> pending = PENDING.get(level);
            if (pending != null) pending.remove(event.getChunk().getPos().toLong());
        }
    }
    private void levelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        Set<Long> pending = PENDING.get(level);
        if (pending == null || pending.isEmpty()) return;
        long deadline = System.nanoTime() + 3_000_000L;
        int count = 0;
        for (long packed : pending) {
            if (count++ >= 8 || System.nanoTime() > deadline) break;
            if (!pending.remove(packed)) continue;
            LevelChunk chunk = level.getChunkSource().getChunkNow(ChunkPos.getX(packed), ChunkPos.getZ(packed));
            if (chunk != null) cleanupChunk(level, chunk);
        }
    }
    public static int cleanupChunk(ServerLevel level, LevelChunk chunk) {
        int removed = 0;
        if (Rules.current.removeBlocks()) {
            var sections = chunk.getSections();
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int index = 0; index < sections.length; index++) {
                var section = sections[index];
                if (section.hasOnlyAir() || !section.maybeHas(Rules::blocksState)) continue;
                int baseY = chunk.getSectionYFromSectionIndex(index) << 4;
                for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
                    var state = section.getBlockState(x, y, z);
                    if (!Rules.blocksState(state)) continue;
                    pos.set(chunk.getPos().getMinBlockX() + x, baseY + y, chunk.getPos().getMinBlockZ() + z);
                    if (!PlayerPlacement.isProtected(chunk, pos, state) && level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2)) removed++;
                }
            }
        }
        if (Rules.current.removeSpawners()) {
            for (BlockPos pos : new ArrayList<>(chunk.getBlockEntitiesPos())) {
                var state = chunk.getBlockState(pos);
                if (!Rules.spawnerState(state) || PlayerPlacement.isProtected(chunk, pos, state)) continue;
                var data = chunk.getBlockEntityNbtForSaving(pos, level.registryAccess());
                if (SpawnerRules.containsBlockedEntity(data) && level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3)) removed++;
            }
        }
        REMOVED_BLOCKS.addAndGet(removed);
        return removed;
    }
    private void serverStopped(ServerStoppedEvent event) {
        PENDING.clear();
        BLOCKED_ENTITIES.set(0);
        REMOVED_BLOCKS.set(0);
    }
    private void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal(ID).requires(source -> source.hasPermission(2))
            .then(Commands.literal("status").executes(ctx -> {
                var matches = BuiltInRegistries.ENTITY_TYPE.keySet().stream().map(Object::toString).filter(Rules::blocksEntityId).sorted().toList();
                ctx.getSource().sendSuccess(() -> Component.literal("蛛类屏蔽：全维度生效。匹配 " + matches.size() + " 种实体，本次拦截 " + BLOCKED_ENTITIES.get() + " 次，清理方块 " + REMOVED_BLOCKS.get() + " 个。"), false);
                LOG.info("Arachnid Guard matched entities: {}", matches);
                return matches.size();
            }))
            .then(Commands.literal("list").executes(ctx -> {
                String ids = String.join(", ", BuiltInRegistries.ENTITY_TYPE.keySet().stream().map(Object::toString).filter(Rules::blocksEntityId).sorted().toList());
                ctx.getSource().sendSuccess(() -> Component.literal(ids), false);
                return 1;
            })));
    }
}
