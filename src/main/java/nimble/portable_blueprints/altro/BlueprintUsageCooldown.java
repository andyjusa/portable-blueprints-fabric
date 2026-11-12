package nimble.portable_blueprints.altro;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules.IntegerValue;
import nimble.portable_blueprints.init.ModGameRules;

public class BlueprintUsageCooldown {
   private static Map<UUID, Long> inCooldown = new HashMap<>();

   public static void setInCooldown(ServerLevel serverLevel, UUID player) {
      inCooldown.put(player, System.currentTimeMillis() + ((IntegerValue)serverLevel.getGameRules().getRule(ModGameRules.BlueprintsCooldown)).get() * 1000L);
   }

   public static int hasCooldown(UUID player) {
      if (inCooldown.containsKey(player)) {
         int val = (int)(inCooldown.get(player) - System.currentTimeMillis()) / 1000;
         if (val <= 0) {
            inCooldown.remove(player);
            return 0;
         } else {
            return val;
         }
      } else {
         return 0;
      }
   }

   public static void reset() {
      inCooldown.clear();
   }
}
