package nimble.portable_blueprints.altro;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import nimble.portable_blueprints.portable_blueprints.MOD;

public class BlockBlueprintUsage {
   private static final Gson GSON = new Gson();
   private static Path CBPHWorldDir = null;
   public static List<String> PlayersBlock = new ArrayList<>();

   public static void load(MinecraftServer server) {
      Path worldDir = server.getWorldPath(LevelResource.ROOT);
      CBPHWorldDir = worldDir.resolve("portable_blueprints/general");

      try {
         creaDirectoryConFileSetting();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public static void loadData() {
      try {
         File BlockBanData = new File(CBPHWorldDir.toFile(), "block.data");
         Type type = (new TypeToken<List<UUID>>() {}).getType();

         try (Reader reader = new FileReader(BlockBanData)) {
            PlayersBlock = (List<String>)GSON.fromJson(reader, type);
         } catch (Exception e) {
            e.printStackTrace();
         }
      } catch (Exception var8) {
      }
   }

   public static void saveData() {
      try {
         File BlockBanData = new File(CBPHWorldDir.toFile(), "block.data");

         try (FileWriter writer = new FileWriter(BlockBanData)) {
            GSON.toJson(PlayersBlock, writer);
         } catch (IOException var6) {
         }
      } catch (Exception var7) {
      }
   }

   public static void creaDirectoryConFileSetting() {
      try {
         Files.createDirectories(CBPHWorldDir);
         File BlockBanData = new File(CBPHWorldDir.toFile(), "block.data");
         if (!BlockBanData.exists()) {
            saveData();
         } else {
            loadData();
         }
      } catch (Exception e) {
         MOD.LOGGER.error("[Portable blueprint] -> Create block.data File error:");
         MOD.LOGGER.error(e.getMessage());
      }
   }
}
