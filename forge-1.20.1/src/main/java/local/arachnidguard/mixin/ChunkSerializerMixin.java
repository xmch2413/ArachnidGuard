package local.arachnidguard.mixin;
import local.arachnidguard.PlacementData;
import local.arachnidguard.PlayerPlacement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkSerializer.class)
public abstract class ChunkSerializerMixin {
    @Inject(method = "write", at = @At("RETURN"))
    private static void arachnidguard$save(ServerLevel level, ChunkAccess chunk, CallbackInfoReturnable<CompoundTag> cir) {
        ChunkAccess actual = chunk instanceof ImposterProtoChunk proxy ? proxy.getWrapped() : chunk;
        if (actual instanceof PlacementData data && !data.arachnidguard$getPlacements().isEmpty())
            cir.getReturnValue().put(PlayerPlacement.KEY, data.arachnidguard$getPlacements().copy());
    }
    @Inject(method = "read", at = @At("RETURN"))
    private static void arachnidguard$load(ServerLevel level, PoiManager poi, ChunkPos pos, CompoundTag tag,
                                         CallbackInfoReturnable<ProtoChunk> cir) {
        if (cir.getReturnValue() instanceof ImposterProtoChunk proxy)
            ((PlacementData)proxy.getWrapped()).arachnidguard$setPlacements(tag.getCompound(PlayerPlacement.KEY).copy());
    }
}
