package io.github.clentgame.dialog

import ktx.app.gdxError

fun dialog(id: String, cfg: Dialog.() -> Unit):  Dialog {
    return Dialog(id).apply(cfg)
}

@DslMarker
annotation class DialogDslMarker


@DialogDslMarker
data class Dialog(
    val id: String,
    private val nodes: MutableList<Node> = mutableListOf(),
    private var complete: Boolean = false,
) {
    lateinit var currentNode: Node

    fun isComplete() : Boolean = complete
    fun node(id: Int, text: String, cfg: Node.() -> Unit) : Node {
        return Node(id, text).apply{
            this.cfg()

            this@Dialog.nodes += this
        }

    }

    fun goToNode(nodeId: Int){
        currentNode = nodes.firstOrNull { it.id == nodeId } ?: error("Node $nodeId not found in dialog $this")

    }
    fun start(){
        complete = false
        currentNode = nodes.first()
    }

    fun end(){
        complete = true
    }

    fun triggerOption(optionId: Int){
       val option = currentNode[optionId] ?:  gdxError("There is no option with id $optionId in node $currentNode")
        option.action()

    }
}
@DialogDslMarker
data class Node(val id: Int, val text: String){
    val options: MutableList<Option> = mutableListOf()
    fun option(text: String, cfg: Option.() -> Unit): Option {
        return Option(options.size, text).apply{
            this.cfg()
            this@Node.options += this
        }

    }
    operator fun get(optionId: Int): Option? {
        return options.getOrNull(optionId)
    }
}

@DialogDslMarker
data class Option(
    val id: Int,
    val text: String,
    var action: () -> Unit = {},
)

