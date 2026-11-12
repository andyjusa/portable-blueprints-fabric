package nimble.portable_blueprints.fabric.network

import java.util.concurrent.Executor
import net.minecraft.world.entity.player.Player

/** The authenticated connection player and its logical-side main-thread executor. */
class PayloadContext(private val player: Player, private val executor: Executor) {
    fun player(): Player = player

    fun enqueueWork(work: Runnable) {
        executor.execute(work)
    }
}
