package ru.batr.sD2.config

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.inventory.ItemStack

object MessagesConfig : Config("messages.yml") {
    val itemChanged = component(
        "itemChanged",
        "<prefix><aqua>Предмет изменён на <item></aqua>",
        listOf("Этот тест, лол"),
        configDefault = "1",
        placeholders = arrayOf(Placeholder.parsed("item", "новый")),
        dynamicPlaceholders = mapOf("item" to { input ->
            Placeholder.parsed(
                "item",
                if (input is ItemStack) input.type.toString() else "новый"
            )
        })
    )
    val reloadMessage by component(
        "reloadMessage",
        "<prefix><aqua>Конфиг <server> успешно перезагружен!</aqua>",
        placeholders = arrayOf(Placeholder.parsed("server", "SD"))
    )
}