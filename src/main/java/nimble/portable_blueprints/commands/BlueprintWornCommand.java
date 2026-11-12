package nimble.portable_blueprints.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import nimble.portable_blueprints.altro.Blueprint;
import nimble.portable_blueprints.init.ModItems;
import nimble.portable_blueprints.portable_blueprints.OldVersionUtilities;

public class BlueprintWornCommand {

   public static void onCommandRegister(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("blueprintworn")
                  .requires(source -> source.hasPermission(2)))
               .then(Commands.literal("add").executes(ctx -> {
                  setWornBlueprint(((CommandSourceStack)ctx.getSource()).getLevel(), ((CommandSourceStack)ctx.getSource()).getPlayer());
                  return 1;
               })))
            .then(Commands.literal("remove").then(Commands.argument("param", StringArgumentType.word()).suggests((ctx, builder) -> {
               for (String perm : blueprintEsistenti(((CommandSourceStack)ctx.getSource()).getLevel())) {
                  builder.suggest(perm);
               }

               return builder.buildFuture();
            }).executes(ctx -> {
               String param = StringArgumentType.getString(ctx, "param");
               removeWornBlueprint(((CommandSourceStack)ctx.getSource()).getLevel(), ((CommandSourceStack)ctx.getSource()).getPlayer(), param);
               return 1;
            })))
      );
   }

   private static void setWornBlueprint(ServerLevel level, ServerPlayer player) {
      if (player != null) {
         if (player.getMainHandItem().getItem().equals(ModItems.BLUEPRINT.get())) {
            CompoundTag nbt = OldVersionUtilities.getItemStackNBT(player.getMainHandItem());
            if (nbt.contains("owner") && nbt.contains("nome")) {
               if (nbt.getString("nome").contains("[Global]")) {
                  if (player != null) {
                     player.sendSystemMessage(
                        Component.translatable("command.portable_blueprints.wornblueprint.set_global_error").withStyle(ChatFormatting.RED), false
                     );
                  }

                  return;
               }

               boolean successo = addWornBlueprint(level, nbt.getString("nome"), nbt.getString("owner"));
               if (!successo) {
                  if (player != null) {
                     player.sendSystemMessage(
                        Component.translatable("command.portable_blueprints.wornblueprint.not_found").withStyle(ChatFormatting.RED), false
                     );
                  }

                  return;
               }

               if (player != null) {
                  player.sendSystemMessage(
                     Component.translatable("command.portable_blueprints.wornblueprint.set_success").withStyle(ChatFormatting.GREEN), false
                  );
               }
            } else if (player != null) {
               player.sendSystemMessage(Component.translatable("command.portable_blueprints.wornblueprint.not_valid_item").withStyle(ChatFormatting.RED), false);
            }
         } else if (player != null) {
            player.sendSystemMessage(Component.translatable("command.portable_blueprints.wornblueprint.no_item").withStyle(ChatFormatting.RED), false);
         }
      }
   }

   private static boolean addWornBlueprint(ServerLevel level, String nome, String owner) {
      File folderWornBlueprints = new File(Blueprint.getWornBlueprintFile(level), nome + ".bp");
      File original = null;

      for (File file : Blueprint.getFileForBlueprints(level, UUID.fromString(owner)).listFiles()) {
         if (file.getName().equals(nome + ".bp")) {
            original = file;
            break;
         }
      }

      if (original != null) {
         try {
            Files.copy(original.toPath(), folderWornBlueprints.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
         } catch (IOException e) {
            e.printStackTrace();
            return false;
         }
      } else {
         return false;
      }
   }

   private static void removeWornBlueprint(ServerLevel level, ServerPlayer player, String nome) {
      File blueprintFile = null;

      for (File file : Blueprint.getWornBlueprintFile(level).listFiles()) {
         if (file.getName().equals(nome + ".bp")) {
            blueprintFile = file;
            break;
         }
      }

      if (blueprintFile != null) {
         if (blueprintFile.delete()) {
            if (player != null) {
               player.sendSystemMessage(
                  Component.translatable("command.portable_blueprints.wornblueprint.delete_success").withStyle(ChatFormatting.GREEN), false
               );
            }
         } else if (player != null) {
            player.sendSystemMessage(Component.translatable("command.portable_blueprints.wornblueprint.delete_error").withStyle(ChatFormatting.RED), false);
         }
      } else if (player != null) {
         player.sendSystemMessage(Component.translatable("command.portable_blueprints.wornblueprint.not_found").withStyle(ChatFormatting.RED), false);
      }
   }

   private static List<String> blueprintEsistenti(ServerLevel level) {
      List<String> nomi = new ArrayList<>();

      for (File file : Blueprint.getWornBlueprintFile(level).listFiles()) {
         nomi.add(file.getName().replace(".bp", ""));
      }

      return nomi;
   }
}
