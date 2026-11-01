package local.arachnidguard.mixin;
import local.arachnidguard.PlayerPlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
    @Redirect(method = "placeBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean arachnidguard$allowPlayer(Level level, BlockPos pos, BlockState state, int flags,
                                             BlockPlaceContext context, BlockState placementState) {
        return PlayerPlacement.place(level, pos, state, flags, context.getPlayer() != null);
    }
}
