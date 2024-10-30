package ru.batr.sD2.config

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.inventory.ItemStack

object MessagesConfig : Config("messages.yml") {
    private val itemChangedDelegate = component(
        "itemChanged",
        "<prefix><aqua>Предмет изменён на <item></aqua>",
        listOf("Этот тест, лол"),
        configDefault = "1",
    )
    var itemChanged by itemChangedDelegate
    fun itemChanged(itemStack: ItemStack) = itemChangedDelegate.add(Placeholder.parsed("item", itemStack.type.toString())).value
    val reloadMessage by component(
        "reloadMessage",
        "<prefix><aqua>Конфиг <server> успешно перезагружен!</aqua>",
        placeholders = arrayOf(Placeholder.parsed("server", "SD"))
    )
}