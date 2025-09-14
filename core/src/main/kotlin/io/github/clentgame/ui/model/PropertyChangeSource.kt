package io.github.clentgame.ui.model

import kotlin.reflect.KProperty

abstract class PropertyChangeSource {
    @PublishedApi
    internal val listenerMap = mutableMapOf<KProperty<*>, MutableList<(Any) -> Unit>>()

    inline fun <reified T> onPropertyChange(property:KProperty<T>, noinline action: (T) -> Unit) {
        val actions = listenerMap.getOrPut(property) { mutableListOf() } as MutableList<(T) -> Unit>
        actions += action
    }
    fun notify(property: KProperty<*>, value: Any) {
        listenerMap[property]?.forEach { it(value) }
    }


}

class PropertyNotifier<T:Any>(initialValue:T){
    private var _value:T = initialValue

    operator fun getValue(thisRef: PropertyChangeSource, property: KProperty<*>): T = _value

    operator fun setValue(thisRef: PropertyChangeSource, property: KProperty<*>, value: T) {
        _value = value
        thisRef.notify(property, value)

    }
}

inline fun <reified T:Any> propertyNotifier(initialValue:T): PropertyNotifier<T> = PropertyNotifier(initialValue)
