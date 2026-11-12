package nimble.portable_blueprints.fabric.client.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import nimble.portable_blueprints.client.BlueprintsPreviewMethods;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void portableBlueprints$hideHud(GuiGraphics graphics, DeltaTracker delta, CallbackInfo ci) {
        if (BlueprintsPreviewMethods.hideAllHud) ci.cancel();
    }
}
