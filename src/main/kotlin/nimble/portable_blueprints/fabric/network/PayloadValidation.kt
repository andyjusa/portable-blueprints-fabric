package nimble.portable_blueprints.fabric.network

import java.util.UUID
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import nimble.portable_blueprints.altro.Blueprint
import nimble.portable_blueprints.altro.ExternalBlueprintContainerMetodi
import nimble.portable_blueprints.network.*
import nimble.portable_blueprints.network.SharedBlueprints.*
import nimble.portable_blueprints.network.blueprint_tags.*
import nimble.portable_blueprints.network.contenuto_container_selezionati.CtoS_ContenutoContainerSelezionati
import nimble.portable_blueprints.network.request_ids_blueprints.CtoS_RequestIDsBlueprints
import nimble.portable_blueprints.network.rimuovi_blueprint.CtoS_BlueprintRemove
import nimble.portable_blueprints.network.save_global_blueprint.CtoS_SaveGlobalBlueprint
import nimble.portable_blueprints.network.update_item_blueprint_nbt.CtoS_UpdateItemBlueprintNBT
import nimble.portable_blueprints.network.update_item_scanner_nbt.CtoS_UpdateItemScannerNBT
import nimble.portable_blueprints.network.visualize_blueprint_data.CtoS_VisualizeBlueprintData
import nimble.portable_blueprints.portable_blueprints.OldVersionUtilities

/** Connection-authoritative checks run on the server thread before world access. */
object PayloadValidation {
    private val obstructions = mutableMapOf<UUID, MutableMap<BlockPos, Long>>()

    @JvmStatic
    fun validName(name: String?): Boolean = name != null && name.isNotBlank() && name.length <= 128 &&
        name != "." && name != ".." && name.none { it.code < 32 || it in "/\\:<>" }

    @JvmStatic
    fun isItem(stack: ItemStack, vararg names: String): Boolean {
        if (stack.isEmpty) return false
        val id = BuiltInRegistries.ITEM.getKey(stack.item)
        return id.namespace == "portable_blueprints" && id.path in names
    }

    @JvmStatic
    fun isBlueprint(stack: ItemStack): Boolean =
        isItem(stack, "blueprint", "server_blueprint", "global_blueprint", "worn_blueprint")

    @JvmStatic
    fun sameHeldItem(player: ServerPlayer, supplied: ItemStack): Boolean {
        val held = player.mainHandItem
        return !held.isEmpty && ItemStack.isSameItem(held, supplied) && sameIdentity(held, supplied)
    }

    private fun sameIdentity(held: ItemStack, supplied: ItemStack): Boolean {
        val expected = OldVersionUtilities.getItemStackNBT(held)
        val received = OldVersionUtilities.getItemStackNBT(supplied)
        // Preview state is client-local; only immutable blueprint identity is compared.
        return listOf("owner", "nome", "blueprint_name").all { expected.getString(it) == received.getString(it) }
    }

    @JvmStatic
    fun canRead(player: ServerPlayer, owner: UUID?, name: String): Boolean {
        if (!validName(name)) return false
        if (owner == null || owner == player.uuid) return true
        return Blueprint.getSharedBlueprints(player.serverLevel(), player.uuid).ContieneSWMBlueprint(owner, name)
    }

    @JvmStatic
    fun validPosition(player: ServerPlayer, pos: BlockPos): Boolean =
        !player.serverLevel().isOutsideBuildHeight(pos) &&
            player.serverLevel().worldBorder.isWithinBounds(pos) && player.serverLevel().hasChunkAt(pos)

    @JvmStatic
    fun rememberObstruction(player: ServerPlayer, pos: BlockPos) {
        val blocks = obstructions.getOrPut(player.uuid) { mutableMapOf() }
        val now = System.currentTimeMillis()
        blocks.values.removeIf { it < now }
        if (blocks.size < 65536) blocks[pos.immutable()] = now + 300000L
    }

    @JvmStatic
    fun consumeObstruction(player: ServerPlayer, pos: BlockPos): Boolean {
        val expiry = obstructions[player.uuid]?.remove(pos) ?: return false
        return expiry >= System.currentTimeMillis()
    }

    @JvmStatic
    fun clear(player: UUID) {
        obstructions.remove(player)
    }

    @JvmStatic
    fun permits(payload: CustomPacketPayload, player: ServerPlayer): Boolean {
        if (player.hasDisconnected()) return false
        val held = player.mainHandItem
        return when (payload) {
            is CtoS_UpdateItemScannerNBT -> isItem(held, "scanner") && sameHeldItem(player, payload.item()) &&
                payload.valore_stringa().length <= 128
            is CtoS_UpdateItemBlueprintNBT -> isBlueprint(held) && sameHeldItem(player, payload.item()) &&
                payload.valore_double().isFinite()
            is CtoS_CreateBlueprint -> isItem(held, "scanner") && payload.uuidRichiesta() != null &&
                (payload.nome().isEmpty() || validName(payload.nome()))
            is CtoS_CreateStructureByBlueprint -> isBlueprint(held) && sameHeldItem(player, payload.blueprint_item()) &&
                (payload.isServerBlueprint() == isItem(held, "server_blueprint")) &&
                (payload.isWornBlueprint() == isItem(held, "worn_blueprint")) && validPosition(player, payload.origin())
            is CtoS_CreaItemBlueprint -> isItem(held, "tablet") && validName(payload.nome()) &&
                (payload.isServerBlueprint() || canRead(player, payload.BPOwner(), payload.nome()))
            is CtoS_AddShareWithOthers -> player.uuid == payload.creatore_richiesta() &&
                payload.ricevente() != null && validName(payload.nome())
            is CtoS_RemoveShareWithOthers -> payload.creatore_richiesta() != null && payload.ricevente() != null &&
                (player.uuid == payload.creatore_richiesta() ||
                    (payload.creatore_richiesta() != payload.ricevente() && player.uuid == payload.ricevente())) &&
                validName(payload.nome())
            is CtoS_SetBlueprintTag -> validName(payload.BPName()) && payload.TagID().length <= 128 &&
                (if (payload.OwnerBP() == null) player.hasPermissions(3) else player.uuid == payload.OwnerBP())
            is CtoS_GetBlueprintTags -> canRead(player, payload.OwnerBP(), payload.BPName())
            is CtoS_RequestIDsBlueprints -> payload.tipo() == CtoS_RequestIDsBlueprints.Tipo.SERVER ||
                canRead(player, payload.owner(), payload.BPnome())
            is CtoS_VisualizeBlueprintData -> validName(payload.nomeBlueprintRichiesto()) &&
                (payload.isServerBlueprint() || payload.isWornBlueprint() ||
                    canRead(player, payload.owner_blueprint(), payload.nomeBlueprintRichiesto()))
            is CtoS_BlueprintRemove -> validName(payload.nome())
            is CtoS_SaveGlobalBlueprint -> {
                val names = payload.nome().split(';')
                names.size <= 2 && names.all(::validName)
            }
            is CtoS_RemoveItem -> isBlueprint(payload.item_da_rimuovere())
            is CtoS_RemoveItemIfBlueprintNotExist -> isBlueprint(payload.item()) && validName(payload.nome())
            is CtoS_ContenutoContainerSelezionati -> isBlueprint(held) && payload.posizioni().size <= 1024 &&
                payload.posizioni().all { validPosition(player, it) } &&
                ExternalBlueprintContainerMetodi.getContainersBlockPosFromBlueprint(held).containsAll(payload.posizioni())
            is CtoS_UpdateServerConfigurationValue -> player.hasPermissions(3)
            else -> true
        }
    }
}
