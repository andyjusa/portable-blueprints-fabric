package nimble.portable_blueprints.fabric.client

/** Canceled world scrolls do not change the hotbar. */
class MouseScrollingEvent(private val delta: Double) {
    private var canceledValue = false

    fun getScrollDeltaY(): Double = delta
    fun setCanceled(canceled: Boolean) {
        canceledValue = canceled
    }
    fun isCanceled(): Boolean = canceledValue
}
