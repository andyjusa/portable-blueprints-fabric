package nimble.portable_blueprints.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import nimble.portable_blueprints.Events.ModEventClientForge;
import nimble.portable_blueprints.Events.RenderBlueprintsPreview;
import nimble.portable_blueprints.altro.Blueprint;
import nimble.portable_blueprints.altro.ProgressBarBlueprintPlayer;
import nimble.portable_blueprints.network.visualize_blueprint_data.CtoS_VisualizeBlueprintData;
import nimble.portable_blueprints.robautile.Operazioni;

public class VisualizeBlueprintDatas {
   private static final Gson GSON = nimble.portable_blueprints.fabric.BlueprintJson.create();
   public static int IDRichiestaBlueprintAttuale = 0;
   public static int IDRichiestaPrincipale = 0;
   public static Blueprint.BlueprintDatas VisualizeBlueprint = null;
   public static UUID VisualizeBlueprintOwner = null;
   public static BlockPos originPosVisualizeBlueprint = null;
   public static List<String> ForzaReloadPerQuesteBlueprints = new ArrayList<>();
   public static StringBuffer JsonClasseBuffer = new StringBuffer();
   private static Map<Integer, StringBuffer> BlueprintDataMap = new HashMap<>();
   public static Map<Integer, Blueprint.BlueprintDatas> BlueprintsMap = new HashMap<>();

   public static int getIDRichiestaBlueprint() {
      return ++IDRichiestaBlueprintAttuale;
   }

   public static int newVisualizeBlueprint(UUID owner_bp, String nome, boolean forzaRiavvio, boolean suPrincipale) {
      if (ForzaReloadPerQuesteBlueprints.contains(nome) && Minecraft.getInstance().player.getUUID().equals(owner_bp)) {
         ForzaReloadPerQuesteBlueprints.remove(nome);
         forzaRiavvio = true;
      }

      if (suPrincipale
         && !forzaRiavvio
         && VisualizeBlueprint != null
         && VisualizeBlueprintOwner != null
         && VisualizeBlueprintOwner.equals(owner_bp)
         && nome.equals(VisualizeBlueprint.nome)) {
         return -1;
      }

      int id = getIDRichiestaBlueprint();
      if (suPrincipale) {
         IDRichiestaPrincipale = id;
         VisualizeBlueprintOwner = owner_bp;
         VisualizeBlueprint = null;
      } else {
         BlueprintDataMap.put(id, new StringBuffer());
      }

      CtoS_VisualizeBlueprintData.send(id, nome, owner_bp);
      return id;
   }

   public static int newVisualizeBlueprint(boolean isServerBlueprint, String nome, boolean forzaRiavvio, boolean suPrincipale) {
      if (suPrincipale && !forzaRiavvio && VisualizeBlueprint != null && nome.equals(VisualizeBlueprint.nome)) {
         return -1;
      }

      int id = getIDRichiestaBlueprint();
      if (suPrincipale) {
         IDRichiestaPrincipale = id;
         VisualizeBlueprint = null;
      } else {
         BlueprintDataMap.put(id, new StringBuffer());
      }

      CtoS_VisualizeBlueprintData.send(id, nome, isServerBlueprint);
      return id;
   }

   public static int newVisualizeBlueprint(boolean isServerBlueprint, boolean isWornBlueprint, String nome, boolean forzaRiavvio, boolean suPrincipale) {
      if (suPrincipale && !forzaRiavvio && VisualizeBlueprint != null && nome.equals(VisualizeBlueprint.nome)) {
         return -1;
      }

      int id = getIDRichiestaBlueprint();
      if (suPrincipale) {
         IDRichiestaPrincipale = id;
         VisualizeBlueprint = null;
      } else {
         BlueprintDataMap.put(id, new StringBuffer());
      }

      CtoS_VisualizeBlueprintData.send(id, nome, isServerBlueprint, isWornBlueprint);
      return id;
   }

   public static void resetToDefaultVisualizeBlueprint() {
      VisualizeBlueprint = null;
      VisualizeBlueprintOwner = null;
      originPosVisualizeBlueprint = BlockPos.ZERO;
      ModEventClientForge.datiBlocchi.clear();
      ModEventClientForge.ultimaLXAltezzaLZSalvata = BlockPos.ZERO;
      ModEventClientForge.offsetVisualizeBlueprint = BlockPos.ZERO;
      RenderBlueprintsPreview.doCleanUp();
   }

   public static void sendJsonToVisualizeBlueprint(int IdRichiestaBlueprint, String offset) {
      boolean isPrincipal = !BlueprintDataMap.containsKey(IdRichiestaBlueprint);
      if (isPrincipal) {
         if (IDRichiestaPrincipale != IdRichiestaBlueprint) {
            return;
         }

         if (offset.contains("[DATI INIZIALI]")) {
            JsonClasseBuffer = new StringBuffer();
            ProgressBarBlueprintPlayer.aggiungi(1, Integer.valueOf(Operazioni.getValueInsideTag(offset, "bsize")));
         } else if (offset.contains("[DATI FINITI]")) {
            VisualizeBlueprint = (Blueprint.BlueprintDatas)GSON.fromJson(JsonClasseBuffer.toString(), Blueprint.BlueprintDatas.class);
            JsonClasseBuffer = new StringBuffer();
         } else {
            JsonClasseBuffer.append(offset);
            ProgressBarBlueprintPlayer.progressoPlus(1);
         }
      } else if (BlueprintDataMap.containsKey(IdRichiestaBlueprint)) {
         if (offset.contains("[DATI FINITI]")) {
            BlueprintsMap.put(
               IdRichiestaBlueprint,
               (Blueprint.BlueprintDatas)GSON.fromJson(BlueprintDataMap.get(IdRichiestaBlueprint).toString(), Blueprint.BlueprintDatas.class)
            );
            BlueprintDataMap.remove(IdRichiestaBlueprint);
         } else if (!offset.contains("[DATI INIZIALI]")) {
            BlueprintDataMap.get(IdRichiestaBlueprint).append(offset);
         }
      }
   }

   public static void reset() {
      IDRichiestaBlueprintAttuale = 0;
      IDRichiestaPrincipale = 0;
      VisualizeBlueprint = null;
      originPosVisualizeBlueprint = BlockPos.ZERO;
      ForzaReloadPerQuesteBlueprints.clear();
      JsonClasseBuffer = new StringBuffer();
      BlueprintDataMap.clear();
      BlueprintsMap.clear();
      resetToDefaultVisualizeBlueprint();
   }
}
