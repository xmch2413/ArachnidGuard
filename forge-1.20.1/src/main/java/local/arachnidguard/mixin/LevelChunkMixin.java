package local.arachnidguard.mixin;
import local.arachnidguard.PlayerPlacement;
import local.arachnidguard.Rules;
import local.arachnidguard.PlacementData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin implements PlacementData {
    @Unique private CompoundTag arachnidguard$placements = new CompoundTag();
    @Override public CompoundTag arachnidguard$getPlacements() { return arachnidguard$placements; }
    @Override public void arachnidguard$setPlacements(CompoundTag data) { arachnidguard$placements = data; }
    @ModifyVariable(method = "setBlockState", at = @At("HEAD"), argsOnly = true)
    private BlockState arachnidguard$filterWeb(BlockState incoming, BlockPos pos, BlockState original, boolean moving) {
        LevelChunk chunk = (LevelChunk)(Object)this;
        if (!chunk.getLevel().isClientSide && Rules.blocksState(incoming)
            && !PlayerPlacement.isActive(chunk.getLevel(), pos)
            && !PlayerPlacement.isProtected(chunk, pos, incoming)) return Blocks.AIR.defaultBlockState();
        return incoming;
    }
    @Inject(method = "setBlockState", at = @At("RETURN"))
    private void arachnidguard$trackPlacement(BlockPos pos, BlockState state, boolean moving, CallbackInfoReturnable<BlockState> cir) {
        if (cir.getReturnValue() != null) PlayerPlacement.afterWrite((LevelChunk)(Object)this, pos, state);
    }
}
