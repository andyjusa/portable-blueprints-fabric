package nimble.portable_blueprints.Events;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.IntegerValue;
import nimble.portable_blueprints.altro.BlueprintUsageCooldown;
import nimble.portable_blueprints.init.ModGameRules;
import nimble.portable_blueprints.network.global_blueprint_net.CtoS_SaveGlobalBlueprintInServer;
import nimble.portable_blueprints.portable_blueprints.ModServerConfig;

public class ServerEvents {

   public static void onPlayerLogout(ServerPlayer player) {
      CtoS_SaveGlobalBlueprintInServer.resetPlayer(player.getUUID());
   }

   public static void onServerStarted(MinecraftServer server) {
      MinecraftServer minecraftServer = server;
      ServerLevel level = minecraftServer.overworld();
      GameRules rules = level.getGameRules();
      if (ModServerConfig.enable_maxScannerBlockCount) {
         ((IntegerValue)rules.getRule(ModGameRules.MaxScannerBlockCount)).set(ModServerConfig.maxScannerBlockCount, minecraftServer);
      }

      BlueprintUsageCooldown.reset();
   }
}
