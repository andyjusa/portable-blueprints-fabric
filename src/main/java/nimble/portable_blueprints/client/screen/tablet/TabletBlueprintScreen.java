package nimble.portable_blueprints.client.screen.tablet;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import nimble.portable_blueprints.altro.Blueprint;
import nimble.portable_blueprints.client.ServerToClientDatasShared;
import nimble.portable_blueprints.client.screen.tablet.mineprint.TabletMinePrintMethods;
import nimble.portable_blueprints.client.screen.tablet.utils.SearchBarElement;
import nimble.portable_blueprints.client.screen.tablet.utils.TabletScrollBar;
import nimble.portable_blueprints.init.ModTabletTags;
import nimble.portable_blueprints.network.ottieni_nome_x_blueprint.CtoS_BlueprintXName;
import nimble.portable_blueprints.network.request_specific_data.CtoS_RequestSpecificData;
import nimble.portable_blueprints.robautile.RenderingMethods;

public class TabletBlueprintScreen extends Screen {
   private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("portable_blueprints", "textures/gui/tablet/tablet_blueprint_gui.png");
   private int CentroX;
   private int CentroY;
   private int W;
   private int H;
   private String valoreEditBox = "";
   private int BlueprintQuantity = -1;
   private int BeforeBlueprintQuantity = this.BlueprintQuantity;
   private int YList = 0;
   private int MaxYList = 0;
   private List<TabletMinePrintMethods.BlueprintPreviewItem> SavedBlueprintPreviewItems = new ArrayList<>();
   private List<TabletMinePrintMethods.BlueprintPreviewItem> blueprintPreviewItems = new ArrayList<>();
   private SearchBarElement SBE = null;
   private TabletScrollBar scrollBar;
   private boolean isGlobalBPAllowed = false;
   private boolean valorePreso = false;
   private boolean valorePreso2 = false;
   private short TickCount = 1;

   protected TabletBlueprintScreen(Component component) {
      super(component);
   }

   public TabletBlueprintScreen() {
      super(Component.literal("portable_blueprint.tablet"));
      TabletMinePrintMethods.BlueprintPreviewItem.resetAllSavedTag();
      CtoS_RequestSpecificData.send(ServerToClientDatasShared.ID_PLAYER_BLUEPRINTS_QUANTITY, CtoS_RequestSpecificData.TipoRichiesta.PLAYER_BLUEPRINTS_QUANTITY);
      CtoS_RequestSpecificData.send(510, CtoS_RequestSpecificData.TipoRichiesta.ALLOW_GLOBAL_BLUEPRINTS);
   }

   protected void init() {
      super.init();
      this.W = this.width;
      this.H = this.height;
      this.CentroX = this.W / 2;
      this.CentroY = this.H / 2;
      this.SBE = new SearchBarElement(
         this.CentroX - 67,
         this.CentroY - 84,
         this.SBE == null ? "" : this.SBE.getValue(),
         this.SBE == null ? new ArrayList<>() : this.SBE.getSelectedTags(),
         this.SBE == null || this.SBE.modeSearch
      );
      this.addRenderableWidget(this.SBE);
      this.addRenderableWidget(this.SBE.getEditBox());
      this.addRenderableWidget(TabletScreen.getTabletButton(this.CentroX, this.CentroY));
      this.addRenderableWidget(
         this.scrollBar = new TabletScrollBar(
            this.CentroX + 68,
            this.CentroY - 60,
            1,
            146,
            -12696497,
            -4406063,
            new TabletScrollBar.ScrollingArea(this.CentroX - 70, this.CentroY - 68, 140, 157, 5)
         )
      );
      if (this.blueprintPreviewItems.size() <= 2) {
         this.scrollBar.visible = false;
      }
   }

   public void render(GuiGraphics gg, int mouseX, int mouseY, float delta) {
      if (ServerToClientDatasShared.getRichiestaBooleana(510) != -1 && !this.valorePreso2) {
         this.valorePreso2 = true;
         this.isGlobalBPAllowed = ServerToClientDatasShared.getRichiestaBooleana(510) == 1;
      }

      if (this.scrollBar != null) {
         this.YList = (int)this.scrollBar.getValore(this.MaxYList);
      }

      if (ServerToClientDatasShared.RichiesteInteger.containsKey(ServerToClientDatasShared.ID_PLAYER_BLUEPRINTS_QUANTITY) && !this.valorePreso) {
         this.BlueprintQuantity = ServerToClientDatasShared.RichiesteInteger.get(ServerToClientDatasShared.ID_PLAYER_BLUEPRINTS_QUANTITY);

         for (int x = 0; x < this.BlueprintQuantity; x++) {
            CtoS_BlueprintXName.send(9501 + x, x);
         }

         Thread tickerThread = new Thread(() -> {
            try {
               boolean allSend = false;

               while (!allSend) {
                  Thread.sleep(50L);
                  allSend = true;

                  for (int x = 0; x < this.BlueprintQuantity; x++) {
                     if (!ServerToClientDatasShared.RichiestaString.containsKey(9501 + x)) {
                        System.err.println("waiting blueprint datas");
                        allSend = false;
                        break;
                     }
                  }
               }

               this.MakeList();
            } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
            }
         });
         tickerThread.setDaemon(true);
         tickerThread.start();
         this.valorePreso = true;
      }

      if (this.SBE != null && this.SBE.isValueChanged()) {
         this.MakeList();
      }

      TabletScreen.renderTabletBackground(gg, this.CentroX, this.CentroY, "b1");
      RenderingMethods.enableScissorFromGui(Minecraft.getInstance(), this.CentroX - 70, this.CentroY - 68, 140, 157);

      try {
         for (TabletMinePrintMethods.BlueprintPreviewItem bpi : this.blueprintPreviewItems) {
            bpi.addToPosY(-this.YList);
            if (bpi.posY >= -150 && bpi.posY <= 90) {
               bpi.createIconStructure = true;
               bpi.render(gg, this.CentroX, this.CentroY, mouseX, mouseY, new Vec2(-70.0F, 70.0F), new Vec2(-67.0F, 89.0F));
            }
         }
      } catch (Exception var8) {
      }

      RenderingMethods.disableScissor();

      try {
         super.render(gg, mouseX, mouseY, delta);
      } catch (Exception var7) {
      }

      TabletScreen.renderTabletUpBar(gg, this.CentroX, this.CentroY);
   }

   private void re_init() {
      this.clearWidgets();

      this.init();
   }

   private void MakeList() {
      this.blueprintPreviewItems.clear();
      int size = this.BlueprintQuantity;
      int count = 0;
      int negativecount = 0;
      if (this.SavedBlueprintPreviewItems.isEmpty()) {
         for (int x = 0; x < size; x++) {
            String blueprint_name = ServerToClientDatasShared.getRichiestaString(9500 + x + 1);
            UUID owner = Minecraft.getInstance().player.getUUID();
            TabletMinePrintMethods.TYPE type = TabletMinePrintMethods.TYPE.PERSONAL;
            TabletMinePrintMethods.BlueprintPreviewItem bpi = new TabletMinePrintMethods.BlueprintPreviewItem(blueprint_name, type, owner);
            this.SavedBlueprintPreviewItems.add(bpi);
         }

         if (this.isGlobalBPAllowed) {
            for (String name : Blueprint.getAllGlobalBlueprintsName()) {
               TabletMinePrintMethods.BlueprintPreviewItem bpi = new TabletMinePrintMethods.BlueprintPreviewItem(
                  name, TabletMinePrintMethods.TYPE.PERSONAL_GLOBAL, Minecraft.getInstance().player.getUUID()
               );
               this.SavedBlueprintPreviewItems.add(bpi);
            }
         }
      }

      for (int x = 0; x < this.SavedBlueprintPreviewItems.size(); x++) {
         if (this.SavedBlueprintPreviewItems.get(x) == null) {
            negativecount++;
         } else {
            TabletMinePrintMethods.BlueprintPreviewItem bpi = this.SavedBlueprintPreviewItems.get(x);
            String blueprint_name = "";
            if (bpi.type.equals(TabletMinePrintMethods.TYPE.PERSONAL_GLOBAL)) {
               blueprint_name = bpi.BName;
            } else {
               blueprint_name = ServerToClientDatasShared.getRichiestaString(9500 + x + 1);
            }

            boolean nonHaIlTag = false;
            if (this.SBE != null && !this.SBE.getSelectedTags().isEmpty()) {
               for (ModTabletTags.TagData tagData : this.SBE.getSelectedTags()) {
                  if (!bpi.hasTag(tagData)) {
                     nonHaIlTag = true;
                     break;
                  }
               }
            }

            if ((this.SBE == null || blueprint_name.toLowerCase().contains(this.SBE.getValue().toLowerCase())) && !nonHaIlTag && !bpi.BName.isEmpty()) {
               bpi.setPosition(
                  this.SavedBlueprintPreviewItems.size() != 1 && (this.SavedBlueprintPreviewItems.size() - 1 != count || count % 2 != 0)
                     ? (count % 2 == 1 ? 3 : -67)
                     : -32,
                  -60 + 90 * (int)Math.floor(count / 2.0F)
               );
               this.blueprintPreviewItems.add(bpi);
               count++;
            } else {
               negativecount++;
            }
         }
      }

      count--;
      this.MaxYList = (int)(Math.floor(count / 2.0F) * 90.0);
      if (count % 2 != 0) {
         this.MaxYList += 90;
      }

      if (this.MaxYList < 0) {
         this.MaxYList = 0;
      }

      if (this.YList > this.MaxYList) {
         this.YList = this.MaxYList;
      }

      if (this.blueprintPreviewItems.size() > 2) {
         if (this.scrollBar != null) {
            this.scrollBar.visible = true;
         }
      } else if (this.scrollBar != null) {
         this.scrollBar.visible = false;
      }
   }

   public void tick() {
      super.tick();
      if (++this.TickCount > 20) {
         this.TickCount = 1;
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      TabletMinePrintMethods.BlueprintPreviewItem bpi_last = null;

      for (TabletMinePrintMethods.BlueprintPreviewItem bpi : this.blueprintPreviewItems) {
         if (bpi.isMHItem) {
            bpi_last = bpi;
         }
      }

      if (bpi_last != null && bpi_last.insideChiss((int)mouseX, (int)mouseY, new Vec2(-70.0F, 70.0F), new Vec2(-67.0F, 89.0F))) {
         System.err.println(bpi_last.isMHItem + " | " + bpi_last.BName);
         bpi_last.openBlueprintInfo(false);
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      return super.mouseReleased(mouseX, mouseY, button);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double delta, double delta2) {
      return this.scrollBar != null && this.scrollBar.mouseScrolled(mouseX, mouseY, delta2, delta) ? true : super.mouseScrolled(mouseX, mouseY, delta2, delta);
   }

   public boolean isPauseScreen() {
      return false;
   }

   public static void OpenGUI() {
      Minecraft.getInstance().setScreen(new TabletBlueprintScreen());
   }

   public void onClose() {
      TabletScreen.resetTablet();
      super.onClose();
   }

   protected void renderBlurredBackground(float partialTick) {
   }

   public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
   }
}
