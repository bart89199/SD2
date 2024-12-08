package ru.batr.sD2.config

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.inventory.ItemStack
import ru.batr.sD2.TextFormatter

object MessagesConfig : Config("messages.yml") {
    private val itemChangedDelegate = string(
        "itemChanged",
        { "<prefix><aqua>Предмет изменён на <item></aqua>" },
        listOf("Этот тест, лол"),
        { "1" },
    )
    var itemChanged by itemChangedDelegate
    fun itemChanged(itemStack: ItemStack) =
        TextFormatter.format(itemChanged, Placeholder.parsed("item", itemStack.type.toString()))

    val reloadMessage by componentS(
        "reloadMessage",
        { "<prefix><aqua>Конфиг <server> успешно перезагружен!</aqua>" },
        placeholders = arrayOf(Placeholder.parsed("server", "SD"))
    )
}