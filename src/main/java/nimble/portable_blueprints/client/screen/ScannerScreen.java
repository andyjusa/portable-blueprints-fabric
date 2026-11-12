package nimble.portable_blueprints.client.screen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import nimble.portable_blueprints.fabric.network.PacketDistributor;
import nimble.portable_blueprints.client.ServerToClientDatasShared;
import nimble.portable_blueprints.client.VisualizeBlueprintDatas;
import nimble.portable_blueprints.network.CtoS_CreateBlueprint;
import nimble.portable_blueprints.network.controllo_esiste_blueprint.CtoS_BlueprintExist;
import nimble.portable_blueprints.network.request_specific_data.CtoS_RequestSpecificData;
import nimble.portable_blueprints.portable_blueprints.ModConfig;
import nimble.portable_blueprints.portable_blueprints.OldVersionUtilities;
import nimble.portable_blueprints.robautile.AdvancedButton;
import nimble.portable_blueprints.robautile.AdvancedEditBox;
import nimble.portable_blueprints.robautile.Operazioni;
import nimble.portable_blueprints.robautile.RenderingMethods;

public class ScannerScreen extends Screen {
   private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("portable_blueprints", "textures/gui/scanner_gui.png");
   private int CentroX;
   private int CentroY;
   private int W;
   private int H;
   private final ItemStack item;
   private AdvancedEditBox editBox;
   private AdvancedButton confirmButton;
   private AdvancedButton SettingsButton;
   private AdvancedButton BackButton;
   private AdvancedButton CHECKBOX_CREATIVE_BLOCKS;
   private AdvancedButton ScanButton;
   private RenderingMethods.GuiGraphicsElement Label;
   private RenderingMethods.GuiGraphicsElement TestoPulsante;
   private String valoreEditBox = "";
   private int nBlueprintSalvati = -1;
   private int quantitaBlocchi = -1;
   private boolean initFinito = false;
   public static final String CharNonConsentiti = "QWERTYUIOPASDFGHJKL;ZXCVBNM .,;'[]{}<>:\"/\\|?*\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\b\t\n\u000b\f\r\u000e\u000f\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001a\u001b\u001c\u001d\u001e\u001f";
   private boolean settingsOpen = false;
   public static boolean SETTINGS_CREATIVE_BLOCKS = false;

   protected ScannerScreen(Component component) {
      super(component);
      this.item = null;
   }

   public ScannerScreen(ItemStack item, int quantitaBlocchi) {
      super(Component.empty());
      this.item = item;
      this.quantitaBlocchi = quantitaBlocchi;
      CtoS_RequestSpecificData.send(507, CtoS_RequestSpecificData.TipoRichiesta.MAX_SCANNER_BLOCK_COUNT);
      CtoS_RequestSpecificData.send(ServerToClientDatasShared.ID_PLAYER_BLUEPRINTS_QUANTITY, CtoS_RequestSpecificData.TipoRichiesta.PLAYER_BLUEPRINTS_QUANTITY);
   }

   protected void init() {
      super.init();
      this.initFinito = false;
      this.W = this.width;
      this.H = this.height;
      this.CentroX = this.W / 2;
      this.CentroY = this.H / 2;
      if (this.settingsOpen) {
         int px = this.CentroX + 52;
         int py = this.CentroY - 20;
         this.CHECKBOX_CREATIVE_BLOCKS = AdvancedButton.builder(Component.literal(""), button -> this.changeCreativeBlockCheckBox())
            .pos(px, py)
            .size(14, 14)
            .defaultImage(new AdvancedButton.Image(TEXTURE, px, py, 242, 228, 14, 14))
            .pressedImageCondition(new AdvancedButton.Image(TEXTURE, px, py, 242, 242, 14, 14))
            .build();
         px++;
         py += 32;
         this.BackButton = AdvancedButton.builder(Component.literal(""), button -> this.closeSettings())
            .pos(px, py)
            .size(13, 13)
            .defaultImage(new AdvancedButton.Image(TEXTURE, px, py, 0, 217, 13, 13))
            .hoverImage(new AdvancedButton.Image(TEXTURE, px, py, 13, 217, 13, 13))
            .tooltip(Tooltip.create(Component.translatable("gui.portable_blueprints.scanner.tooltip.back_button")))
            .build();
         this.addRenderableWidget(this.BackButton);
         this.addRenderableWidget(this.CHECKBOX_CREATIVE_BLOCKS);
      } else {
         this.editBox = AdvancedEditBox.builder(Component.literal(""))
            .pos(this.CentroX - 65, this.CentroY - 24)
            .size(130, 20)
            .setWithBorder(true)
            .setvisibleText(true)
            .build();
         this.editBox.setResponder(this::EditBoxValueChange);
         this.editBox.setHint(Component.translatable("gui.portable_blueprints.scanner.label1"));
         int px = this.CentroX + 53;
         int py = this.CentroY + 12;
         this.SettingsButton = AdvancedButton.builder(Component.literal(""), button -> this.openSettings())
            .pos(px, py)
            .size(13, 13)
            .defaultImage(new AdvancedButton.Image(TEXTURE, px, py, 0, 243, 13, 13))
            .hoverImage(new AdvancedButton.Image(TEXTURE, px, py, 13, 243, 13, 13))
            .tooltip(Tooltip.create(Component.translatable("gui.portable_blueprints.scanner.tooltip.settings_button")))
            .build();
         py -= 14;
         this.ScanButton = AdvancedButton.builder(Component.literal(""), button -> this.CreateBlueprint())
            .pos(px, py)
            .size(13, 13)
            .defaultImage(new AdvancedButton.Image(TEXTURE, px, py, 0, 230, 13, 13))
            .hoverImage(new AdvancedButton.Image(TEXTURE, px, py, 13, 230, 13, 13))
            .tooltip(Tooltip.create(Component.translatable("gui.portable_blueprints.scanner.tooltip.scan_button_confirm")))
            .build();
         this.Label = new RenderingMethods.GuiGraphicsElement(
            new RenderingMethods.GuiGraphicsElement.TESTO_Parametri(
               this.CentroX - 66,
               this.CentroY + 3,
               Component.literal("").getString(),
               1.0F,
               Minecraft.getInstance().font,
               16777215,
               false,
               RenderingMethods.PosizioneTesto.SINISTRA,
               true
            )
         );
         this.addRenderableWidget(this.editBox);
         this.addRenderableWidget(this.SettingsButton);
         this.addRenderableWidget(this.ScanButton);
         this.EditBoxValueChange(this.valoreEditBox);
      }

      this.initFinito = true;
   }

   private void EditBoxValueChange(String valore) {
      boolean char_consentiti = true;

      for (char v : valore.toCharArray()) {
         if ("QWERTYUIOPASDFGHJKL;ZXCVBNM .,;'[]{}<>:\"/\\|?*\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\b\t\n\u000b\f\r\u000e\u000f\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017\u0018\u0019\u001a\u001b\u001c\u001d\u001e\u001f"
               .indexOf(v)
            >= 0) {
            char_consentiti = false;
            break;
         }
      }

      if (char_consentiti) {
         this.valoreEditBox = valore;
      } else {
         this.editBox.setValue(this.valoreEditBox);
      }

      PacketDistributor.sendToServer(new CtoS_BlueprintExist(2058, this.editBox.getValue()), new CustomPacketPayload[0]);
   }

   private void CreateBlueprint() {
      if (this.nBlueprintSalvati < ServerToClientDatasShared.getRichiestaInteger(508)
         && this.item != null
         && !this.editBox.getValue().replaceAll(" ", "").equals("")) {
         new Thread(
               () -> {
                  UUID richiestaID = UUID.randomUUID();
                  CtoS_CreateBlueprint.send(
                     new BlockPos(Operazioni.getVec3iFromString(OldVersionUtilities.getItemStackNBT(this.item).getString("coordinateAngolo1"))),
                     new BlockPos(Operazioni.getVec3iFromString(OldVersionUtilities.getItemStackNBT(this.item).getString("coordinateAngolo2"))),
                     this.editBox.getValue(),
                     SETTINGS_CREATIVE_BLOCKS,
                     richiestaID
                  );

                  for (String val : CtoS_CreateBlueprint.creaListaDati(new ArrayList<>((Collection<? extends String>)ModConfig.ScannerIgnoredBlocks.get()))) {
                     CtoS_CreateBlueprint.send(val, richiestaID);

                     try {
                        Thread.sleep(10L);
                     } catch (InterruptedException e) {
                        e.printStackTrace();
                     }
                  }
               }
            )
            .start();
         if (!VisualizeBlueprintDatas.ForzaReloadPerQuesteBlueprints.contains(this.editBox.getValue())) {
            VisualizeBlueprintDatas.ForzaReloadPerQuesteBlueprints.add(this.editBox.getValue());
         }

         this.minecraft.player.closeContainer();
      }
   }

   public void render(GuiGraphics gg, int mouseX, int mouseY, float delta) {
      gg.blit(TEXTURE, this.CentroX - 80, this.CentroY - 38, 0, 0, 160, 76);
      super.render(gg, mouseX, mouseY, delta);
      if (this.nBlueprintSalvati == -1) {
         CtoS_RequestSpecificData.send(508, CtoS_RequestSpecificData.TipoRichiesta.MAX_DIGITIZED_STRUCTURES_FOR_PLAYER);
      }

      if (ServerToClientDatasShared.RichiesteInteger.containsKey(ServerToClientDatasShared.ID_PLAYER_BLUEPRINTS_QUANTITY) && this.nBlueprintSalvati == -1) {
         this.nBlueprintSalvati = ServerToClientDatasShared.RichiesteInteger.get(ServerToClientDatasShared.ID_PLAYER_BLUEPRINTS_QUANTITY);
      }

      if (this.nBlueprintSalvati != -1) {
         if (this.CHECKBOX_CREATIVE_BLOCKS != null) {
            this.CHECKBOX_CREATIVE_BLOCKS.PressedImageCondition = SETTINGS_CREATIVE_BLOCKS;
         }

         if (!this.initFinito) {
            return;
         }

         if (this.settingsOpen) {
            RenderingMethods.GuiGraphicsTestoScalato(
               gg,
               Component.translatable("gui.portable_blueprints.scanner.checkbox.creative_blocks").getString(),
               0.8F,
               Minecraft.getInstance().font,
               new Vec2(this.CentroX - 66, this.CentroY - 20),
               16777215,
               false,
               RenderingMethods.PosizioneTesto.SINISTRA,
               true
            );
         } else {
            if (this.nBlueprintSalvati < ServerToClientDatasShared.getRichiestaInteger(508)
               && this.quantitaBlocchi <= ServerToClientDatasShared.getRichiestaInteger(507)) {
               this.editBox.setEditable(true);
               this.editBox.setVisible(true);
               this.SettingsButton.visible = true;
               this.ScanButton.visible = true;
               if (ServerToClientDatasShared.getRichiestaBooleana(2058) == 0) {
                  this.editBox.setTextColor(16777215);
                  this.Label.testoParametri.setTesto("");
                  this.ScanButton.setTooltip(Tooltip.create(Component.translatable("gui.portable_blueprints.scanner.tooltip.scan_button_confirm")));
               } else if (ServerToClientDatasShared.getRichiestaBooleana(2058) == 1) {
                  this.editBox.setTextColor(16711680);
                  this.Label.testoParametri.setTesto(Component.translatable("gui.portable_blueprints.scanner.label2").getString());
                  this.Label.testoParametri.color = 16711680;
                  this.Label.testoParametri.startPosY = this.CentroY + 3;
                  this.Label.testoParametri.startPosX = this.CentroX - 66;
                  this.Label.testoParametri.posizione = RenderingMethods.PosizioneTesto.SINISTRA;
                  this.ScanButton.setTooltip(Tooltip.create(Component.translatable("gui.portable_blueprints.scanner.tooltip.scan_button_replace")));
               }
            } else {
               this.Label.testoParametri.startPosY = this.CentroY - 20;
               this.Label.testoParametri.startPosX = this.CentroX;
               this.Label.testoParametri.posizione = RenderingMethods.PosizioneTesto.CENTRO;
               if (this.nBlueprintSalvati >= ServerToClientDatasShared.getRichiestaInteger(508)) {
                  this.Label
                     .testoParametri
                     .setTesto(
                        Component.translatable("gui.portable_blueprints.scanner.label_max_structure_reached")
                           .getString()
                           .replace("%v%", String.valueOf(ServerToClientDatasShared.getRichiestaInteger(508)))
                     );
                  this.Label.testoParametri.color = 16711680;
               } else {
                  String testo = Component.translatable("gui.portable_blueprints.scanner.label_max_structure_count")
                     .getString()
                     .replace("%v1%", String.valueOf(this.quantitaBlocchi))
                     .replace("%v2%", String.valueOf(ServerToClientDatasShared.getRichiestaInteger(507)));
                  this.Label.testoParametri.setTesto(testo.replace("%v2%", String.valueOf(ServerToClientDatasShared.getRichiestaInteger(507))));
                  this.Label.testoParametri.color = 16711680;
               }

               this.editBox.setEditable(false);
               this.editBox.setVisible(false);
               this.SettingsButton.visible = false;
               this.ScanButton.visible = false;
            }

            this.Label.render(gg);
         }
      }
   }

   public void openSettings() {
      this.settingsOpen = true;
      this.re_init();
   }

   public void closeSettings() {
      this.settingsOpen = false;
      this.re_init();
   }

   private void re_init() {
      this.clearWidgets();

      this.init();
   }

   public void changeCreativeBlockCheckBox() {
      SETTINGS_CREATIVE_BLOCKS = !SETTINGS_CREATIVE_BLOCKS;
   }

   public boolean isPauseScreen() {
      return false;
   }

   protected void renderBlurredBackground(float partialTick) {
   }

   public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
   }
}
