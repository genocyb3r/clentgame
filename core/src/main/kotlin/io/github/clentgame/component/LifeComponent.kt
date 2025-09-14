package io.github.clentgame.component

data class LifeComponent(
    var life: Float = 30f,
    var maxLife: Float = 30f,
    var regeneration: Float = 0f,
    var takeDamage: Float = 0f,
) {
    val isDead: Boolean
        get()= life <= 0f
}
