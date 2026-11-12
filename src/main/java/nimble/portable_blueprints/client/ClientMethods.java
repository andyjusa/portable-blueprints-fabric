package nimble.portable_blueprints.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import nimble.portable_blueprints.fabric.network.PacketDistributor;
import nimble.portable_blueprints.Events.ModEventClientForge;
import nimble.portable_blueprints.altro.ProgressBarBlueprintPlayer;
import nimble.portable_blueprints.client.screen.BlueprintScreen;
import nimble.portable_blueprints.client.screen.ScannerScreen;
import nimble.portable_blueprints.network.contenuto_container_selezionati.CtoS_ContenutoContainerSelezionati;

public class ClientMethods {
   public static Map<Item, Integer> ItemDentroInventari = new HashMap<>();
   private static int ID_ItemDentroInventori = -1;
   public static boolean SendingGlobalBlueprintToServer = false;
   public static List<String> ServerBlackListBlocks = new ArrayList<>();
   public static boolean ServerBlackListBlocksCompleta = false;

   public static void openHoloScannerItemGUI(ItemStack item, int quantitaBlocchi) {
      if (Minecraft.getInstance() != null) {
         Minecraft.getInstance().setScreen(new ScannerScreen(item, quantitaBlocchi));
      }
   }

   public static void openBlueprintItemGUI(ItemStack blueprintItem, List<BlockPos> posContainer, boolean openAnimation) {
      Minecraft.getInstance().setScreen(new BlueprintScreen(blueprintItem, posContainer, openAnimation));
   }

   public static int getID_ItemDentroInventori() {
      return ++ID_ItemDentroInventori;
   }

   public static void mandaRichiestaItemQuantityContainer(List<BlockPos> p) {
      ItemDentroInventari.clear();
      PacketDistributor.sendToServer(new CtoS_ContenutoContainerSelezionati(p, getID_ItemDentroInventori()), new CustomPacketPayload[0]);
   }

   public static void addItemQuantityContainer(int IDRequest, Item item, int Quantity) {
      if (IDRequest == ID_ItemDentroInventori) {
         if (ItemDentroInventari.containsKey(item)) {
            ItemDentroInventari.replace(item, Quantity);
         } else {
            ItemDentroInventari.put(item, Quantity);
         }
      }
   }

   public static void Reset() {
      ItemDentroInventari.clear();
      ID_ItemDentroInventori = 0;
      SendingGlobalBlueprintToServer = false;
      VisualizeBlueprintDatas.reset();
      ServerBlackListBlocks.clear();
      ServerBlackListBlocksCompleta = false;
      ModEventClientForge.ObstructionBlockManager.reset();
      ServerToClientDatasShared.reset();
      BlueprintsPreviewMethods.MapIconsRL.clear();
      ProgressBarBlueprintPlayer.Reset();
   }
}
