package nimble.portable_blueprints.test

import com.google.gson.Gson
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.gametest.framework.GameTest
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.GameType
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.ChestBlockEntity
import nimble.portable_blueprints.altro.Blueprint
import nimble.portable_blueprints.fabric.inventory.ItemHandlers
import nimble.portable_blueprints.robautile.Operazioni
import java.io.File
import java.util.UUID
import com.mojang.authlib.GameProfile
import io.netty.channel.embedded.EmbeddedChannel
import net.minecraft.network.Connection
import net.minecraft.network.protocol.PacketFlow
import net.minecraft.server.level.ClientInformation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.CommonListenerCookie

class BlueprintGameTests : FabricGameTest {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    fun linkedChestConsumesExactlyOne(helper: GameTestHelper) {
        val player = survivalPlayer(helper)
        try {
            player.setGameMode(GameType.SURVIVAL)
            player.inventory.clearContent()
            val relative = BlockPos(2, 1, 2)
            helper.setBlock(relative, Blocks.CHEST)
            val chest = helper.getBlockEntity<ChestBlockEntity>(relative)
            chest.setItem(0, ItemStack(Items.STONE, 3))
            chest.setItem(1, ItemStack(Items.STONE, 4))
            val absolute = helper.absolutePos(relative)
            val handler = requireNotNull(ItemHandlers.find(helper.level, absolute, Direction.DOWN))
            handler.extractItem(0, 2, true)
            helper.assertTrue(chest.getItem(0).count == 3, "Simulated Fabric extraction must roll back")
            val result = Blueprint.removeItemFromTheContainers(player, helper.level, listOf(absolute), Items.STONE)
            helper.assertTrue(result.a, "Linked chest materials must be usable")
            helper.assertTrue(chest.getItem(0).count + chest.getItem(1).count == 6,
                "Building one block must consume exactly one material even when several slots match")
            helper.succeed()
        } finally {
            player.discard()
            player.server.playerList.remove(player)
        }
    }

    // GameTestServer accelerates ticks; the original placement animation waits one wall-clock second.
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 20000)
    fun scanSaveAndBuildInSurvival(helper: GameTestHelper) {
        val player = survivalPlayer(helper)
        player.setGameMode(GameType.SURVIVAL)
        player.inventory.clearContent()
        player.inventory.add(ItemStack(Items.STONE, 1))
        val source = helper.absolutePos(BlockPos(1, 1, 1))
        val destinationRelative = BlockPos(5, 1, 5)
        val destination = helper.absolutePos(destinationRelative)
        helper.setBlock(BlockPos(1, 1, 1), Blocks.STONE)
        val name = "scan-roundtrip-${player.uuid}"
        Blueprint.CreateBlueprint(helper.level, source, source, name, player.uuid, false, listOf("minecraft:air"))
        val file = File(Blueprint.getFileForBlueprints(helper.level, player.uuid), "$name.bp")
        helper.runAfterDelay(10) {
            helper.assertTrue(file.isFile, "Scanner queue must save a .bp file")
            val data = file.reader().use { nimble.portable_blueprints.fabric.BlueprintJson.create().fromJson(it, Blueprint.BlueprintDatas::class.java) }
            helper.assertTrue(data.getAllBlocksCount() == 1, "Saved scan must contain the source block")
            data.offset = destination
            Blueprint.AggiungiStructureAllaCoda(helper.level, data, player, emptyList(), false, (-1).toShort(), false)
        }
        helper.succeedWhen {
            helper.assertBlockPresent(Blocks.STONE, destinationRelative)
            helper.assertTrue(player.inventory.countItem(Items.STONE) == 0, "Survival build must consume the player's material")
            file.delete()
            player.discard()
            player.server.playerList.remove(player)
        }
    }

    private fun survivalPlayer(helper: GameTestHelper): ServerPlayer {
        // Vanilla's GameTest mock overrides isCreative() to always return true,
        // even after setGameMode(SURVIVAL). Use a real ServerPlayer with the same embedded transport.
        val profile = GameProfile(UUID.randomUUID(), "survival-test")
        val server = helper.level.server
        val player = ServerPlayer(server, helper.level, profile, ClientInformation.createDefault())
        val connection = Connection(PacketFlow.SERVERBOUND)
        EmbeddedChannel(connection)
        server.playerList.placeNewPlayer(connection, player, CommonListenerCookie.createInitial(profile, false))
        player.setGameMode(GameType.SURVIVAL)
        return player
    }
}
