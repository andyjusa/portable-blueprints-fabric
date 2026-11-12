package nimble.portable_blueprints.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import nimble.portable_blueprints.altro.Blueprint;

public class ServerToClientDatasShared {
   private static int IDS = 0;
   public static List<String> blueprintsIDs = new ArrayList<>();
   public static Map<Integer, String> StringMap = new HashMap<>();
   public static final Map<Integer, Boolean> RichiesteBooleane = new HashMap<>();
   public static final Map<Integer, Integer> RichiesteInteger = new HashMap<>();
   public static final Map<Integer, String> RichiestaString = new HashMap<>();
   public static boolean endTrasferingSharedWithMeBlueprints = false;
   public static Blueprint.SharedWithMe SharedWithMeBlueprints = new Blueprint.SharedWithMe();
   public static boolean endTrasferingServerBlueprints = false;
   public static List<String> ServerBlueprints = new ArrayList<>();
   public static boolean endTrasferingWorldPlayersUUID = false;
   public static List<UUID> WorldPlayersUUID = new ArrayList<>();
   public static Map<String, List<String>> BlueprintTags = new HashMap<>();
   public static final Integer ID_PLAYER_BLUEPRINTS_QUANTITY = 6000;

   public static void addRichiestaBooleana(int richiestaID, boolean valore) {
      RichiesteBooleane.put(richiestaID, valore);
   }

   public static void addRichiestaInteger(int richiestaID, int valore) {
      RichiesteInteger.put(richiestaID, valore);
   }

   public static int getRichiestaBooleana(int richiestaID) {
      if (RichiesteBooleane.containsKey(richiestaID)) {
         return RichiesteBooleane.get(richiestaID) ? 1 : 0;
      } else {
         return -1;
      }
   }

   public static int getRichiestaInteger(int richiestaID) {
      return RichiesteInteger.containsKey(richiestaID) ? RichiesteInteger.get(richiestaID) : 0;
   }

   public static void addRichiestaString(int idRichiesta, String nome) {
      RichiestaString.put(idRichiesta, nome);
   }

   public static String getRichiestaString(int richiestaID) {
      return RichiestaString.containsKey(richiestaID) ? RichiestaString.get(richiestaID) : "";
   }

   public static void reset() {
      blueprintsIDs.clear();
      StringMap.clear();
      IDS = 0;
      RichiestaString.clear();
      RichiesteInteger.clear();
      RichiesteBooleane.clear();
      endTrasferingSharedWithMeBlueprints = false;
      SharedWithMeBlueprints = new Blueprint.SharedWithMe();
      endTrasferingServerBlueprints = false;
      ServerBlueprints.clear();
      endTrasferingWorldPlayersUUID = false;
      WorldPlayersUUID.clear();
      BlueprintTags.clear();
   }

   public static int getID() {
      return ++IDS;
   }
}
