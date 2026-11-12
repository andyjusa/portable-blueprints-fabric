package nimble.portable_blueprints.client.screen.tablet;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import nimble.portable_blueprints.fabric.network.PacketDistributor;
import nimble.portable_blueprints.client.ServerToClientDatasShared;
import nimble.portable_blueprints.client.screen.tablet.mineprint.TabletMinePrintMethods;
import nimble.portable_blueprints.network.SharedBlueprints.CtoS_AddShareWithOthers;
import nimble.portable_blueprints.network.SharedBlueprints.CtoS_RemoveShareWithOthers;
import nimble.portable_blueprints.network.ottieni_players_uuid_mondo.CtoS_GetWorldPlayersUUID;
import nimble.portable_blueprints.network.ottieni_shared_blueprint_data.CtoS_SharedBlueprintData;
import nimble.portable_blueprints.robautile.AdvancedButton;
import nimble.portable_blueprints.robautile.Operazioni;
import nimble.portable_blueprints.robautile.RenderingMethods;

public class TabletBlueprintShareScreen extends Screen {
   private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("portable_blueprints", "textures/gui/tablet/tablet_blueprint_gui.png");
   private static int CentroX;
   private static int CentroY;
   private static int W;
   private static int H;
   private RenderingMethods.GuiGraphicsElement BG;
   private String Titolo = "";
   private int ModeScreen = 0;
   private UUID BPOwner;
   private AdvancedButton B_PRIVATE;
   private AdvancedButton B_WHITELIST;
   private AdvancedButton B_PUBLIC;
   private short blueprint_state = 0;
   private List<TabletBlueprintShareScreen.PlayerItem> PlayerItems = new ArrayList<>();
   private int YLista = 0;
   private int MaxYLista = 0;
   private short TickCount = 1;

   protected TabletBlueprintShareScreen(Component component) {
      super(component);
   }

   public TabletBlueprintShareScreen(String titolo, int modeScreen, UUID BPOwner) {
      super(Component.literal("portable_blueprint.tablet"));
      this.ModeScreen = modeScreen;
      this.BPOwner = BPOwner;
      this.Titolo = titolo;
      CtoS_SharedBlueprintData.send();
      CtoS_GetWorldPlayersUUID.send();
      Thread tickerThread = new Thread(
         () -> {
            try {
               while (true) {
                  if (ServerToClientDatasShared.endTrasferingSharedWithMeBlueprints) {
                     if (ServerToClientDatasShared.SharedWithMeBlueprints.ContieneSWOBlueprint(Minecraft.getInstance().player.getUUID(), this.Titolo)) {
                        this.blueprint_state = 2;
                     }

                     while (!ServerToClientDatasShared.endTrasferingWorldPlayersUUID) {
                        Thread.sleep(10L);
                     }

                     Thread.sleep(100L);
                     int count = 0;

                     for (UUID uuid : ServerToClientDatasShared.WorldPlayersUUID) {
                        if (!uuid.equals(Minecraft.getInstance().player.getUUID())
                           && ServerToClientDatasShared.SharedWithMeBlueprints.ContieneSWOBlueprint(uuid, this.Titolo)) {
                           this.blueprint_state = 1;
                           break;
                        }
                     }

                     for (UUID uuid : ServerToClientDatasShared.WorldPlayersUUID) {
                        if (!uuid.equals(Minecraft.getInstance().player.getUUID())) {
                           count++;
                           TabletBlueprintShareScreen.PlayerItem pi = new TabletBlueprintShareScreen.PlayerItem(uuid);
                           pi.isInList = ServerToClientDatasShared.SharedWithMeBlueprints.ContieneSWOBlueprint(uuid, this.Titolo);
                           this.PlayerItems.add(pi);
                           this.MaxYLista = (count - 6) * 20;
                           if (this.MaxYLista < 0) {
                              this.MaxYLista = 0;
                           }

                           Thread.sleep(100L);
                        }
                     }
                     break;
                  }

                  Thread.sleep(1L);
               }
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
      );
      tickerThread.setDaemon(true);
      tickerThread.start();
   }

   public void onClose() {
      TabletScreen.resetTablet();
      super.onClose();
   }

   protected void init() {
      super.init();
      W = this.width;
      H = this.height;
      CentroX = W / 2;
      CentroY = H / 2;
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
      py = CentroY + 64;
      this.addRenderableWidget(
         this.B_PRIVATE = AdvancedButton.builder(
               Component.translatable("portable_blueprints.gui.tablet.blueprint_share.button.private"), pAdvanceButton -> this.SetState((short)0)
            )
            .pos(px, py)
            .size(42, 16)
            .defaultImage(new AdvancedButton.Image(TEXTURE, px, py, 0, 204, 42, 16))
            .hoverImage(new AdvancedButton.Image(TEXTURE, px, py, 42, 204, 42, 16))
            .pressedImageCondition(new AdvancedButton.Image(TEXTURE, px, py, 42, 204, 42, 16))
            .build()
      );
      px += 44;
      this.addRenderableWidget(
         this.B_WHITELIST = AdvancedButton.builder(
               Component.translatable("portable_blueprints.gui.tablet.blueprint_share.button.whitelist"), pAdvanceButton -> this.SetState((short)1)
            )
            .pos(px, py)
            .size(42, 16)
            .defaultImage(new AdvancedButton.Image(TEXTURE, px, py, 0, 204, 42, 16))
            .hoverImage(new AdvancedButton.Image(TEXTURE, px, py, 42, 220, 42, 16))
            .pressedImageCondition(new AdvancedButton.Image(TEXTURE, px, py, 42, 220, 42, 16))
            .build()
      );
      px += 44;
      this.addRenderableWidget(
         this.B_PUBLIC = AdvancedButton.builder(
               Component.translatable("portable_blueprints.gui.tablet.blueprint_share.button.public"), pAdvanceButton -> this.SetState((short)2)
            )
            .pos(px, py)
            .size(42, 16)
            .defaultImage(new AdvancedButton.Image(TEXTURE, px, py, 0, 204, 42, 16))
            .hoverImage(new AdvancedButton.Image(TEXTURE, px, py, 0, 220, 42, 16))
            .pressedImageCondition(new AdvancedButton.Image(TEXTURE, px, py, 0, 220, 42, 16))
            .build()
      );
      this.addRenderableWidget(TabletScreen.getTabletButton(CentroX, CentroY));
   }

   private void SetState(short state) {
      this.blueprint_state = state;
      if (state == 0 || state == 1) {
         PacketDistributor.sendToServer(
            new CtoS_RemoveShareWithOthers(this.Titolo, Minecraft.getInstance().player.getUUID(), Minecraft.getInstance().player.getUUID()),
            new CustomPacketPayload[0]
         );
      }

      if (state == 0 || state == 2) {
         for (TabletBlueprintShareScreen.PlayerItem pi : this.PlayerItems) {
            PacketDistributor.sendToServer(
               new CtoS_RemoveShareWithOthers(this.Titolo, Minecraft.getInstance().player.getUUID(), pi.PlayerUUID), new CustomPacketPayload[0]
            );
            pi.isInList = false;
         }
      }

      if (state != 0 && state != 1 && state == 2) {
         PacketDistributor.sendToServer(
            new CtoS_AddShareWithOthers(this.Titolo, Minecraft.getInstance().player.getUUID(), Minecraft.getInstance().player.getUUID()),
            new CustomPacketPayload[0]
         );
      }
   }

   private void Back() {
      TabletBlueprintInfoScreen.OpenGUIByAppMinePrint(
         this.Titolo,
         this.ModeScreen == 0
            ? TabletMinePrintMethods.TYPE.PERSONAL
            : (
               this.ModeScreen == 2
                  ? TabletMinePrintMethods.TYPE.SERVER
                  : (this.ModeScreen == 3 ? TabletMinePrintMethods.TYPE.PERSONAL_GLOBAL : TabletMinePrintMethods.TYPE.SHARED_GLOBAL)
            ),
         this.BPOwner,
         false
      );
   }

   public void render(GuiGraphics gg, int mouseX, int mouseY, float delta) {
      Minecraft mc = Minecraft.getInstance();
      boolean end = ServerToClientDatasShared.endTrasferingSharedWithMeBlueprints;
      if (this.B_PRIVATE != null) {
         this.B_PRIVATE.active = end;
         this.B_PRIVATE.PressedImageCondition = end && this.blueprint_state == 0;
         this.B_PRIVATE.setFontColor(!this.B_PRIVATE.isHovered() && !this.B_PRIVATE.PressedImageCondition ? 0 : 16777215);
      }

      if (this.B_WHITELIST != null) {
         this.B_WHITELIST.active = end;
         this.B_WHITELIST.PressedImageCondition = end && this.blueprint_state == 1;
         this.B_WHITELIST.setFontColor(!this.B_WHITELIST.isHovered() && !this.B_WHITELIST.PressedImageCondition ? 0 : 16777215);
      }

      if (this.B_PUBLIC != null) {
         this.B_PUBLIC.active = end;
         this.B_PUBLIC.PressedImageCondition = end && this.blueprint_state == 2;
         this.B_PUBLIC.setFontColor(!this.B_PUBLIC.isHovered() && !this.B_PUBLIC.PressedImageCondition ? 0 : 16777215);
      }

      TabletScreen.renderTabletBackground(gg, CentroX, CentroY, "b1");
      this.BG.render(gg);
      RenderingMethods.GuiGraphicsTestoScalato(
         gg,
         this.Titolo,
         this.Titolo.length() > 16 ? 1.0F - this.Titolo.length() / 80.0F : 1.0F,
         Minecraft.getInstance().font,
         new Vec2(CentroX, CentroY - 72),
         0,
         false,
         RenderingMethods.PosizioneTesto.CENTRO,
         true
      );
      if (this.blueprint_state == 1) {
         gg.fill(CentroX - 60, CentroY - 64, CentroX + 60, CentroY + 60, -13092808);
         RenderingMethods.enableScissorFromGui(Minecraft.getInstance(), CentroX - 60, CentroY - 64, 120, 124);

         try {
            int count = 0;

            for (TabletBlueprintShareScreen.PlayerItem pi : this.PlayerItems) {
               pi.render(gg, CentroX, CentroY - 60 + 20 * count - this.YLista);
               count++;
            }
         } catch (Exception var11) {
         }

         RenderingMethods.disableScissor();
      } else {
         RenderingMethods.GuiGraphicsTestoScalato(
            gg,
            Component.translatable("portable_blueprints.gui.tablet.blueprint_share.label" + (this.blueprint_state == 0 ? "1" : "3")).getString(),
            1.0F,
            Minecraft.getInstance().font,
            new Vec2(CentroX, CentroY - 55),
            0,
            false,
            RenderingMethods.PosizioneTesto.CENTRO,
            true
         );
      }

      try {
         super.render(gg, mouseX, mouseY, delta);
      } catch (Exception var10) {
      }

      TabletScreen.renderTabletUpBar(gg, CentroX, CentroY);
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
      for (TabletBlueprintShareScreen.PlayerItem pi : this.PlayerItems) {
         if (pi.isMouseHover((int)mouseX, (int)mouseY)) {
            pi.changeValue();
            break;
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean isPauseScreen() {
      return false;
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      return super.mouseReleased(mouseX, mouseY, button);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double delta, double delta2) {
      if (mouseX >= CentroX - 60 && mouseX < CentroX - 60 + 120 && mouseY >= CentroY - 64 && mouseY < CentroY - 64 + 124) {
         if (delta < 0.0) {
            this.YLista += 10;
            if (this.YLista > this.MaxYLista) {
               this.YLista = this.MaxYLista;
            }
         } else if (delta > 0.0) {
            this.YLista -= 10;
            if (this.YLista < 0) {
               this.YLista = 0;
            }
         }
      }

      return super.mouseScrolled(mouseX, mouseY, delta, delta2);
   }

   public static void OpenGUI(String Titolo, int modeScreen, UUID BPOwner) {
      Minecraft.getInstance().setScreen(new TabletBlueprintShareScreen(Titolo, modeScreen, BPOwner));
   }

   protected void renderBlurredBackground(float partialTick) {
   }

   public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
   }

   public class PlayerItem {
      private final String PlayerName;
      private final UUID PlayerUUID;
      final BufferedImage ImageFaccia;
      private boolean isInList = false;
      private int posX;
      private int posY;

      public PlayerItem(UUID PlayerUUID) {
         this.PlayerUUID = PlayerUUID;
         this.PlayerName = Operazioni.getNameViaSessionServer(PlayerUUID.toString());
         this.ImageFaccia = Operazioni.getPlayerFace(PlayerUUID);
      }

      public void render(GuiGraphics gg, int posX, int posY) {
         this.posX = posX;
         this.posY = posY;
         RenderingMethods.GuiGraphicsTestoScalato(
            gg,
            this.PlayerName,
            this.PlayerName.length() > 9 ? Operazioni.lerp(1.3F, 0.825F, (this.PlayerName.length() - 9) / 7.0F) : 1.5F,
            Minecraft.getInstance().font,
            new Vec2(posX - 38, posY + 8.5F),
            16777215,
            false,
            RenderingMethods.PosizioneTesto.SINISTRA,
            true
         );
         gg.blit(TabletBlueprintShareScreen.TEXTURE, posX + 43, posY + 1, this.isInList ? 0 : 14, 242, 14, 14);
         posX -= 137;
         posY -= 2;
         if (this.ImageFaccia != null) {
            for (int x = 0; x < this.ImageFaccia.getWidth(); x++) {
               for (int y = 0; y < this.ImageFaccia.getHeight(); y++) {
                  int color = this.ImageFaccia.getRGB(x, y);
                  int addX = 2 * x + posX + 81;
                  int addY = 2 * y + posY + 2;
                  gg.fill(addX, addY, addX + 2, addY + 2, color);
               }
            }
         } else {
            gg.blit(TabletBlueprintShareScreen.TEXTURE, posX - 56, posY, 32, 240, 16, 16);
         }
      }

      public boolean isMouseHover(int MouseX, int MouseY) {
         return MouseX >= TabletBlueprintShareScreen.CentroX - 60
            && MouseX < TabletBlueprintShareScreen.CentroX - 60 + 120
            && MouseY >= TabletBlueprintShareScreen.CentroY - 64
            && MouseY < TabletBlueprintShareScreen.CentroY - 64 + 124
            && MouseX >= this.posX + 43
            && MouseX < this.posX + 14 + 43
            && MouseY >= this.posY + 1
            && MouseY < this.posY + 15;
      }

      public void changeValue() {
         this.isInList = !this.isInList;
         if (this.isInList) {
            PacketDistributor.sendToServer(
               new CtoS_AddShareWithOthers(TabletBlueprintShareScreen.this.Titolo, Minecraft.getInstance().player.getUUID(), this.PlayerUUID),
               new CustomPacketPayload[0]
            );
         } else {
            PacketDistributor.sendToServer(
               new CtoS_RemoveShareWithOthers(TabletBlueprintShareScreen.this.Titolo, Minecraft.getInstance().player.getUUID(), this.PlayerUUID),
               new CustomPacketPayload[0]
            );
         }
      }
   }
}
