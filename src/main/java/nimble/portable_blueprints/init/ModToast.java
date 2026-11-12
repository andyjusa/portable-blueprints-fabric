package nimble.portable_blueprints.init;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.components.toasts.Toast.Visibility;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ModToast implements Toast {
   private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath("portable_blueprints", "textures/gui/toasts.png");
   private static final ResourceLocation TEXTURE2 = ResourceLocation.fromNamespaceAndPath("portable_blueprints", "textures/gui/tablet/tablet_blueprint_gui.png");
   private final Component title;
   private final Component subtitle;
   private final ItemStack icon;
   private final int title_color;
   private final int subtitle_color;

   public ModToast(Component title, Component subtitle, ItemStack icon, int titleColor, int subtitleColor) {
      this.title = title;
      this.subtitle = subtitle;
      this.icon = icon;
      this.title_color = titleColor;
      this.subtitle_color = subtitleColor;
   }

   public Visibility render(GuiGraphics graphics, ToastComponent toastComponent, long timeSinceLastUpdate) {
      graphics.blit(BACKGROUND_TEXTURE, 0, 0, 0, 32, this.width(), this.height());
      if (this.icon.getItem().equals(ModItems.GLOBAL_BLUEPRINT.get())) {
         graphics.blit(TEXTURE2, 6, 6, 225, 240, 16, 16);
      } else {
         graphics.renderFakeItem(this.icon, 6, 6);
      }

      graphics.drawString(toastComponent.getMinecraft().font, this.title, 30, 7, this.title_color, false);
      graphics.drawString(toastComponent.getMinecraft().font, this.subtitle, 30, 18, this.subtitle_color, false);
      return timeSinceLastUpdate >= 5000L ? Visibility.HIDE : Visibility.SHOW;
   }

   public static void showToast(Component title, Component subtitle, ItemStack icon, int title_color, int subtitle_color) {
      Minecraft.getInstance().getToasts().addToast(new ModToast(title, subtitle, icon, title_color, subtitle_color));
   }
}
