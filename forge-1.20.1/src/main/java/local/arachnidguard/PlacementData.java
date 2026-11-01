package local.arachnidguard;
import net.minecraft.nbt.CompoundTag;

/** Implemented on full chunks; serialized with the chunk rather than kept in a global map. */
public interface PlacementData {
    CompoundTag arachnidguard$getPlacements();
    void arachnidguard$setPlacements(CompoundTag data);
}
