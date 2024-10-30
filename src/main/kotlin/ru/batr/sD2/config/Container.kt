package ru.batr.sD2.config

import kotlin.reflect.KProperty

interface Container<T> {
    var value: T
    operator fun getValue(thisRef: Any, property: KProperty<*>): T = value
    operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        this.value = value
    }
}