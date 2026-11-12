package nimble.portable_blueprints.init;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Tuple;
import net.fabricmc.loader.api.FabricLoader;

import nimble.portable_blueprints.portable_blueprints.MOD;

public class TabletClientDatas {
   private static final Gson GSON = new Gson();
   private static final Path dir = FabricLoader.getInstance().getGameDir().resolve("portable_blueprints/tablet_data");
   private static TabletClientDatas.Datas datas = new TabletClientDatas.Datas();
   private static TabletClientDatas.Settings settings = new TabletClientDatas.Settings();

   public static void onClientJoinWorld() {
      load();
   }

   public static void save() {
      try {
         Files.createDirectories(dir);
         File file = new File(dir.toUri());
         file = new File(file, getCurrentWorldOrServerId() + ".data");

         try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(datas, writer);
         } catch (IOException var10) {
         }
      } catch (Exception e) {
         MOD.printLoggerError("Can't create tablet data file `" + getCurrentWorldOrServerId() + "`!");
      }

      try {
         Files.createDirectories(dir);
         File file = new File(dir.toUri());
         file = new File(file, "tablet_settings.data");

         try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(settings, writer);
         } catch (IOException var6) {
         }
      } catch (Exception e) {
         MOD.printLoggerError("Can't create tablet settings data file!");
      }
   }

   public static void load() {
      try {
         Files.createDirectories(dir);
         File file = new File(dir.toUri());
         file = new File(file, getCurrentWorldOrServerId() + ".data");
         if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
               datas = (TabletClientDatas.Datas)GSON.fromJson(reader, TabletClientDatas.Datas.class);
            } catch (Exception e) {
               e.printStackTrace();
            }
         } else {
            datas = new TabletClientDatas.Datas();
            save();
         }
      } catch (Exception e) {
         MOD.printLoggerError("Can't load tablet data file `" + getCurrentWorldOrServerId() + "`!");
      }

      try {
         Files.createDirectories(dir);
         File file = new File(dir.toUri());
         file = new File(file, "tablet_settings.data");
         if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
               settings = (TabletClientDatas.Settings)GSON.fromJson(reader, TabletClientDatas.Settings.class);
            } catch (Exception e) {
               e.printStackTrace();
            }
         } else {
            settings = new TabletClientDatas.Settings();
            save();
         }
      } catch (Exception e) {
         MOD.printLoggerError("Can't load tablet settings data file!");
      }
   }

   public static String getCurrentWorldOrServerId() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.isLocalServer()) {
         MinecraftServer server = mc.getSingleplayerServer();
         if (server != null) {
            String levelName = server.getWorldData().getLevelName();
            String safeLevelName = levelName.toLowerCase().replaceAll("[^a-z0-9_-]", "_");
            String uniqueId = Integer.toHexString(levelName.hashCode());
            return "world_" + safeLevelName + "_" + uniqueId;
         }
      }

      ServerData data = mc.getCurrentServer();
      if (data != null) {
         String safeName = data.name.toLowerCase().replaceAll("[^a-z0-9_-]", "_");
         String ipHash = Integer.toHexString(data.ip.hashCode());
         return "server_" + safeName + "_" + ipHash;
      } else {
         return "unknown_world";
      }
   }

   public static TabletClientDatas.Datas getDatas() {
      if (datas == null) {
         datas = new TabletClientDatas.Datas();
      }

      return datas;
   }

   public static TabletClientDatas.Settings getSettings() {
      if (settings == null) {
         settings = new TabletClientDatas.Settings();
      }

      return settings;
   }

   public static void setDatas(TabletClientDatas.Datas data) {
      datas = data;
   }

   public static void setSettings(TabletClientDatas.Settings setting) {
      settings = setting;
   }

   public static class Datas {
      public List<Tuple<String, UUID>> followedData = new ArrayList<>();
   }

   public static class Settings {
      public short selectedBG = 1;
   }
}
