package ru.batr.sD2.handlers

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import ru.batr.sD2.TextFormatter
import ru.batr.sD2.config.MainConfig

class JoinListener: Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player

        player.sendMessage(TextFormatter.format("<prefix><#9afbfc>Hi our discord link is <bold><#508ed9><discord></#508ed9></bold>"))
        player.inventory.setItemInMainHand(MainConfig.testItem)
    }
}