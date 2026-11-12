package nimble.portable_blueprints.client.screen.tablet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import nimble.portable_blueprints.fabric.network.PacketDistributor;
import nimble.portable_blueprints.client.ServerToClientDatasShared;
import nimble.portable_blueprints.client.screen.tablet.utils.TabletScrollBar;
import nimble.portable_blueprints.network.CtoS_UpdateServerConfigurationValue;
import nimble.portable_blueprints.network.request_specific_data.CtoS_RequestSpecificData;
import nimble.portable_blueprints.portable_blueprints.ModConfig;
import nimble.portable_blueprints.robautile.AdvancedEditBox;
import nimble.portable_blueprints.robautile.RenderingMethods;

public class TabletConfigurationScreen extends Screen {
   private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
      "portable_blueprints", "textures/gui/tablet/tablet_configuration_gui.png"
   );
   private static int CentroX;
   private static int CentroY;
   private static int W;
   private static int H;
   public int ScrollMax = 0;
   public static int Scroll = 0;
   private boolean modeClient = true;
   private boolean serverDatasArrived = false;
   private boolean isPlayerOp = false;
   private TabletScrollBar scrollBar;
   private short TickCount = 1;

   protected TabletConfigurationScreen(Component component) {
      super(component);
   }

   public TabletConfigurationScreen() {
      super(Component.literal("portable_blueprint.tablet.configuration"));
      CtoS_RequestSpecificData.send(770, CtoS_RequestSpecificData.TipoRichiesta.IS_PLAYER_OP);
      CtoS_RequestSpecificData.send(507, CtoS_RequestSpecificData.TipoRichiesta.MAX_SCANNER_BLOCK_COUNT);
      CtoS_RequestSpecificData.send(508, CtoS_RequestSpecificData.TipoRichiesta.MAX_DIGITIZED_STRUCTURES_FOR_PLAYER);
      CtoS_RequestSpecificData.send(510, CtoS_RequestSpecificData.TipoRichiesta.ALLOW_GLOBAL_BLUEPRINTS);
      CtoS_RequestSpecificData.send(511, CtoS_RequestSpecificData.TipoRichiesta.MAX_BLUEPRINT_CONTAINERS);
      CtoS_RequestSpecificData.send(512, CtoS_RequestSpecificData.TipoRichiesta.BLUEPRINTS_COOLDOWN);
      CtoS_RequestSpecificData.send(513, CtoS_RequestSpecificData.TipoRichiesta.ALLOW_NBT_DATA_COPY);
      CtoS_RequestSpecificData.send(514, CtoS_RequestSpecificData.TipoRichiesta.BUSINESS_VILLAGER_ENABLED);
      CtoS_RequestSpecificData.send(53, CtoS_RequestSpecificData.TipoRichiesta.GET_TABLET_SOCIAL_LINKS);
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
      int px = CentroX;
      int py = CentroY - 60;
      Scroll = 0;
      this.ScrollMax = 25;
      if (this.isPlayerOp && !this.modeClient) {
         String[] links = new String[5];
         String v = ServerToClientDatasShared.RichiestaString.containsKey(53) ? ServerToClientDatasShared.RichiestaString.get(53) : "";

         for (int x = 0; x < 5; x++) {
            if (v.split(";").length - 1 >= x) {
               String link = v.split(";")[x];
               links[x] = link;
            } else {
               links[x] = "";
            }
         }

         TabletConfigurationScreen.ConfigElement ce = TabletConfigurationScreen.ConfigElement.createString("serverconfig:tablet_social_1", links[0], px, py);
         this.addRenderableWidget(ce);
         py += ce.altezza();
         ce = TabletConfigurationScreen.ConfigElement.createString("serverconfig:tablet_social_2", links[1], px, py);
         this.addRenderableWidget(ce);
         py += ce.altezza();
         ce = TabletConfigurationScreen.ConfigElement.createString("serverconfig:tablet_social_3", links[2], px, py);
         this.addRenderableWidget(ce);
         py += ce.altezza();
         ce = TabletConfigurationScreen.ConfigElement.createString("serverconfig:tablet_social_4", links[3], px, py);
         this.addRenderableWidget(ce);
         py += ce.altezza();
         this.ScrollMax = this.ScrollMax + ce.altezza;
         ce = TabletConfigurationScreen.ConfigElement.createString("serverconfig:tablet_social_5", links[4], px, py);
         this.addRenderableWidget(ce);
         py += ce.altezza();
         this.ScrollMax = this.ScrollMax + ce.altezza;
         ce = TabletConfigurationScreen.ConfigElement.createIntValue(
            "serverconfig:max_digitized_structures_for_player", ServerToClientDatasShared.getRichiestaInteger(508), px, py
         );
         this.addRenderableWidget(ce);
         py += ce.altezza();
         this.ScrollMax = this.ScrollMax + ce.altezza;
         ce = TabletConfigurationScreen.ConfigElement.createIntValue(
            "serverconfig:maxScannerBlockCount", ServerToClientDatasShared.getRichiestaInteger(507), px, py
         );
         this.addRenderableWidget(ce);
         py += ce.altezza();
         this.ScrollMax = this.ScrollMax + ce.altezza;
         ce = TabletConfigurationScreen.ConfigElement.createIntValue(
            "serverconfig:maxBlueprintContainers", ServerToClientDatasShared.getRichiestaInteger(511), px, py
         );
         this.addRenderableWidget(ce);
         py += ce.altezza();
         this.ScrollMax = this.ScrollMax + ce.altezza;
         ce = TabletConfigurationScreen.ConfigElement.createIntValue(
            "serverconfig:blueprintsCooldown", ServerToClientDatasShared.getRichiestaInteger(512), px, py
         );
         this.addRenderableWidget(ce);
         py += ce.altezza();
         this.ScrollMax = this.ScrollMax + ce.altezza;
         ce = TabletConfigurationScreen.ConfigElement.createBoolean(
            "serverconfig:allowGlobalBlueprints", ServerToClientDatasShared.getRichiestaBooleana(510) == 1, px, py
         );
         this.addRenderableWidget(ce);
         py += ce.altezza();
         this.ScrollMax = this.ScrollMax + ce.altezza;
         ce = TabletConfigurationScreen.ConfigElement.createBoolean(
            "serverconfig:allowNBTDataCopy", ServerToClientDatasShared.getRichiestaBooleana(513) == 1, px, py
         );
         this.addRenderableWidget(ce);
         py += ce.altezza();
         this.ScrollMax = this.ScrollMax + ce.altezza;
         ce = TabletConfigurationScreen.ConfigElement.createBoolean(
            "serverconfig:businessVillagerEnabled", ServerToClientDatasShared.getRichiestaBooleana(514) == 1, px, py
         );
         this.addRenderableWidget(ce);
         py += ce.altezza();
         this.ScrollMax = this.ScrollMax + ce.altezza;
      } else {
         TabletConfigurationScreen.ConfigElement ce = TabletConfigurationScreen.ConfigElement.createBoolean(
            "clientconfig:scanningAreaTextureEnabled", (Boolean)ModConfig.ScanningAreaTextureEnabled.get(), px, py
         );
         this.addRenderableWidget(ce);
         py += ce.altezza();
         this.ScrollMax = this.ScrollMax + ce.altezza;
         ce = TabletConfigurationScreen.ConfigElement.createBoolean(
            "clientconfig:selectedContainerAreaTextureEnabled", (Boolean)ModConfig.SelectedContainerAreaTextureEnabled.get(), px, py
         );
         this.addRenderableWidget(ce);
         py += ce.altezza();
         this.ScrollMax = this.ScrollMax + ce.altezza;
         ce = TabletConfigurationScreen.ConfigElement.createBoolean(
            "clientconfig:scanningAnimationEnabled", (Boolean)ModConfig.ScanningAnimationEnabled.get(), px, py
         );
         this.addRenderableWidget(ce);
         py += ce.altezza();
         this.ScrollMax = this.ScrollMax + ce.altezza;
         ce = TabletConfigurationScreen.ConfigElement.createBoolean(
            "clientconfig:containerMessagesEnabled", (Boolean)ModConfig.ContainerMessagesEnabled.get(), px, py
         );
         this.addRenderableWidget(ce);
         py += ce.altezza();
         this.ScrollMax = this.ScrollMax + ce.altezza;
         ce = TabletConfigurationScreen.ConfigElement.createBoolean(
            "clientconfig:buildAnimationEnabled", (Boolean)ModConfig.BuildAnimationEnabled.get(), px, py
         );
         this.addRenderableWidget(ce);
         py += ce.altezza();
         this.ScrollMax = this.ScrollMax + ce.altezza;
         ce = TabletConfigurationScreen.ConfigElement.createRangeIntValue(
            "clientconfig:defaultHologramTransparency", (Integer)ModConfig.DefaultHologramTransparency.get(), 0, 100, px, py
         );
         this.addRenderableWidget(ce);
         py += ce.altezza();
         this.ScrollMax = this.ScrollMax + ce.altezza;
         ce = TabletConfigurationScreen.ConfigElement.createColorValue(
            "clientconfig:scannerAreaColor", (int)Long.parseLong("FF" + ((String)ModConfig.ScannerAreaColor.get()).replace("#", ""), 16), px, py
         );
         this.addRenderableWidget(ce);
         py += ce.altezza();
         this.ScrollMax = this.ScrollMax + ce.altezza;
         ce = TabletConfigurationScreen.ConfigElement.createColorValue(
            "clientconfig:containerAreaColor", (int)Long.parseLong("FF" + ((String)ModConfig.ContainerAreaColor.get()).replace("#", ""), 16), px, py
         );
         this.addRenderableWidget(ce);
         py += ce.altezza();
         this.ScrollMax = this.ScrollMax + ce.altezza;
         ce = TabletConfigurationScreen.ConfigElement.createColorValue(
            "clientconfig:blueprintAreaColor", (int)Long.parseLong("FF" + ((String)ModConfig.BlueprintAreaColor.get()).replace("#", ""), 16), px, py
         );
         this.addRenderableWidget(ce);
         py += ce.altezza();
         this.ScrollMax = this.ScrollMax + ce.altezza;
      }

      if (this.ScrollMax < 0) {
         this.ScrollMax = 0;
      }

      this.addRenderableWidget(TabletScreen.getTabletButton(CentroX, CentroY));
      this.addRenderableWidget(
         this.scrollBar = new TabletScrollBar(
            CentroX + 67, CentroY - 60, 2, 146, -12696497, -668309, new TabletScrollBar.ScrollingArea(CentroX - 70, CentroY - 60, 140, 150, 5)
         )
      );
   }

   public void render(GuiGraphics gg, int mouseX, int mouseY, float delta) {
      if (!this.serverDatasArrived
         && ServerToClientDatasShared.getRichiestaBooleana(770) != -1
         && ServerToClientDatasShared.RichiesteInteger.containsKey(507)
         && ServerToClientDatasShared.RichiesteInteger.containsKey(508)
         && ServerToClientDatasShared.getRichiestaBooleana(510) != -1
         && ServerToClientDatasShared.RichiestaString.containsKey(53)
         && ServerToClientDatasShared.RichiesteInteger.containsKey(511)
         && ServerToClientDatasShared.getRichiestaBooleana(513) != -1
         && ServerToClientDatasShared.getRichiestaBooleana(514) != -1) {
         this.serverDatasArrived = true;
         this.isPlayerOp = ServerToClientDatasShared.getRichiestaBooleana(770) == 1;
         this.re_init();
      }

      if (this.scrollBar != null) {
         Scroll = (int)this.scrollBar.getValore(this.ScrollMax);
      }

      Minecraft mc = Minecraft.getInstance();
      TabletScreen.renderTabletBackground(gg, CentroX, CentroY, "b8");
      RenderingMethods.enableScissorFromGui(Minecraft.getInstance(), CentroX - 70, CentroY - 60, 140, 149);

      for (GuiEventListener renderable : this.children()) {
         if (renderable instanceof TabletConfigurationScreen.ConfigElement configElement) {
            configElement.customRender(gg, mouseX, mouseY, delta);
         }
      }

      RenderingMethods.disableScissor();

      try {
         super.render(gg, mouseX, mouseY, delta);
      } catch (Exception var9) {
      }

      gg.fill(CentroX - 70, CentroY - 87, CentroX + 70, CentroY - 60, -2013265920);
      if (this.isPlayerOp) {
         new RenderingMethods.GuiGraphicsElement(new RenderingMethods.GuiGraphicsElement.BLIT_Parametri(CentroX + 1, CentroY - 83, 0, 160, 64, 16, TEXTURE))
            .render(gg);
         new RenderingMethods.GuiGraphicsElement(new RenderingMethods.GuiGraphicsElement.BLIT_Parametri(CentroX - 65, CentroY - 83, 0, 160, 64, 16, TEXTURE))
            .render(gg);
         if (this.modeClient) {
            gg.fill(CentroX - 63, CentroY - 81, CentroX - 3, CentroY - 81 + 12, -1);
         } else {
            gg.fill(CentroX + 3, CentroY - 81, CentroX + 63, CentroY - 81 + 12, -1);
         }

         String var10001 = Component.translatable("portable_blueprints.gui.tablet.app.configuration.server.title").getString();
         int var10003 = this.modeClient ? 16777215 : 0;
         float var10004 = CentroX + 4;
         float var10005 = CentroY - 80;
         RenderingMethods.GUITextAutoResize(gg, var10001, Minecraft.getInstance().font, var10003, var10004, var10005, 59, 13, false);
         var10001 = Component.translatable("portable_blueprints.gui.tablet.app.configuration.client.title").getString();
         var10003 = this.modeClient ? 0 : 16777215;
         var10004 = CentroX - 62;
         var10005 = CentroY - 80;
         RenderingMethods.GUITextAutoResize(gg, var10001, Minecraft.getInstance().font, var10003, var10004, var10005, 59, 13, false);
      } else {
         new RenderingMethods.GuiGraphicsElement(new RenderingMethods.GuiGraphicsElement.BLIT_Parametri(CentroX - 32, CentroY - 83, 0, 160, 64, 16, TEXTURE))
            .render(gg);
         gg.fill(CentroX - 30, CentroY - 81, CentroX + 30, CentroY - 81 + 12, -1);
         int var12 = this.modeClient ? 0 : 16777215;
         float var14 = CentroX - 30;
         float var16 = CentroY - 80;
         RenderingMethods.GUITextAutoResize(gg, "Client", Minecraft.getInstance().font, var12, var14, var16, 59, 13, false);
      }

      TabletScreen.renderTabletUpBar(gg, CentroX, CentroY);
   }

   public void tick() {
      super.tick();
      if (++this.TickCount > 20) {
         this.TickCount = 1;
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.isPlayerOp) {
         boolean beforeMode = this.modeClient;
         if (mouseX >= CentroX + 4 && mouseX < CentroX + 63 && mouseY >= CentroY - 82 && mouseY < CentroY - 82 + 13) {
            this.modeClient = false;
         } else if (mouseX >= CentroX - 62 && mouseX < CentroX - 3 && mouseY >= CentroY - 82 && mouseY < CentroY - 82 + 13) {
            this.modeClient = true;
         }

         if (beforeMode != this.modeClient) {
            this.re_init();
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   private void re_init() {
      this.clearWidgets();

      this.init();
   }

   public boolean isPauseScreen() {
      return false;
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      return super.mouseReleased(mouseX, mouseY, button);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double delta, double delta2) {
      return this.scrollBar != null && this.scrollBar.mouseScrolled(mouseX, mouseY, delta2, delta) ? true : super.mouseScrolled(mouseX, mouseY, delta, delta2);
   }

   public static void OpenGUI() {
      Minecraft.getInstance().setScreen(new TabletConfigurationScreen());
   }

   protected void renderBlurredBackground(float partialTick) {
   }

   public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
   }

   private static class ConfigElement extends AbstractWidget {
      private String configValue;
      private TabletConfigurationScreen.ConfigElement.TYPE type;
      private int altezza = 0;
      private boolean boolean_state = false;
      private String string_value = "";
      private RenderingMethods.GuiGraphicsElement img1;
      private RenderingMethods.GuiGraphicsElement img2;
      private String Title = "";
      private int ePosX;
      private int ePosY;
      private int yCenter = 0;
      private int minRange = 0;
      private int maxRange = 0;
      private int valueRange = 0;
      private AdvancedEditBox advancedEditBox = null;

      public ConfigElement(String configValue, TabletConfigurationScreen.ConfigElement.TYPE type, String value, int ePosX, int ePosY) {
         super(0, 0, 0, 0, Component.empty());
         this.configValue = configValue;
         this.type = type;
         this.ePosX = ePosX;
         this.ePosY = ePosY;
         int maxLenghtText = 32;
         if (configValue.contains("clientconfig:")) {
            String value_config = configValue.replace("clientconfig:", "");
            this.Title = Component.translatable("portable_blueprints.gui.tablet.app.configuration.client." + value_config).getString();
         } else if (configValue.contains("serverconfig:")) {
            String value_config = configValue.replace("serverconfig:", "");
            this.Title = Component.translatable(
                  "portable_blueprints.gui.tablet.app.configuration.server." + (value_config.contains("tablet_social_") ? "tablet_social" : value_config)
               )
               .getString();
         }

         this.Title = dividiTestoMaxLenght(this.Title, maxLenghtText);
         int altezza_title = this.Title.split("\n").length * 7 - 2;
         switch (type) {
            case BOOLEAN:
               this.boolean_state = Boolean.valueOf(value);
               this.img1 = new RenderingMethods.GuiGraphicsElement(
                  new RenderingMethods.GuiGraphicsElement.BLIT_Parametri(
                     ePosX - 64, ePosY + altezza_title + 5, 0, 32, 128, 16, TabletConfigurationScreen.TEXTURE
                  )
               );
               this.img2 = new RenderingMethods.GuiGraphicsElement(
                  new RenderingMethods.GuiGraphicsElement.BLIT_Parametri(
                     ePosX - 64, ePosY + altezza_title + 5, 0, 48, 128, 16, TabletConfigurationScreen.TEXTURE
                  )
               );
               this.altezza = altezza_title + 16 + 5;
               if (this.altezza < 16) {
                  this.altezza = 16;
               }
               break;
            case RangeIntValue:
               String[] vals = value.split("_");
               this.valueRange = Integer.parseInt(vals[0]);
               this.minRange = Integer.parseInt(vals[1]);
               this.maxRange = Integer.parseInt(vals[2]);
               this.img1 = new RenderingMethods.GuiGraphicsElement(
                  new RenderingMethods.GuiGraphicsElement.BLIT_Parametri(
                     ePosX - 64, ePosY + altezza_title + 5, 0, 0, 128, 16, TabletConfigurationScreen.TEXTURE
                  )
               );
               this.img2 = new RenderingMethods.GuiGraphicsElement(
                  new RenderingMethods.GuiGraphicsElement.BLIT_Parametri(
                     ePosX - 64, ePosY + altezza_title + 5, 0, 16, 128, 16, TabletConfigurationScreen.TEXTURE
                  )
               );
               this.altezza = altezza_title + 16 + 5;
               if (this.altezza < 16) {
                  this.altezza = 16;
               }
               break;
            case STRING:
               this.img1 = new RenderingMethods.GuiGraphicsElement(
                  new RenderingMethods.GuiGraphicsElement.BLIT_Parametri(
                     ePosX - 64, ePosY + altezza_title + 5, 0, 0, 128, 16, TabletConfigurationScreen.TEXTURE
                  )
               );
               this.img2 = new RenderingMethods.GuiGraphicsElement(
                  new RenderingMethods.GuiGraphicsElement.BLIT_Parametri(
                     ePosX - 64, ePosY + altezza_title + 5, 0, 16, 128, 16, TabletConfigurationScreen.TEXTURE
                  )
               );
               this.altezza = altezza_title + 16 + 5;
               if (this.altezza < 16) {
                  this.altezza = 16;
               }
               break;
            case COLOR:
               int color = Integer.parseInt(value);
               this.string_value = String.format("#%06X", 16777215 & color);
               this.img1 = new RenderingMethods.GuiGraphicsElement(
                  new RenderingMethods.GuiGraphicsElement.BLIT_Parametri(
                     ePosX - 64, ePosY + altezza_title + 5, 0, 64, 128, 19, TabletConfigurationScreen.TEXTURE
                  )
               );
               this.img2 = new RenderingMethods.GuiGraphicsElement(
                  new RenderingMethods.GuiGraphicsElement.BLIT_Parametri(
                     ePosX - 64, ePosY + altezza_title + 5, 0, 83, 128, 19, TabletConfigurationScreen.TEXTURE
                  )
               );
               this.altezza = altezza_title + 19 + 5;
               if (this.altezza < 19) {
                  this.altezza = 19;
               }
         }

         if (Minecraft.getInstance().screen != null && Minecraft.getInstance().screen instanceof TabletConfigurationScreen tabletConfigurationScreen) {
            switch (type) {
               case RangeIntValue:
                  this.advancedEditBox = AdvancedEditBox.builder(Component.literal(""))
                     .pos(ePosX - 64, ePosY + altezza_title + 5)
                     .size(128, 16)
                     .setWithBorder(false)
                     .setvisibleText(false)
                     .build();
                  this.advancedEditBox.setValue(String.valueOf(this.valueRange));
                  this.advancedEditBox.setResponder(this::EditBoxValueChange);
                  this.advancedEditBox.setFilter(s -> s.matches("\\d*"));
                  tabletConfigurationScreen.addRenderableWidget(this.advancedEditBox);
                  break;
               case STRING:
                  this.advancedEditBox = AdvancedEditBox.builder(Component.literal(""))
                     .pos(ePosX - 64, ePosY + altezza_title + 5)
                     .size(128, 16)
                     .setWithBorder(false)
                     .setvisibleText(false)
                     .build();
                  this.advancedEditBox.setValue(value);
                  this.advancedEditBox.setResponder(this::EditBoxValueChange);
                  this.advancedEditBox.setMaxLength(300);
                  tabletConfigurationScreen.addRenderableWidget(this.advancedEditBox);
                  break;
               case COLOR:
                  this.advancedEditBox = AdvancedEditBox.builder(Component.literal(""))
                     .pos(ePosX - 64, ePosY + altezza_title + 5)
                     .size(128, 16)
                     .setWithBorder(false)
                     .setvisibleText(false)
                     .build();
                  this.advancedEditBox.setValue(this.string_value);
                  this.advancedEditBox.setResponder(this::EditBoxValueChange);
                  this.advancedEditBox.setFilter(s -> s.matches("^#?[0-9a-fA-F]*$"));
                  this.advancedEditBox.setMaxLength(7);
                  tabletConfigurationScreen.addRenderableWidget(this.advancedEditBox);
            }
         }
      }

      public ConfigElement(int p_93629_, int p_93630_, int p_93631_, int p_93632_, Component p_93633_) {
         super(p_93629_, p_93630_, p_93631_, p_93632_, p_93633_);
      }

      private void EditBoxValueChange(String input) {
         switch (this.type) {
            case RangeIntValue:
               if (input.isEmpty()) {
                  return;
               }

               try {
                  int value = Integer.parseInt(input);
                  boolean changed = false;
                  if (value < this.minRange) {
                     value = this.minRange;
                     changed = true;
                  } else if (value > this.maxRange) {
                     value = this.maxRange;
                     changed = true;
                  }

                  if (changed) {
                     this.valueRange = value;
                     this.advancedEditBox.setValue(String.valueOf(value));
                  } else {
                     this.valueRange = value;
                  }
               } catch (NumberFormatException e) {
                  this.advancedEditBox.setValue(String.valueOf(this.minRange));
               }
               break;
            case COLOR:
               if (input.length() == 1 && !input.contains("#")) {
                  input = "#" + input;
                  this.advancedEditBox.setValue(input);
               }

               this.string_value = input;
         }

         this.changeValue();
      }

      public void customRender(GuiGraphics gg, int i, int i1, float v) {
         if (this.advancedEditBox != null) {
            this.advancedEditBox.active = this.advancedEditBox.getY() + this.advancedEditBox.getHeight() >= TabletConfigurationScreen.CentroY - 60
               && this.advancedEditBox.getY() < TabletConfigurationScreen.CentroY + 80;
         }

         gg.pose().pushPose();
         gg.pose().translate(0.0F, -TabletConfigurationScreen.Scroll, 0.0F);
         gg.fill(this.ePosX - 66, this.ePosY, this.ePosX + 66, this.ePosY + this.altezza + 3, -11050897);
         gg.fill(this.ePosX - 65, this.ePosY + 1, this.ePosX + 65, this.ePosY + this.altezza + 2, -13683390);
         if (this.advancedEditBox != null) {
            this.advancedEditBox.setY(this.advancedEditBox._posY - TabletConfigurationScreen.Scroll);
         }

         switch (this.type) {
            case BOOLEAN:
               if (this.boolean_state) {
                  this.img1.render(gg);
               } else {
                  this.img2.render(gg);
               }

               RenderingMethods.GuiGraphicsTestoScalato(
                  gg,
                  this.boolean_state ? "ON" : "OFF",
                  1.2F,
                  Minecraft.getInstance().font,
                  new Vec2(this.ePosX, this.img1.blitParametri.startPosY + 3.5F),
                  16777215,
                  false,
                  RenderingMethods.PosizioneTesto.CENTRO
               );
               break;
            case RangeIntValue:
               if (this.advancedEditBox.isFocused()) {
                  this.img1.render(gg);
               } else {
                  this.img2.render(gg);
               }

               String var8 = this.advancedEditBox.getValue();
               float var10 = this.img1.blitParametri.startPosX + 2;
               RenderingMethods.GUITextAutoResize(
                  gg, var8, Minecraft.getInstance().font, 16777215, var10, this.img1.blitParametri.startPosY + 2.5F, 125, 14, false
               );
               break;
            case STRING:
               if (this.advancedEditBox.isFocused()) {
                  this.img1.render(gg);
               } else {
                  this.img2.render(gg);
               }

               String var7 = this.advancedEditBox.getValue().isEmpty() ? "..." : this.advancedEditBox.getValue();
               float var9 = this.img1.blitParametri.startPosX + 2;
               RenderingMethods.GUITextAutoResize(
                  gg, var7, Minecraft.getInstance().font, 16777215, var9, this.img1.blitParametri.startPosY + 2.5F, 125, 14, false
               );
               break;
            case COLOR:
               if (this.advancedEditBox.isFocused()) {
                  this.img1.render(gg);
               } else {
                  this.img2.render(gg);
               }

               String var10001 = this.advancedEditBox.getValue();
               float var10004 = this.img1.blitParametri.startPosX + 2;
               RenderingMethods.GUITextAutoResize(
                  gg, var10001, Minecraft.getInstance().font, 16777215, var10004, this.img1.blitParametri.startPosY + 2.5F, 125, 14, false
               );
               int px = this.img1.blitParametri.startPosX + 2;
               int py = this.img1.blitParametri.startPosY + 16;
               gg.fill(px + 4, py, px + 120, py + 2, (int)Long.parseLong("FF" + this.string_value.replace("#", ""), 16));
         }

         RenderingMethods.GuiGraphicsTestoScalato(
            gg,
            this.Title,
            0.73F,
            Minecraft.getInstance().font,
            new Vec2(this.ePosX - 64, this.ePosY + 2),
            16777215,
            false,
            RenderingMethods.PosizioneTesto.SINISTRA
         );
         gg.pose().popPose();
      }

      protected void renderWidget(GuiGraphics p_282139_, int p_268034_, int p_268009_, float p_268085_) {
      }

      protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
      }

      public boolean mouseClicked(double mouseX, double _mouseY, int button) {
         double mouseY = _mouseY + TabletConfigurationScreen.Scroll;
         if (_mouseY < TabletConfigurationScreen.CentroY + 90 && _mouseY > TabletConfigurationScreen.CentroY - 60 && button == 0) {
            switch (this.type) {
               case BOOLEAN:
                  if (mouseX >= this.img1.blitParametri.startPosX
                     && mouseX < this.img1.blitParametri.startPosX + this.img1.blitParametri.width
                     && mouseY >= this.img1.blitParametri.startPosY
                     && mouseY < this.img1.blitParametri.startPosY + this.img1.blitParametri.height) {
                     this.boolean_state = !this.boolean_state;
                     this.changeValue();
                  }
            }
         }

         if (this.advancedEditBox != null && this.advancedEditBox.isFocused() && !this.advancedEditBox.isMouseOver(mouseX, mouseY)) {
            this.advancedEditBox.setFocused(false);
         }

         return super.mouseClicked(mouseX, mouseY, button);
      }

      private void changeValue() {
         if (this.configValue.contains("clientconfig:")) {
            String value = this.configValue.replace("clientconfig:", "");
            if (value.equals("scanningAnimationEnabled")) {
               ModConfig.ScanningAnimationEnabled.set(this.boolean_state);
            } else if (value.equals("scanningAreaTextureEnabled")) {
               ModConfig.ScanningAreaTextureEnabled.set(this.boolean_state);
            } else if (value.equals("selectedContainerAreaTextureEnabled")) {
               ModConfig.SelectedContainerAreaTextureEnabled.set(this.boolean_state);
            } else if (value.equals("containerMessagesEnabled")) {
               ModConfig.ContainerMessagesEnabled.set(this.boolean_state);
            } else if (value.equals("buildAnimationEnabled")) {
               ModConfig.BuildAnimationEnabled.set(this.boolean_state);
            } else if (value.equals("defaultHologramTransparency")) {
               ModConfig.DefaultHologramTransparency.set(Integer.parseInt(this.advancedEditBox.getValue()));
            } else if (value.equals("scannerAreaColor")) {
               if (this.string_value.length() == 7) {
                  ModConfig.ScannerAreaColor.set(this.string_value.toUpperCase());
               }
            } else if (value.equals("containerAreaColor")) {
               if (this.string_value.length() == 7) {
                  ModConfig.ContainerAreaColor.set(this.string_value.toUpperCase());
               }
            } else if (value.equals("blueprintAreaColor") && this.string_value.length() == 7) {
               ModConfig.BlueprintAreaColor.set(this.string_value.toUpperCase());
            }

            ModConfig.saveConfigFile();
         } else if (this.configValue.contains("serverconfig:")) {
            String value = this.configValue.replace("serverconfig:", "");
            if (value.contains("tablet_social_")) {
               PacketDistributor.sendToServer(
                  new CtoS_UpdateServerConfigurationValue(
                     CtoS_UpdateServerConfigurationValue.TipoValore.SOCIAL_LINK, value.replace("tablet_social_", "") + ";" + this.advancedEditBox.getValue()
                  ),
                  new CustomPacketPayload[0]
               );
            } else if (value.equals("max_digitized_structures_for_player")) {
               PacketDistributor.sendToServer(
                  new CtoS_UpdateServerConfigurationValue(
                     CtoS_UpdateServerConfigurationValue.TipoValore.Max_digitized_structures_for_player, this.advancedEditBox.getValue()
                  ),
                  new CustomPacketPayload[0]
               );
            } else if (value.equals("maxScannerBlockCount")) {
               PacketDistributor.sendToServer(
                  new CtoS_UpdateServerConfigurationValue(CtoS_UpdateServerConfigurationValue.TipoValore.MaxScannerBlockCount, this.advancedEditBox.getValue()),
                  new CustomPacketPayload[0]
               );
            } else if (value.equals("maxBlueprintContainers")) {
               PacketDistributor.sendToServer(
                  new CtoS_UpdateServerConfigurationValue(
                     CtoS_UpdateServerConfigurationValue.TipoValore.MaxBlueprintContainers, this.advancedEditBox.getValue()
                  ),
                  new CustomPacketPayload[0]
               );
            } else if (value.equals("blueprintsCooldown")) {
               PacketDistributor.sendToServer(
                  new CtoS_UpdateServerConfigurationValue(CtoS_UpdateServerConfigurationValue.TipoValore.BlueprintsCooldown, this.advancedEditBox.getValue()),
                  new CustomPacketPayload[0]
               );
            } else if (value.equals("allowGlobalBlueprints")) {
               PacketDistributor.sendToServer(
                  new CtoS_UpdateServerConfigurationValue(
                     CtoS_UpdateServerConfigurationValue.TipoValore.AllowGlobalBlueprints, String.valueOf(this.boolean_state)
                  ),
                  new CustomPacketPayload[0]
               );
            } else if (value.equals("allowNBTDataCopy")) {
               PacketDistributor.sendToServer(
                  new CtoS_UpdateServerConfigurationValue(CtoS_UpdateServerConfigurationValue.TipoValore.AllowNBTDataCopy, String.valueOf(this.boolean_state)),
                  new CustomPacketPayload[0]
               );
            } else if (value.equals("businessVillagerEnabled")) {
               PacketDistributor.sendToServer(
                  new CtoS_UpdateServerConfigurationValue(
                     CtoS_UpdateServerConfigurationValue.TipoValore.BusinessVillagerEnabled, String.valueOf(this.boolean_state)
                  ),
                  new CustomPacketPayload[0]
               );
            }
         }
      }

      public int altezza() {
         return this.altezza + 5;
      }

      public static TabletConfigurationScreen.ConfigElement createBoolean(String configValue, boolean boolean_state, int eposx, int eposy) {
         return new TabletConfigurationScreen.ConfigElement(
            configValue, TabletConfigurationScreen.ConfigElement.TYPE.BOOLEAN, String.valueOf(boolean_state), eposx, eposy
         );
      }

      public static TabletConfigurationScreen.ConfigElement createRangeIntValue(String configValue, int value, int minvalue, int maxvalue, int eposx, int eposy) {
         return new TabletConfigurationScreen.ConfigElement(
            configValue, TabletConfigurationScreen.ConfigElement.TYPE.RangeIntValue, value + "_" + minvalue + "_" + maxvalue, eposx, eposy
         );
      }

      public static TabletConfigurationScreen.ConfigElement createIntValue(String configValue, int value, int eposx, int eposy) {
         return new TabletConfigurationScreen.ConfigElement(
            configValue, TabletConfigurationScreen.ConfigElement.TYPE.RangeIntValue, value + "_" + 0 + "_" + Integer.MAX_VALUE, eposx, eposy
         );
      }

      public static TabletConfigurationScreen.ConfigElement createColorValue(String configValue, int color, int eposx, int eposy) {
         return new TabletConfigurationScreen.ConfigElement(
            configValue, TabletConfigurationScreen.ConfigElement.TYPE.COLOR, String.valueOf(color), eposx, eposy
         );
      }

      public static TabletConfigurationScreen.ConfigElement createString(String configValue, String value, int eposx, int eposy) {
         return new TabletConfigurationScreen.ConfigElement(configValue, TabletConfigurationScreen.ConfigElement.TYPE.STRING, value, eposx, eposy);
      }

      public static String dividiTestoMaxLenght(String testo, int maxLength) {
         StringBuilder result = new StringBuilder();
         int lineLength = 0;

         for (String token : testo.split("(?<=\\n)| ")) {
            if (token.contains("\n")) {
               result.append(token);
               lineLength = 0;
            } else {
               int wordLength = token.length();
               if (lineLength > 0 && lineLength + 1 + wordLength > maxLength) {
                  result.append("\n");
                  lineLength = 0;
               } else if (lineLength > 0) {
                  result.append(" ");
                  lineLength++;
               }

               result.append(token);
               lineLength += wordLength;
            }
         }

         return result.toString();
      }

      enum TYPE {
         BOOLEAN,
         RangeIntValue,
         STRING,
         COLOR;
      }
   }
}
