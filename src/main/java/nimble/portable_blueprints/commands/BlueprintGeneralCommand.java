package nimble.portable_blueprints.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import nimble.portable_blueprints.altro.BlockBlueprintUsage;

public class BlueprintGeneralCommand {

   public static void onCommandRegister(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("portable_blueprint")
                     .requires(source -> source.hasPermission(2)))
                  .then(Commands.literal("blockedlist").executes(ctx -> {
                     BlockListPlayer(((CommandSourceStack)ctx.getSource()).getLevel(), ((CommandSourceStack)ctx.getSource()).getPlayer());
                     return 1;
                  })))
               .then(Commands.literal("block").then(Commands.argument("param", StringArgumentType.word()).executes(ctx -> {
                  String param = StringArgumentType.getString(ctx, "param");
                  BlockPlayer(((CommandSourceStack)ctx.getSource()).getLevel(), ((CommandSourceStack)ctx.getSource()).getPlayer(), param);
                  return 1;
               }))))
            .then(Commands.literal("unblock").then(Commands.argument("param", StringArgumentType.word()).executes(ctx -> {
               String param = StringArgumentType.getString(ctx, "param");
               UnBlockPlayer(((CommandSourceStack)ctx.getSource()).getLevel(), ((CommandSourceStack)ctx.getSource()).getPlayer(), param);
               return 1;
            })))
      );
   }

   public static void BlockPlayer(ServerLevel level, ServerPlayer Player, String BlockPlayerName) {
      if (!BlockBlueprintUsage.PlayersBlock.contains(BlockPlayerName)) {
         BlockBlueprintUsage.PlayersBlock.add(BlockPlayerName);
         BlockBlueprintUsage.saveData();
         if (Player != null) {
            Player.sendSystemMessage(
               Component.literal(Component.translatable("command.portable_blueprints.block.block_ok").getString().replace("%v%", BlockPlayerName))
                  .withStyle(ChatFormatting.GREEN),
               false
            );
         }
      } else if (Player != null) {
         Player.sendSystemMessage(
            Component.literal(Component.translatable("command.portable_blueprints.block.block_error").getString().replace("%v%", BlockPlayerName))
               .withStyle(ChatFormatting.RED),
            false
         );
      }
   }

   public static void UnBlockPlayer(ServerLevel level, ServerPlayer Player, String BlockPlayerName) {
      if (BlockBlueprintUsage.PlayersBlock.contains(BlockPlayerName)) {
         BlockBlueprintUsage.PlayersBlock.remove(BlockPlayerName);
         BlockBlueprintUsage.saveData();
         if (Player != null) {
            Player.sendSystemMessage(
               Component.literal(Component.translatable("command.portable_blueprints.block.unblock").getString().replace("%v%", BlockPlayerName))
                  .withStyle(ChatFormatting.GREEN),
               false
            );
         }
      } else if (Player != null) {
         Player.sendSystemMessage(
            Component.literal(Component.translatable("command.portable_blueprints.block.unblock_error").getString().replace("%v%", BlockPlayerName))
               .withStyle(ChatFormatting.RED),
            false
         );
      }
   }

   public static void BlockListPlayer(ServerLevel level, ServerPlayer Player) {
      if (Player != null) {
         String pList = "";

         for (int x = 0; x < BlockBlueprintUsage.PlayersBlock.size(); x++) {
            pList = pList + BlockBlueprintUsage.PlayersBlock.get(x);
            if (x != BlockBlueprintUsage.PlayersBlock.size() - 1) {
               pList = pList + ", ";
            }
         }

         Player.sendSystemMessage(
            Component.literal(Component.translatable("command.portable_blueprints.block.list").getString().replace("%v%", pList))
               .withStyle(ChatFormatting.GREEN),
            false
         );
      }
   }
}
