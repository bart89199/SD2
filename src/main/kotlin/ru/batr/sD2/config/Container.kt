package ru.batr.sD2.config

import kotlin.reflect.KProperty

interface Container<T> {
    operator fun getValue(thisRef: Any, property: KProperty<*>): T
}

interface EditableContainer<I>: Container<I> {
    operator fun setValue(thisRef: Any, property: KProperty<*>, value: I)
}