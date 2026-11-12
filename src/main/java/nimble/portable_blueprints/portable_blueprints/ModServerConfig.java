package nimble.portable_blueprints.portable_blueprints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nimble.portable_blueprints.fabric.config.ModConfigSpec;
import nimble.portable_blueprints.fabric.config.ModConfigSpec.BooleanValue;
import nimble.portable_blueprints.fabric.config.ModConfigSpec.Builder;
import nimble.portable_blueprints.fabric.config.ModConfigSpec.ConfigValue;

public class ModServerConfig {
   public static final Builder BUILDER = new Builder();
   public static final ModConfigSpec SPEC = BUILDER.build();
   public static ConfigValue<List<? extends String>> BlacklistBlock = BUILDER.comment(
         "Blacklist of block IDs ignored by the scanner and blueprints (example: [\"minecraft:stone\", \"minecraft:dirt\"] )"
      )
      .defineListAllowEmpty("blacklistBlocks", new ArrayList(), obj -> obj instanceof String);
   public static ConfigValue<Integer> MaxScannerBlockCount = BUILDER.comment("Set the MaxScannerBlockCount gamerule").define("maxscannerblockcount", 30000);
   public static BooleanValue Enable_maxScannerBlockCount = BUILDER.comment("Enable maxScannerBlockCount server config")
      .define("enable_maxscannerblockcount", false);
   public static ConfigValue<String> TABLET_SOCIAL_1 = BUILDER.comment("Add a link to the tablet home page").define("tablet_social_1", "");
   public static ConfigValue<String> TABLET_SOCIAL_2 = BUILDER.comment("Add a link to the tablet home page").define("tablet_social_2", "");
   public static ConfigValue<String> TABLET_SOCIAL_3 = BUILDER.comment("Add a link to the tablet home page").define("tablet_social_3", "");
   public static ConfigValue<String> TABLET_SOCIAL_4 = BUILDER.comment("Add a link to the tablet home page").define("tablet_social_4", "");
   public static ConfigValue<String> TABLET_SOCIAL_5 = BUILDER.comment("Add a link to the tablet home page").define("tablet_social_5", "");
   public static List<String> blacklistBlock = new ArrayList<>();
   public static boolean enable_maxScannerBlockCount = false;
   public static int maxScannerBlockCount = 30000;
   public static String tablet_social_1 = "";
   public static String tablet_social_2 = "";
   public static String tablet_social_3 = "";
   public static String tablet_social_4 = "";
   public static String tablet_social_5 = "";

   public static void load() {
      SPEC.load(net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("portable_blueprints-server.json"));
      reloadData();
   }

   public static void reload() {
      SPEC.reload();
      reloadData();
   }

   public static void reloadData() {
      blacklistBlock = new ArrayList<>((Collection<? extends String>)BlacklistBlock.get());
      enable_maxScannerBlockCount = (Boolean)Enable_maxScannerBlockCount.get();
      maxScannerBlockCount = (Integer)MaxScannerBlockCount.get();
      tablet_social_1 = (String)TABLET_SOCIAL_1.get();
      tablet_social_2 = (String)TABLET_SOCIAL_2.get();
      tablet_social_3 = (String)TABLET_SOCIAL_3.get();
      tablet_social_4 = (String)TABLET_SOCIAL_4.get();
      tablet_social_5 = (String)TABLET_SOCIAL_5.get();
   }

   public static void saveConfigFile() {
      SPEC.save();
      reloadData();
   }
}
