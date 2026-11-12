package nimble.portable_blueprints.mod_compatibility.projecte;

import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

/** NeoForge ProjectE integration is explicitly disabled on Fabric. */
public final class ProjectEMetodi {
    public static long getEMCPlayer(UUID player) { return 0L; }
    public static long getEMCForItem(Item item) { return 0L; }
    public static boolean removeEMCForPlayer(ServerPlayer player, Long emcValue) { return false; }
    public static boolean removeEMCForItem(ServerPlayer player, Item item) { return false; }
}
