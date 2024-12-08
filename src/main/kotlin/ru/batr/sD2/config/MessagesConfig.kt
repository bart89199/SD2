package ru.batr.sD2.config

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.inventory.ItemStack
import ru.batr.sD2.TextFormatter

object MessagesConfig : Config("messages.yml") {
    private val itemChangedDelegate = cctContainerC(
        "itemChanged",
        { input ->
            input?.let {
                { itemStack: ItemStack ->
                    TextFormatter.format(
                        input.toString(),
                        Placeholder.parsed("item", itemStack.type.toString())
                    )
                }
            }
        },
        { "<prefix><aqua>Предмет изменён на <item></aqua>" },
        listOf("Этот тест, лол"),
        { "1" },
    )

    val itemChanged by itemChangedDelegate
    fun setItemChanged(value: String) {
        itemChangedDelegate.set(value)
    }

    val reloadMessage by componentS(
        "reloadMessage",
        { "<prefix><aqua>Конфиг <server> успешно перезагружен!</aqua>" },
        placeholders = arrayOf(Placeholder.parsed("server", "SD"))
    )
}