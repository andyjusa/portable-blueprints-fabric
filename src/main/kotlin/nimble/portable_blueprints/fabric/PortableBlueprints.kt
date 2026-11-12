package nimble.portable_blueprints.fabric

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import nimble.portable_blueprints.Events.BlueprintWorldInjection
import nimble.portable_blueprints.Events.ServerEvents
import nimble.portable_blueprints.altro.Blueprint
import nimble.portable_blueprints.altro.BlockBlueprintUsage
import nimble.portable_blueprints.altro.CustomBlockProprietyHandle
import nimble.portable_blueprints.altro.PlacingBlockManager
import nimble.portable_blueprints.commands.*
import nimble.portable_blueprints.init.*
import nimble.portable_blueprints.mod_compatibility.ModCompatibilityInit
import nimble.portable_blueprints.network.PacketHandler
import nimble.portable_blueprints.portable_blueprints.ModServerConfig

object PortableBlueprints : ModInitializer {
    override fun onInitialize() {
        ModItems.register()
        ModTabs.register()
        ModEntities.register()
        ModGameRules.register()
        ModServerConfig.load()
        Blueprint.setup()
        PacketHandler.register()
        BlueprintWorldInjection.creaDirectoryConFileSetting()
        ModCompatibilityInit.init()
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            BlueprintGeneralCommand.onCommandRegister(dispatcher)
            BlueprintServerCommand.onCommandRegister(dispatcher)
            BlueprintWornCommand.onCommandRegister(dispatcher)
        }
        ServerLifecycleEvents.SERVER_STARTING.register(PlacingBlockManager::onServerStarted)
        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            ServerEvents.onServerStarted(server)
            BlockBlueprintUsage.load(server)
            CustomBlockProprietyHandle.load(server)
            ModTabletTags.ServerSide.serverStart(server)
            BlueprintWorldInjection.onServerStarted(server)
        }
        ServerLifecycleEvents.BEFORE_SAVE.register { server, _, _ -> ModTabletTags.ServerSide.onServerSave(server) }
        ServerLifecycleEvents.SERVER_STOPPED.register { server ->
            PlacingBlockManager.onServerStopped(server)
            Blueprint.resetQueues()
        }
        ServerTickEvents.END_SERVER_TICK.register(Blueprint::onServerTick)
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ -> ServerEvents.onPlayerLogout(handler.player) }
    }
}
