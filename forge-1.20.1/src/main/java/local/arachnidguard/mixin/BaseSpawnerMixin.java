package local.arachnidguard.mixin;
import local.arachnidguard.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EntityType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(BaseSpawner.class)
public abstract class BaseSpawnerMixin {
    @Unique private long arachnidguard$lastCheck = -20;
    @Unique private Rules.Snapshot arachnidguard$lastRules;
    // Structure data or a spawn egg can change the entity without replacing the block.
    @Inject(method = "load", at = @At("RETURN"))
    private void arachnidguard$loaded(Level level, BlockPos pos, CompoundTag tag, CallbackInfo ci) {
        arachnidguard$lastRules = null;
    }
    @Inject(method = "setEntityId", at = @At("RETURN"))
    private void arachnidguard$changed(EntityType<?> type, Level level, RandomSource random, BlockPos pos, CallbackInfo ci) {
        arachnidguard$lastRules = null;
    }
    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true)
    private void arachnidguard$removeTarget(ServerLevel level, BlockPos pos, CallbackInfo ci) {
        if (!Rules.current.removeSpawners() || !Rules.spawnerState(level.getBlockState(pos))) return;
        if (PlayerPlacement.isProtected(level.getChunkAt(pos), pos, level.getBlockState(pos))) return;
        if (arachnidguard$lastRules == Rules.current && level.getGameTime() - arachnidguard$lastCheck < 20) return;
        arachnidguard$lastRules = Rules.current;
        arachnidguard$lastCheck = level.getGameTime();
        if (SpawnerRules.containsBlockedEntity(((BaseSpawner)(Object)this).save(new CompoundTag()))) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            ci.cancel();
        }
    }
}
