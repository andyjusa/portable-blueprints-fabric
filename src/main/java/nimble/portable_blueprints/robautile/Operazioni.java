package nimble.portable_blueprints.robautile;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import nimble.portable_blueprints.portable_blueprints.MOD;

public class Operazioni {
   private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5L)).build();
   private static final Map<UUID, String> CacheUUIDNAME = new HashMap<>();

   public static double getRandomBetween(double min, double max) {
      Random random = new Random();
      return random.nextDouble() * (max - min) + min;
   }

   public static int getRandomBetween(int min, int max) {
      Random random = new Random();
      return random.nextInt(max - min + 1) + min;
   }

   public static double lerp(double start, double end, double t) {
      return start + t * (end - start);
   }

   public static float lerp(float start, float end, float t) {
      return start + t * (end - start);
   }

   public static double getDistanceToGround(Level level, Entity entity, double distanzaMax) {
      Vec3 startPosition = entity.position();
      Vec3 endPosition = startPosition.subtract(0.0, distanzaMax, 0.0);
      BlockHitResult result = level.clip(new ClipContext(startPosition, endPosition, Block.COLLIDER, Fluid.NONE, entity));
      return result.getType() == Type.BLOCK ? result.getLocation().distanceTo(startPosition) : Double.MAX_VALUE;
   }

   public static int lerpColor(int colorStart, int colorEnd, float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      int a1 = colorStart >> 24 & 0xFF;
      int r1 = colorStart >> 16 & 0xFF;
      int g1 = colorStart >> 8 & 0xFF;
      int b1 = colorStart & 0xFF;
      int a2 = colorEnd >> 24 & 0xFF;
      int r2 = colorEnd >> 16 & 0xFF;
      int g2 = colorEnd >> 8 & 0xFF;
      int b2 = colorEnd & 0xFF;
      int a = (int)(a1 + (a2 - a1) * t);
      int r = (int)(r1 + (r2 - r1) * t);
      int g = (int)(g1 + (g2 - g1) * t);
      int b = (int)(b1 + (b2 - b1) * t);
      return a << 24 | r << 16 | g << 8 | b;
   }

   public static Vec3 getColorFromHex(int hex) {
      int r = hex >> 16 & 0xFF;
      int g = hex >> 8 & 0xFF;
      int b = hex & 0xFF;
      return new Vec3(r / 255.0F, g / 255.0F, b / 255.0F);
   }

   public static float calculateDistance(Vec2 point1, Vec2 point2) {
      float dx = point2.x - point1.x;
      float dy = point2.y - point1.y;
      return (float)Math.sqrt(dx * dx + dy * dy);
   }

   public static ItemStack getItemStackFromId(String itemId) {
      Item item = (Item)BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId));
      return item != null ? new ItemStack(item) : ItemStack.EMPTY;
   }

   public static ItemStack getCorrectItemStackFromId(net.minecraft.world.level.block.Block blocco) {
      Item item = Item.byBlock(blocco);
      if (item == Items.AIR) {
         item = blocco.asItem();
      }

      if (item == Items.AIR) {
         if (blocco.equals(Blocks.WALL_TORCH)) {
            item = Items.TORCH;
         } else if (blocco.equals(Blocks.REDSTONE_WALL_TORCH)) {
            item = Items.REDSTONE_TORCH;
         } else if (blocco.equals(Blocks.WATER) || blocco.equals(Blocks.BUBBLE_COLUMN)) {
            item = Items.WATER_BUCKET;
         } else if (blocco.equals(Blocks.LAVA)) {
            item = Items.LAVA_BUCKET;
         }
      }

      return new ItemStack(item);
   }

   public static String getValueInsideTag(String input, String Tag) {
      try {
         int start = input.indexOf("<" + Tag + ">") + Tag.length() + 2;
         int end = input.indexOf("</" + Tag + ">");
         return start != -1 && end != -1 && start < end ? input.substring(start, end) : "";
      } catch (Exception e) {
         return "";
      }
   }

   public static int addItemToInventory(Inventory pInventory, ItemStack stack, int quantity) {
      int quantitaRimasta = quantity;

      while (pInventory.getFreeSlot() != -1 && quantitaRimasta > 0) {
         ItemStack stackNuovo = stack.copy();
         stackNuovo.setCount(quantitaRimasta >= 64 ? 64 : quantitaRimasta);
         quantitaRimasta -= stackNuovo.getCount();
         pInventory.add(stackNuovo);
      }

      for (int slot = 0; slot < pInventory.getContainerSize() && quantitaRimasta > 0; slot++) {
         ItemStack stackSlot = pInventory.getItem(slot);
         if (ItemStack.isSameItemSameComponents(stackSlot, stack)) {
            int quantitaChePuoMettere = 64 - stackSlot.getCount();
            if (quantitaChePuoMettere > quantitaRimasta) {
               quantitaChePuoMettere = quantitaRimasta;
            }

            stackSlot.setCount(stackSlot.getCount() + quantitaChePuoMettere);
            quantitaRimasta -= quantitaChePuoMettere;
         }
      }

      return quantitaRimasta;
   }

   public static void dropItem(Level level, Vec3 pos, ItemStack stack) {
      if (!level.isClientSide && !stack.isEmpty()) {
         level.addFreshEntity(new ItemEntity(level, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, stack));
      }
   }

   public static BlockPos SommaBlockPos(BlockPos blockPos, BlockPos offsetBloccoSecondario) {
      int x = blockPos.getX() + offsetBloccoSecondario.getX();
      int y = blockPos.getY() + offsetBloccoSecondario.getY();
      int z = blockPos.getZ() + offsetBloccoSecondario.getZ();
      return new BlockPos(x, y, z);
   }

   public static int getEuclideaBlockDistance(BlockPos pos1, BlockPos pos2) {
      return (int)Math.round(Math.sqrt(pos1.distSqr(pos2)));
   }

   public static int getManhattanBlockDistance(BlockPos pos1, BlockPos pos2) {
      return Math.abs(pos1.getX() - pos2.getX()) + Math.abs(pos1.getY() - pos2.getY()) + Math.abs(pos1.getZ() - pos2.getZ());
   }

   public static void glowingItemStack(ItemStack item, boolean effectOn) {
      item.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, effectOn);
   }

   public static void saveVec3iToNBTIntArray(String nome, Vec3i vec, CompoundTag nbt) {
      if (vec != null && nbt != null) {
         nbt.putIntArray(nome + "_Vec3i", new int[]{vec.getX(), vec.getY(), vec.getZ()});
      }
   }

   public static Vec3i getVec3iByNBTIntArray(String nome, CompoundTag nbt) {
      if (nbt != null && nbt.contains(nome + "_Vec3i")) {
         int[] valori = nbt.getIntArray(nome + "_Vec3i");
         return new Vec3i(valori[0], valori[1], valori[2]);
      } else {
         return null;
      }
   }

   public static Direction getDirectionBetween(Vec3 vec1, Vec3 vec2) {
      double dx = vec2.x - vec1.x;
      double dy = vec2.y - vec1.y;
      double dz = vec2.z - vec1.z;
      if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > Math.abs(dz)) {
         return dx > 0.0 ? Direction.EAST : Direction.WEST;
      } else if (Math.abs(dy) > Math.abs(dx) && Math.abs(dy) > Math.abs(dz)) {
         return dy > 0.0 ? Direction.UP : Direction.DOWN;
      } else {
         return dz > 0.0 ? Direction.SOUTH : Direction.NORTH;
      }
   }

   public static String getVec3iString(Vec3i v) {
      return "x=" + v.getX() + ",y=" + v.getY() + ",z=" + v.getZ();
   }

   public static String getVec3String(Vec3 v) {
      return "x=" + v.x() + ",y=" + v.y() + ",z=" + v.z();
   }

   public static Vec3i getVec3iFromString(String v) {
      if (v.isEmpty()) {
         return Vec3i.ZERO;
      }

      v = v.replaceAll("=", "").replaceAll("x", "").replaceAll("y", "").replaceAll("z", "");
      String[] p = v.split(",");
      return new Vec3i(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]));
   }

   public static Vec3 getVec3FromString(String v) {
      if (v.isEmpty()) {
         return Vec3.ZERO;
      }

      v = v.replaceAll("=", "").replaceAll("x", "").replaceAll("y", "").replaceAll("z", "");
      String[] p = v.split(",");
      return new Vec3(Double.parseDouble(p[0]), Double.parseDouble(p[1]), Double.parseDouble(p[2]));
   }

   public static String getBlockStateString(BlockState state) {
      net.minecraft.world.level.block.Block block = state.getBlock();
      String id = BuiltInRegistries.BLOCK.getKey(block).toString();
      String nbtString = "null";
      if (state.getProperties().size() > 0) {
         nbtString = ConvertPropertiesToString(state);
      }

      return "<bid>" + id + "</bid>" + nbtString;
   }

   public static String ConvertPropertiesToString(BlockState state) {
      String compilation = "<properties>";

      for (Property p : state.getProperties()) {
         compilation = compilation
            + "<propertie><name>"
            + p.getName()
            + "</name><value>"
            + state.getValue(p)
            + "</value><classe>"
            + p.getValueClass().toString().replace("class ", "")
            + "</classe></propertie>;!v!;";
      }

      return compilation + "</properties>";
   }

   public static net.minecraft.world.level.block.Block getBlockFromString(String v) {
      return (net.minecraft.world.level.block.Block)BuiltInRegistries.BLOCK.get(ResourceLocation.parse(v));
   }

   public static BlockState getPropertiesStringToBlock(net.minecraft.world.level.block.Block blocco, String proprieta) {
      BlockState state = blocco.defaultBlockState();
      HashMap<String, Tuple<String, String>> data = new HashMap<>();
      proprieta = proprieta.replace("<properties>", "").replace("</properties>", "");

      for (String v : proprieta.split(";!v!;")) {
         if (!v.equals("")) {
            data.put(
               v.substring(v.indexOf("<name>") + 6, v.indexOf("</name>")),
               new Tuple(v.substring(v.indexOf("<value>") + 7, v.indexOf("</value>")), v.substring(v.indexOf("<classe>") + 8, v.indexOf("</classe>")))
            );
         }
      }

      for (Property p : state.getProperties()) {
         if (data.containsKey(p.getName())) {
            state = setDynamicProperty(state, p, (String)data.get(p.getName()).getA());
         }
      }

      return state;
   }

   public static <T extends Comparable<T>> BlockState setDynamicProperty(BlockState state, Property<T> property, String value) {
      Optional<T> parsed = property.getValue(value.toLowerCase(Locale.ROOT));
      if (parsed.isPresent()) {
         return (BlockState)state.setValue(property, parsed.get());
      }

      MOD.LOGGER
         .warn(
            "[Portable blueprint] Invalid value '{}' for property '{}'. Allowed: {}",
            new Object[]{value, property.getName(), property.getPossibleValues().stream().map(property::getName).toList()}
         );
      return state;
   }

   public static Direction rotateFacing(Direction currentFacing, int rotations) {
      if (!currentFacing.equals(Direction.DOWN) && !currentFacing.equals(Direction.UP)) {
         Direction[] directions = new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
         int currentIndex = 0;

         for (int i = 0; i < directions.length; i++) {
            if (directions[i] == currentFacing) {
               currentIndex = i;
               break;
            }
         }

         int newIndex = (currentIndex + rotations) % directions.length;
         return directions[newIndex];
      } else {
         return currentFacing;
      }
   }

   public static Vec3 lerp(Vec3 start, Vec3 end, double t) {
      double x = start.x + (end.x - start.x) * t;
      double y = start.y + (end.y - start.y) * t;
      double z = start.z + (end.z - start.z) * t;
      return new Vec3(x, y, z);
   }

   public Item getItemFromId(String id) {
      ResourceLocation itemId = ResourceLocation.parse(id);
      return (Item)BuiltInRegistries.ITEM.get(itemId);
   }

   public static BufferedImage getPlayerFace(UUID uuid) {
      try {
         String url = "https://minotar.net/helm/" + uuid + "/8";
         InputStream inputStream = new URL(url).openStream();
         return ImageIO.read(inputStream);
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   public static String getPlayerNameFromUUID(UUID uuid) {
      try {
         URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString().replaceAll("-", ""));
         HttpURLConnection con = (HttpURLConnection)url.openConnection();
         con.setRequestMethod("GET");
         con.setConnectTimeout(500);
         con.setReadTimeout(500);
         int status = con.getResponseCode();
         if (status != 200) {
            return null;
         }

         BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
         StringBuilder content = new StringBuilder();

         String line;
         while ((line = in.readLine()) != null) {
            content.append(line);
         }

         in.close();
         JsonObject obj = JsonParser.parseString(content.toString()).getAsJsonObject();
         return obj.get("name").getAsString();
      } catch (Exception e) {
         return "";
      }
   }

   public static List<String> splitStringByLength(String input, int chunkSize) {
      List<String> result = new ArrayList<>();
      int length = input.length();
      int i = 0;

      while (i < length) {
         result.add(input.substring(i, Math.min(length, i + chunkSize)));
         i += chunkSize;
      }

      return result;
   }

   public static String getWorldTimeString() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null) {
         return "??:??";
      }

      long time = mc.level.getDayTime() % 24000L;
      int hours = (int)((time / 1000L + 6L) % 24L);
      int minutes = (int)(time % 1000L * 60L / 1000L);
      String ampm = hours >= 12 ? "PM" : "AM";
      int displayHour = hours % 12;
      if (displayHour == 0) {
         displayHour = 12;
      }

      return String.format("%02d:%02d %s", displayHour, minutes, ampm);
   }

   public static String getNameViaSessionServer(String uuid) {
      String v = getPlayerName(UUID.fromString(uuid));
      if (!v.isEmpty()) {
         return v;
      }

      if (CacheUUIDNAME.containsKey(UUID.fromString(uuid))) {
         return CacheUUIDNAME.get(UUID.fromString(uuid));
      }

      try {
         String clean = stripDashes(uuid);
         String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + clean;
         HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(5L)).GET().header("Accept", "application/json").build();
         HttpResponse<String> resp = CLIENT.send(req, BodyHandlers.ofString());
         if (resp.statusCode() == 200) {
            JsonObject obj = JsonParser.parseString(resp.body()).getAsJsonObject();
            v = obj.has("name") ? obj.get("name").getAsString() : "";
         } else {
            if (resp.statusCode() != 204 && resp.statusCode() != 404) {
               throw new RuntimeException("Sessionserver error: " + resp.statusCode());
            }

            v = "";
         }
      } catch (Exception e) {
         e.printStackTrace();
      }

      if (!CacheUUIDNAME.containsKey(UUID.fromString(uuid))) {
         CacheUUIDNAME.put(UUID.fromString(uuid), v);
      }

      return v;
   }

   private static String stripDashes(String uuid) {
      return uuid.replace("-", "");
   }

   public static String getPlayerName(UUID uuid) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && uuid != null) {
         AbstractClientPlayer player = (AbstractClientPlayer)mc.level.getPlayerByUUID(uuid);
         if (player != null) {
            return player.getGameProfile().getName();
         } else {
            return mc.player != null && mc.player.getUUID().equals(uuid) ? mc.player.getGameProfile().getName() : "";
         }
      } else {
         return "";
      }
   }
}
