package nimble.portable_blueprints.client.screen.tablet.utils;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import nimble.portable_blueprints.init.ModTabletTags;
import nimble.portable_blueprints.robautile.AdvancedEditBox;
import nimble.portable_blueprints.robautile.RenderingMethods;

public class SearchBarElement extends AbstractWidget {
   private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("portable_blueprints", "textures/gui/tablet/tablet_gui.png");
   final AdvancedEditBox editBox;
   private String valueBefore = "";
   final int sizeX = 110;
   final int sizeY = 14;
   final RenderingMethods.GuiGraphicsElement SearchBarBG;
   private final List<SearchBarElement.ItemTag> ItemTags = new ArrayList<>();
   public boolean modeSearch = true;
   private int beforeSize = 0;
   int XListTag = 0;
   int MaxXListTag = 0;

   public SearchBarElement(int px, int py, String valoreIniziale, List<ModTabletTags.TagData> itemTagsAttivi, boolean modeSearch) {
      super(px, py, 110, 14, Component.literal(""));
      this.editBox = AdvancedEditBox.builder(Component.literal("")).pos(px + 15, py).size(110, 14).setWithBorder(false).setvisibleText(false).build();
      this.editBox.setValue(valoreIniziale);
      this.SearchBarBG = new RenderingMethods.GuiGraphicsElement(new RenderingMethods.GuiGraphicsElement.BLIT_Parametri(px, py, 0, 239, 134, 17, TEXTURE));
      int offsetX = 0;
      this.beforeSize = itemTagsAttivi.size();

      for (ModTabletTags.TagData tagData : ModTabletTags.TagDatas) {
         SearchBarElement.ItemTag it = new SearchBarElement.ItemTag(tagData, px + offsetX + 15, py + 8);

         for (ModTabletTags.TagData tdc : itemTagsAttivi) {
            if (tdc.getOriginalTag().equals(tagData.getOriginalTag())) {
               it.isSelected = true;
               break;
            }
         }

         this.ItemTags.add(it);
         offsetX += it.getTextWidth() + 5;
      }

      this.MaxXListTag = offsetX - this.width - 10;
      if (this.MaxXListTag < 0) {
         this.MaxXListTag = 0;
      }

      this.modeSearch = modeSearch;
   }

   public AdvancedEditBox getEditBox() {
      return this.editBox;
   }

   public boolean isValueChanged() {
      boolean v = !this.valueBefore.equals(this.editBox.getValue());
      this.valueBefore = this.editBox.getValue();
      int size = this.getSelectedTags().size();
      if (!v) {
         v = size != this.beforeSize;
         if (v) {
            this.beforeSize = size;
         }
      }

      return v;
   }

   public String getValue() {
      return this.valueBefore;
   }

   public List<ModTabletTags.TagData> getSelectedTags() {
      List<ModTabletTags.TagData> selected = new ArrayList<>();

      for (SearchBarElement.ItemTag it : this.ItemTags) {
         if (it.isSelected) {
            selected.add(it.tagData);
         }
      }

      return selected;
   }

   public void renderWidget(GuiGraphics gg, int mouseX, int mouseY, float delta) {
      this.SearchBarBG.render(gg);
      int _px = this.getX() + 15;
      int _py = this.getY() + 1;
      if (this.modeSearch) {
         if (this.editBox.isMouseOver(mouseX, mouseY) || this.editBox.isFocused()) {
            gg.fill(_px, _py + 12, _px + 110, _py + 12 + 2, -7631989);
         }

         String testo = this.editBox.getValue();
         int cursorPos = this.editBox.getCursorPosition();
         String v = !this.editBox.isFocused() ? testo : testo.substring(0, cursorPos) + "|" + testo.substring(cursorPos);
         RenderingMethods.GUITextAutoResize(gg, v, Minecraft.getInstance().font, 4473924, _px, _py, 110, 15, false);
      } else {
         RenderingMethods.enableScissorFromGui(Minecraft.getInstance(), this.getX() + 15, this.getY(), this.width + 9, this.height);

         for (SearchBarElement.ItemTag it : this.ItemTags) {
            it.render(gg);
         }

         RenderingMethods.disableScissor();
      }

      gg.blit(TEXTURE, this.getX() + 1, this.getY() + 3, this.modeSearch ? 0 : 11, 228, 11, 11);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.modeSearch) {
         if (button == 0 && this.editBox.isMouseOver(mouseX, mouseY)) {
            this.editBox.setFocused(true);
         } else if (button == 0 && !this.editBox.isMouseOver(mouseX, mouseY)) {
            this.editBox.setFocused(false);
         }
      }

      if (mouseX >= this.getX() + 1 && mouseX < this.getX() + 12 && mouseY >= this.getY() + 3 && mouseY < this.getY() + 14 && button == 0) {
         this.modeSearch = !this.modeSearch;
         this.editBox.active = this.modeSearch;
         this.editBox.setFocused(false);
      }

      if (!this.modeSearch && this.getArea((int)mouseX, (int)mouseY)) {
         for (SearchBarElement.ItemTag it : this.ItemTags) {
            if (it.select((int)mouseX, (int)mouseY)) {
               break;
            }
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   private boolean getArea(int mouseX, int mouseY) {
      return mouseX >= this.getX() + 15 && mouseX < this.getX() + this.width + 24 && mouseY >= this.getY() && mouseY < this.getY() + this.height;
   }

   protected void updateWidgetNarration(NarrationElementOutput p_259858_) {
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      return this.modeSearch && this.editBox.keyPressed(keyCode, scanCode, modifiers) ? true : super.keyPressed(keyCode, scanCode, modifiers);
   }

   public boolean charTyped(char codePoint, int modifiers) {
      return this.modeSearch && this.editBox.charTyped(codePoint, modifiers) ? true : super.charTyped(codePoint, modifiers);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double delta, double delta2) {
      if (this.getArea((int)mouseX, (int)mouseY)) {
         this.XListTag += delta > 0.0 ? 10 : -10;
         if (this.XListTag > this.MaxXListTag) {
            this.XListTag = this.MaxXListTag;
         }

         if (this.XListTag < 0) {
            this.XListTag = 0;
         }
      }

      return super.mouseScrolled(mouseX, mouseY, delta2, delta);
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
         RenderingMethods.GuiGraphicsTestoScalato(
            gg,
            this.tagData.getCurrentLenguageTag(),
            1.0F,
            Minecraft.getInstance().font,
            new Vec2(this.px - SearchBarElement.this.XListTag, this.py),
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
         int _px = this.px - SearchBarElement.this.XListTag;
         int _py = this.py - 4;
         if (mouseX >= _px && mouseX < _px + this.getTextWidth() - 1 && mouseY >= _py && mouseY < _py + 7) {
            this.isSelected = !this.isSelected;
            return true;
         } else {
            return false;
         }
      }
   }
}
