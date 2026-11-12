package nimble.portable_blueprints.init
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry
import net.minecraft.world.level.GameRules.Category
object ModGameRules {
    @JvmField val Max_digitized_structures_for_player = intRule("maxDigitizedStructuresForPlayer", 20)
    @JvmField val MaxScannerBlockCount = intRule("maxScannerBlockCount", 30000)
    @JvmField val MaxBlueprintContainers = intRule("maxBlueprintContainers", 20)
    @JvmField val AllowGlobalBlueprints = booleanRule("allowGlobalBlueprints", false)
    @JvmField val AllowNBTDataCopy = booleanRule("allowNBTDataCopy", false)
    @JvmField val BlueprintsCooldown = intRule("blueprintsCooldown", 0)
    @JvmField val BusinessVillagerEnabled = booleanRule("businessVillagerEnabled", false)
    private fun intRule(name: String, value: Int) = GameRuleRegistry.register(name, Category.PLAYER, GameRuleFactory.createIntRule(value))
    private fun booleanRule(name: String, value: Boolean) = GameRuleRegistry.register(name, Category.PLAYER, GameRuleFactory.createBooleanRule(value))
    @JvmStatic fun register() = Unit
}
