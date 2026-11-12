package nimble.portable_blueprints.altro;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.fabricmc.loader.api.FabricLoader;
import nimble.portable_blueprints.portable_blueprints.MOD;

public class CustomBlockProprietyHandle {
   private static final Gson GSON = new Gson();
   private static final String rotateInterno = "/portable_blueprints/settings/blueprint_rotation_settings.json";
   private static final String mirrorInterno = "/portable_blueprints/settings/blueprint_mirror_settings.json";
   private static final String DirUtente = "config/portable_blueprints/custom_block_propriety_handle";

   public static void load(MinecraftServer server) {
      Path worldDir = server.getWorldPath(LevelResource.ROOT);
      Path CBPHWorldDir = worldDir.resolve("portable_blueprints/custom_block_propriety_handle");
      Path modpackCBPHSourceDir = FabricLoader.getInstance().getGameDir().resolve("config/portable_blueprints/custom_block_propriety_handle");
      creaDirectoryConFileSetting();
      creaDirectoryConFileSettingWorld(CBPHWorldDir);

      try {
         Files.createDirectories(CBPHWorldDir);
         File[] WorldCBPHFiles = CBPHWorldDir.toFile().listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

         for (File file : WorldCBPHFiles) {
            try {
               if (file.getName().equals("rotation_settings.json") || file.getName().equals("mirror_settings.json")) {
                  file.delete();
               }
            } catch (Exception e) {
               e.printStackTrace();
            }
         }

         try (Stream<Path> files = Files.walk(modpackCBPHSourceDir)) {
            files.filter(x$0 -> Files.isRegularFile(x$0)).filter(f -> f.toString().endsWith(".json")).forEach(f -> {
               Path target = CBPHWorldDir.resolve(f.getFileName());

               try {
                  Files.copy(f, target, StandardCopyOption.REPLACE_EXISTING);
               } catch (IOException e) {
                  e.printStackTrace();
               }
            });
         }
      } catch (Exception e) {
         e.printStackTrace();
      }

      Map<String, List<CustomBlockProprietyHandle.Rotate.Rule>> modRules = new HashMap<>();

      try (InputStream in = CustomBlockProprietyHandle.class.getResourceAsStream("/portable_blueprints/settings/blueprint_rotation_settings.json")) {
         if (in == null) {
            throw new IOException("Internal Rotation Settings File not found!");
         }

         try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            Type type = (new TypeToken<Map<String, List<CustomBlockProprietyHandle.Rotate.Rule>>>() {}).getType();
            modRules = (Map<String, List<CustomBlockProprietyHandle.Rotate.Rule>>)GSON.fromJson(reader, type);
         } catch (Exception e) {
            e.printStackTrace();
         }
      } catch (Exception e) {
         MOD.LOGGER.error("[Portable blueprint] -> rotation_settings.json external File error:");
         e.printStackTrace();
      }

      Map<String, List<CustomBlockProprietyHandle.Rotate.Rule>> userRules = new HashMap<>();
      File userFile = CBPHWorldDir.resolve("rotation_settings.json").toFile();
      if (userFile.exists()) {
         try (BufferedReader reader = new BufferedReader(new FileReader(userFile, StandardCharsets.UTF_8))) {
            Type type = (new TypeToken<Map<String, List<CustomBlockProprietyHandle.Rotate.Rule>>>() {}).getType();
            userRules = (Map<String, List<CustomBlockProprietyHandle.Rotate.Rule>>)GSON.fromJson(reader, type);
         } catch (Exception e) {
            MOD.LOGGER.error("[Portable blueprint] -> mirror_settings.json external File error:");
            e.printStackTrace();
         }
      }

      CustomBlockProprietyHandle.Rotate.RULES = modRules;
      if (userRules != null && !userRules.isEmpty()) {
         for (String key : userRules.keySet()) {
            if (CustomBlockProprietyHandle.Rotate.RULES.containsKey(key)) {
               CustomBlockProprietyHandle.Rotate.RULES.replace(key, userRules.get(key));
            } else {
               CustomBlockProprietyHandle.Rotate.RULES.put(key, userRules.get(key));
            }
         }
      }

      Map<String, List<CustomBlockProprietyHandle.Mirror.Rule>> modmirrorRules = new HashMap<>();

      try (InputStream in = CustomBlockProprietyHandle.class.getResourceAsStream("/portable_blueprints/settings/blueprint_mirror_settings.json")) {
         if (in == null) {
            throw new IOException("Internal mirror Settings File not found!");
         }

         try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            Type type = (new TypeToken<Map<String, List<CustomBlockProprietyHandle.Mirror.Rule>>>() {}).getType();
            modmirrorRules = (Map<String, List<CustomBlockProprietyHandle.Mirror.Rule>>)GSON.fromJson(reader, type);
         }
      } catch (Exception e) {
         e.printStackTrace();
      }

      Map<String, List<CustomBlockProprietyHandle.Mirror.Rule>> usermirrorRules = new HashMap<>();
      File usermirrorFile = CBPHWorldDir.resolve("mirror_settings.json").toFile();
      if (usermirrorFile.exists()) {
         try (BufferedReader reader = new BufferedReader(new FileReader(usermirrorFile, StandardCharsets.UTF_8))) {
            Type type = (new TypeToken<Map<String, List<CustomBlockProprietyHandle.Mirror.Rule>>>() {}).getType();
            usermirrorRules = (Map<String, List<CustomBlockProprietyHandle.Mirror.Rule>>)GSON.fromJson(reader, type);
         } catch (Exception e) {
            e.printStackTrace();
         }
      }

      CustomBlockProprietyHandle.Mirror.RULES = modmirrorRules;
      if (usermirrorRules != null && !usermirrorRules.isEmpty()) {
         for (String key : usermirrorRules.keySet()) {
            if (CustomBlockProprietyHandle.Mirror.RULES.containsKey(key)) {
               CustomBlockProprietyHandle.Mirror.RULES.replace(key, usermirrorRules.get(key));
            } else {
               CustomBlockProprietyHandle.Mirror.RULES.put(key, usermirrorRules.get(key));
            }
         }
      }
   }

   public static void creaDirectoryConFileSetting() {
      Path modpackBpSourceDir = FabricLoader.getInstance().getGameDir().resolve("config/portable_blueprints/custom_block_propriety_handle");

      try {
         Files.createDirectories(modpackBpSourceDir);
         File WorldCustomBlockProprietyHandleRotation = new File(modpackBpSourceDir.toFile(), "rotation_settings.json");
         if (!WorldCustomBlockProprietyHandleRotation.exists()) {
            try (FileWriter writer = new FileWriter(WorldCustomBlockProprietyHandleRotation)) {
               writer.write("//Introduction to using Custom Block Property Handle Rotation:\n");
               writer.write("//Here you can modify the final result of any blocks you want during blueprint rotation.\n");
               writer.write("//Warning: once you add any block here, it will no longer be affected by the automatic rotation handled by the mod!\n");
               writer.write(
                  "//The mod already includes an internal file like this, but any block you define here will override the internal one and take priority. (Priority order is always: user settings > mod default)\n"
               );
               writer.write(
                  "//For details on how to use this file, please refer to the wiki on the mod’s GitHub: https://github.com/Nimblonia/Mod_Portable_blueprint_Community/discussions/17\n"
               );
               writer.write("{}");
            } catch (IOException e) {
               MOD.LOGGER.error("[Portable blueprint] -> Create rotation_settings.json File error:");
               MOD.LOGGER.error(e.getMessage());
            }
         }
      } catch (Exception e) {
         MOD.LOGGER.error("[Portable blueprint] -> Create rotation_settings.json File error:");
         MOD.LOGGER.error(e.getMessage());
      }

      try {
         Files.createDirectories(modpackBpSourceDir);
         File WorldCustomBlockProprietyHandlemirror = new File(modpackBpSourceDir.toFile(), "mirror_settings.json");
         if (!WorldCustomBlockProprietyHandlemirror.exists()) {
            try (FileWriter writer = new FileWriter(WorldCustomBlockProprietyHandlemirror)) {
               writer.write("//Introduction to using Custom Block Property Handle mirror:\n");
               writer.write("//Here you can modify the final result of any blocks you want during blueprint XYZ mirroring.\n");
               writer.write("//Warning: once you add any block here, it will no longer be affected by the automatic XYZ mirroring handled by the mod!\n");
               writer.write(
                  "//The mod already includes an internal file like this, but any block you define here will override the internal one and take priority. (Priority order is always: user settings > mod default)\n"
               );
               writer.write(
                  "//For details on how to use this file, please refer to the wiki on the mod’s GitHub: https://github.com/Nimblonia/Mod_Portable_blueprint_Community/discussions/17\n"
               );
               writer.write("{}");
            } catch (IOException e) {
               MOD.LOGGER.error("[Portable blueprint] -> Create mirror_settings.json File error:");
               MOD.LOGGER.error(e.getMessage());
            }
         }
      } catch (Exception e) {
         MOD.LOGGER.error("[Portable blueprint] -> Create mirror_settings.json File error:");
         MOD.LOGGER.error(e.getMessage());
      }
   }

   public static void creaDirectoryConFileSettingWorld(Path CBPHWorldDir) {
      try {
         Files.createDirectories(CBPHWorldDir);
         File WorldCustomBlockProprietyHandleRotation = new File(CBPHWorldDir.toFile(), "rotation_settings.json");
         if (!WorldCustomBlockProprietyHandleRotation.exists()) {
            try (FileWriter writer = new FileWriter(WorldCustomBlockProprietyHandleRotation)) {
               writer.write("//Introduction to using Custom Block Property Handle Rotation:\n");
               writer.write("//Here you can modify the final result of any blocks you want during blueprint rotation.\n");
               writer.write("//Warning: once you add any block here, it will no longer be affected by the automatic rotation handled by the mod!\n");
               writer.write(
                  "//The mod already includes an internal file like this, but any block you define here will override the internal one and take priority. (Priority order is always: user settings > mod default)\n"
               );
               writer.write(
                  "//For details on how to use this file, please refer to the wiki on the mod’s GitHub: https://github.com/Nimblonia/Mod_Portable_blueprint_Community/discussions/17\n"
               );
               writer.write("{}");
            } catch (IOException e) {
               MOD.LOGGER.error("[Portable blueprint] -> Create rotation_settings.json File error:");
               MOD.LOGGER.error(e.getMessage());
            }
         }
      } catch (Exception e) {
         MOD.LOGGER.error("[Portable blueprint] -> Create rotation_settings.json File error:");
         MOD.LOGGER.error(e.getMessage());
      }

      try {
         Files.createDirectories(CBPHWorldDir);
         File WorldCustomBlockProprietyHandlemirror = new File(CBPHWorldDir.toFile(), "mirror_settings.json");
         if (!WorldCustomBlockProprietyHandlemirror.exists()) {
            try (FileWriter writer = new FileWriter(WorldCustomBlockProprietyHandlemirror)) {
               writer.write("//Introduction to using Custom Block Property Handle mirror:\n");
               writer.write("//Here you can modify the final result of any blocks you want during blueprint XYZ mirroring.\n");
               writer.write("//Warning: once you add any block here, it will no longer be affected by the automatic XYZ mirroring handled by the mod!\n");
               writer.write(
                  "//The mod already includes an internal file like this, but any block you define here will override the internal one and take priority. (Priority order is always: user settings > mod default)\n"
               );
               writer.write(
                  "//For details on how to use this file, please refer to the wiki on the mod’s GitHub: https://github.com/Nimblonia/Mod_Portable_blueprint_Community/discussions/17\n"
               );
               writer.write("{}");
            } catch (IOException e) {
               MOD.LOGGER.error("[Portable blueprint] -> Create mirror_settings.json File error:");
               MOD.LOGGER.error(e.getMessage());
            }
         }
      } catch (Exception e) {
         MOD.LOGGER.error("[Portable blueprint] -> Create mirror_settings.json File error:");
         MOD.LOGGER.error(e.getMessage());
      }
   }

   public static class Mirror {
      public static Map<String, List<CustomBlockProprietyHandle.Mirror.Rule>> RULES = new HashMap<>();

      public static class PropertyRule {
         public String condition;
         public List<CustomBlockProprietyHandle.Mirror.PropertyValue> result = new ArrayList<>();
      }

      public static class PropertyValue {
         public String name;
         public String value;
      }

      public static class Rule {
         public Axis mirror;
         public List<CustomBlockProprietyHandle.Mirror.PropertyRule> properties;
      }
   }

   public static class Rotate {
      public static Map<String, List<CustomBlockProprietyHandle.Rotate.Rule>> RULES = new HashMap<>();

      public static class PropertyRule {
         public String condition;
         public List<CustomBlockProprietyHandle.Rotate.PropertyValue> result = new ArrayList<>();
      }

      public static class PropertyValue {
         public String name;
         public String value;
      }

      public static class Rule {
         public short rotation;
         public List<CustomBlockProprietyHandle.Rotate.PropertyRule> properties;

         public short getRot() {
            return this.rotation;
         }
      }
   }
}
