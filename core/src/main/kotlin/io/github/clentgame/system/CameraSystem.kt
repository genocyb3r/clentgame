package io.github.clentgame.system

import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import io.github.clentgame.component.ImageComponent
import io.github.clentgame.component.PlayerComponent
import io.github.clentgame.event.MapChangeEvent
import ktx.tiled.height
import ktx.tiled.width
import kotlin.math.max
import kotlin.math.min

@AllOf([PlayerComponent::class, ImageComponent::class])
class CameraSystem(
    private val imageCmps: ComponentMapper<ImageComponent>,
    stage: Stage
):EventListener, IteratingSystem(){

    private var maxW = 0f
    private var maxH = 0f
    private val camera = stage.camera

    override fun onTickEntity(entity: Entity) {
        with(imageCmps[entity]){
            val viewW = camera.viewportWidth*0.5f
            val viewH = camera.viewportHeight*0.5f
            val camMinW = min(viewW, maxW - viewW)
            val camMaxW = max(viewW, maxW - viewW)
            val camMinH = min(viewH, maxH - viewH)
            val camMaxH = max(viewH, maxH - viewH)
            camera.position.set(

                image.x.coerceIn(camMinW, camMaxW),
                image.y.coerceIn(camMinH, camMaxH),
                camera.position.z
            )
        }
    }

    override fun handle(event: Event?): Boolean {
        if(event is MapChangeEvent){
            maxW = event.map.width.toFloat()
            maxH = event.map.height.toFloat()
            return true
        }
        return false
    }
}
