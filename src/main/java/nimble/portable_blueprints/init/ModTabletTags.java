package nimble.portable_blueprints.init;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import nimble.portable_blueprints.fabric.network.PacketDistributor;
import nimble.portable_blueprints.network.request_server_tags.CtoS_RequestServerTags;
import nimble.portable_blueprints.portable_blueprints.MOD;

public class ModTabletTags {
   private static final Gson GSON = nimble.portable_blueprints.fabric.BlueprintJson.create();
   public static List<ModTabletTags.TagData> TagDatas = new ArrayList<>();
   public static Map<String, List<String>> BlueprintTagsData = new HashMap<>();

   public static ModTabletTags.TagData getTagByID(String v) {
      for (ModTabletTags.TagData td : TagDatas) {
         if (td.getOriginalTag().equals(v)) {
            return td;
         }
      }

      return null;
   }

   public class ClientSide {

      public static void onClientLogin() {
         try (InputStream is = ModTabletTags.class.getResourceAsStream("/portable_blueprints/settings/tags.json")) {
            if (is == null) {
               throw new IOException("tags.json not found in internal settings");
            }

            try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
               Type listType = (new TypeToken<List<ModTabletTags.TagData>>() {}).getType();
               ModTabletTags.TagDatas = (List<ModTabletTags.TagData>)ModTabletTags.GSON.fromJson(reader, listType);
               System.err.println(ModTabletTags.TagDatas.size());
            }
         } catch (Exception e) {
            MOD.printLoggerError("Can't load Tags from the mod!");
            e.printStackTrace();
         }

         PacketDistributor.sendToServer(new CtoS_RequestServerTags(), new CustomPacketPayload[0]);
      }
   }

   public class ServerSide {

      public static void serverStart(MinecraftServer server) {
            Path worldDir = server.getWorldPath(LevelResource.ROOT);
         File CBPHWorldDir = worldDir.resolve("serverconfig").toFile();

         try {
            CBPHWorldDir.mkdir();
            File server_tags = new File(CBPHWorldDir, "server_tags.json");
            if (!server_tags.exists()) {
               try (FileWriter writer = new FileWriter(server_tags)) {
                  ModTabletTags.GSON.toJson(ModTabletTags.TagDatas, writer);
               } catch (IOException var18) {
               }
            }
         } catch (Exception e) {
            MOD.printLoggerError("Can't load Tags from the world/server!");
         }

         CBPHWorldDir = worldDir.resolve("portable_blueprints/general").toFile();

         try {
            CBPHWorldDir.mkdir();
            File blueprints_tags = new File(CBPHWorldDir, "blueprints_tags.data");
            if (!blueprints_tags.exists()) {
               try (FileWriter writer = new FileWriter(blueprints_tags)) {
                  ModTabletTags.GSON.toJson(ModTabletTags.BlueprintTagsData, writer);
               } catch (IOException var14) {
               }
            } else {
               Type type = (new TypeToken<Map<String, List<String>>>() {}).getType();

               try (Reader reader = new FileReader(blueprints_tags)) {
                  ModTabletTags.BlueprintTagsData = (Map<String, List<String>>)ModTabletTags.GSON.fromJson(reader, type);
               } catch (Exception e) {
                  e.printStackTrace();
               }
            }
         } catch (Exception e) {
            MOD.printLoggerError("Can't load Blueprints Tags from the world/server!");
         }
      }

      public static void onServerSave(MinecraftServer server) {
         Path worldDir = server.getWorldPath(LevelResource.ROOT);
         File blueprints_tags = new File(worldDir.resolve("portable_blueprints/general").toFile(), "blueprints_tags.data");

         try (FileWriter writer = new FileWriter(blueprints_tags)) {
            ModTabletTags.GSON.toJson(ModTabletTags.BlueprintTagsData, writer);
         } catch (IOException var9) {
         }
      }

      public static File getFile(ServerLevel level) {
         MinecraftServer server = level.getServer();
         Path worldDir = server.getWorldPath(LevelResource.ROOT);
         File CBPHWorldDir = worldDir.resolve("serverconfig").toFile();
         return new File(CBPHWorldDir, "server_tags.json");
      }
   }

   public static class TagData {
      public Map<String, String> tag_name = new HashMap<>();

      public TagData() {
      }

      public TagData(Map<String, String> tag_name) {
         this.tag_name = tag_name;
      }

      public String getCurrentLenguageTag() {
         Minecraft mc = Minecraft.getInstance();
         LanguageManager langManager = mc.getLanguageManager();
         String li = langManager.getSelected();
         return this.tag_name.containsKey(li) ? this.tag_name.get(li) : this.tag_name.getOrDefault("en_us", "");
      }

      public String getOriginalTag() {
         return this.tag_name.getOrDefault("en_us", "");
      }
   }
}
