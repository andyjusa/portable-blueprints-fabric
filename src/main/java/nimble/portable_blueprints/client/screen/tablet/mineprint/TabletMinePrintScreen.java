package nimble.portable_blueprints.client.screen.tablet.mineprint;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.Vec2;
import nimble.portable_blueprints.altro.Blueprint;
import nimble.portable_blueprints.client.ServerToClientDatasShared;
import nimble.portable_blueprints.client.screen.tablet.TabletScreen;
import nimble.portable_blueprints.client.screen.tablet.utils.SearchBarElement;
import nimble.portable_blueprints.client.screen.tablet.utils.TabletScrollBar;
import nimble.portable_blueprints.init.ModTabletTags;
import nimble.portable_blueprints.init.TabletClientDatas;
import nimble.portable_blueprints.network.ServerBlueprints.CtoS_ServerBlueprintsData;
import nimble.portable_blueprints.network.ottieni_shared_blueprint_data.CtoS_SharedBlueprintData;
import nimble.portable_blueprints.robautile.AdvancedButton;
import nimble.portable_blueprints.robautile.Operazioni;
import nimble.portable_blueprints.robautile.RenderingMethods;

public class TabletMinePrintScreen extends Screen {
   private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("portable_blueprints", "textures/gui/tablet/tablet_mineprint_gui.png");
   private int CentroX;
   private int CentroY;
   private int W;
   private int H;
   private TabletMinePrintScreen.PageType pageType;
   private List<TabletMinePrintMethods.BlueprintPreviewItem> SavedBlueprintPreviewItems = new ArrayList<>();
   private List<TabletMinePrintMethods.BlueprintPreviewItem> blueprintPreviewItems = new ArrayList<>();
   private int dimPrimaDatas = 0;
   private int YList = 0;
   private int MaxYList = 0;
   private SearchBarElement SBE = null;
   private TabletScrollBar scrollBar;
   private boolean singleShot = false;
   private short TickCount = 1;

   protected TabletMinePrintScreen(Component component) {
      super(component);
   }

   public TabletMinePrintScreen(TabletMinePrintScreen.PageType pageType) {
      super(Component.literal("portable_blueprint.tablet.mineprint"));
      this.pageType = pageType;
      this.dimPrimaDatas = 0;
      if (pageType.equals(TabletMinePrintScreen.PageType.SERVER)) {
         CtoS_ServerBlueprintsData.send();
      } else if (pageType.equals(TabletMinePrintScreen.PageType.GLOBAL)) {
         CtoS_SharedBlueprintData.send();
         ServerToClientDatasShared.SharedWithMeBlueprints.SharedWithMeBlueprints.clear();
      } else if (pageType.equals(TabletMinePrintScreen.PageType.FOLLOW)) {
         CtoS_ServerBlueprintsData.send();
         CtoS_SharedBlueprintData.send();
         ServerToClientDatasShared.SharedWithMeBlueprints.SharedWithMeBlueprints.clear();
      }
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
      short count = 0;

      for (AdvancedButton AB : this.getButtonsMinePrints(this.CentroX, this.CentroY)) {
         if (count == 0 && this.pageType.equals(TabletMinePrintScreen.PageType.FOLLOW)
            || count == 1 && this.pageType.equals(TabletMinePrintScreen.PageType.GLOBAL)
            || count == 2 && this.pageType.equals(TabletMinePrintScreen.PageType.SERVER)) {
            AB.PressedImageCondition = true;
            AB.active = false;
         }

         this.addRenderableWidget(AB);
         count++;
      }

      this.addRenderableWidget(TabletScreen.getTabletButton(this.CentroX, this.CentroY));
      this.addRenderableWidget(
         this.scrollBar = new TabletScrollBar(
            this.CentroX + 68,
            this.CentroY - 60,
            1,
            126,
            -12696497,
            -4406063,
            new TabletScrollBar.ScrollingArea(this.CentroX - 70, this.CentroY - 68, 140, 157, 5)
         )
      );
      if (this.blueprintPreviewItems.size() <= 2) {
         this.scrollBar.visible = false;
      }
   }

   public List<AdvancedButton> getButtonsMinePrints(int CentroX, int CentroY) {
      List<AdvancedButton> Buttons = new ArrayList<>();
      int px = CentroX - 52;
      int py = CentroY + 72;
      Buttons.add(
         AdvancedButton.builder(Component.literal(""), pAdvanceButton -> this.B_SetPageMinePrint((short)0))
            .pos(px, py)
            .size(8, 13)
            .defaultImage(new AdvancedButton.Image(TEXTURE, px, py, 4, 209, 8, 13))
            .hoverImage(new AdvancedButton.Image(TEXTURE, px, py, 20, 209, 8, 13))
            .pressedImageCondition(new AdvancedButton.Image(TEXTURE, px, py, 20, 209, 8, 13))
            .tooltip(Tooltip.create(Component.translatable("portable_blueprints.gui.tablet.mineprint.area.follow_title")))
            .build()
      );
      px = CentroX - 6;
      py = CentroY + 73;
      Buttons.add(
         AdvancedButton.builder(Component.literal(""), pAdvanceButton -> this.B_SetPageMinePrint((short)1))
            .pos(px, py)
            .size(12, 12)
            .defaultImage(new AdvancedButton.Image(TEXTURE, px, py, 34, 210, 12, 12))
            .hoverImage(new AdvancedButton.Image(TEXTURE, px, py, 50, 210, 12, 12))
            .pressedImageCondition(new AdvancedButton.Image(TEXTURE, px, py, 50, 210, 12, 12))
            .tooltip(Tooltip.create(Component.translatable("portable_blueprints.gui.tablet.mineprint.area.players_title")))
            .build()
      );
      px = CentroX + 38;
      py = CentroY + 72;
      Buttons.add(
         AdvancedButton.builder(Component.literal(""), pAdvanceButton -> this.B_SetPageMinePrint((short)2))
            .pos(px, py)
            .size(16, 15)
            .defaultImage(new AdvancedButton.Image(TEXTURE, px, py, 64, 209, 16, 15))
            .hoverImage(new AdvancedButton.Image(TEXTURE, px, py, 80, 209, 16, 15))
            .pressedImageCondition(new AdvancedButton.Image(TEXTURE, px, py, 80, 209, 16, 15))
            .tooltip(Tooltip.create(Component.translatable("portable_blueprints.gui.tablet.mineprint.area.server_title")))
            .build()
      );
      return Buttons;
   }

   private void B_SetPageMinePrint(short p) {
      if (p == 0) {
         OpenGUI(TabletMinePrintScreen.PageType.FOLLOW);
      } else if (p == 1) {
         OpenGUI(TabletMinePrintScreen.PageType.GLOBAL);
      } else if (p == 2) {
         OpenGUI(TabletMinePrintScreen.PageType.SERVER);
      }
   }

   private void MakeList() {
      this.blueprintPreviewItems.clear();
      int size = this.pageType.equals(TabletMinePrintScreen.PageType.FOLLOW)
         ? TabletClientDatas.getDatas().followedData.size()
         : (
            this.pageType.equals(TabletMinePrintScreen.PageType.GLOBAL)
               ? ServerToClientDatasShared.SharedWithMeBlueprints.SharedWithMeBlueprints.size()
               : ServerToClientDatasShared.ServerBlueprints.size()
         );
      int count = 0;
      int negativecount = 0;
      List<Tuple<String, UUID>> toRemoveFromFollowed = new ArrayList<>();
      if (this.SavedBlueprintPreviewItems.isEmpty()) {
         for (int x = 0; x < size; x++) {
            String blueprint_name = "";
            UUID owner = null;
            TabletMinePrintMethods.TYPE type = null;
            if (this.pageType.equals(TabletMinePrintScreen.PageType.FOLLOW)) {
               Tuple<String, UUID> data = TabletClientDatas.getDatas().followedData.get(x);
               blueprint_name = Blueprint.getNomeByBlueprintID((String)data.getA());
               System.out.println("nome blueprint: " + (String)data.getA() + " | " + blueprint_name);
               owner = (UUID)data.getB();
               if (owner == null) {
                  if (!ServerToClientDatasShared.ServerBlueprints.contains(blueprint_name)) {
                     toRemoveFromFollowed.add(data);
                     continue;
                  }
               } else if (!ServerToClientDatasShared.SharedWithMeBlueprints
                  .SharedWithMeBlueprints
                  .contains("<owner>" + owner.toString() + "</owner><name>" + blueprint_name + "</name>")) {
                  toRemoveFromFollowed.add(data);
                  continue;
               }

               type = owner == null ? TabletMinePrintMethods.TYPE.SERVER : TabletMinePrintMethods.TYPE.SHARED_GLOBAL;
            } else if (this.pageType.equals(TabletMinePrintScreen.PageType.GLOBAL)) {
               String data = ServerToClientDatasShared.SharedWithMeBlueprints.SharedWithMeBlueprints.get(x);
               owner = UUID.fromString(Operazioni.getValueInsideTag(data, "owner"));
               blueprint_name = Operazioni.getValueInsideTag(data, "name");
               type = TabletMinePrintMethods.TYPE.SHARED_GLOBAL;
            } else if (this.pageType.equals(TabletMinePrintScreen.PageType.SERVER)) {
               blueprint_name = ServerToClientDatasShared.ServerBlueprints.get(x);
               type = TabletMinePrintMethods.TYPE.SERVER;
            }

            TabletMinePrintMethods.BlueprintPreviewItem bpi = new TabletMinePrintMethods.BlueprintPreviewItem(blueprint_name, type, owner);
            this.SavedBlueprintPreviewItems.add(bpi);
         }
      }

      for (Tuple<String, UUID> data : toRemoveFromFollowed) {
         if (TabletClientDatas.getDatas().followedData.contains(data)) {
            TabletClientDatas.Datas datas = TabletClientDatas.getDatas();
            datas.followedData.remove(data);
            TabletClientDatas.setDatas(datas);
         }
      }

      if (!toRemoveFromFollowed.isEmpty()) {
         TabletClientDatas.save();
      }

      size = this.SavedBlueprintPreviewItems.size();

      for (int x = 0; x < this.SavedBlueprintPreviewItems.size(); x++) {
         TabletMinePrintMethods.BlueprintPreviewItem bpi = this.SavedBlueprintPreviewItems.get(x);
         boolean nonHaIlTag = false;
         if (this.SBE != null && !this.SBE.getSelectedTags().isEmpty()) {
            for (ModTabletTags.TagData tagData : this.SBE.getSelectedTags()) {
               if (!bpi.hasTag(tagData)) {
                  nonHaIlTag = true;
                  break;
               }
            }
         }

         if ((this.SBE == null || bpi.BName.toLowerCase().contains(this.SBE.getValue().toLowerCase())) && !nonHaIlTag) {
            bpi.setPosition(size != 1 && (size - 1 != count || count % 2 != 0) ? (count % 2 == 1 ? 3 : -67) : -32, -60 + 90 * (int)Math.floor(count / 2.0F));
            this.blueprintPreviewItems.add(bpi);
            count++;
         } else {
            negativecount++;
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

   public void render(GuiGraphics gg, int mouseX, int mouseY, float delta) {
      if (this.scrollBar != null) {
         this.YList = (int)this.scrollBar.getValore(this.MaxYList);
      }

      boolean valoreAggiornato = this.pageType.equals(TabletMinePrintScreen.PageType.SERVER)
         ? ServerToClientDatasShared.endTrasferingServerBlueprints
         : (
            this.pageType.equals(TabletMinePrintScreen.PageType.GLOBAL)
               ? ServerToClientDatasShared.endTrasferingSharedWithMeBlueprints
               : ServerToClientDatasShared.endTrasferingSharedWithMeBlueprints && ServerToClientDatasShared.endTrasferingServerBlueprints
         );
      if (valoreAggiornato && !this.singleShot) {
         this.singleShot = true;
         this.MakeList();
      }

      if (this.SBE != null && this.SBE.isValueChanged()) {
         this.MakeList();
      }

      TabletScreen.renderTabletBackground(gg, this.CentroX, this.CentroY, "b2");
      RenderingMethods.enableScissorFromGui(Minecraft.getInstance(), this.CentroX - 70, this.CentroY - 68, 140, 144);
      Component tooltipDaVisualizzare = null;

      try {
         for (TabletMinePrintMethods.BlueprintPreviewItem bpi : this.blueprintPreviewItems) {
            bpi.addToPosY(-this.YList);
            if (bpi.posY >= -150 && bpi.posY <= 90) {
               bpi.createIconStructure = true;
               Component tdv = bpi.render(gg, this.CentroX, this.CentroY, mouseX, mouseY, new Vec2(-70.0F, 70.0F), new Vec2(-67.0F, 70.0F));
               if (tdv != null) {
                  tooltipDaVisualizzare = tdv;
               }
            }
         }
      } catch (Exception var11) {
      }

      RenderingMethods.disableScissor();
      if (tooltipDaVisualizzare != null) {
         gg.renderTooltip(Minecraft.getInstance().font, tooltipDaVisualizzare, mouseX, mouseY);
      }

      this.renderBottomBar(gg);

      try {
         super.render(gg, mouseX, mouseY, delta);
      } catch (Exception var10) {
      }

      TabletScreen.renderTabletUpBar(gg, this.CentroX, this.CentroY);
   }

   public void renderBottomBar(GuiGraphics gg) {
      int px = this.CentroX - 70;
      int py = this.CentroY + 69;
      gg.fill(px, py, px + 140, py + 20, -13683397);
   }

   private void re_init() {
      this.clearWidgets();

      this.init();
   }

   public void tick() {
      super.tick();
      if (++this.TickCount > 20) {
         this.TickCount = 1;
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      TabletMinePrintMethods.BlueprintPreviewItem bpi_last = null;
      TabletMinePrintMethods.BlueprintPreviewItem bpi_last2 = null;

      for (TabletMinePrintMethods.BlueprintPreviewItem bpi : this.blueprintPreviewItems) {
         if (bpi.isMHItem) {
            bpi_last = bpi;
         } else if (bpi.isMHFollow) {
            bpi_last2 = bpi;
         }
      }

      if (bpi_last != null) {
         bpi_last.openBlueprintInfo(false);
      } else if (bpi_last2 != null) {
         bpi_last2.followAction();
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

   public static void OpenGUI(TabletMinePrintScreen.PageType pageType) {
      Minecraft.getInstance().setScreen(new TabletMinePrintScreen(pageType));
   }

   public void onClose() {
      TabletScreen.resetTablet();
      super.onClose();
   }

   protected void renderBlurredBackground(float partialTick) {
   }

   public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
   }

   public enum PageType {
      FOLLOW,
      GLOBAL,
      SERVER;
   }
}
