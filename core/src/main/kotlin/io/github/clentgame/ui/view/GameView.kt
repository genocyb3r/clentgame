package io.github.clentgame.ui.view

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.delay
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn
import com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut
import com.badlogic.gdx.scenes.scene2d.actions.DelayAction
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import io.github.clentgame.ui.Drawables
import io.github.clentgame.ui.Labels
import io.github.clentgame.ui.widget.CharacterInfo
import io.github.clentgame.ui.widget.characterInfo
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import io.github.clentgame.ui.get
import io.github.clentgame.ui.model.GameModel
import ktx.actors.alpha
import ktx.actors.plusAssign
import ktx.scene2d.label
import ktx.scene2d.table

class GameView(
    model: GameModel,
    skin:  Skin,
): Table(skin), KTable{

    private val playerInfo: CharacterInfo
    private val enemyInfo: CharacterInfo
    private val popupLabel: Label

    init {
        //UI
        setFillParent(true)
        enemyInfo = characterInfo(Drawables.PLAYER){
            this.alpha = 0f
            it.row()
        }
        table{
            background = skin[Drawables.FRAME_BGD]
            this@GameView.popupLabel = label("", style = Labels.FRAME.skinKey){ lblCell ->
                this.setAlignment(Align.topLeft)
                this.wrap = true
                lblCell.expand().fill().pad(14f)



            }
            this.alpha = 0f
            it.expand().width(130f).height(90f).top().row()
        }
        playerInfo = characterInfo(Drawables.PLAYER)

        //Data Binding
        model.onPropertyChange(GameModel::playerLife){ playerLife ->
            playerLife(playerLife)
        }
        model.onPropertyChange(GameModel::lootText){ lootInfo ->
            popUp(lootInfo)
        }
        model.onPropertyChange(GameModel::enemyLife){ enemyLife  ->
            enemyLife(enemyLife)
        }
        model.onPropertyChange(GameModel::enemyType){ enemyType  ->
            when(enemyType){
                "slime" -> showEnemyInfo(Drawables.SLIME, model.enemyLife)

            }
        }

    }

    fun playerLife(percentage: Float) = playerInfo.life(percentage)
    fun enemyLife(percentage: Float) = enemyInfo.life(percentage)

    private fun Actor.resetFadeOutDelay(){
        this.actions
            .filterIsInstance<SequenceAction>()
            .lastOrNull()
            ?.let {sequence ->
                val delay = sequence.actions.last() as DelayAction
                delay.time = 0f
            }
    }

    fun showEnemyInfo(charDrawables: Drawables, lifePercentage: Float){
        enemyInfo.character(charDrawables)
        enemyInfo.life(lifePercentage, 0f)

        if (enemyInfo.alpha == 0f){
            enemyInfo.clearActions()
            enemyInfo += Actions.sequence(
                Actions.fadeIn(1f, Interpolation.bounceIn),
                Actions.delay(5f, Actions.fadeOut(0.5f)))

        }else{
            enemyInfo.resetFadeOutDelay()
        }


    }

    fun popUp(infoText: String){
        popupLabel.setText(infoText)
        if (popupLabel.parent.alpha == 0f){
            popupLabel.parent.clearActions()
            popupLabel.parent += Actions.sequence(fadeIn(0.2f), delay(4f, fadeOut(0.75f)))
        }else {
            popupLabel.parent.resetFadeOutDelay()
        }

    }

}

@Scene2dDsl
fun <S> KWidget<S>.gameView(
    model: GameModel,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: GameView.(S) -> Unit = {}

): GameView = actor(GameView(model, skin), init)

