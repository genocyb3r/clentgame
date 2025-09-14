package io.github.clentgame.component

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable

enum class AnimationModel{
    PLAYER, SLIME, CHEST, UNDEFINE;

    val atlasKey: String = this.toString().lowercase()
}

enum class AnimationType{
    IDLE, RUN, JUMP, FALL, ATTACK, DEATH, OPEN;

    val atlasKey: String = this.toString().lowercase()
}

data class AnimationComponent(
    var model: AnimationModel = AnimationModel.UNDEFINE,
    var stateTime: Float = 0f,
    var playMode: Animation.PlayMode = Animation.PlayMode.LOOP){

    lateinit var animation: Animation<TextureRegionDrawable>
    var nextAnimation: String = NO_ANIMATION

   val isAnimationDone: Boolean
      get() = ::animation.isInitialized && animation.isAnimationFinished(stateTime)

    fun nextAnimation(model: AnimationModel, type: AnimationType){
        this.model = model
        nextAnimation = "${model.atlasKey}/${type.atlasKey}"

    }
    fun nextAnimation(type: AnimationType){
        nextAnimation = "${model.atlasKey}/${type.atlasKey}"

    }
    companion object{
        val NO_ANIMATION = ""
    }
}




