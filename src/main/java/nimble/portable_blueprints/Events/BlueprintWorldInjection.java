package nimble.portable_blueprints.Events;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.fabricmc.loader.api.FabricLoader;

public class BlueprintWorldInjection {
   private static final Gson GSON = nimble.portable_blueprints.fabric.BlueprintJson.create();
   private static final String FolderAssetPath = "config/portable_blueprints/world_injection_worn_blueprints";

   public static void onServerStarted(MinecraftServer server) {
      Path worldDir = server.getWorldPath(LevelResource.ROOT);
      Path wornBlueprintWorldDir = worldDir.resolve("portable_blueprints/worn blueprints");
      Path modpackBpSourceDir = FabricLoader.getInstance().getGameDir().resolve("config/portable_blueprints/world_injection_worn_blueprints");

      try {
         Files.createDirectories(wornBlueprintWorldDir);
         creaDirectoryConFileSetting();
         BlueprintWorldInjection.WornBlueprintWorldInjectionSettings WBWIS = null;

         try (Reader reader = new FileReader(new File(modpackBpSourceDir.toFile(), "settings.json"))) {
            WBWIS = (BlueprintWorldInjection.WornBlueprintWorldInjectionSettings)GSON.fromJson(
               reader, BlueprintWorldInjection.WornBlueprintWorldInjectionSettings.class
            );
         } catch (Exception e) {
            e.printStackTrace();
         }

         File[] WorldBpFiles = wornBlueprintWorldDir.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".bp"));
         if (WBWIS != null && WorldBpFiles != null) {
            for (File file : WorldBpFiles) {
               try {
                  if (WBWIS.WornBlueprintToRemove.contains(file.getName())) {
                     file.delete();
                  }
               } catch (Exception e) {
                  e.printStackTrace();
               }
            }
         }

         try (Stream<Path> files = Files.walk(modpackBpSourceDir)) {
            files.filter(x$0 -> Files.isRegularFile(x$0)).filter(f -> f.toString().endsWith(".bp")).forEach(f -> {
               Path target = wornBlueprintWorldDir.resolve(f.getFileName());

               try {
                  Files.copy(f, target, StandardCopyOption.REPLACE_EXISTING);
               } catch (IOException e) {
                  e.printStackTrace();
               }
            });
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public static void creaDirectoryConFileSetting() {
      Path modpackBpSourceDir = FabricLoader.getInstance().getGameDir().resolve("config/portable_blueprints/world_injection_worn_blueprints");

      try {
         Files.createDirectories(modpackBpSourceDir);
         File wornBlueprintWorldInjectionSettings = new File(modpackBpSourceDir.toFile(), "settings.json");
         if (!wornBlueprintWorldInjectionSettings.exists()) {
            try (FileWriter writer = new FileWriter(wornBlueprintWorldInjectionSettings)) {
               writer.write("//Introduction to using World Injection Worn Blueprints:\n");
               writer.write("//Place all your .bp files in this folder to have them automatically loaded into the world when a player enters it.\n");
               writer.write(
                  "//Using the WornBlueprintToRemove parameter, you can specify a list of .bp filenames to remove from the world (useful if you previously added some you no longer want).\n"
               );
               writer.write("//The names must exactly match the filename of the blueprint to remove. For example: \"house.bp\",\"tower.bp\"\n\n\n\n");
               writer.write("{\n");
               writer.write("  \"WornBlueprintToRemove\": []\n");
               writer.write("}\n");
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public static class WornBlueprintWorldInjectionSettings {
      public List<String> WornBlueprintToRemove = List.of("babba.bp");
   }
}
