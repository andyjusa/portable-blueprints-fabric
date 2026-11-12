package nimble.portable_blueprints.test

import com.google.gson.Gson
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.Screenshot
import net.minecraft.client.gui.screens.TitleScreen
import net.minecraft.core.registries.Registries
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Blocks
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.Difficulty
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.GameType
import net.minecraft.world.level.LevelSettings
import net.minecraft.world.level.WorldDataConfiguration
import net.minecraft.world.level.levelgen.WorldOptions
import net.minecraft.world.level.levelgen.presets.WorldPresets
import nimble.portable_blueprints.Events.RenderBlueprintsPreview
import nimble.portable_blueprints.altro.Blueprint
import nimble.portable_blueprints.client.BlueprintsPreviewMethods
import nimble.portable_blueprints.client.VisualizeBlueprintDatas
import nimble.portable_blueprints.client.screen.ScannerScreen
import nimble.portable_blueprints.client.screen.tablet.TabletScreen
import nimble.portable_blueprints.init.ModItems
import nimble.portable_blueprints.portable_blueprints.OldVersionUtilities
import nimble.portable_blueprints.robautile.Operazioni
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/** Development-only smoke driver. This source set is excluded from the distributable mod. */
class ClientSmoke : ClientModInitializer {
    private enum class Stage { TITLE, WORLD, SCANNER_WORLD, SCANNER_UI, TABLET_UI, BLUEPRINT_WORLD, FINISHED }
    private var stage = Stage.TITLE
    private var stableTicks = 0
    private var initialized = false
    private var lastScreen = ""
    private lateinit var output: Path
    private lateinit var scanner: ItemStack
    private val finished = AtomicBoolean(false)
    private val logger = LoggerFactory.getLogger("portable-blueprints-client-smoke")

    override fun onInitializeClient() {
        if (!java.lang.Boolean.getBoolean("portable_blueprints.smoke")) return
        output = Path.of(System.getProperty("portable_blueprints.smoke.dir", "build/smoke-results")).toAbsolutePath()
        Files.createDirectories(output)
        record("RUNNING")
        val watchdog = Executors.newSingleThreadScheduledExecutor { task ->
            Thread(task, "portable-blueprints-smoke-watchdog").apply { isDaemon = true }
        }
        watchdog.schedule({ finish(false, "Timed out after 300 seconds") }, 300, TimeUnit.SECONDS)
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (!finished.get()) {
                try {
                    tick(client)
                } catch (failure: Throwable) {
                    logger.error("Client smoke failed at {}", stage, failure)
                    finish(false, "${failure.javaClass.name}: ${failure.message}")
                }
            }
        }
        logger.info("Client smoke enabled; artifacts: {}", output)
    }

    private fun tick(client: Minecraft) {
        if (!initialized) {
            val runDirectory = client.gameDirectory.toPath().toRealPath()
            require(runDirectory.fileName.toString() == "client-smoke" && runDirectory.parent.fileName.toString() == "build") {
                "Refusing to create a smoke world outside build/client-smoke: $runDirectory"
            }
            client.options.pauseOnLostFocus = false
            client.options.renderDistance().set(4)
            client.options.simulationDistance().set(6)
            client.options.framerateLimit().set(60)
            initialized = true
        }
        val currentScreen = client.screen?.javaClass?.simpleName ?: "world"
        if (lastScreen != currentScreen) {
            logger.info("Smoke stage={}, screen={}", stage, currentScreen)
            lastScreen = currentScreen
        }
        // Only the isolated development profile can reach this onboarding bypass.
        if (currentScreen == "AccessibilityOnboardingScreen" && client.overlay == null) {
            client.setScreen(TitleScreen())
            return
        }
        when (stage) {
            Stage.TITLE -> if (stable(client.screen is TitleScreen && client.overlay == null, 40)) {
                capture(client, "title.png")
                stage = Stage.WORLD
                record("CREATING_WORLD")
                val rules = GameRules()
                rules.getRule(GameRules.RULE_SPAWN_CHUNK_RADIUS).set(0, null)
                val name = "portable_blueprints_smoke_${System.currentTimeMillis()}"
                val settings = LevelSettings(name, GameType.CREATIVE, false, Difficulty.PEACEFUL, true, rules, WorldDataConfiguration.DEFAULT)
                client.createWorldOpenFlows().createFreshLevel(
                    name, settings, WorldOptions(510L, false, false),
                    { access -> access.registryOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT).createWorldDimensions() },
                    TitleScreen()
                )
            }
            Stage.WORLD -> if (stable(client.player != null && client.level != null && client.screen == null && client.overlay == null, 60)) {
                capture(client, "world.png")
                equipScanner(client)
                stage = Stage.SCANNER_WORLD
                record("WORLD_LOADED")
            }
            Stage.SCANNER_WORLD -> if (stable(client.screen == null && client.player?.mainHandItem?.`is`(ModItems.SCANNER.get()) == true, 40)) {
                capture(client, "world-scanner.png")
                client.setScreen(ScannerScreen(scanner.copy(), 27))
                stage = Stage.SCANNER_UI
            }
            Stage.SCANNER_UI -> if (stable(client.screen is ScannerScreen && client.overlay == null, 40)) {
                capture(client, "scanner.png")
                TabletScreen.OpenGUI(false)
                stage = Stage.TABLET_UI
            }
            Stage.TABLET_UI -> if (stable(client.screen is TabletScreen && client.overlay == null, 40)) {
                capture(client, "tablet.png")
                client.screen?.onClose()
                equipBlueprint(client)
                stage = Stage.BLUEPRINT_WORLD
                record("PREPARING_BLUEPRINT_PREVIEW")
            }
            Stage.BLUEPRINT_WORLD -> if (stable(client.screen == null && previewReady(client), 120)) {
                capture(client, "world-blueprint.png")
                stage = Stage.FINISHED
                finish(true, "Title, flat world, scanner selection, scanner UI, tablet UI and three-block solid/translucent VBO blueprint preview captured")
            }
            Stage.FINISHED -> Unit
        }
    }

    private fun stable(ready: Boolean, ticks: Int): Boolean {
        stableTicks = if (ready) stableTicks + 1 else 0
        if (stableTicks < ticks) return false
        stableTicks = 0
        return true
    }

    private fun equipScanner(client: Minecraft) {
        val player = requireNotNull(client.player)
        val start = player.blockPosition().offset(-1, 0, 4)
        val end = start.offset(2, 2, 2)
        scanner = ItemStack(ModItems.SCANNER.get())
        val tag = CompoundTag().apply {
            putBoolean("angolo1", true)
            putBoolean("angolo2", true)
            putString("coordinateAngolo1", Operazioni.getVec3iString(start))
            putString("coordinateAngolo2", Operazioni.getVec3iString(end))
        }
        OldVersionUtilities.impostaItemStackNBT(scanner, tag)
        player.inventory.selected = 0
        player.inventory.setItem(0, scanner.copy())
        player.yRot = 0f
        player.xRot = 15f
        val server = requireNotNull(client.singleplayerServer)
        val uuid = player.getUUID()
        val x = player.x
        val y = player.y
        val z = player.z
        val serverStack = scanner.copy()
        server.execute {
            val serverPlayer = requireNotNull(server.playerList.getPlayer(uuid))
            serverPlayer.inventory.selected = 0
            serverPlayer.inventory.setItem(0, serverStack)
            serverPlayer.teleportTo(serverPlayer.serverLevel(), x, y, z, 0f, 15f)
            serverPlayer.inventoryMenu.broadcastChanges()
        }
    }

    private fun equipBlueprint(client: Minecraft) {
        val player = requireNotNull(client.player)
        val owner = player.getUUID()
        val origin = player.blockPosition().offset(-1, 0, 4)
        val data = Blueprint.BlueprintDatas("fabric_smoke_preview").apply {
            altezza = 1
            lunghezzaX = 3
            lunghezzaZ = 1
            BlocksData[0] = hashMapOf(
                Operazioni.getVec3iString(BlockPos(0, 0, 0)) to Operazioni.getBlockStateString(Blocks.STONE.defaultBlockState()),
                Operazioni.getVec3iString(BlockPos(1, 0, 0)) to Operazioni.getBlockStateString(Blocks.OAK_STAIRS.defaultBlockState()),
                Operazioni.getVec3iString(BlockPos(2, 0, 0)) to Operazioni.getBlockStateString(Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState())
            )
        }
        val item = ItemStack(ModItems.BLUEPRINT.get())
        val tag = CompoundTag().apply {
            putString("nome", data.nome)
            putString("owner", owner.toString())
            putString("owner_name", "Smoke test")
            putInt("altezza", data.altezza)
            putInt("lunghezzaX", data.lunghezzaX)
            putInt("lunghezzaZ", data.lunghezzaZ)
            putBoolean("visualizeBuild", true)
            putString("originPos", Operazioni.getVec3iString(origin))
        }
        OldVersionUtilities.impostaItemStackNBT(item, tag)
        // Follow the normal item-equipping path; keep a real .bp so server validation succeeds.
        val server = requireNotNull(client.singleplayerServer)
        server.execute {
            try {
                val serverPlayer = requireNotNull(server.playerList.getPlayer(owner))
                val directory = Blueprint.getFileForBlueprints(serverPlayer.serverLevel(), owner).toPath()
                Files.writeString(directory.resolve("${data.nome}.bp"), Gson().toJson(data))
                serverPlayer.inventory.selected = 0
                serverPlayer.inventory.setItem(0, item.copy())
                serverPlayer.inventoryMenu.broadcastChanges()
                client.execute {
                    VisualizeBlueprintDatas.VisualizeBlueprint = data
                    VisualizeBlueprintDatas.VisualizeBlueprintOwner = owner
                    VisualizeBlueprintDatas.originPosVisualizeBlueprint = origin
                    RenderBlueprintsPreview.doCleanUp()
                    player.inventory.selected = 0
                    player.inventory.setItem(0, item.copy())
                    player.yRot = 0f
                    player.xRot = 15f
                }
            } catch (failure: Throwable) {
                logger.error("Smoke blueprint setup failed", failure)
                finish(false, "Blueprint setup: ${failure.message}")
            }
        }
    }

    private fun previewReady(client: Minecraft): Boolean {
        if (client.player?.mainHandItem?.`is`(ModItems.BLUEPRINT.get()) != true) return false
        // Readiness is deliberately inspected in this development source set, without production hooks.
        val dataField = RenderBlueprintsPreview::class.java.getDeclaredField("BPD").apply { isAccessible = true }
        val preview = dataField.get(null) as? BlueprintsPreviewMethods.BlueprintPreviewData ?: return false
        if (!preview.isDatiVertexPronti) return false
        val buffersField = preview.javaClass.getDeclaredField("vertexBuffers").apply { isAccessible = true }
        val buffers = buffersField.get(preview) as Map<*, *>
        return buffers.size >= 2 && buffers.values.all { it is List<*> && it.isNotEmpty() }
    }

    private fun capture(client: Minecraft, name: String) {
        check(client.isSameThread) { "Screenshots must run on the render/client thread" }
        Screenshot.takeScreenshot(client.mainRenderTarget).use { image ->
            check(image.width > 0 && image.height > 0) { "Empty framebuffer" }
            image.writeToFile(output.resolve(name))
        }
        logger.info("Captured {}", output.resolve(name))
    }

    private fun record(status: String) {
        Files.writeString(output.resolve("result.txt"), "$status\nstage=$stage\n")
    }

    private fun finish(success: Boolean, detail: String) {
        if (!finished.compareAndSet(false, true)) return
        val status = if (success) "PASS" else "FAIL"
        try {
            Files.writeString(output.resolve("result.txt"), "$status\nstage=$stage\n$detail\n")
        } finally {
            logger.info("Client smoke {}: {}", status, detail)
            Minecraft.getInstance()?.stop()
        }
    }
}
