package nimble.portable_blueprints.client.screen.tablet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import nimble.portable_blueprints.client.screen.tablet.utils.TabletScrollBar;
import nimble.portable_blueprints.init.ModItems;
import nimble.portable_blueprints.robautile.AdvancedButton;
import nimble.portable_blueprints.robautile.Operazioni;
import nimble.portable_blueprints.robautile.RenderingMethods;
import org.joml.Vector2i;

public class TabletInfoScreen extends Screen {
   private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("portable_blueprints", "textures/gui/tablet/tablet_info_gui.png");
   private static final ResourceLocation TEXTURE2 = ResourceLocation.fromNamespaceAndPath("portable_blueprints", "textures/gui/tablet/tablet_gui.png");
   private static int CentroX;
   private static int CentroY;
   private static int W;
   private static int H;
   private final TabletInfoScreen.AreaCategory areaCategory;
   private short activedocumentation = 0;
   public int ScrollMax = 0;
   public int Scroll = 0;
   private final TabletInfoScreen.Documentation[] PagineDocumentazione = new TabletInfoScreen.Documentation[]{
      new TabletInfoScreen.Documentation() {
         private String DescrizioneTablet1 = "";

         @Override
         public void start() {
            this.DescrizioneTablet1 = TabletInfoScreen.Documentation.dividiTestoMaxLenght(
               Component.translatable("portable_blueprints.gui.tablet.app.info.tablet.description1")
                  .getString()
                  .replace("%v%", Component.translatable(((Item)ModItems.TABLET.get()).getDescriptionId()).getString()),
               25
            );
            this.addCraftingItem((Item)ModItems.TABLET.get(), 16, -10);
            TabletInfoScreen.this.ScrollMax = 105 + (this.DescrizioneTablet1.split("\n").length <= 19 ? 0 : this.DescrizioneTablet1.split("\n").length * 5);
         }

         @Override
         public void render(GuiGraphics gg) {
            super.render(gg);
            String var10001 = ((Item)ModItems.TABLET.get()).getName(new ItemStack((ItemLike)ModItems.TABLET.get())).getString();
            float var10004 = TabletInfoScreen.CentroX - 10;
            float var10005 = TabletInfoScreen.CentroY - 64;
            RenderingMethods.GUITextAutoResize(gg, var10001, Minecraft.getInstance().font, 0, var10004, var10005, 74, 40, false);
            gg.pose().pushPose();
            gg.pose().scale(2.0F, 2.0F, 1.0F);
            gg.pose().translate(-TabletInfoScreen.CentroX / 2.0F, -TabletInfoScreen.CentroY / 2.0F, 0.0F);
            gg.renderFakeItem(new ItemStack((ItemLike)ModItems.TABLET.get()), TabletInfoScreen.CentroX - 22, TabletInfoScreen.CentroY - 30);
            gg.pose().popPose();
            int posY = TabletInfoScreen.CentroY - 14;
            if (this.craftingItem != null) {
               RenderingMethods.GuiGraphicsTestoScalato(
                  gg,
                  Component.translatable("portable_blueprints.gui.tablet.app.info.general.recipe").getString(),
                  1.0F,
                  Minecraft.getInstance().font,
                  new Vec2(TabletInfoScreen.CentroX - 40, posY),
                  0,
                  false,
                  RenderingMethods.PosizioneTesto.SINISTRA,
                  true
               );
               Map<String, Integer> item_quantity = new HashMap<>();

               for (Item item : this.craftingItem.itemShow) {
                  String id = item.getDescriptionId();
                  if (item_quantity.containsKey(id)) {
                     item_quantity.put(id, item_quantity.get(id) + 1);
                  } else {
                     item_quantity.put(id, 1);
                  }
               }

               posY += 5;

               for (String itemid : item_quantity.keySet()) {
                  String testo = "• " + Component.translatable(itemid).getString() + " x" + item_quantity.get(itemid);
                  posY += 6;
                  RenderingMethods.GuiGraphicsTestoScalato(
                     gg,
                     testo,
                     testo.length() <= 29 ? 0.35F : Operazioni.lerp(0.35F, 0.01F, (testo.length() - 29) / 50.0F),
                     Minecraft.getInstance().font,
                     new Vec2(TabletInfoScreen.CentroX - 40, posY),
                     0,
                     false,
                     RenderingMethods.PosizioneTesto.SINISTRA,
                     true
                  );
               }

               posY += 60 - (item_quantity.size() < 10 ? item_quantity.size() * 6 : 0);
            }

            RenderingMethods.GuiGraphicsTestoScalato(
               gg,
               this.DescrizioneTablet1,
               0.65F,
               Minecraft.getInstance().font,
               new Vec2(TabletInfoScreen.CentroX - 40, posY),
               0,
               false,
               RenderingMethods.PosizioneTesto.SINISTRA,
               true
            );
         }
      },
      new TabletInfoScreen.Documentation() {
         private String DescrizioneScanner1 = "";

         @Override
         public void start() {
            this.DescrizioneScanner1 = TabletInfoScreen.Documentation.dividiTestoMaxLenght(
               Component.translatable("portable_blueprints.gui.tablet.app.info.scanner.description1")
                  .getString()
                  .replace("%v%", Component.translatable(((Item)ModItems.SCANNER.get()).getDescriptionId()).getString())
                  .replace("%v2%", Component.translatable(((Item)ModItems.BLUEPRINT.get()).getDescriptionId()).getString()),
               25
            );
            this.addCraftingItem((Item)ModItems.SCANNER.get(), 16, -10);
            TabletInfoScreen.this.ScrollMax = 105 + (this.DescrizioneScanner1.split("\n").length <= 19 ? 0 : this.DescrizioneScanner1.split("\n").length * 5);
         }

         @Override
         public void render(GuiGraphics gg) {
            super.render(gg);
            String var10001 = ((Item)ModItems.SCANNER.get()).getName(new ItemStack((ItemLike)ModItems.SCANNER.get())).getString();
            float var10004 = TabletInfoScreen.CentroX - 6;
            float var10005 = TabletInfoScreen.CentroY - 64;
            RenderingMethods.GUITextAutoResize(gg, var10001, Minecraft.getInstance().font, 0, var10004, var10005, 74, 40, false);
            gg.pose().pushPose();
            gg.pose().scale(2.0F, 2.0F, 1.0F);
            gg.pose().translate(-TabletInfoScreen.CentroX / 2.0F, -TabletInfoScreen.CentroY / 2.0F, 0.0F);
            gg.renderFakeItem(new ItemStack((ItemLike)ModItems.SCANNER.get()), TabletInfoScreen.CentroX - 20, TabletInfoScreen.CentroY - 30);
            gg.pose().popPose();
            int posY = TabletInfoScreen.CentroY - 14;
            if (this.craftingItem != null) {
               RenderingMethods.GuiGraphicsTestoScalato(
                  gg,
                  Component.translatable("portable_blueprints.gui.tablet.app.info.general.recipe").getString(),
                  1.0F,
                  Minecraft.getInstance().font,
                  new Vec2(TabletInfoScreen.CentroX - 40, posY),
                  0,
                  false,
                  RenderingMethods.PosizioneTesto.SINISTRA,
                  true
               );
               Map<String, Integer> item_quantity = new HashMap<>();

               for (Item item : this.craftingItem.itemShow) {
                  String id = item.getDescriptionId();
                  if (item_quantity.containsKey(id)) {
                     item_quantity.put(id, item_quantity.get(id) + 1);
                  } else {
                     item_quantity.put(id, 1);
                  }
               }

               posY += 5;

               for (String itemid : item_quantity.keySet()) {
                  String testo = "• " + Component.translatable(itemid).getString() + " x" + item_quantity.get(itemid);
                  posY += 6;
                  RenderingMethods.GuiGraphicsTestoScalato(
                     gg,
                     testo,
                     testo.length() <= 29 ? 0.35F : Operazioni.lerp(0.35F, 0.01F, (testo.length() - 29) / 50.0F),
                     Minecraft.getInstance().font,
                     new Vec2(TabletInfoScreen.CentroX - 40, posY),
                     0,
                     false,
                     RenderingMethods.PosizioneTesto.SINISTRA,
                     true
                  );
               }

               posY += 60 - (item_quantity.size() < 10 ? item_quantity.size() * 6 : 0);
            }

            RenderingMethods.GuiGraphicsTestoScalato(
               gg,
               this.DescrizioneScanner1,
               0.65F,
               Minecraft.getInstance().font,
               new Vec2(TabletInfoScreen.CentroX - 40, posY),
               0,
               false,
               RenderingMethods.PosizioneTesto.SINISTRA,
               true
            );
         }
      },
      new TabletInfoScreen.Documentation() {
         private String DescrizioneBlueprint1 = "";

         @Override
         public void start() {
            this.DescrizioneBlueprint1 = TabletInfoScreen.Documentation.dividiTestoMaxLenght(
               Component.translatable("portable_blueprints.gui.tablet.app.info.blueprint.description1")
                  .getString()
                  .replace("%v%", Component.translatable(((Item)ModItems.BLUEPRINT.get()).getDescriptionId()).getString())
                  .replace("%v2%", Component.translatable(((Item)ModItems.SCANNER.get()).getDescriptionId()).getString())
                  .replace("%v3%", Component.translatable(((Item)ModItems.TABLET.get()).getDescriptionId()).getString()),
               25
            );
            TabletInfoScreen.this.ScrollMax = 40
               + (this.DescrizioneBlueprint1.split("\n").length <= 19 ? 0 : this.DescrizioneBlueprint1.split("\n").length * 5);
         }

         @Override
         public void render(GuiGraphics gg) {
            super.render(gg);
            String var10001 = ((Item)ModItems.BLUEPRINT.get()).getName(new ItemStack((ItemLike)ModItems.BLUEPRINT.get())).getString();
            float var10004 = TabletInfoScreen.CentroX - 6;
            float var10005 = TabletInfoScreen.CentroY - 64;
            RenderingMethods.GUITextAutoResize(gg, var10001, Minecraft.getInstance().font, 0, var10004, var10005, 74, 40, false);
            gg.pose().pushPose();
            gg.pose().scale(2.0F, 2.0F, 1.0F);
            gg.pose().translate(-TabletInfoScreen.CentroX / 2.0F, -TabletInfoScreen.CentroY / 2.0F, 0.0F);
            gg.renderFakeItem(new ItemStack((ItemLike)ModItems.BLUEPRINT.get()), TabletInfoScreen.CentroX - 20, TabletInfoScreen.CentroY - 30);
            gg.pose().popPose();
            int posY = TabletInfoScreen.CentroY - 14;
            RenderingMethods.GuiGraphicsTestoScalato(
               gg,
               this.DescrizioneBlueprint1,
               0.65F,
               Minecraft.getInstance().font,
               new Vec2(TabletInfoScreen.CentroX - 40, posY),
               0,
               false,
               RenderingMethods.PosizioneTesto.SINISTRA,
               true
            );
         }
      },
      new TabletInfoScreen.Documentation() {
         private String DescrizioneSearchbar1 = "";
         private String DescrizioneSearchbar2 = "";

         @Override
         public void start() {
            this.DescrizioneSearchbar1 = TabletInfoScreen.Documentation.dividiTestoMaxLenght(
               Component.translatable("portable_blueprints.gui.tablet.app.info.searchbar.description1").getString(), 25
            );
            this.DescrizioneSearchbar2 = TabletInfoScreen.Documentation.dividiTestoMaxLenght(
               Component.translatable("portable_blueprints.gui.tablet.app.info.searchbar.description2")
                  .getString()
                  .replace("%v%", Component.translatable(Items.NAME_TAG.getDescriptionId()).getString()),
               25
            );
            TabletInfoScreen.this.ScrollMax = 50
               + (this.DescrizioneSearchbar1.split("\n").length <= 18 ? 0 : (this.DescrizioneSearchbar1.split("\n").length - 4) * 6)
               + (this.DescrizioneSearchbar2.split("\n").length - 4) * 6;
         }

         @Override
         public void render(GuiGraphics gg) {
            super.render(gg);
            String var10001 = Component.translatable("portable_blueprints.gui.tablet.app.info.searchbar.title").getString();
            float var10004 = TabletInfoScreen.CentroX - 6;
            float var10005 = TabletInfoScreen.CentroY - 64;
            RenderingMethods.GUITextAutoResize(gg, var10001, Minecraft.getInstance().font, 0, var10004, var10005, 74, 40, false);
            gg.pose().pushPose();
            gg.pose().translate(TabletInfoScreen.CentroX - 49, TabletInfoScreen.CentroY - 70, 0.0F);
            gg.pose().scale(3.0F, 3.0F, 1.0F);
            gg.blit(TabletInfoScreen.TEXTURE, 0, 0, 48, 240, 16, 16);
            gg.pose().popPose();
            int posY = TabletInfoScreen.CentroY - 14;
            RenderingMethods.GuiGraphicsTestoScalato(
               gg,
               this.DescrizioneSearchbar1,
               0.65F,
               Minecraft.getInstance().font,
               new Vec2(TabletInfoScreen.CentroX - 40, posY),
               0,
               false,
               RenderingMethods.PosizioneTesto.SINISTRA,
               true
            );
            posY += this.DescrizioneSearchbar1.split("\n").length * 6;
            gg.blit(TabletInfoScreen.TEXTURE, TabletInfoScreen.CentroX - 43, posY - 6, 48, 240, 16, 16);
            posY += 16;
            RenderingMethods.GuiGraphicsTestoScalato(
               gg,
               this.DescrizioneSearchbar2,
               0.65F,
               Minecraft.getInstance().font,
               new Vec2(TabletInfoScreen.CentroX - 40, posY),
               0,
               false,
               RenderingMethods.PosizioneTesto.SINISTRA,
               true
            );
            posY += this.DescrizioneSearchbar2.split("\n").length * 6;
            gg.blit(TabletInfoScreen.TEXTURE, TabletInfoScreen.CentroX - 43, posY - 6, 48, 208, 16, 16);
         }
      },
      new TabletInfoScreen.Documentation() {
         private String Descrizione1 = "";

         @Override
         public void start() {
            this.Descrizione1 = TabletInfoScreen.Documentation.dividiTestoMaxLenght(
               Component.translatable("portable_blueprints.gui.tablet.app.info.blueprint_app.description1")
                  .getString()
                  .replace("%v%", Component.translatable(((Item)ModItems.BLUEPRINT.get()).getDescriptionId()).getString()),
               25
            );
            TabletInfoScreen.this.ScrollMax = 40 + (this.Descrizione1.split("\n").length <= 19 ? 0 : this.Descrizione1.split("\n").length * 5);
         }

         @Override
         public void render(GuiGraphics gg) {
            super.render(gg);
            String var10001 = Component.translatable("portable_blueprints.gui.tablet.app.info.blueprint_app.title").getString();
            float var10004 = TabletInfoScreen.CentroX - 6;
            float var10005 = TabletInfoScreen.CentroY - 64;
            RenderingMethods.GUITextAutoResize(gg, var10001, Minecraft.getInstance().font, 0, var10004, var10005, 74, 40, false);
            gg.blit(TabletInfoScreen.TEXTURE2, TabletInfoScreen.CentroX - 40, TabletInfoScreen.CentroY - 60, 192, 0, 32, 32);
            int posY = TabletInfoScreen.CentroY - 14;
            RenderingMethods.GuiGraphicsTestoScalato(
               gg,
               this.Descrizione1,
               0.65F,
               Minecraft.getInstance().font,
               new Vec2(TabletInfoScreen.CentroX - 40, posY),
               0,
               false,
               RenderingMethods.PosizioneTesto.SINISTRA,
               true
            );
         }
      },
      new TabletInfoScreen.Documentation() {
         private String Descrizione1 = "";

         @Override
         public void start() {
            this.Descrizione1 = TabletInfoScreen.Documentation.dividiTestoMaxLenght(
               Component.translatable("portable_blueprints.gui.tablet.app.info.mineprint_app.description1")
                  .getString()
                  .replace("%v%", Component.translatable("portable_blueprints.gui.tablet.mineprint.area.follow_title").getString())
                  .replace("%v2%", Component.translatable("portable_blueprints.gui.tablet.mineprint.area.players_title").getString())
                  .replace("%v3%", Component.translatable("portable_blueprints.gui.tablet.mineprint.area.server_title").getString())
                  .replace("%v4%", Component.translatable(((Item)ModItems.BLUEPRINT.get()).getDescriptionId()).getString()),
               25
            );
            TabletInfoScreen.this.ScrollMax = 40 + (this.Descrizione1.split("\n").length <= 19 ? 0 : this.Descrizione1.split("\n").length * 5);
         }

         @Override
         public void render(GuiGraphics gg) {
            super.render(gg);
            String var10001 = Component.translatable("portable_blueprints.gui.tablet.app.info.mineprint_app.title").getString();
            float var10004 = TabletInfoScreen.CentroX - 6;
            float var10005 = TabletInfoScreen.CentroY - 64;
            RenderingMethods.GUITextAutoResize(gg, var10001, Minecraft.getInstance().font, 0, var10004, var10005, 74, 40, false);
            gg.blit(TabletInfoScreen.TEXTURE2, TabletInfoScreen.CentroX - 40, TabletInfoScreen.CentroY - 60, 224, 0, 32, 32);
            int posY = TabletInfoScreen.CentroY - 14;
            RenderingMethods.GuiGraphicsTestoScalato(
               gg,
               this.Descrizione1,
               0.65F,
               Minecraft.getInstance().font,
               new Vec2(TabletInfoScreen.CentroX - 40, posY),
               0,
               false,
               RenderingMethods.PosizioneTesto.SINISTRA,
               true
            );
         }
      },
      new TabletInfoScreen.Documentation() {
         private String Descrizione1 = "";
         private int posXButton = 0;
         private int posYButton = 0;

         @Override
         public void start() {
            this.Descrizione1 = TabletInfoScreen.Documentation.dividiTestoMaxLenght(
               Component.translatable("portable_blueprints.gui.tablet.app.info.github.description1").getString(), 25
            );
            TabletInfoScreen.this.ScrollMax = 40 + (this.Descrizione1.split("\n").length <= 19 ? 0 : this.Descrizione1.split("\n").length * 5);
         }

         @Override
         public void render(GuiGraphics gg) {
            super.render(gg);
            String var10001 = Component.translatable("portable_blueprints.gui.tablet.app.info.github.title").getString();
            float var10004 = TabletInfoScreen.CentroX - 6;
            float var10005 = TabletInfoScreen.CentroY - 64;
            RenderingMethods.GUITextAutoResize(gg, var10001, Minecraft.getInstance().font, 0, var10004, var10005, 74, 40, false);
            gg.blit(TabletInfoScreen.TEXTURE, TabletInfoScreen.CentroX - 40, TabletInfoScreen.CentroY - 60, 224, 176, 32, 32);
            int posY = TabletInfoScreen.CentroY - 14;
            RenderingMethods.GuiGraphicsTestoScalato(
               gg,
               this.Descrizione1,
               0.65F,
               Minecraft.getInstance().font,
               new Vec2(TabletInfoScreen.CentroX - 40, posY),
               0,
               false,
               RenderingMethods.PosizioneTesto.SINISTRA,
               true
            );
            posY += this.Descrizione1.split("\n").length * 6;
            this.posXButton = TabletInfoScreen.CentroX - 36;
            this.posYButton = posY;
            gg.blit(TabletInfoScreen.TEXTURE, this.posXButton, this.posYButton, 0, 160, 96, 16);
            RenderingMethods.GuiGraphicsTestoScalato(
               gg,
               Component.translatable("portable_blueprints.gui.tablet.app.info.github.button").getString(),
               1.4F,
               Minecraft.getInstance().font,
               new Vec2(TabletInfoScreen.CentroX + 12, posY + 8),
               0,
               false,
               RenderingMethods.PosizioneTesto.CENTRO,
               true
            );
         }

         @Override
         public void mouseClicked(double mouseX, double mouseY, int button) {
            if (mouseX >= this.posXButton && mouseX < this.posXButton + 96 && mouseY >= this.posYButton && mouseY < this.posYButton + 16) {
               Util.getPlatform().openUri("https://github.com/Nimblonia/Mod_Portable_blueprint_Community/discussions/categories/wiki");
            }
         }
      }
   };
   private TabletScrollBar scrollBar;
   private short TickCount = 1;

   protected TabletInfoScreen(Component component) {
      super(component);
      this.areaCategory = new TabletInfoScreen.AreaCategory();
   }

   public TabletInfoScreen() {
      super(Component.literal("portable_blueprint.tablet.info"));
      this.areaCategory = new TabletInfoScreen.AreaCategory();
      this.PagineDocumentazione[this.activedocumentation].start();
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
      int px = CentroX - 65;
      int py = CentroY - 76;
      this.addRenderableWidget(TabletScreen.getTabletButton(CentroX, CentroY));
      this.PagineDocumentazione[this.activedocumentation].init();

      for (AdvancedButton button : this.areaCategory.init()) {
         this.addRenderableWidget(button);
      }

      this.addRenderableWidget(
         this.scrollBar = new TabletScrollBar(
            CentroX + 68, CentroY - 60, 1, 146, -12696497, -4406063, new TabletScrollBar.ScrollingArea(CentroX - 40, CentroY - 60, 110, 150, 5)
         )
      );
   }

   public void render(GuiGraphics gg, int mouseX, int mouseY, float delta) {
      if (this.scrollBar != null) {
         this.Scroll = (int)this.scrollBar.getValore(this.ScrollMax);
      }

      Minecraft mc = Minecraft.getInstance();
      TabletScreen.renderTabletBackground(gg, CentroX, CentroY, "");
      gg.fill(CentroX - 70, CentroY - 97, CentroX + 70, CentroY + 89, -1);
      RenderingMethods.GuiGraphicsTestoScalato(
         gg,
         Component.translatable("portable_blueprints.gui.tablet.app.info.general.title").getString(),
         2.0F,
         Minecraft.getInstance().font,
         new Vec2(CentroX, CentroY - 76),
         65535,
         false,
         RenderingMethods.PosizioneTesto.CENTRO,
         true
      );
      RenderingMethods.enableScissorFromGui(Minecraft.getInstance(), CentroX - 40, CentroY - 60, 110, 149);
      gg.pose().pushPose();
      gg.pose().translate(0.0F, -this.Scroll, 0.0F);
      this.PagineDocumentazione[this.activedocumentation].render(gg);
      gg.pose().popPose();
      RenderingMethods.disableScissor();
      this.areaCategory.render(gg, CentroX - 70 - 45, CentroY - 60);

      try {
         super.render(gg, mouseX, mouseY, delta);
      } catch (Exception var7) {
      }

      TabletScreen.renderTabletUpBar(gg, CentroX, CentroY);
   }

   public void tick() {
      super.tick();
      if (this.PagineDocumentazione[this.activedocumentation].craftingItem != null) {
         this.PagineDocumentazione[this.activedocumentation].craftingItem.tick(this.TickCount);
      }

      if (++this.TickCount > 20) {
         this.TickCount = 1;
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      this.PagineDocumentazione[this.activedocumentation].mouseClicked(mouseX, mouseY, button);
      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean isPauseScreen() {
      return false;
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      return super.mouseReleased(mouseX, mouseY, button);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double delta, double delta2) {
      return this.scrollBar != null && this.scrollBar.mouseScrolled(mouseX, mouseY, delta2, delta) ? true : super.mouseScrolled(mouseX, mouseY, delta2, delta);
   }

   public static void OpenGUI() {
      Minecraft.getInstance().setScreen(new TabletInfoScreen());
   }

   private void setCategoryPage(short i) {
      this.Scroll = 0;
      if (this.scrollBar != null) {
         this.scrollBar.setAltezzaAttuale(0);
      }

      this.activedocumentation = i;
      this.PagineDocumentazione[this.activedocumentation].start();
      this.PagineDocumentazione[this.activedocumentation].init();
   }

   protected void renderBlurredBackground(float partialTick) {
   }

   public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
   }

   private class AreaCategory {
      AdvancedButton B_TABLET;
      AdvancedButton B_SCANNER;
      AdvancedButton B_BLUEPRINT;
      AdvancedButton B_SEARCHBAR;
      AdvancedButton B_BLUEPRINT_APP;
      AdvancedButton B_MINEPRINT_APP;
      AdvancedButton B_GITHUB;

      public List<AdvancedButton> init() {
         List<AdvancedButton> buttons = new ArrayList<>();
         int posX = TabletInfoScreen.CentroX - 69;
         int posY = TabletInfoScreen.CentroY - 56;
         this.B_TABLET = AdvancedButton.builder(Component.literal(""), button -> TabletInfoScreen.this.setCategoryPage((short)0))
            .pos(posX, posY)
            .size(16, 16)
            .defaultImage(new AdvancedButton.Image(TabletInfoScreen.TEXTURE, posX, posY, 0, 224, 16, 16))
            .hoverImage(new AdvancedButton.Image(TabletInfoScreen.TEXTURE, posX, posY, 0, 240, 16, 16))
            .pressedImageCondition(new AdvancedButton.Image(TabletInfoScreen.TEXTURE, posX, posY, 0, 240, 16, 16))
            .build();
         buttons.add(this.B_TABLET);
         posY += 18;
         this.B_SCANNER = AdvancedButton.builder(Component.literal(""), button -> TabletInfoScreen.this.setCategoryPage((short)1))
            .pos(posX, posY)
            .size(16, 16)
            .defaultImage(new AdvancedButton.Image(TabletInfoScreen.TEXTURE, posX, posY, 16, 224, 16, 16))
            .hoverImage(new AdvancedButton.Image(TabletInfoScreen.TEXTURE, posX, posY, 16, 240, 16, 16))
            .pressedImageCondition(new AdvancedButton.Image(TabletInfoScreen.TEXTURE, posX, posY, 16, 240, 16, 16))
            .build();
         buttons.add(this.B_SCANNER);
         posY += 18;
         this.B_BLUEPRINT = AdvancedButton.builder(Component.literal(""), button -> TabletInfoScreen.this.setCategoryPage((short)2))
            .pos(posX, posY)
            .size(16, 16)
            .defaultImage(new AdvancedButton.Image(TabletInfoScreen.TEXTURE, posX, posY, 32, 224, 16, 16))
            .hoverImage(new AdvancedButton.Image(TabletInfoScreen.TEXTURE, posX, posY, 32, 240, 16, 16))
            .pressedImageCondition(new AdvancedButton.Image(TabletInfoScreen.TEXTURE, posX, posY, 32, 240, 16, 16))
            .build();
         buttons.add(this.B_BLUEPRINT);
         posY += 18;
         this.B_SEARCHBAR = AdvancedButton.builder(Component.literal(""), button -> TabletInfoScreen.this.setCategoryPage((short)3))
            .pos(posX, posY)
            .size(16, 16)
            .defaultImage(new AdvancedButton.Image(TabletInfoScreen.TEXTURE, posX, posY, 48, 224, 16, 16))
            .hoverImage(new AdvancedButton.Image(TabletInfoScreen.TEXTURE, posX, posY, 48, 240, 16, 16))
            .pressedImageCondition(new AdvancedButton.Image(TabletInfoScreen.TEXTURE, posX, posY, 48, 240, 16, 16))
            .build();
         buttons.add(this.B_SEARCHBAR);
         posY += 18;
         this.B_BLUEPRINT_APP = AdvancedButton.builder(Component.literal(""), button -> TabletInfoScreen.this.setCategoryPage((short)4))
            .pos(posX, posY)
            .size(16, 16)
            .defaultImage(new AdvancedButton.Image(TabletInfoScreen.TEXTURE, posX, posY, 64, 224, 16, 16))
            .hoverImage(new AdvancedButton.Image(TabletInfoScreen.TEXTURE, posX, posY, 64, 240, 16, 16))
            .pressedImageCondition(new AdvancedButton.Image(TabletInfoScreen.TEXTURE, posX, posY, 64, 240, 16, 16))
            .build();
         buttons.add(this.B_BLUEPRINT_APP);
         posY += 18;
         this.B_MINEPRINT_APP = AdvancedButton.builder(Component.literal(""), button -> TabletInfoScreen.this.setCategoryPage((short)5))
            .pos(posX, posY)
            .size(16, 16)
            .defaultImage(new AdvancedButton.Image(TabletInfoScreen.TEXTURE, posX, posY, 80, 224, 16, 16))
            .hoverImage(new AdvancedButton.Image(TabletInfoScreen.TEXTURE, posX, posY, 80, 240, 16, 16))
            .pressedImageCondition(new AdvancedButton.Image(TabletInfoScreen.TEXTURE, posX, posY, 80, 240, 16, 16))
            .build();
         buttons.add(this.B_MINEPRINT_APP);
         posY += 18;
         this.B_GITHUB = AdvancedButton.builder(Component.literal(""), button -> TabletInfoScreen.this.setCategoryPage((short)6))
            .pos(posX, posY)
            .size(16, 16)
            .defaultImage(new AdvancedButton.Image(TabletInfoScreen.TEXTURE, posX, posY, 240, 224, 16, 16))
            .hoverImage(new AdvancedButton.Image(TabletInfoScreen.TEXTURE, posX, posY, 240, 240, 16, 16))
            .pressedImageCondition(new AdvancedButton.Image(TabletInfoScreen.TEXTURE, posX, posY, 240, 240, 16, 16))
            .build();
         buttons.add(this.B_GITHUB);
         posY += 18;
         return buttons;
      }

      public void render(GuiGraphics gg, int posX, int posY) {
         if (this.B_TABLET != null) {
            this.B_TABLET.PressedImageCondition = TabletInfoScreen.this.activedocumentation == 0;
         }

         if (this.B_SCANNER != null) {
            this.B_SCANNER.PressedImageCondition = TabletInfoScreen.this.activedocumentation == 1;
         }

         if (this.B_BLUEPRINT != null) {
            this.B_BLUEPRINT.PressedImageCondition = TabletInfoScreen.this.activedocumentation == 2;
         }

         if (this.B_SEARCHBAR != null) {
            this.B_SEARCHBAR.PressedImageCondition = TabletInfoScreen.this.activedocumentation == 3;
         }

         if (this.B_BLUEPRINT_APP != null) {
            this.B_BLUEPRINT_APP.PressedImageCondition = TabletInfoScreen.this.activedocumentation == 4;
         }

         if (this.B_MINEPRINT_APP != null) {
            this.B_MINEPRINT_APP.PressedImageCondition = TabletInfoScreen.this.activedocumentation == 5;
         }

         if (this.B_GITHUB != null) {
            this.B_GITHUB.PressedImageCondition = TabletInfoScreen.this.activedocumentation == 6;
         }

         RenderingMethods.enableScissorFromGui(Minecraft.getInstance(), TabletInfoScreen.CentroX - 70, posY, 100, 149);
         gg.blit(TabletInfoScreen.TEXTURE, posX, posY, 0, 0, 64, 176);
         RenderingMethods.disableScissor();
      }
   }

   private static class Documentation {
      public TabletInfoScreen.Documentation.CraftingItem craftingItem = null;

      public void start() {
      }

      public void render(GuiGraphics gg) {
         if (this.craftingItem != null) {
            this.craftingItem.render(gg);
         }
      }

      public void init() {
         if (this.craftingItem != null) {
            this.craftingItem.init(TabletInfoScreen.CentroX, TabletInfoScreen.CentroY);
         }
      }

      public void mouseClicked(double mouseX, double mouseY, int button) {
      }

      public void addCraftingItem(Item item, int posx, int posy) {
         this.craftingItem = new TabletInfoScreen.Documentation.CraftingItem(item, posx, posy);
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

      private class CraftingItem {
         private int pX;
         private int pY;
         private int posX;
         private int posY;
         private Map<Integer, List<Item>> item = new HashMap<>();
         public List<Item> itemShow = new ArrayList<>();
         private int numeroVarianti = 0;
         private int indexList = 0;

         public CraftingItem(Item item, int posX, int posY) {
            this.pX = posX;
            this.pY = posY;
            this.posX = posX;
            this.posY = posY;
            this.item = getAllCraftingGridsForItem(item);
            this.numeroVarianti = this.item.keySet().size();
            this.init(0, 0);
            this.tick((short)20);
         }

         public void render(GuiGraphics gg) {
            gg.blit(TabletInfoScreen.TEXTURE, this.posX, this.posY, 206, 0, 50, 50);

            for (int y = 0; y < 3; y++) {
               for (int x = 0; x < 3; x++) {
                  int index = x + y * 3;
                  if (index >= this.itemShow.size()) {
                     break;
                  }

                  if (!this.itemShow.get(index).equals(Items.AIR)) {
                     RenderingMethods.RenderItemInGui(
                        gg,
                        new Vector2i(this.posX + 3 + 16 * x, this.posY + 3 + 16 * y),
                        new Vec2(1.0F, -0.5F),
                        0.75F,
                        new ItemStack((ItemLike)this.itemShow.get(index))
                     );
                  }
               }
            }
         }

         public void tick(short nTick) {
            if (nTick == 20) {
               if (++this.indexList >= this.numeroVarianti) {
                  this.indexList = 0;
               }

               this.itemShow.clear();

               try {
                  this.itemShow = new ArrayList<>(this.item.get(this.indexList));
               } catch (Exception e) {
                  this.itemShow = new ArrayList<>();
               }
            }
         }

         public void init(int CentroX, int CentroY) {
            this.posX = CentroX + this.pX;
            this.posY = CentroY + this.pY;
         }

         public static Map<Integer, List<Item>> getAllCraftingGridsForItem(Item resultItem) {
            Minecraft mc = Minecraft.getInstance();
            Level level = mc.level;
            RecipeManager recipeManager = level.getRecipeManager();
            Map<Integer, List<Item>> resultMap = new HashMap<>();
            int recipeIndex = 0;

            for (RecipeHolder<?> holder : recipeManager.getAllRecipesFor(RecipeType.CRAFTING)) {
               Recipe<?> recipe = holder.value();
               if (recipe instanceof CraftingRecipe craftingRecipe) {
                  Item result = craftingRecipe.getResultItem(level.registryAccess()).getItem();
                  if (result == resultItem) {
                     NonNullList<Ingredient> ingredients = craftingRecipe.getIngredients();
                     if (!ingredients.isEmpty()) {
                        int width = 3;
                        int height = 3;
                        if (recipe instanceof ShapedRecipe shaped) {
                           width = shaped.getWidth();
                           height = shaped.getHeight();
                        }

                        boolean hasTagVariant = false;

                        for (Ingredient tagIngredient : ingredients) {
                           if (tagIngredient.getItems().length > 1) {
                              hasTagVariant = true;

                              for (ItemStack variantStack : tagIngredient.getItems()) {
                                 List<Item> grid = new ArrayList<>(Collections.nCopies(9, Items.AIR));
                                 int ingredientIndex = 0;

                                 for (int y = 0; y < height; y++) {
                                    for (int x = 0; x < width; x++) {
                                       int slot = y * 3 + x;
                                       if (ingredientIndex < ingredients.size()) {
                                          Ingredient curr = (Ingredient)ingredients.get(ingredientIndex++);
                                          Item itemToUse;
                                          if (areIngredientsEqual(curr, tagIngredient)) {
                                             itemToUse = variantStack.getItem();
                                          } else if (curr.getItems().length > 0) {
                                             itemToUse = curr.getItems()[0].getItem();
                                          } else {
                                             itemToUse = Items.AIR;
                                          }

                                          grid.set(slot, itemToUse);
                                       }
                                    }
                                 }

                                 resultMap.put(recipeIndex++, grid);
                              }
                              break;
                           }
                        }

                        if (!hasTagVariant) {
                           List<Item> grid = new ArrayList<>(Collections.nCopies(9, Items.AIR));
                           int ingredientIndex = 0;

                           for (int y = 0; y < height; y++) {
                              for (int x = 0; x < width; x++) {
                                 int slot = y * 3 + x;
                                 if (ingredientIndex < ingredients.size()) {
                                    Ingredient ing = (Ingredient)ingredients.get(ingredientIndex++);
                                    if (ing.getItems().length > 0) {
                                       grid.set(slot, ing.getItems()[0].getItem());
                                    }
                                 }
                              }
                           }

                           resultMap.put(recipeIndex++, grid);
                        }
                     }
                  }
               }
            }

            return resultMap;
         }

         private static boolean areIngredientsEqual(Ingredient a, Ingredient b) {
            ItemStack[] aItems = a.getItems();
            ItemStack[] bItems = b.getItems();
            if (aItems.length != bItems.length) {
               return false;
            }

            for (int i = 0; i < aItems.length; i++) {
               if (!ItemStack.isSameItemSameComponents(aItems[i], bItems[i])) {
                  return false;
               }
            }

            return true;
         }
      }
   }
}
