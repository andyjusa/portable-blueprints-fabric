package nimble.portable_blueprints.mod_compatibility.projecte;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import nimble.portable_blueprints.fabric.network.PacketDistributor;
import nimble.portable_blueprints.client.ClientMethods;
import nimble.portable_blueprints.client.screen.BlueprintScreen;
import nimble.portable_blueprints.mod_compatibility.projecte.network.CtoS_GetEMCPlayer;
import nimble.portable_blueprints.network.update_item_blueprint_nbt.CtoS_UpdateItemBlueprintNBT;
import nimble.portable_blueprints.portable_blueprints.OldVersionUtilities;
import nimble.portable_blueprints.robautile.AdvancedButton;
import nimble.portable_blueprints.robautile.RenderingMethods;

public class ProjectEBlueprintScreen {
   private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("portable_blueprints", "textures/gui/blueprint_gui.png");
   private int CentroX;
   private int CentroY;
   private int W;
   private int H;
   private AdvancedButton B_ActiveEMC;
   private BlueprintScreen blueprintScreen;
   private ItemStack BlueprintItem;
   private int PX = 0;
   private int PY = 0;
   public boolean EMC_Attivo = false;
   public static long PlayerEMC = 0L;
   public long StructureEMC = 0L;
   private boolean noItemEmc = false;
   private RenderingMethods.GuiGraphicsElement Titolo;
   private RenderingMethods.GuiGraphicsElement Label1;
   private RenderingMethods.GuiGraphicsElement Label2;
   private RenderingMethods.GuiGraphicsElement Label3;
   private Map<Item, Long> ItemEMC = new HashMap<>();
   private Map<Item, Integer> ItemEMCCount = new HashMap<>();

   public ProjectEBlueprintScreen(BlueprintScreen blueprintScreen, ItemStack BlueprintItem) {
      this.EMC_Attivo = OldVersionUtilities.getItemStackNBT(BlueprintItem).contains("projecte_active_emc")
         && OldVersionUtilities.getItemStackNBT(BlueprintItem).getBoolean("projecte_active_emc");
      this.BlueprintItem = BlueprintItem;
      this.blueprintScreen = blueprintScreen;
      this.Reload();
   }

   public void Reload() {
      PlayerEMC = 0L;
      this.StructureEMC = 0L;
      PacketDistributor.sendToServer(new CtoS_GetEMCPlayer(), new CustomPacketPayload[0]);
      Thread tickerThread = new Thread(() -> {
         try {
            Thread.sleep(200L);
            this.ItemEMC.clear();
            this.ItemEMCCount.clear();
            long copyPlayerEMC = PlayerEMC;

            for (Item key : this.blueprintScreen.ItemQuantity.keySet()) {
               int moltiplicazione = this.blueprintScreen.ItemQuantity.get(key);
               if (ClientMethods.ItemDentroInventari.containsKey(key)) {
                  moltiplicazione -= ClientMethods.ItemDentroInventari.get(key);
                  if (moltiplicazione < 0) {
                     moltiplicazione = 0;
                  }
               }

               if (!this.ItemEMCCount.containsKey(key)) {
                  this.ItemEMCCount.put(key, 0);
               }

               long v = ProjectEMetodi.getEMCForItem(key) * moltiplicazione;
               this.StructureEMC += v;
               if (ProjectEMetodi.getEMCForItem(key) == 0L) {
                  this.noItemEmc = true;
               } else if (copyPlayerEMC - v >= 0L) {
                  this.ItemEMC.put(key, 0L);
                  copyPlayerEMC -= v;
                  this.ItemEMCCount.put(key, this.ItemEMCCount.get(key) + 1);
               } else if (copyPlayerEMC < v) {
                  this.ItemEMC.put(key, v - copyPlayerEMC);
                  copyPlayerEMC = 0L;
                  this.ItemEMCCount.put(key, this.ItemEMCCount.get(key) + 1);
               } else {
                  this.ItemEMC.put(key, v);
                  this.ItemEMCCount.put(key, this.ItemEMCCount.get(key) + 1);
               }
            }
         } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
         }
      });
      tickerThread.setDaemon(true);
      tickerThread.start();
   }

   public void init(int CentroX, int CentroY, int W, int H) {
      this.W = W;
      this.H = H;
      this.CentroX = CentroX;
      this.CentroY = CentroY;
      this.PX = CentroX + 70;
      this.PY = CentroY - 80;
      int px = CentroX - 61;
      int py = CentroY + 27;
      this.B_ActiveEMC = AdvancedButton.builder(Component.literal(""), pAdvanceButton -> this.ActiveEMC())
         .pos(px, py)
         .size(12, 12)
         .defaultImage(new AdvancedButton.Image(TEXTURE, px, py, 146, 226, 12, 12))
         .pressedImageCondition(new AdvancedButton.Image(TEXTURE, px, py, 146, 242, 12, 12))
         .tooltip(
            Tooltip.create(
               Component.translatable("gui.portable_blueprints.compatibility.projecte.gui.tooltip." + (this.EMC_Attivo ? "deactive" : "active") + "_emc")
            )
         )
         .build();
      this.Titolo = new RenderingMethods.GuiGraphicsElement(
         new RenderingMethods.GuiGraphicsElement.TESTO_Parametri(
            this.PX + 28, this.PY + 15, "ProjectE", 1.25F, Minecraft.getInstance().font, 11808612, false, RenderingMethods.PosizioneTesto.CENTRO, true
         )
      );
      this.Label1 = new RenderingMethods.GuiGraphicsElement(
         new RenderingMethods.GuiGraphicsElement.TESTO_Parametri(
            this.PX + 27,
            this.PY + 25,
            Component.translatable("gui.portable_blueprints.compatibility.projecte.gui.label1").getString(),
            0.9F,
            Minecraft.getInstance().font,
            16777215,
            false,
            RenderingMethods.PosizioneTesto.CENTRO,
            true
         )
      );
      this.Label2 = new RenderingMethods.GuiGraphicsElement(
         new RenderingMethods.GuiGraphicsElement.TESTO_Parametri(
            this.PX + 27,
            this.PY + 68,
            Component.translatable("gui.portable_blueprints.compatibility.projecte.gui.label2").getString(),
            0.9F,
            Minecraft.getInstance().font,
            16777215,
            false,
            RenderingMethods.PosizioneTesto.CENTRO,
            true
         )
      );
      this.Label3 = new RenderingMethods.GuiGraphicsElement(
         new RenderingMethods.GuiGraphicsElement.TESTO_Parametri(
            this.PX + 27,
            this.PY + 100,
            Component.translatable("gui.portable_blueprints.compatibility.projecte.gui.label3").getString(),
            0.7F,
            Minecraft.getInstance().font,
            8912923,
            false,
            RenderingMethods.PosizioneTesto.CENTRO,
            true
         )
      );
   }

   private void ActiveEMC() {
      this.EMC_Attivo = !this.EMC_Attivo;
      this.B_ActiveEMC
         .setTooltip(
            Tooltip.create(
               Component.translatable("gui.portable_blueprints.compatibility.projecte.gui.tooltip." + (this.EMC_Attivo ? "deactive" : "active") + "_emc")
            )
         );
      CtoS_UpdateItemBlueprintNBT.send(
         this.BlueprintItem, "projecte_active_emc", !OldVersionUtilities.getItemStackNBT(this.BlueprintItem).getBoolean("projecte_active_emc")
      );
   }

   public void render(GuiGraphics gg, int mouseX, int mouseY, float delta) {
      if (this.B_ActiveEMC != null) {
         this.B_ActiveEMC.PressedImageCondition = this.EMC_Attivo;
      }

      if (this.EMC_Attivo) {
         gg.blit(TEXTURE, this.CentroX + 65, this.CentroY - 72, 192, 0, 1, 144);
         if (this.Titolo != null) {
            this.Titolo.render(gg);
         }

         if (this.Label1 != null) {
            this.Label1.render(gg);
         }

         if (this.Label2 != null) {
            this.Label2.render(gg);
         }

         this.Label3.render(gg);
         String var10001 = String.valueOf(this.StructureEMC);
         float var10004 = this.PX - 3;
         float var10005 = this.PY + 45;
         RenderingMethods.GUITextAutoResize(gg, var10001, Minecraft.getInstance().font, 16755200, var10004, var10005, 61, 10, false);
         var10001 = String.valueOf(PlayerEMC);
         int var10003 = this.StructureEMC != 0L && PlayerEMC != 0L && PlayerEMC >= this.StructureEMC ? 43520 : 11141120;
         var10004 = this.PX - 3;
         var10005 = this.PY + 80;
         RenderingMethods.GUITextAutoResize(gg, var10001, Minecraft.getInstance().font, var10003, var10004, var10005, 61, 10, false);
      }
   }

   public List<AdvancedButton> getRenderable() {
      try {
         return List.of(this.B_ActiveEMC);
      } catch (Exception e) {
         e.printStackTrace();
         return new ArrayList<>();
      }
   }

   public long getItemEMCValue(Item item) {
      return this.ItemEMC.containsKey(item) ? this.ItemEMC.get(item) : -1L;
   }

   public int getItemEMCPCount(Item item) {
      return this.ItemEMCCount.containsKey(item) ? this.ItemEMCCount.get(item) : 0;
   }
}
