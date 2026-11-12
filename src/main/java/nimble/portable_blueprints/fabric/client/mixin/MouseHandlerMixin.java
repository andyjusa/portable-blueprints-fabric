package nimble.portable_blueprints.fabric.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import nimble.portable_blueprints.Events.ModEventClientForge;
import nimble.portable_blueprints.fabric.client.MouseScrollingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void portableBlueprints$scroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (window != client.getWindow().getWindow() || client.screen != null || client.player == null) return;
        MouseScrollingEvent event = new MouseScrollingEvent(vertical);
        ModEventClientForge.ScrollingMouseEvent(event);
        if (event.isCanceled()) ci.cancel();
    }
}
