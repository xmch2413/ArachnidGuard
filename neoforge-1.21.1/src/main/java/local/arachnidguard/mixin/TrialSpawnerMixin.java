package local.arachnidguard.mixin;

import local.arachnidguard.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TrialSpawner.class)
public abstract class TrialSpawnerMixin {
    @Unique private long arachnidguard$lastCheck = Long.MIN_VALUE;
    @Unique private Rules.Snapshot arachnidguard$lastRules;

    @Inject(method = "tickServer", at = @At("HEAD"), cancellable = true)
    private void arachnidguard$removeTarget(ServerLevel level, BlockPos pos, boolean ominous, CallbackInfo ci) {
        if (!Rules.current.removeSpawners()) return;
        if (PlayerPlacement.isProtected(level.getChunkAt(pos), pos, level.getBlockState(pos))) return;
        if (arachnidguard$lastRules == Rules.current && level.getGameTime() - arachnidguard$lastCheck < 20) return;
        arachnidguard$lastRules = Rules.current;
        arachnidguard$lastCheck = level.getGameTime();
        var blockEntity = level.getBlockEntity(pos);
        // Serialization includes both normal and ominous spawn pools, plus the next spawn.
        if (blockEntity != null && SpawnerRules.containsBlockedEntity(blockEntity.saveWithoutMetadata(level.registryAccess()))) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            ArachnidGuard.REMOVED_BLOCKS.incrementAndGet();
            ci.cancel();
        }
    }
}
