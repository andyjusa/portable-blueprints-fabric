package nimble.portable_blueprints.client.screen.tablet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import nimble.portable_blueprints.fabric.client.RenderLevelStageEvent;
import nimble.portable_blueprints.fabric.client.RenderLevelStageEvent.Stage;
import nimble.portable_blueprints.fabric.network.PacketDistributor;
import nimble.portable_blueprints.altro.Blueprint;
import nimble.portable_blueprints.client.BlueprintsPreviewMethods;
import nimble.portable_blueprints.client.ServerToClientDatasShared;
import nimble.portable_blueprints.client.VisualizeBlueprintDatas;
import nimble.portable_blueprints.client.screen.tablet.mineprint.TabletMinePrintMethods;
import nimble.portable_blueprints.client.screen.tablet.mineprint.TabletMinePrintScreen;
import nimble.portable_blueprints.init.ModTabletTags;
import nimble.portable_blueprints.network.CtoS_CreaItemBlueprint;
import nimble.portable_blueprints.network.SharedBlueprints.CtoS_RemoveShareWithOthers;
import nimble.portable_blueprints.network.blueprint_tags.CtoS_GetBlueprintTags;
import nimble.portable_blueprints.network.blueprint_tags.CtoS_SetBlueprintTag;
import nimble.portable_blueprints.network.request_specific_data.CtoS_RequestSpecificData;
import nimble.portable_blueprints.network.rimuovi_blueprint.CtoS_BlueprintRemove;
import nimble.portable_blueprints.network.save_global_blueprint.CtoS_SaveGlobalBlueprint;
import nimble.portable_blueprints.network.save_global_blueprint.StoC_SaveGlobalBlueprint;
import nimble.portable_blueprints.portable_blueprints.OldVersionUtilities;
import nimble.portable_blueprints.robautile.AdvancedButton;
import nimble.portable_blueprints.robautile.Operazioni;
import nimble.portable_blueprints.robautile.RenderingMethods;

public class TabletBlueprintInfoScreen extends Screen {
   private static final Gson GSON = nimble.portable_blueprints.fabric.BlueprintJson.create();
   private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("portable_blueprints", "textures/gui/tablet/tablet_blueprint_gui.png");
   private static int CentroX;
   private static int CentroY;
   private static int W;
   private static int H;
   private RenderingMethods.GuiGraphicsElement BG;
   private static BlueprintsPreviewMethods.BlueprintPreviewData BPD;
   private AdvancedButton B_TAGS;
   private AdvancedButton B_EXPORT;
   private AdvancedButton B_GETBLUEPRINT;
   private AdvancedButton B_SHARE;
   private AdvancedButton B_RESETP;
   private AdvancedButton B_BAN;
   private static boolean inPreparazione = false;
   private static boolean isOpen = false;
   private String Titolo = "";
   private static float rotX = 0.0F;
   private static float rotY = 0.0F;
   private static float zoom = 0.0F;
   private static float offsetX = 0.0F;
   private static float offsetY = 0.0F;
   private static int MAX_ZOOM = 0;
   private static int MAX_OFFSET = 0;
   private int ModeScreen = 0;
   private static int BlueprintDataID = -1;
   private static Blueprint.BlueprintDatas visualizeBlueprintDatas = null;
   private boolean isGlobalBPAllowed = false;
   private RenderingMethods.GuiGraphicsElement TrashImage;
   private boolean TenendoPremutoTrash = false;
   private Long startTimeForTrash = 0L;
   private UUID BPOwner;
   private int nblocks = 0;
   private BufferedImage ImageFaccia;
   private String OwnerName = "";
   private boolean inTagMode = false;
   int XListTag = 0;
   int MaxXListTag = 0;
   private final List<TabletBlueprintInfoScreen.ItemTag> ItemTags = new ArrayList<>();
   private String IDStruttura;
   private List<ModTabletTags.TagData> TagsBlueprint = new ArrayList<>();
   private boolean TagImpostati = false;
   private Long timeoutTagImpostati = 0L;
   private boolean openAreaIsFollow = false;
   private boolean oneShot = false;
   private short TickCount = 1;
   private boolean startDrag = false;

   protected TabletBlueprintInfoScreen(Component component) {
      super(component);
   }

   public TabletBlueprintInfoScreen(String titolo, int modeScreen, UUID BPOwner, boolean openAreaIsFollow) {
      super(Component.literal("portable_blueprint.tablet"));
      this.ModeScreen = modeScreen;
      this.BPOwner = BPOwner;
      this.openAreaIsFollow = openAreaIsFollow;
      if (BPOwner != null) {
         Thread tickerThread = new Thread(
            () -> {
               try {
                  this.ImageFaccia = Operazioni.getPlayerFace(BPOwner);
                  this.OwnerName = Minecraft.getInstance().player.getUUID().equals(BPOwner)
                     ? Minecraft.getInstance().player.getName().getString()
                     : Operazioni.getPlayerNameFromUUID(BPOwner);
               } catch (Exception e) {
                  e.printStackTrace();
               }
            }
         );
         tickerThread.setDaemon(true);
         tickerThread.start();
      } else {
         this.OwnerName = Component.translatable("portable_blueprints.gui.tablet.blueprint_info.info.by_server").getString();
      }

      if (BlueprintDataID != -1 && VisualizeBlueprintDatas.BlueprintsMap.containsKey(BlueprintDataID)) {
         VisualizeBlueprintDatas.BlueprintsMap.remove(BlueprintDataID);
      }

      visualizeBlueprintDatas = null;
      if (modeScreen == 0) {
         BlueprintDataID = VisualizeBlueprintDatas.newVisualizeBlueprint(Minecraft.getInstance().player.getUUID(), titolo, false, false);
      } else if (modeScreen == 1) {
         BlueprintDataID = VisualizeBlueprintDatas.newVisualizeBlueprint(BPOwner, titolo, false, false);
      } else if (modeScreen == 2) {
         BlueprintDataID = VisualizeBlueprintDatas.newVisualizeBlueprint(true, titolo, false, false);
      }

      rotX = 0.0F;
      rotY = 0.0F;
      zoom = 0.0F;
      offsetX = 0.0F;
      offsetY = 0.0F;
      MAX_ZOOM = 0;
      MAX_OFFSET = 0;
      isOpen = true;
      this.Titolo = titolo;
      inPreparazione = false;
      if (BPD != null) {
         BPD.cleanupStructureData();
      }

      BPD = null;
      CtoS_RequestSpecificData.send(510, CtoS_RequestSpecificData.TipoRichiesta.ALLOW_GLOBAL_BLUEPRINTS);
      CtoS_RequestSpecificData.send(770, CtoS_RequestSpecificData.TipoRichiesta.IS_PLAYER_OP);
      this.timeoutTagImpostati = System.currentTimeMillis() + 5000L;
   }

   public void onClose() {
      this.reset();
      TabletScreen.resetTablet();
      super.onClose();
   }

   protected void init() {
      super.init();
      W = this.width;
      H = this.height;
      CentroX = W / 2;
      CentroY = H / 2;
      this.isGlobalBPAllowed = ServerToClientDatasShared.getRichiestaBooleana(510) == 1;
      this.BG = new RenderingMethods.GuiGraphicsElement(
         new RenderingMethods.GuiGraphicsElement.BLIT_Parametri(CentroX - 67, CentroY - 78, 0, 0, 134, 160, TEXTURE)
      );
      int px = CentroX - 65;
      int py = CentroY - 76;
      this.addRenderableWidget(
         AdvancedButton.builder(Component.literal(""), pAdvanceButton -> this.Back())
            .pos(px, py)
            .size(9, 8)
            .defaultImage(new AdvancedButton.Image(TEXTURE, px, py, 247, 16, 9, 8))
            .hoverImage(new AdvancedButton.Image(TEXTURE, px, py, 247, 24, 9, 8))
            .tooltip(Tooltip.create(Component.translatable("portable_blueprints.gui.tablet.blueprint_info.button.back")))
            .build()
      );
      py += 11;
      this.addRenderableWidget(
         this.B_RESETP = AdvancedButton.builder(Component.literal(""), pAdvanceButton -> this.B_Reset())
            .pos(px, py)
            .size(9, 9)
            .defaultImage(new AdvancedButton.Image(TEXTURE, px, py, 212, 0, 9, 9))
            .hoverImage(new AdvancedButton.Image(TEXTURE, px, py, 203, 0, 9, 9))
            .tooltip(Tooltip.create(Component.translatable("portable_blueprints.gui.tablet.blueprint_info.button.reset_preview")))
            .build()
      );
      px = CentroX + 51;
      py = CentroY - 76;
      this.addRenderableWidget(
         this.B_GETBLUEPRINT = AdvancedButton.builder(Component.literal(""), pAdvanceButton -> this.B_BlueprintGetItem())
            .pos(px, py)
            .size(15, 12)
            .defaultImage(new AdvancedButton.Image(TEXTURE, px, py, 241, 191, 15, 16))
            .hoverImage(new AdvancedButton.Image(TEXTURE, px, py, this.ModeScreen == 2 ? 211 : (this.ModeScreen == 3 ? 196 : 226), 191, 15, 16))
            .tooltip(
               Tooltip.create(
                  Component.translatable(
                     "portable_blueprints.gui.tablet.blueprint_info.button." + (this.ModeScreen == 3 ? "import_blueprint" : "get_blueprint")
                  )
               )
            )
            .build()
      );
      py += 21;
      if (this.ModeScreen == 0 || this.ModeScreen == 3) {
         this.TrashImage = new RenderingMethods.GuiGraphicsElement(new RenderingMethods.GuiGraphicsElement.BLIT_Parametri(px, py, 241, 225, 15, 15, TEXTURE));
         py += 20;
         if (this.ModeScreen == 0) {
            this.addRenderableWidget(
               this.B_SHARE = AdvancedButton.builder(
                     Component.literal(""), pAdvanceButton -> TabletBlueprintShareScreen.OpenGUI(this.Titolo, this.ModeScreen, this.BPOwner)
                  )
                  .pos(px, py)
                  .size(15, 17)
                  .defaultImage(new AdvancedButton.Image(TEXTURE, px, py, 241, 207, 15, 17))
                  .hoverImage(new AdvancedButton.Image(TEXTURE, px, py, 225, 207, 15, 17))
                  .tooltip(Tooltip.create(Component.translatable("portable_blueprints.gui.tablet.blueprint_info.button.share_blueprint")))
                  .build()
            );
            py += 22;
            if (this.isGlobalBPAllowed) {
               this.addRenderableWidget(
                  this.B_EXPORT = AdvancedButton.builder(Component.literal(""), pAdvanceButton -> this.B_ExportToGlobal())
                     .pos(px, py)
                     .size(15, 15)
                     .defaultImage(new AdvancedButton.Image(TEXTURE, px, py, 241, 241, 15, 15))
                     .hoverImage(new AdvancedButton.Image(TEXTURE, px, py, 226, 241, 15, 15))
                     .tooltip(Tooltip.create(Component.translatable("portable_blueprints.gui.tablet.blueprint_info.button.export_blueprint")))
                     .build()
               );
               py += 18;
            }
         }
      }

      if (ServerToClientDatasShared.getRichiestaBooleana(770) == 1 && this.ModeScreen == 1) {
         this.addRenderableWidget(
            this.B_BAN = AdvancedButton.builder(Component.literal(""), pAdvanceButton -> this.B_BAN())
               .pos(px, py)
               .size(15, 15)
               .defaultImage(new AdvancedButton.Image(TEXTURE, px, py, 241, 176, 15, 15))
               .hoverImage(new AdvancedButton.Image(TEXTURE, px, py, 226, 176, 15, 15))
               .tooltip(Tooltip.create(Component.translatable("portable_blueprints.gui.tablet.blueprint_info.button.ban")))
               .build()
         );
      }

      if (this.ModeScreen != 3) {
         px = CentroX - 63;
         py = CentroY + 63;
         this.addRenderableWidget(
            this.B_TAGS = AdvancedButton.builder(Component.literal(""), pAdvanceButton -> this.TagMode())
               .pos(px, py)
               .size(15, 15)
               .defaultImage(new AdvancedButton.Image(TEXTURE, px - 1, py - 1, 64, 240, 16, 16))
               .pressedImageCondition(new AdvancedButton.Image(TEXTURE, px - 1, py - 1, 80, 240, 16, 16))
               .build()
         );
      }

      this.addRenderableWidget(TabletScreen.getTabletButton(CentroX, CentroY));
      this.MakeTagList();
   }

   private void B_BAN() {
      if (this.BPOwner != null) {
         PacketDistributor.sendToServer(new CtoS_RemoveShareWithOthers(this.Titolo, this.BPOwner, this.BPOwner), new CustomPacketPayload[0]);
         this.Back();
      }
   }

   private void TagMode() {
      System.out.println(this.TagImpostati);
      if ((this.BPOwner != null || ServerToClientDatasShared.getRichiestaBooleana(770) != -1)
         && (this.BPOwner == null || this.BPOwner.equals(Minecraft.getInstance().player.getUUID()))
         && this.TagImpostati) {
         this.inTagMode = !this.inTagMode;
         this.MakeTagList();
      }
   }

   private void B_ExportToGlobal() {
      if (!StoC_SaveGlobalBlueprint.StaGiaGestendoUnaBlueprint && this.isGlobalBPAllowed) {
         String nome = this.Titolo;

         for (int tentativi = 1; Blueprint.globalBlueprintExist(nome); tentativi++) {
            nome = this.Titolo + "(" + tentativi + ")";
         }

         PacketDistributor.sendToServer(new CtoS_SaveGlobalBlueprint(this.Titolo + ";" + nome), new CustomPacketPayload[0]);
      }
   }

   private void B_Reset() {
      offsetX = 0.0F;
      offsetY = 0.0F;
      rotX = 0.0F;
      rotY = 0.0F;
   }

   private void B_RemoveBlueprint() {
      if (this.ModeScreen == 0) {
         PacketDistributor.sendToServer(new CtoS_BlueprintRemove(this.Titolo), new CustomPacketPayload[0]);
         this.Back();
      } else if (this.ModeScreen == 3) {
         Blueprint.removeGlobalBlueprint(this.Titolo);
         this.Back();
      }
   }

   private void B_BlueprintGetItem() {
      if (this.ModeScreen == 0) {
         CtoS_CreaItemBlueprint.send(this.Titolo);
      } else if (this.ModeScreen == 1) {
         CtoS_CreaItemBlueprint.send(this.BPOwner, this.Titolo);
      } else if (this.ModeScreen == 2) {
         CtoS_CreaItemBlueprint.send(true, this.Titolo);
      } else if (this.ModeScreen == 3) {
         CtoS_RequestSpecificData.send(507, CtoS_RequestSpecificData.TipoRichiesta.MAX_SCANNER_BLOCK_COUNT);
         CtoS_RequestSpecificData.send(508, CtoS_RequestSpecificData.TipoRichiesta.MAX_DIGITIZED_STRUCTURES_FOR_PLAYER);
         CtoS_RequestSpecificData.send(
            ServerToClientDatasShared.ID_PLAYER_BLUEPRINTS_QUANTITY, CtoS_RequestSpecificData.TipoRichiesta.PLAYER_BLUEPRINTS_QUANTITY
         );
         Blueprint.sendToServerGlobalBlueprint(this.Titolo);
      }
   }

   private void Back() {
      this.reset();
      if (this.openAreaIsFollow) {
         TabletMinePrintScreen.OpenGUI(TabletMinePrintScreen.PageType.FOLLOW);
      } else {
         if (this.ModeScreen == 0 || this.ModeScreen == 3) {
            TabletBlueprintScreen.OpenGUI();
         }

         if (this.ModeScreen == 1) {
            TabletMinePrintScreen.OpenGUI(TabletMinePrintScreen.PageType.GLOBAL);
         } else if (this.ModeScreen == 2) {
            TabletMinePrintScreen.OpenGUI(TabletMinePrintScreen.PageType.SERVER);
         }
      }
   }

   public void render(GuiGraphics gg, int mouseX, int mouseY, float delta) {
      if (!this.oneShot && ServerToClientDatasShared.getRichiestaBooleana(510) != -1) {
         this.oneShot = true;
         this.re_init();
      }

      if (BPD == null && this.ModeScreen == 3) {
         try {
            try (Reader reader = new FileReader(new File(Blueprint.getGlobalBlueprintDir(), this.Titolo + ".bp"))) {
               visualizeBlueprintDatas = (Blueprint.BlueprintDatas)GSON.fromJson(reader, Blueprint.BlueprintDatas.class);
            } catch (Exception e) {
               e.printStackTrace();
            }

            BPD = new BlueprintsPreviewMethods.BlueprintPreviewData(visualizeBlueprintDatas);
            BPD.prepareData(null);
            this.nblocks = visualizeBlueprintDatas.getAllBlocksCount();
         } catch (Exception e) {
            e.printStackTrace();
         }
      } else if (this.ModeScreen != 3) {
         if (BPD == null && VisualizeBlueprintDatas.BlueprintsMap.containsKey(BlueprintDataID)) {
            visualizeBlueprintDatas = VisualizeBlueprintDatas.BlueprintsMap.get(BlueprintDataID);
            this.IDStruttura = Blueprint.getIDBlueprint(this.BPOwner == null ? "server" : this.BPOwner.toString(), visualizeBlueprintDatas);
            this.nblocks = visualizeBlueprintDatas.getAllBlocksCount();
            BPD = new BlueprintsPreviewMethods.BlueprintPreviewData(visualizeBlueprintDatas);
            VisualizeBlueprintDatas.BlueprintsMap.remove(BlueprintDataID);
            ServerToClientDatasShared.BlueprintTags.remove(this.IDStruttura);
            PacketDistributor.sendToServer(new CtoS_GetBlueprintTags(this.BPOwner, this.Titolo), new CustomPacketPayload[0]);
         }

         if (this.IDStruttura != null
            && ServerToClientDatasShared.BlueprintTags.containsKey(this.IDStruttura)
            && this.TagsBlueprint.isEmpty()
            && !this.TagImpostati) {
            this.TagImpostati = true;

            for (String v : ServerToClientDatasShared.BlueprintTags.get(this.IDStruttura)) {
               ModTabletTags.TagData td = ModTabletTags.getTagByID(v);
               if (td != null) {
                  this.TagsBlueprint.add(td);
               }
            }

            ServerToClientDatasShared.BlueprintTags.remove(this.IDStruttura);
            this.MakeTagList();
         }

         if (!this.TagImpostati && System.currentTimeMillis() > this.timeoutTagImpostati) {
            this.TagImpostati = true;
            this.timeoutTagImpostati = 0L;
         }
      }

      if (this.B_TAGS != null) {
         this.B_TAGS.PressedImageCondition = this.inTagMode;
      }

      Minecraft mc = Minecraft.getInstance();

      for (int x = 0; x < 4; x++) {
         if (x == 0) {
            RenderingMethods.enableScissorFromGui(mc, CentroX - 78, CentroY - 104, 23, 210);
         } else if (x == 1) {
            RenderingMethods.enableScissorFromGui(mc, CentroX - 78, CentroY - 104, 180, 28);
         } else if (x == 2) {
            RenderingMethods.enableScissorFromGui(mc, CentroX - 78 + 128, CentroY - 104, 30, 210);
         } else if (x == 3) {
            RenderingMethods.enableScissorFromGui(mc, CentroX - 78, CentroY - 104 + 133, 180, 80);
         }

         TabletScreen.renderTabletBackground(gg, CentroX, CentroY, "b3");
         this.BG.render(gg);
         RenderingMethods.disableScissor();
      }

      try {
         super.render(gg, mouseX, mouseY, delta);
      } catch (Exception var11) {
      }

      if (BPD != null && BPD.isDatiVertexPronti) {
         gg.blit(TEXTURE, CentroX - 65, CentroY - 54, 222, 0, 9, 9);
         gg.blit(TEXTURE, CentroX - 65, CentroY - 43, 231, 0, 9, 9);
         if (visualizeBlueprintDatas != null && mouseX >= CentroX - 65 && mouseX < CentroX - 65 + 9 && mouseY >= CentroY - 54 && mouseY < CentroY - 54 + 9) {
            gg.renderTooltip(
               Minecraft.getInstance().font,
               List.of(
                  Component.literal(
                     Component.translatable("portable_blueprints.gui.tablet.blueprint_info.info.lenght.x")
                        .getString()
                        .replace("%v%", String.valueOf(visualizeBlueprintDatas.lunghezzaX))
                  ),
                  Component.literal(
                     Component.translatable("portable_blueprints.gui.tablet.blueprint_info.info.lenght.y")
                        .getString()
                        .replace("%v%", String.valueOf(visualizeBlueprintDatas.altezza))
                  ),
                  Component.literal(
                     Component.translatable("portable_blueprints.gui.tablet.blueprint_info.info.lenght.z")
                        .getString()
                        .replace("%v%", String.valueOf(visualizeBlueprintDatas.lunghezzaZ))
                  )
               ),
               Optional.empty(),
               mouseX,
               mouseY
            );
         } else if (visualizeBlueprintDatas != null
            && mouseX >= CentroX - 65
            && mouseX < CentroX - 65 + 9
            && mouseY >= CentroY - 43
            && mouseY < CentroY - 43 + 9) {
            gg.renderTooltip(
               Minecraft.getInstance().font,
               Component.literal(
                  Component.translatable("portable_blueprints.gui.tablet.blueprint_info.info.nblocks").getString().replace("%v%", String.valueOf(this.nblocks))
               ),
               mouseX,
               mouseY
            );
         }

         if (visualizeBlueprintDatas != null) {
            String testo = visualizeBlueprintDatas.nome;
            RenderingMethods.GuiGraphicsTestoScalato(
               gg,
               testo,
               testo.length() > 22 ? testo.length() / 35.0F : 1.0F,
               Minecraft.getInstance().font,
               new Vec2(CentroX - 1, CentroY + 34),
               0,
               false,
               RenderingMethods.PosizioneTesto.CENTRO,
               true
            );
            int l = Minecraft.getInstance().font.width(testo) / 2;
            if (l > 50) {
               l = 50;
            }

            gg.fill(CentroX - l - 5, CentroY + 38, CentroX + l + 2, CentroY + 39, -16777216);
         }

         if (this.ImageFaccia == null) {
            gg.blit(TEXTURE, CentroX - 64, CentroY + 42, this.BPOwner == null ? 48 : 32, 240, 16, 16);
         } else {
            for (int x = 0; x < this.ImageFaccia.getWidth(); x++) {
               for (int y = 0; y < this.ImageFaccia.getHeight(); y++) {
                  int color = this.ImageFaccia.getRGB(x, y);
                  int addX = 2 * x + CentroX - 64;
                  int addY = 2 * y + CentroY + 42;
                  gg.fill(addX, addY, addX + 2, addY + 2, color);
               }
            }
         }

         if (this.OwnerName != null) {
            RenderingMethods.GuiGraphicsTestoScalato(
               gg,
               this.OwnerName,
               this.OwnerName.length() < 14 ? 1.25F : Operazioni.lerp(1.25F, 1.1F, (this.OwnerName.length() - 14) / 2.0F),
               Minecraft.getInstance().font,
               new Vec2(CentroX - 46, CentroY + 52),
               0,
               false,
               RenderingMethods.PosizioneTesto.SINISTRA,
               true
            );
         }

         if (this.TrashImage != null) {
            this.TrashImage.render(gg);
            if (this.TenendoPremutoTrash && this.startTimeForTrash != 0L) {
               float time = 1.0F - (float)(this.startTimeForTrash - System.currentTimeMillis()) / 2000.0F;
               if (time > 1.2F) {
                  this.TenendoPremutoTrash = false;
                  this.startTimeForTrash = 0L;
                  this.B_RemoveBlueprint();
                  System.out.println("Elimina Blueprint Personale");
               }

               int h = (int)Math.min(Operazioni.lerp(1.0F, 15.0F, time), 15.0F);
               int v2 = 240 - h;
               gg.blit(TEXTURE, this.TrashImage.blitParametri.startPosX, this.TrashImage.blitParametri.startPosY - h + 15, 225, v2, 15, h);
            }
         }

         try {
            RenderingMethods.enableScissorFromGui(Minecraft.getInstance(), CentroX - 46, CentroY + 67, 110, 10);

            for (TabletBlueprintInfoScreen.ItemTag it : this.ItemTags) {
               it.render(gg);
            }

            RenderingMethods.disableScissor();
         } catch (Exception var16) {
         }

         if (this.TrashImage != null
            && mouseX >= this.TrashImage.blitParametri.startPosX
            && mouseX < this.TrashImage.blitParametri.startPosX + this.TrashImage.blitParametri.width
            && mouseY >= this.TrashImage.blitParametri.startPosY
            && mouseY < this.TrashImage.blitParametri.startPosY + this.TrashImage.blitParametri.height) {
            gg.renderTooltip(
               Minecraft.getInstance().font, Component.translatable("portable_blueprints.gui.tablet.blueprint_info.button.delete_blueprint"), mouseX, mouseY
            );
         }

         if (this.B_TAGS != null) {
            this.B_TAGS.visible = true;
         }

         if (this.B_EXPORT != null) {
            this.B_EXPORT.visible = true;
         }

         if (this.B_GETBLUEPRINT != null) {
            this.B_GETBLUEPRINT.visible = true;
         }

         if (this.B_SHARE != null) {
            this.B_SHARE.visible = true;
         }

         if (this.B_RESETP != null) {
            this.B_RESETP.visible = true;
         }

         if (this.B_BAN != null) {
            this.B_BAN.visible = true;
         }
      } else {
         RenderingMethods.GuiGraphicsTestoScalato(
            gg,
            Component.translatable("portable_blueprints.gui.tablet.blueprint_info.loading_data").getString(),
            1.0F,
            Minecraft.getInstance().font,
            new Vec2(CentroX - 2, CentroY - 30),
            16777215,
            false,
            RenderingMethods.PosizioneTesto.CENTRO,
            true
         );
         if (this.B_TAGS != null) {
            this.B_TAGS.visible = false;
         }

         if (this.B_EXPORT != null) {
            this.B_EXPORT.visible = false;
         }

         if (this.B_GETBLUEPRINT != null) {
            this.B_GETBLUEPRINT.visible = false;
         }

         if (this.B_SHARE != null) {
            this.B_SHARE.visible = false;
         }

         if (this.B_RESETP != null) {
            this.B_RESETP.visible = false;
         }

         if (this.B_BAN != null) {
            this.B_BAN.visible = false;
         }
      }

      TabletScreen.renderTabletUpBar(gg, CentroX, CentroY);
   }

   private void re_init() {
      this.clearWidgets();

      this.init();
   }

   public void MakeTagList() {
      this.ItemTags.clear();
      int offsetX = 0;

      for (ModTabletTags.TagData tagData : this.inTagMode ? ModTabletTags.TagDatas : this.TagsBlueprint) {
         TabletBlueprintInfoScreen.ItemTag it = new TabletBlueprintInfoScreen.ItemTag(tagData, CentroX + offsetX - 45, CentroY + 72);
         if (this.inTagMode) {
            for (ModTabletTags.TagData tagData1 : this.TagsBlueprint) {
               if (tagData.getOriginalTag().equals(tagData1.getOriginalTag())) {
                  it.isSelected = true;
                  break;
               }
            }
         }

         this.ItemTags.add(it);
         offsetX += it.getTextWidth() + 5;
      }

      this.MaxXListTag = offsetX - 105 - 10 + 1;
      System.err.println("v: " + this.MaxXListTag);
      if (this.MaxXListTag < 0) {
         this.MaxXListTag = 0;
      }

      if (this.XListTag > this.MaxXListTag) {
         this.XListTag = this.MaxXListTag;
      }
   }

   public void tick() {
      super.tick();
      if (++this.TickCount > 20) {
         this.TickCount = 1;
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.TrashImage != null
         && mouseX >= this.TrashImage.blitParametri.startPosX
         && mouseX < this.TrashImage.blitParametri.startPosX + this.TrashImage.blitParametri.width
         && mouseY >= this.TrashImage.blitParametri.startPosY
         && mouseY < this.TrashImage.blitParametri.startPosY + this.TrashImage.blitParametri.height) {
         this.TenendoPremutoTrash = true;
         this.startTimeForTrash = System.currentTimeMillis() + 2000L;
      } else if (this.inTagMode && mouseX >= CentroX - 46 && mouseX < CentroX - 46 + 110 && mouseY >= CentroY + 67 && mouseY < CentroY + 67 + 10) {
         for (TabletBlueprintInfoScreen.ItemTag it : this.ItemTags) {
            if (it.select((int)mouseX, (int)mouseY)) {
               break;
            }
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean isPauseScreen() {
      return false;
   }

   private void reset() {
      isOpen = false;
      if (BPD != null) {
         BPD.cleanupStructureData();
      }

      inPreparazione = false;
   }

   public static void onRenderLevel(RenderLevelStageEvent event) {
      if (Minecraft.getInstance().screen instanceof TabletBlueprintInfoScreen
         && (BlueprintsPreviewMethods.BlueprintPreviewData.AllUsedStage.contains(event.getStage()) || event.getStage().equals(Stage.AFTER_PARTICLES))) {
         PoseStack poseStack = event.getPoseStack();
         Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
         float yaw = camera.getYRot();
         float pitch = camera.getXRot();
         RenderingMethods.enableScissorFromGui(Minecraft.getInstance(), CentroX - 55, CentroY - 76, 105, 105);
         if (event.getStage().equals(Stage.AFTER_PARTICLES) || event.getStage().equals(Stage.AFTER_TRANSLUCENT_BLOCKS)) {
            float r = 0.18431373F;
            float g = 0.21176471F;
            float b = 0.2509804F;
            Vec3 _lookVec = Minecraft.getInstance().player.getLookAngle();
            _lookVec = _lookVec.scale(0.9F);
            poseStack.pushPose();
            poseStack.translate(_lookVec.x, _lookVec.y, _lookVec.z);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
            Tesselator tess = Tesselator.getInstance();
            BufferBuilder buf = tess.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            float scale = 0.8F;
            buf.addVertex(poseStack.last().pose(), -scale, -scale, 0.0F).setColor(r, g, b, 1.0F);
            buf.addVertex(poseStack.last().pose(), scale, -scale, 0.0F).setColor(r, g, b, 1.0F);
            buf.addVertex(poseStack.last().pose(), scale, scale, 0.0F).setColor(r, g, b, 1.0F);
            buf.addVertex(poseStack.last().pose(), -scale, scale, 0.0F).setColor(r, g, b, 1.0F);
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            BufferUploader.drawWithShader(buf.build());
            poseStack.popPose();
         }

         if (BPD != null && visualizeBlueprintDatas != null) {
            if (!BPD.isDatiVertexPronti && !inPreparazione) {
               inPreparazione = true;
               MAX_ZOOM = (int)((float)(Math.max(visualizeBlueprintDatas.altezza, visualizeBlueprintDatas.lunghezzaX) * 2.2) * 1.5F);
               MAX_OFFSET = MAX_ZOOM;
               BPD.prepareData(null);
            }

            if (BPD.isDatiVertexPronti) {
               Vec3 lookVec = Minecraft.getInstance().player.getLookAngle();
               Vec3 targetPos = lookVec.scale(Math.max(visualizeBlueprintDatas.altezza, visualizeBlueprintDatas.lunghezzaX) * 2.2 + zoom);
               poseStack = OldVersionUtilities.getEventPosestack();
               poseStack.pushPose();
               poseStack.translate(targetPos.x, targetPos.y, targetPos.z);
               poseStack.translate(0.0F, -offsetY, -offsetX);
               poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
               poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
               poseStack.mulPose(Axis.YP.rotationDegrees(-rotX));
               poseStack.mulPose(Axis.XP.rotationDegrees(rotY));
               poseStack.pushPose();
               poseStack.translate(
                  -visualizeBlueprintDatas.lunghezzaX / 2.0F, -visualizeBlueprintDatas.altezza / 5.0F, -visualizeBlueprintDatas.lunghezzaZ / 2.0F
               );
               RenderSystem.enablePolygonOffset();
               RenderSystem.polygonOffset(-1.0F, -1000000.0F);
               BPD.renderStructureCustomPosestack(poseStack, event.getProjectionMatrix(), event.getStage(), targetPos, true);
               RenderSystem.disablePolygonOffset();
               poseStack.popPose();
               poseStack.popPose();
            }
         }

         RenderingMethods.disableScissor();
      }
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
      if (this.startDrag || mouseX >= CentroX - 54 && mouseX < CentroX - 54 + 105 && mouseY >= CentroY - 76 && mouseY < CentroY - 76 + 105) {
         this.startDrag = true;
         float sensRot = 2.0F;
         float sensMov = Math.max(1.0E-4F, 1.0E-4F * (MAX_ZOOM * 2 * ((MAX_ZOOM + zoom) / (MAX_ZOOM * 2))));
         if (visualizeBlueprintDatas != null) {
            sensMov *= (float)Math.sqrt(
               visualizeBlueprintDatas.lunghezzaX * visualizeBlueprintDatas.lunghezzaX
                  + visualizeBlueprintDatas.altezza * visualizeBlueprintDatas.altezza
                  + visualizeBlueprintDatas.lunghezzaZ * visualizeBlueprintDatas.lunghezzaZ
            );
         }

         if (button == 0) {
            if (dragX > 0.0) {
               rotX -= sensRot;
            } else if (dragX < 0.0) {
               rotX += sensRot;
            }

            if (rotX >= 360.0F) {
               rotX -= 360.0F;
            } else if (rotX < 0.0F) {
               rotX += 360.0F;
            }

            if (dragY > 0.0) {
               rotY -= sensRot;
            } else if (dragY < 0.0) {
               rotY += sensRot;
            }

            if (rotY >= 360.0F) {
               rotY -= 360.0F;
            } else if (rotY < 0.0F) {
               rotY += 360.0F;
            }
         } else if (button == 1) {
         }
      }

      if (this.TrashImage != null
         && (
            !(mouseX >= this.TrashImage.blitParametri.startPosX)
               || !(mouseX < this.TrashImage.blitParametri.startPosX + this.TrashImage.blitParametri.width)
               || !(mouseY >= this.TrashImage.blitParametri.startPosY)
               || !(mouseY < this.TrashImage.blitParametri.startPosY + this.TrashImage.blitParametri.height)
         )) {
         this.TenendoPremutoTrash = false;
         this.startTimeForTrash = 0L;
      }

      return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.startDrag = false;
      this.TenendoPremutoTrash = false;
      this.startTimeForTrash = 0L;
      return super.mouseReleased(mouseX, mouseY, button);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double delta, double delta2) {
      float sens = 5.0F;
      if (mouseX >= CentroX - 54 && mouseX < CentroX - 54 + 105 && mouseY >= CentroY - 76 && mouseY < CentroY - 76 + 105) {
         zoom += (float)(sens * -delta2);
         if (zoom > MAX_ZOOM) {
            zoom = MAX_ZOOM;
         } else if (zoom < -MAX_ZOOM) {
            zoom = -MAX_ZOOM;
         }
      } else if (mouseX >= CentroX - 46 && mouseX < CentroX - 46 + 110 && mouseY >= CentroY + 67 && mouseY < CentroY + 67 + 10) {
         this.XListTag += (int)(10.0 * -delta2);
         System.err.println(this.MaxXListTag);
         if (this.XListTag > this.MaxXListTag) {
            this.XListTag = this.MaxXListTag;
         } else if (this.XListTag < 0) {
            this.XListTag = 0;
         }
      }

      return super.mouseScrolled(mouseX, mouseY, delta2, delta);
   }

   public static void OpenGUIByAppBlueprint(String Titolo) {
      Minecraft.getInstance().setScreen(new TabletBlueprintInfoScreen(Titolo, 0, null, false));
   }

   public static void OpenGUIByAppMinePrint(String Titolo, TabletMinePrintMethods.TYPE type, UUID BPOwner, boolean isOpenByFollowArea) {
      Minecraft.getInstance()
         .setScreen(
            new TabletBlueprintInfoScreen(
               Titolo,
               type.equals(TabletMinePrintMethods.TYPE.PERSONAL)
                  ? 0
                  : (type.equals(TabletMinePrintMethods.TYPE.SERVER) ? 2 : (type.equals(TabletMinePrintMethods.TYPE.PERSONAL_GLOBAL) ? 3 : 1)),
               BPOwner,
               isOpenByFollowArea
            )
         );
   }

   protected void renderBlurredBackground(float partialTick) {
   }

   public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
   }

   public class ItemTag {
      final ModTabletTags.TagData tagData;
      final int px;
      final int py;
      private boolean isSelected = false;

      public ItemTag(ModTabletTags.TagData tagData, int px, int py) {
         this.tagData = tagData;
         this.px = px;
         this.py = py;
      }

      public void render(GuiGraphics gg) {
         gg.fill(
            this.px - 1 - TabletBlueprintInfoScreen.this.XListTag,
            this.py - 5,
            this.px + this.getTextWidth() - TabletBlueprintInfoScreen.this.XListTag,
            this.py + 5,
            -3618616
         );
         RenderingMethods.GuiGraphicsTestoScalato(
            gg,
            this.tagData.getCurrentLenguageTag(),
            1.0F,
            Minecraft.getInstance().font,
            new Vec2(this.px - TabletBlueprintInfoScreen.this.XListTag, this.py),
            this.isSelected ? 255 : 0,
            false,
            RenderingMethods.PosizioneTesto.SINISTRA,
            true
         );
      }

      public int getTextWidth() {
         return Minecraft.getInstance().font.width(this.tagData.getCurrentLenguageTag());
      }

      public boolean select(int mouseX, int mouseY) {
         int _px = this.px - TabletBlueprintInfoScreen.this.XListTag;
         int _py = this.py - 4;
         if (mouseX >= _px && mouseX < _px + this.getTextWidth() - 1 && mouseY >= _py && mouseY < _py + 7) {
            this.isSelected = !this.isSelected;
            PacketDistributor.sendToServer(
               new CtoS_SetBlueprintTag(
                  TabletBlueprintInfoScreen.this.BPOwner, TabletBlueprintInfoScreen.this.Titolo, this.tagData.getOriginalTag(), this.isSelected
               ),
               new CustomPacketPayload[0]
            );
            if (this.isSelected) {
               TabletBlueprintInfoScreen.this.TagsBlueprint.add(this.tagData);
            } else {
               ModTabletTags.TagData to_remove = null;

               for (ModTabletTags.TagData td : TabletBlueprintInfoScreen.this.TagsBlueprint) {
                  if (td.getOriginalTag().equals(this.tagData.getOriginalTag())) {
                     to_remove = td;
                     break;
                  }
               }

               if (to_remove != null) {
                  TabletBlueprintInfoScreen.this.TagsBlueprint.remove(to_remove);
               }
            }

            return true;
         } else {
            return false;
         }
      }
   }
}
