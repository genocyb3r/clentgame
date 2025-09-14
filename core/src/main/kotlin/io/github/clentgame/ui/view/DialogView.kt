package io.github.clentgame.ui.view

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import io.github.clentgame.ui.Buttons
import io.github.clentgame.ui.Drawables
import io.github.clentgame.ui.Labels
import io.github.clentgame.ui.get
import io.github.clentgame.ui.model.DialogModel
import ktx.actors.alpha
import ktx.actors.onClick
import ktx.actors.txt
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.label
import ktx.scene2d.table
import ktx.scene2d.textButton

class DialogView(
    private val model: DialogModel,
    skin: Skin,
): Table(skin), KTable {

    private val dialogTxt: Label
    private val buttonArea: Table

    init {
        setFillParent(true)
        this.alpha = 0f

        table {
            background = skin[Drawables.FRAME_BGD]

            this@DialogView.dialogTxt = label("", Labels.FRAME.skinKey) { lblCell ->
                this.setAlignment(Align.topLeft)
                this.wrap = true
                lblCell.expand().fill().pad(8f).row()

            }
            // button area
            this@DialogView.buttonArea = table { btnAreaCell ->
                this.defaults().expand()

                textButton("", Buttons.TEXT_BUTTON.skinkey)
                textButton("", Buttons.TEXT_BUTTON.skinkey)

                btnAreaCell.expandX().fillX().pad(0f,8f,8f,8f)
            }
            it.expand().width(200f).height(130f).center().row()
        }

        model.onPropertyChange(DialogModel::text){
            dialogTxt.txt = it
            this.alpha = 1f
        }

        model.onPropertyChange(DialogModel::completed){completed ->
            if (completed) {
                this.alpha = 0f
                this.buttonArea.clearChildren()
            }
        }
        model.onPropertyChange(DialogModel::options) { dialogOptions ->
            buttonArea.clearChildren()

            dialogOptions.forEach {
                buttonArea.add(textButton(it.text, Buttons.TEXT_BUTTON.skinkey).apply {
                    onClick { this@DialogView.model.triggerOption(it.idx) }
                })
            }

        }
    }
}
@Scene2dDsl
fun <S> KWidget<S>.dialogView(
    model: DialogModel,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: DialogView.(S) -> Unit = {}
): DialogView = actor(DialogView(model, skin), init)
