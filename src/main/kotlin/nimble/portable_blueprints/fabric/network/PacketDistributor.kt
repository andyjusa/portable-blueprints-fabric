package nimble.portable_blueprints.fabric.network

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import nimble.portable_blueprints.network.CtoS_CreateBlueprint
import nimble.portable_blueprints.network.global_blueprint_net.CtoS_SaveGlobalBlueprintInServer

/** Fabric transport with static Java entrypoints for the original payload call sites. */
object PacketDistributor {
    @Volatile private var server: MinecraftServer? = null
    private var initialized = false

    @JvmStatic
    @Synchronized
    fun initialize() {
        if (initialized) return
        initialized = true
        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            val id = handler.player.uuid
            PayloadValidation.clear(id)
            CtoS_CreateBlueprint.resetPlayer(id)
            CtoS_SaveGlobalBlueprintInServer.resetPlayer(id)
        }
        ServerLifecycleEvents.SERVER_STARTED.register { server = it }
        ServerLifecycleEvents.SERVER_STOPPED.register { if (server === it) server = null }
    }

    @JvmStatic
    @Environment(EnvType.CLIENT)
    fun sendToServer(payload: CustomPacketPayload, vararg additional: CustomPacketPayload) {
        ClientSender.send(payload, additional)
    }

    @JvmStatic
    fun sendToPlayer(player: ServerPlayer?, payload: CustomPacketPayload, vararg additional: CustomPacketPayload) {
        if (player == null || player.connection == null) return
        val active = player.server
        active.execute {
            if (player.hasDisconnected() || player.connection == null) return@execute
            if (ServerPlayNetworking.canSend(player, payload.type())) ServerPlayNetworking.send(player, payload)
            for (extra in additional) {
                if (ServerPlayNetworking.canSend(player, extra.type())) ServerPlayNetworking.send(player, extra)
            }
        }
    }

    @JvmStatic
    fun sendToAllPlayers(payload: CustomPacketPayload, vararg additional: CustomPacketPayload) {
        server?.let { sendToAllPlayers(it, payload, *additional) }
    }

    @JvmStatic
    fun sendToAllPlayers(active: MinecraftServer, payload: CustomPacketPayload, vararg additional: CustomPacketPayload) {
        active.execute {
            for (player in active.playerList.players) sendToPlayer(player, payload, *additional)
        }
    }

    /** This class is linked only by the client-specific send entrypoint. */
    @Environment(EnvType.CLIENT)
    private object ClientSender {
        fun send(payload: CustomPacketPayload, additional: Array<out CustomPacketPayload>) {
            val client = net.minecraft.client.Minecraft.getInstance()
            client.execute {
                if (client.connection == null) return@execute
                net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(payload)
                for (extra in additional) net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(extra)
            }
        }
    }
}
