package nimble.portable_blueprints.fabric.client

import com.mojang.blaze3d.vertex.PoseStack
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.Camera
import net.minecraft.client.renderer.culling.Frustum
import org.joml.Matrix4f
import org.joml.Quaternionf

/** Preview phases dispatched by Fabric world render callbacks. */
class RenderLevelStageEvent(val stage: Stage, private val context: WorldRenderContext) {
    enum class Stage {
        AFTER_SOLID_BLOCKS, AFTER_CUTOUT_MIPPED_BLOCKS, AFTER_CUTOUT_BLOCKS,
        AFTER_ENTITIES, AFTER_BLOCK_ENTITIES, AFTER_TRANSLUCENT_BLOCKS,
        AFTER_TRIPWIRE_BLOCKS, AFTER_PARTICLES, AFTER_WEATHER, AFTER_LEVEL
    }

    val poseStack: PoseStack = context.matrixStack() ?: PoseStack().apply {
        mulPose(context.camera().rotation().conjugate(Quaternionf()))
    }
    val projectionMatrix: Matrix4f get() = context.projectionMatrix()
    val camera: Camera get() = context.camera()

    init {
        currentFrustum = context.frustum()
    }

    companion object {
        private var currentFrustum: Frustum? = null

        @JvmStatic
        fun getActiveFrustum(): Frustum? = currentFrustum
    }
}
