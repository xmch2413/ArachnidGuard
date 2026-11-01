package local.arachnidguard;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.state.BlockState;

public final class PlayerPlacement {
    private PlayerPlacement() {}
    private record Context(Level level, BlockPos pos) {}
    private static final ThreadLocal<Context> ACTIVE = new ThreadLocal<>();
    public static final String KEY = "ArachnidGuardPlayerBlocks";
    public static boolean isActive(Level level, BlockPos pos) {
        Context context = ACTIVE.get();
        return context != null && context.level == level && context.pos.equals(pos);
    }
    public static boolean place(Level level, BlockPos pos, BlockState state, int flags, boolean hasPlayer) {
        if (!hasPlayer || level.isClientSide) return level.setBlock(pos, state, flags);
        Context old = ACTIVE.get();
        ACTIVE.set(new Context(level, pos.immutable()));
        try { return level.setBlock(pos, state, flags); }
        finally { if (old == null) ACTIVE.remove(); else ACTIVE.set(old); }
    }
    public static boolean isProtected(LevelChunk chunk, BlockPos pos, BlockState state) {
        return ((PlacementData)chunk).arachnidguard$getPlacements().getString(Long.toString(pos.asLong())).equals(Rules.blockId(state));
    }
    public static void afterWrite(LevelChunk chunk, BlockPos pos, BlockState state) {
        if (chunk.getLevel().isClientSide) return;
        CompoundTag protectedBlocks = ((PlacementData)chunk).arachnidguard$getPlacements();
        if (!isActive(chunk.getLevel(), pos) && protectedBlocks.isEmpty()) return;
        String key = Long.toString(pos.asLong());
        if (isActive(chunk.getLevel(), pos) && (Rules.current.blocks().contains(Rules.blockId(state)) || Rules.spawnerState(state))) {
            protectedBlocks = protectedBlocks.copy();
            protectedBlocks.putString(key, Rules.blockId(state));
            ((PlacementData)chunk).arachnidguard$setPlacements(protectedBlocks);
            chunk.setUnsaved(true);
        } else if (protectedBlocks.contains(key) && !isProtected(chunk, pos, state)) {
            protectedBlocks = protectedBlocks.copy();
            protectedBlocks.remove(key);
            ((PlacementData)chunk).arachnidguard$setPlacements(protectedBlocks);
            chunk.setUnsaved(true);
        }
    }
}
