package nimble.portable_blueprints.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import nimble.portable_blueprints.altro.Blueprint;
import nimble.portable_blueprints.init.ModItems;
import nimble.portable_blueprints.portable_blueprints.OldVersionUtilities;

public class BlueprintServerCommand {
   private static final Gson GSON = nimble.portable_blueprints.fabric.BlueprintJson.create();

   public static void onCommandRegister(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("blueprintserver")
                     .requires(source -> source.hasPermission(2)))
                  .then(((LiteralArgumentBuilder)Commands.literal("set").then(Commands.literal("free").executes(ctx -> {
                     setServerBlueprint(((CommandSourceStack)ctx.getSource()).getLevel(), true, ((CommandSourceStack)ctx.getSource()).getPlayer());
                     return 1;
                  }))).then(Commands.literal("permission").executes(ctx -> {
                     setServerBlueprint(((CommandSourceStack)ctx.getSource()).getLevel(), false, ((CommandSourceStack)ctx.getSource()).getPlayer());
                     return 1;
                  }))))
               .then(Commands.literal("remove").then(Commands.argument("param", StringArgumentType.word()).suggests((ctx, builder) -> {
                  for (String perm : blueprintEsistenti(((CommandSourceStack)ctx.getSource()).getLevel())) {
                     builder.suggest(perm);
                  }

                  return builder.buildFuture();
               }).executes(ctx -> {
                  String param = StringArgumentType.getString(ctx, "param");
                  removeServerBlueprint(((CommandSourceStack)ctx.getSource()).getLevel(), ((CommandSourceStack)ctx.getSource()).getPlayer(), param);
                  return 1;
               }))))
            .then(
               Commands.literal("permission")
                  .then(
                     Commands.argument("player", EntityArgument.player())
                        .then(
                           Commands.argument("blueprint", StringArgumentType.word())
                              .suggests((ctx, builder) -> {
                                 for (String perm : blueprintEsistenti(((CommandSourceStack)ctx.getSource()).getLevel())) {
                                    builder.suggest(perm);
                                 }

                                 return builder.buildFuture();
                              })
                              .then(
                                 Commands.argument("allowed", BoolArgumentType.bool())
                                    .executes(
                                       ctx -> {
                                          ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                          String permission = StringArgumentType.getString(ctx, "blueprint");
                                          boolean allowed = BoolArgumentType.getBool(ctx, "allowed");
                                          setPlayerPermissionForServerBlueprint(
                                             ((CommandSourceStack)ctx.getSource()).getLevel(),
                                             ((CommandSourceStack)ctx.getSource()).getPlayer(),
                                             player,
                                             permission,
                                             allowed
                                          );
                                          return 1;
                                       }
                                    )
                              )
                        )
                  )
            )
      );
   }

   private static void setServerBlueprint(ServerLevel level, boolean isFree, ServerPlayer player) {
      if (player != null) {
         if (player.getMainHandItem().getItem().equals(ModItems.BLUEPRINT.get())) {
            CompoundTag nbt = OldVersionUtilities.getItemStackNBT(player.getMainHandItem());
            if (nbt.contains("owner") && nbt.contains("nome")) {
               if (nbt.getString("nome").contains("[Global]")) {
                  if (player != null) {
                     player.sendSystemMessage(
                        Component.translatable("command.portable_blueprints.serverblueprint.set_global_error").withStyle(ChatFormatting.RED), false
                     );
                  }

                  return;
               }

               boolean successo = addServerBlueprint(level, nbt.getString("nome"), nbt.getString("owner"));
               if (!successo) {
                  if (player != null) {
                     player.sendSystemMessage(
                        Component.translatable("command.portable_blueprints.serverblueprint.not_found").withStyle(ChatFormatting.RED), false
                     );
                  }

                  return;
               }

               addBlueprintPermission(level, nbt.getString("nome"), isFree);
               if (player != null) {
                  player.sendSystemMessage(
                     Component.translatable("command.portable_blueprints.serverblueprint.set_success").withStyle(ChatFormatting.GREEN), false
                  );
               }
            } else if (player != null) {
               player.sendSystemMessage(
                  Component.translatable("command.portable_blueprints.serverblueprint.not_valid_item").withStyle(ChatFormatting.RED), false
               );
            }
         } else if (player != null) {
            player.sendSystemMessage(Component.translatable("command.portable_blueprints.serverblueprint.no_item").withStyle(ChatFormatting.RED), false);
         }
      }
   }

   private static boolean addServerBlueprint(ServerLevel level, String nome, String owner) {
      File folderServerBlueprints = new File(Blueprint.getServerBlueprintFile(level), nome + ".bp");
      File original = null;

      for (File file : Blueprint.getFileForBlueprints(level, UUID.fromString(owner)).listFiles()) {
         if (file.getName().equals(nome + ".bp")) {
            original = file;
            break;
         }
      }

      if (original != null) {
         try {
            Files.copy(original.toPath(), folderServerBlueprints.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
         } catch (IOException e) {
            e.printStackTrace();
            return false;
         }
      } else {
         return false;
      }
   }

   private static void addBlueprintPermission(ServerLevel level, String nome, boolean isfree) {
      try {
         File permissionFile = Blueprint.getServerPremissionFile(level);
         Blueprint.BlueprintPermission bp = null;

         try (Reader reader = new FileReader(permissionFile)) {
            bp = (Blueprint.BlueprintPermission)GSON.fromJson(reader, Blueprint.BlueprintPermission.class);
         } catch (Exception e) {
            e.printStackTrace();
            return;
         }

         bp.addServerBlueprintPermission(nome, isfree);

         try (FileWriter writer = new FileWriter(permissionFile)) {
            GSON.toJson(bp, writer);
         } catch (IOException e) {
            e.printStackTrace();
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   private static void removeServerBlueprint(ServerLevel level, ServerPlayer player, String nome) {
      File blueprintFile = null;

      for (File file : Blueprint.getServerBlueprintFile(level).listFiles()) {
         if (file.getName().equals(nome + ".bp")) {
            blueprintFile = file;
            break;
         }
      }

      if (blueprintFile != null) {
         if (blueprintFile.delete()) {
            try {
               File permissionFile = Blueprint.getServerPremissionFile(level);
               Blueprint.BlueprintPermission bp = null;

               try (Reader reader = new FileReader(permissionFile)) {
                  bp = (Blueprint.BlueprintPermission)GSON.fromJson(reader, Blueprint.BlueprintPermission.class);
               } catch (Exception e) {
                  e.printStackTrace();
                  return;
               }

               bp.removeServerBlueprintPermission(nome);

               try (FileWriter writer = new FileWriter(permissionFile)) {
                  GSON.toJson(bp, writer);
               } catch (IOException e) {
                  e.printStackTrace();
               }
            } catch (Exception e) {
               e.printStackTrace();
            }

            if (player != null) {
               player.sendSystemMessage(
                  Component.translatable("command.portable_blueprints.serverblueprint.delete_success").withStyle(ChatFormatting.GREEN), false
               );
            }
         } else if (player != null) {
            player.sendSystemMessage(Component.translatable("command.portable_blueprints.serverblueprint.delete_error").withStyle(ChatFormatting.RED), false);
         }
      } else if (player != null) {
         player.sendSystemMessage(Component.translatable("command.portable_blueprints.serverblueprint.not_found").withStyle(ChatFormatting.RED), false);
      }
   }

   private static List<String> blueprintEsistenti(ServerLevel level) {
      List<String> nomi = new ArrayList<>();

      for (File file : Blueprint.getServerBlueprintFile(level).listFiles()) {
         nomi.add(file.getName().replace(".bp", ""));
      }

      return nomi;
   }

   private static void setPlayerPermissionForServerBlueprint(ServerLevel level, ServerPlayer player, ServerPlayer target, String nome, boolean hasPermission) {
      try {
         File permissionFile = Blueprint.getServerPremissionFile(level);
         Blueprint.BlueprintPermission bp = null;

         try (Reader reader = new FileReader(permissionFile)) {
            bp = (Blueprint.BlueprintPermission)GSON.fromJson(reader, Blueprint.BlueprintPermission.class);
         } catch (Exception e) {
            e.printStackTrace();
            return;
         }

         bp.setPermissionForPlayer(target, nome, hasPermission);

         try (FileWriter writer = new FileWriter(permissionFile)) {
            GSON.toJson(bp, writer);
            if (player != null) {
               player.sendSystemMessage(
                  Component.translatable("command.portable_blueprints.serverblueprint.set_player_permission_success").withStyle(ChatFormatting.GREEN), false
               );
            }
         } catch (IOException e) {
            e.printStackTrace();
            if (player != null) {
               player.sendSystemMessage(
                  Component.translatable("command.portable_blueprints.serverblueprint.set_player_permission_error").withStyle(ChatFormatting.RED), false
               );
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
         if (player != null) {
            player.sendSystemMessage(
               Component.translatable("command.portable_blueprints.serverblueprint.set_player_permission_error").withStyle(ChatFormatting.RED), false
            );
         }
      }
   }
}
