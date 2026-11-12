package nimble.portable_blueprints.portable_blueprints;

import java.util.List;
import net.minecraft.world.phys.Vec3;
import nimble.portable_blueprints.fabric.config.ModConfigSpec;
import nimble.portable_blueprints.fabric.config.ModConfigSpec.BooleanValue;
import nimble.portable_blueprints.fabric.config.ModConfigSpec.Builder;
import nimble.portable_blueprints.fabric.config.ModConfigSpec.ConfigValue;
import nimble.portable_blueprints.robautile.Operazioni;

public class ModConfig {
   public static final Builder BUILDER = new Builder();
   public static final ModConfigSpec SPEC = BUILDER.build();
   public static ConfigValue<List<? extends String>> ScannerIgnoredBlocks = BUILDER.comment(
         "List of block IDs ignored by the scanner (example: [\"minecraft:stone\", \"minecraft:dirt\"] )"
      )
      .defineListAllowEmpty("scannerIgnoredBlocks", List.of("minecraft:air"), obj -> obj instanceof String);
   public static ConfigValue<String> ScannerAreaColor = BUILDER.comment("Scanner selection area color (default: \"#00FFFF\")")
      .define("scannerAreaColor", "#00FFFF");
   public static ConfigValue<String> ContainerAreaColor = BUILDER.comment("Container selected area color (default: \"#FFFF00\")")
      .define("containerAreaColor", "#FFFF00");
   public static ConfigValue<String> BlueprintAreaColor = BUILDER.comment("Blueprint preview area color (default: \"#0000FF\")")
      .define("blueprintAreaColor", "#0000FF");
   public static BooleanValue ScanningAnimationEnabled = BUILDER.comment("Enable scanner animation when creating a new blueprint (default: true)")
      .define("scanningAnimationEnabled", true);
   public static BooleanValue ScanningAreaTextureEnabled = BUILDER.comment(
         "Enable texture borders in the scanner selected area, disabling it will show only outlines (default: true)"
      )
      .define("scanningAreaTextureEnabled", true);
   public static BooleanValue SelectedContainerAreaTextureEnabled = BUILDER.comment(
         "Enable texture borders in the container area selected by blueprint, disabling it will show only outlines (default: true)"
      )
      .define("selectedContainerAreaTextureEnabled", true);
   public static ConfigValue<Integer> DefaultHologramTransparency = BUILDER.comment("Default hologram transparency value (default: 0 | min: 0 | max: 100)")
      .define("defaultHologramTransparency", 0);
   public static BooleanValue ContainerMessagesEnabled = BUILDER.comment("Enables messages related to containers linked via the blueprint item")
      .define("containerMessagesEnabled", true);
   public static BooleanValue BuildAnimationEnabled = BUILDER.comment("Enable block animations during construction").define("buildAnimationEnabled", true);

   public static Vec3 getScannerAreaColor() {
      String color = (String)ScannerAreaColor.get();
      return color.length() == 7 && color.charAt(0) == '#' ? Operazioni.getColorFromHex(Integer.parseInt(color.replace("#", ""), 16)) : new Vec3(0.0, 0.0, 0.0);
   }

   public static Vec3 getContainerAreaColor() {
      String color = (String)ContainerAreaColor.get();
      return color.length() == 7 && color.charAt(0) == '#' ? Operazioni.getColorFromHex(Integer.parseInt(color.replace("#", ""), 16)) : new Vec3(0.0, 0.0, 0.0);
   }

   public static Vec3 getBlueprintAreaColor() {
      String color = (String)BlueprintAreaColor.get();
      return color.length() == 7 && color.charAt(0) == '#' ? Operazioni.getColorFromHex(Integer.parseInt(color.replace("#", ""), 16)) : new Vec3(0.0, 0.0, 0.0);
   }

   public static float getDefaultHologramTransparency() {
      int v = (Integer)DefaultHologramTransparency.get();
      if (v > 100) {
         v = 100;
      }

      if (v < 0) {
         v = 0;
      }

      return 1.0F - v / 100.0F;
   }

   public static void load() {
      SPEC.load(net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("portable_blueprints-client.json"));
   }

   public static void saveConfigFile() {
      SPEC.save();
   }
}
