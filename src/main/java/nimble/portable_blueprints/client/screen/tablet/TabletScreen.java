package nimble.portable_blueprints.client.screen.tablet;

import com.mojang.blaze3d.vertex.PoseStack;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import nimble.portable_blueprints.client.ServerToClientDatasShared;
import nimble.portable_blueprints.client.VisualizeBlueprintDatas;
import nimble.portable_blueprints.client.screen.tablet.mineprint.TabletMinePrintScreen;
import nimble.portable_blueprints.init.TabletClientDatas;
import nimble.portable_blueprints.network.request_specific_data.CtoS_RequestSpecificData;
import nimble.portable_blueprints.robautile.AdvancedButton;
import nimble.portable_blueprints.robautile.Operazioni;
import nimble.portable_blueprints.robautile.RenderingMethods;
import org.joml.Vector2i;

public class TabletScreen extends Screen {
   private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("portable_blueprints", "textures/gui/tablet/tablet_gui.png");
   private int CentroX;
   private int CentroY;
   private int W;
   private int H;
   private AdvancedButton B_APP_CONFIGURATION;
   private AdvancedButton B_APP_BLUEPRINT;
   private AdvancedButton B_APP_MINEPRINT;
   private AdvancedButton B_APP_INFO;
   private AdvancedButton B_SETTINGS;
   private AdvancedButton B_CHANGE_BG_LEFT;
   private AdvancedButton B_CHANGE_BG_RIGHT;
   private final TabletScreen.OpenAppAnimation OAA;
   private final Long EndAnimation;
   private TabletScreen.linkItem[] linkItems = new TabletScreen.linkItem[5];
   private boolean settingsOpen = false;
   private static final short BGMAX = 8;
   private boolean isOpeningApp = false;
   private short TickCount = 1;
   public static List<Integer> IDSBlueprints = new ArrayList<>();

   protected TabletScreen(boolean withAnimation, Component component) {
      super(component);
      this.OAA = new TabletScreen.OpenAppAnimation();
      this.EndAnimation = System.currentTimeMillis() + (withAnimation ? 400 : 0);
      CtoS_RequestSpecificData.send(53, CtoS_RequestSpecificData.TipoRichiesta.GET_TABLET_SOCIAL_LINKS);
   }

   public TabletScreen(boolean withAnimation) {
      super(Component.literal("portable_blueprint.tablet"));
      this.OAA = new TabletScreen.OpenAppAnimation();
      this.EndAnimation = System.currentTimeMillis() + (withAnimation ? 400 : 0);
      CtoS_RequestSpecificData.send(53, CtoS_RequestSpecificData.TipoRichiesta.GET_TABLET_SOCIAL_LINKS);
   }

   protected void init() {
      super.init();
      this.W = this.width;
      this.H = this.height;
      this.CentroX = this.W / 2;
      this.CentroY = this.H / 2;
      this.setButtonsApp();
      this.addRenderableWidget(getTabletButton(this.CentroX, this.CentroY));
   }

   public void render(GuiGraphics gg, int mouseX, int mouseY, float delta) {
      if (this.linkItems[0] == null && ServerToClientDatasShared.RichiestaString.containsKey(53)) {
         String v = ServerToClientDatasShared.RichiestaString.get(53);

         for (int x = 0; x < 5; x++) {
            if (v.split(";").length - 1 >= x) {
               String link = v.split(";")[x];
               if (!link.equals("")) {
                  this.linkItems[x] = new TabletScreen.linkItem(link);
               } else {
                  this.linkItems[x] = null;
               }
            } else {
               this.linkItems[x] = null;
            }
         }
      }

      PoseStack poseStack = gg.pose();
      poseStack.pushPose();
      float t = 1.0F - (float)(this.EndAnimation - System.currentTimeMillis()) / 400.0F;
      if (t > 1.0F) {
         t = 1.0F;
      }

      poseStack.translate(this.CentroX, this.CentroY, 0.0F);
      float scale = Operazioni.lerp(0.4F, 1.0F, t);
      poseStack.scale(scale, scale, 1.0F);
      poseStack.translate(-this.CentroX, -this.CentroY, 0.0F);
      poseStack.translate(0.0F, Operazioni.lerp(this.CentroY * 3, 0.0F, t), 0.0F);
      renderTabletBackground(gg, this.CentroX, this.CentroY, "b" + TabletClientDatas.getSettings().selectedBG);
      if (!this.settingsOpen) {
         int posPlus = 0;

         for (TabletScreen.linkItem linkItem : this.linkItems) {
            if (linkItem != null) {
               linkItem.render(gg, this.CentroX - 68 + posPlus, this.CentroY + 69, mouseX, mouseY);
               posPlus += 24;
            }
         }
      } else {
         gg.fill(this.CentroX - 70, this.CentroY - 97, this.CentroX - 40, this.CentroY + 89, 1442840575);
         gg.fill(this.CentroX + 70, this.CentroY - 97, this.CentroX + 40, this.CentroY + 89, 1442840575);
      }

      try {
         super.render(gg, mouseX, mouseY, delta);
      } catch (Exception var13) {
      }

      if (!this.OAA.isStarted && !this.settingsOpen) {
         if (this.B_APP_BLUEPRINT != null) {
            RenderingMethods.GuiGraphicsTestoScalato(
               gg,
               Component.translatable("portable_blueprints.gui.tablet.app.blueprint").getString(),
               0.5F,
               Minecraft.getInstance().font,
               new Vec2(this.B_APP_BLUEPRINT.posX + 16, this.B_APP_BLUEPRINT.posY + 36),
               16777215,
               true,
               RenderingMethods.PosizioneTesto.CENTRO,
               true
            );
         }

         if (this.B_APP_MINEPRINT != null) {
            RenderingMethods.GuiGraphicsTestoScalato(
               gg,
               Component.translatable("portable_blueprints.gui.tablet.app.mineprint").getString(),
               0.5F,
               Minecraft.getInstance().font,
               new Vec2(this.B_APP_MINEPRINT.posX + 16, this.B_APP_MINEPRINT.posY + 36),
               16777215,
               true,
               RenderingMethods.PosizioneTesto.CENTRO,
               true
            );
         }

         if (this.B_APP_INFO != null) {
            RenderingMethods.GuiGraphicsTestoScalato(
               gg,
               Component.translatable("portable_blueprints.gui.tablet.app.info").getString(),
               0.5F,
               Minecraft.getInstance().font,
               new Vec2(this.B_APP_INFO.posX + 16, this.B_APP_INFO.posY + 36),
               16777215,
               true,
               RenderingMethods.PosizioneTesto.CENTRO,
               true
            );
         }

         if (this.B_APP_CONFIGURATION != null) {
            RenderingMethods.GuiGraphicsTestoScalato(
               gg,
               Component.translatable("portable_blueprints.gui.tablet.app.configuration").getString(),
               0.5F,
               Minecraft.getInstance().font,
               new Vec2(this.B_APP_CONFIGURATION.posX + 16, this.B_APP_CONFIGURATION.posY + 36),
               16777215,
               true,
               RenderingMethods.PosizioneTesto.CENTRO,
               true
            );
         }
      }

      this.OAA.render(gg);
      renderTabletUpBar(gg, this.CentroX, this.CentroY);
      poseStack.popPose();
   }

   public void setButtonsApp() {
      int ofX = 192;
      int ofY = 0;
      int posX = this.CentroX - 60;
      int posY = this.CentroY - 77;
      this.B_APP_BLUEPRINT = AdvancedButton.builder(Component.literal(""), button -> this.B_Open_App(0))
         .pos(posX, posY)
         .size(32, 32)
         .defaultImage(new AdvancedButton.Image(TEXTURE, posX, posY, ofX, ofY, 32, 32))
         .build();
      this.addRenderableWidget(this.B_APP_BLUEPRINT);
      this.B_APP_BLUEPRINT.visible = !this.settingsOpen;
      posX = this.CentroX - 16;
      ofX += 32;
      this.B_APP_MINEPRINT = AdvancedButton.builder(Component.literal(""), button -> this.B_Open_App(1))
         .pos(posX, posY)
         .size(32, 32)
         .defaultImage(new AdvancedButton.Image(TEXTURE, posX, posY, ofX, ofY, 32, 32))
         .build();
      this.addRenderableWidget(this.B_APP_MINEPRINT);
      this.B_APP_MINEPRINT.visible = !this.settingsOpen;
      posX = this.CentroX + 28;
      ofX -= 32;
      ofY += 32;
      this.B_APP_INFO = AdvancedButton.builder(Component.literal(""), button -> this.B_Open_App(2))
         .pos(posX, posY)
         .size(32, 32)
         .defaultImage(new AdvancedButton.Image(TEXTURE, posX, posY, ofX, ofY, 32, 32))
         .build();
      this.addRenderableWidget(this.B_APP_INFO);
      this.B_APP_INFO.visible = !this.settingsOpen;
      posX = this.CentroX - 60;
      posY += 50;
      ofX += 32;
      this.B_APP_CONFIGURATION = AdvancedButton.builder(Component.literal(""), button -> this.B_Open_App(3))
         .pos(posX, posY)
         .size(32, 32)
         .defaultImage(new AdvancedButton.Image(TEXTURE, posX, posY, ofX, ofY, 32, 32))
         .build();
      this.addRenderableWidget(this.B_APP_CONFIGURATION);
      this.B_APP_CONFIGURATION.visible = !this.settingsOpen;
      posX = this.CentroX + 52;
      posY = this.CentroY + 70;
      this.B_SETTINGS = AdvancedButton.builder(Component.literal(""), button -> this.B_OpenSettings())
         .pos(posX, posY)
         .size(16, 16)
         .defaultImage(new AdvancedButton.Image(TEXTURE, posX, posY, 32, 208, 16, 16))
         .build();
      this.addRenderableWidget(this.B_SETTINGS);
      posX = this.CentroX - 70;
      posY = this.CentroY - 97;
      this.B_CHANGE_BG_LEFT = AdvancedButton.builder(Component.literal(""), button -> this.B_CHANGE_BG(false))
         .pos(posX, posY)
         .size(30, 186)
         .defaultImage(new AdvancedButton.Image(TEXTURE, posX + 4, this.CentroY - 18, 216, 188, 20, 36))
         .build();
      this.addRenderableWidget(this.B_CHANGE_BG_LEFT);
      this.B_CHANGE_BG_LEFT.visible = this.settingsOpen;
      posX = this.CentroX + 40;
      posY = this.CentroY - 97;
      this.B_CHANGE_BG_RIGHT = AdvancedButton.builder(Component.literal(""), button -> this.B_CHANGE_BG(true))
         .pos(posX, posY)
         .size(30, 186)
         .defaultImage(new AdvancedButton.Image(TEXTURE, posX + 6, this.CentroY - 18, 236, 188, 20, 36))
         .build();
      this.addRenderableWidget(this.B_CHANGE_BG_RIGHT);
      this.B_CHANGE_BG_RIGHT.visible = this.settingsOpen;
   }

   private void B_CHANGE_BG(boolean right) {
      TabletClientDatas.getSettings().selectedBG += (short)(right ? 1 : -1);
      if (TabletClientDatas.getSettings().selectedBG <= 0) {
         TabletClientDatas.Settings settings = TabletClientDatas.getSettings();
         settings.selectedBG = 8;
         TabletClientDatas.setSettings(settings);
      } else if (TabletClientDatas.getSettings().selectedBG > 8) {
         TabletClientDatas.Settings settings = TabletClientDatas.getSettings();
         settings.selectedBG = 1;
         TabletClientDatas.setSettings(settings);
      }

      TabletClientDatas.save();
   }

   private void B_OpenSettings() {
      this.settingsOpen = !this.settingsOpen;
      this.B_APP_BLUEPRINT.visible = !this.settingsOpen;
      this.B_APP_MINEPRINT.visible = !this.settingsOpen;
      this.B_APP_INFO.visible = !this.settingsOpen;
      this.B_APP_CONFIGURATION.visible = !this.settingsOpen;
      this.B_CHANGE_BG_LEFT.visible = this.settingsOpen;
      this.B_CHANGE_BG_RIGHT.visible = this.settingsOpen;
   }

   private void B_Open_App(int tipo) {
      if (!this.isOpeningApp) {
         this.isOpeningApp = true;
         AdvancedButton bSelect = tipo == 3
            ? this.B_APP_CONFIGURATION
            : (tipo == 2 ? this.B_APP_INFO : (tipo == 0 ? this.B_APP_BLUEPRINT : this.B_APP_MINEPRINT));
         bSelect.visible = false;
         this.OAA.start(tipo);
      }
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
      if (!this.settingsOpen) {
         for (TabletScreen.linkItem li : this.linkItems) {
            if (li != null && li.isOver) {
               li.use();
               break;
            }
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      return super.mouseReleased(mouseX, mouseY, button);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double delta, double delta2) {
      return super.mouseScrolled(mouseX, mouseY, delta2, delta);
   }

   public boolean isPauseScreen() {
      return false;
   }

   public static void renderTabletBackground(GuiGraphics gg, int CentroX, int CentroY, String bgName) {
      gg.blit(TEXTURE, CentroX - 77, CentroY - 104, 0, 0, 154, 208);
      ResourceLocation bg = ResourceLocation.fromNamespaceAndPath("portable_blueprints", "textures/gui/tablet/backgrounds/" + bgName + ".png");
      if (Minecraft.getInstance().getResourceManager().getResource(bg).isPresent()) {
         gg.blit(bg, CentroX - 70, CentroY - 97, 0, 0, 140, 186);
      }
   }

   public static void renderTabletUpBar(GuiGraphics gg, int CentroX, int CentroY) {
      gg.fill(CentroX - 70, CentroY - 97, CentroX + 70, CentroY - 87, -1442840576);
      RenderingMethods.GuiGraphicsTestoScalato(
         gg,
         Operazioni.getWorldTimeString(),
         0.8F,
         Minecraft.getInstance().font,
         new Vec2(CentroX - 67, CentroY - 92),
         16777215,
         false,
         RenderingMethods.PosizioneTesto.SINISTRA,
         true
      );
      gg.blit(TEXTURE, CentroX + 44, CentroY - 96, 0, 217, 23, 7);
   }

   public static AdvancedButton getTabletButton(int CentroX, int CentroY) {
      int posX = CentroX - 15;
      int posY = CentroY + 90;
      return AdvancedButton.builder(Component.literal(""), button -> OpenGUI(false))
         .pos(posX, posY)
         .size(30, 11)
         .defaultImage(new AdvancedButton.Image(TEXTURE, posX, posY, 226, 234, 30, 11))
         .hoverImage(new AdvancedButton.Image(TEXTURE, posX, posY, 226, 245, 30, 11))
         .build();
   }

   public static void OpenGUI(boolean withAnimation) {
      Minecraft.getInstance().setScreen(new TabletScreen(withAnimation));
   }

   public static void OpenGUI() {
      Minecraft.getInstance().setScreen(new TabletScreen(true));
   }

   public void onClose() {
      resetTablet();
      super.onClose();
   }

   public static void resetTablet() {
      for (int requestid : IDSBlueprints) {
         if (VisualizeBlueprintDatas.BlueprintsMap.containsKey(requestid)) {
            VisualizeBlueprintDatas.BlueprintsMap.remove(requestid);
         }
      }

      IDSBlueprints.clear();
   }

   protected void renderBlurredBackground(float partialTick) {
   }

   public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
   }

   private class OpenAppAnimation {
      public Long StartAnim = 0L;
      public boolean isStarted = false;
      public RenderingMethods.GuiGraphicsElement Icona;
      private int startPX;
      private int startPY;
      private int color = -1;
      private int tipo = -1;

      public void start(int tipo) {
         if (tipo == 0) {
            this.Icona = new RenderingMethods.GuiGraphicsElement(
               new RenderingMethods.GuiGraphicsElement.BLIT_Parametri(0, 0, 192, 0, 32, 32, TabletScreen.TEXTURE)
            );
            this.startPX = -60;
            this.startPY = -77;
            this.color = -13864770;
         } else if (tipo == 1) {
            this.Icona = new RenderingMethods.GuiGraphicsElement(
               new RenderingMethods.GuiGraphicsElement.BLIT_Parametri(0, 0, 224, 0, 32, 32, TabletScreen.TEXTURE)
            );
            this.startPX = -18;
            this.startPY = -77;
            this.color = -5383962;
         } else if (tipo == 2) {
            this.Icona = new RenderingMethods.GuiGraphicsElement(
               new RenderingMethods.GuiGraphicsElement.BLIT_Parametri(0, 0, 192, 32, 32, 32, TabletScreen.TEXTURE)
            );
            this.startPX = 28;
            this.startPY = -77;
            this.color = -13198269;
         } else if (tipo == 3) {
            this.Icona = new RenderingMethods.GuiGraphicsElement(
               new RenderingMethods.GuiGraphicsElement.BLIT_Parametri(0, 0, 224, 32, 32, 32, TabletScreen.TEXTURE)
            );
            this.startPX = -60;
            this.startPY = -27;
            this.color = -11050897;
         }

         this.StartAnim = System.currentTimeMillis() + 350L;
         this.isStarted = true;
         this.tipo = tipo;
      }

      public void render(GuiGraphics gg) {
         if (this.isStarted) {
            float t = 1.0F - (float)(this.StartAnim - System.currentTimeMillis()) / 350.0F;
            RenderingMethods.enableScissorFromGui(Minecraft.getInstance(), TabletScreen.this.CentroX - 70, TabletScreen.this.CentroY - 97, 140, 186);
            float t2 = t;
            if (t2 > 1.0F) {
               t2 = 1.0F;
            }

            Vec2 p = new Vec2(
               Operazioni.lerp(TabletScreen.this.CentroX + this.startPX, TabletScreen.this.CentroX - 16, t2),
               Operazioni.lerp(TabletScreen.this.CentroY + this.startPY, TabletScreen.this.CentroY - 16, t2)
            );
            int size = (int)Operazioni.lerp(0.0F, 97.0F, t2 / 0.8F);
            gg.fill((int)p.x - size + 16, (int)p.y - size + 16, (int)p.x + size + 16, (int)p.y + size + 16, this.color);
            RenderingMethods.disableScissor();
            this.Icona.blitParametri.setPos((int)p.x, (int)p.y);
            this.Icona.render(gg);
            if (t > 2.0F) {
               if (this.tipo == 0) {
                  TabletBlueprintScreen.OpenGUI();
               } else if (this.tipo == 1) {
                  TabletMinePrintScreen.OpenGUI(TabletMinePrintScreen.PageType.SERVER);
               } else if (this.tipo == 2) {
                  TabletInfoScreen.OpenGUI();
               } else if (this.tipo == 3) {
                  TabletConfigurationScreen.OpenGUI();
               }
            }
         }
      }
   }

   private class linkItem {
      private final Vector2i icon;
      private final String link;
      private boolean isOver = false;
      private static Map<String, Vector2i> Icons = Map.of(
         "general",
         new Vector2i(134, 238),
         "discord",
         new Vector2i(206, 238),
         "youtube",
         new Vector2i(188, 238),
         "instagram",
         new Vector2i(170, 238),
         "twitch",
         new Vector2i(152, 238)
      );

      public linkItem(String link) {
         this.link = link;
         Vector2i icon_v = Icons.get("general");

         try {
            URI uri = new URI(link);
            String host = uri.getHost();
            if (host != null) {
               if (host.startsWith("www.")) {
                  host = host.substring(4);
               }

               String dominio = host.split("\\.")[0];
               if (Icons.containsKey(dominio)) {
                  icon_v = Icons.get(dominio);
               }
            }
         } catch (Exception var7) {
         }

         this.icon = icon_v;
      }

      public void render(GuiGraphics gg, int posx, int posy, int mouseX, int mouseY) {
         gg.blit(TabletScreen.TEXTURE, posx, posy, this.icon.x, this.icon.y, 18, 18);
         if (this.isOver(mouseX, mouseY, posx, posy)) {
            gg.renderTooltip(Minecraft.getInstance().font, Component.literal(this.link), posx + 1 - Minecraft.getInstance().font.width(this.link) / 2, posy - 5);
            gg.blit(TabletScreen.TEXTURE, posx + 4, posy - 6, 0, 211, 10, 5);
            this.isOver = true;
         } else {
            this.isOver = false;
         }
      }

      private boolean isOver(int mouseX, int mouseY, int posx, int posy) {
         return mouseX >= posx && mouseX < posx + 18 && mouseY >= posy && mouseY < posy + 18;
      }

      private void use() {
         if (this.isOver) {
            Util.getPlatform().openUri(this.link);
         }
      }
   }
}
