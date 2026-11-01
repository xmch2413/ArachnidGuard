package local.arachnidguard;
import net.minecraft.nbt.*;

public final class SpawnerRules {
    private SpawnerRules() {}
    public static boolean containsBlockedEntity(Tag tag) { return search(tag, 0); }
    private static boolean search(Tag tag, int depth) {
        if (tag == null || depth > 32) return false;
        if (tag instanceof StringTag text) {
            String value = text.getAsString();
            return value.matches("[a-z0-9_.-]+:[a-z0-9_./-]+") && Rules.blocksEntityId(value);
        }
        if (tag instanceof CompoundTag compound) {
            for (String key : compound.getAllKeys()) {
                if (key.equals("CustomName") || key.equals("ForgeCaps") || key.equals("ForgeData")) continue;
                if (search(compound.get(key), depth + 1)) return true;
            }
        } else if (tag instanceof ListTag list) {
            for (Tag child : list) if (search(child, depth + 1)) return true;
        }
        return false;
    }
}
