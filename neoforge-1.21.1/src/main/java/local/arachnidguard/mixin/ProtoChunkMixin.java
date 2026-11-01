package local.arachnidguard.mixin;
import local.arachnidguard.Rules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
@Mixin(ProtoChunk.class)
public abstract class ProtoChunkMixin {
    @ModifyVariable(method = "setBlockState", at = @At("HEAD"), argsOnly = true)
    private BlockState arachnidguard$filterGeneration(BlockState state) {
        return Rules.blocksState(state) ? Blocks.AIR.defaultBlockState() : state;
    }
}
