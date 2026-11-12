package nimble.portable_blueprints.fabric.client.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import nimble.portable_blueprints.client.BlueprintsPreviewMethods;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Inject(method = "renderWithTooltip", at = @At("HEAD"), cancellable = true)
    private void portableBlueprints$hideScreen(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (BlueprintsPreviewMethods.hideAllHud) ci.cancel();
    }
}
