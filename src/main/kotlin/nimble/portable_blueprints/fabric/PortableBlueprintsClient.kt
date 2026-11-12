package nimble.portable_blueprints.fabric

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.Minecraft
import nimble.portable_blueprints.Events.BlueprintEvent
import nimble.portable_blueprints.Events.ChangeShapeScanningAreaEvent
import nimble.portable_blueprints.Events.ModBusClientEvents
import nimble.portable_blueprints.Events.ModClientEvents
import nimble.portable_blueprints.Events.ModEventClientForge
import nimble.portable_blueprints.Events.RenderBlueprintsPreview
import nimble.portable_blueprints.Events.RenderScannerAnimation
import nimble.portable_blueprints.altro.ProgressBarBlueprintPlayer
import nimble.portable_blueprints.client.BlueprintsPreviewMethods
import nimble.portable_blueprints.client.screen.tablet.TabletBlueprintInfoScreen
import nimble.portable_blueprints.fabric.client.RenderLevelStageEvent
import nimble.portable_blueprints.fabric.client.RenderLevelStageEvent.Stage
import nimble.portable_blueprints.init.ModKeyMappings
import nimble.portable_blueprints.init.ModTabletTags
import nimble.portable_blueprints.init.TabletClientDatas
import nimble.portable_blueprints.network.PacketHandler
import nimble.portable_blueprints.portable_blueprints.ModConfig

/** Client registrations stay behind Fabric's client-only entrypoint. */
class PortableBlueprintsClient : ClientModInitializer {
    override fun onInitializeClient() {
        ModConfig.load()
        PacketHandler.registerClient()
        ModKeyMappings.registerKeyMappings()
        ModClientEvents.registerRenderers()
        ModBusClientEvents.registerLayer()
        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            TabletClientDatas.onClientJoinWorld()
            ModTabletTags.ClientSide.onClientLogin()
        }
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            ModEventClientForge.playerEsceDalMondo()
        }
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            ModKeyMappings.KeyEventListener.onClientTick()
            BlueprintEvent.clientTick(client)
            ProgressBarBlueprintPlayer.ClientTick()
            ChangeShapeScanningAreaEvent.clientTick()
            ModEventClientForge.playerEntraNelMondo()
        }
        HudRenderCallback.EVENT.register { graphics, _ ->
            if (!BlueprintsPreviewMethods.hideAllHud) ProgressBarBlueprintPlayer.RenderOverlay(graphics)
        }
        WorldRenderEvents.BEFORE_ENTITIES.register { context ->
            render(context, Stage.AFTER_SOLID_BLOCKS)
            render(context, Stage.AFTER_CUTOUT_BLOCKS)
        }
        WorldRenderEvents.AFTER_TRANSLUCENT.register { render(it, Stage.AFTER_TRANSLUCENT_BLOCKS) }
        WorldRenderEvents.LAST.register { render(it, Stage.AFTER_PARTICLES) }
        WorldRenderEvents.END.register { BlueprintsPreviewMethods.capturePendingIcon() }
    }

    private fun render(context: WorldRenderContext, stage: Stage) {
        val client = Minecraft.getInstance()
        if (client.player == null || client.level == null) return
        val event = RenderLevelStageEvent(stage, context)
        ModEventClientForge.renderLevelStageEvent(event)
        RenderBlueprintsPreview.onRenderLevel(event)
        RenderScannerAnimation.onRenderLevel(event)
        TabletBlueprintInfoScreen.onRenderLevel(event)
        BlueprintsPreviewMethods.renderStage(event)
    }
}
