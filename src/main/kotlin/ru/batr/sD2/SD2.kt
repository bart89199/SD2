package ru.batr.sD2

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import ru.batr.sD2.config.MainConfig
import ru.batr.sD2.config.MessagesConfig
import ru.batr.sD2.config.Settings
import ru.batr.sD2.handlers.JoinListener

class SD2 : JavaPlugin() {

    override fun onEnable() {
        instance = this
        SD2.classLoader = this.classLoader
        Bukkit.getPluginManager().registerEvents(JoinListener(), this)

      //  getCommand("sd")?.setExecutor(this)
       // getCommand("sd")?.tabCompleter = this
        Settings.load()
        MainConfig.load()
        MessagesConfig.load()

        TextFormatter.reload()
        Bukkit.getConsoleSender()
            .sendMessage(TextFormatter.format("<prefix><#9afbfc>Hi, plugin enabled, our discord: <bold><#508ed9><discord></#508ed9></bold>"))
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (!args.isNullOrEmpty()) {
            if (sender !is Player) return false
            when(args[0]) {
                "1" -> {
                    MainConfig.testItem = sender.inventory.itemInMainHand
                    sender.sendMessage(MessagesConfig.itemChanged(MainConfig.testItem))
                    sender.sendMessage(MessagesConfig.itemChanged)
                }
                "2" -> {
                    MainConfig.testList.forEach {
                        sender.sendMessage(it)
                    }
                    val list = MainConfig.testList.toMutableList()
                    list.add(args[1])
                    MainConfig.testList = list
                }
                "3" -> {
                    val map = MainConfig.testMap.toMutableMap()
                    for ((key, value) in MainConfig.testMap) {
                        sender.sendMessage("$key=${value.type} ${value.amount}")
                    }
                    map[args[1]] = sender.inventory.itemInMainHand
                    MainConfig.testMap = map
                }
            }
        } else {
            reload()
            sender.sendMessage(MessagesConfig.reloadMessage)
        }
        return true
    }

    fun reload() {
        instance = this
        SD2.classLoader = this.classLoader
        Settings.reload()
        MainConfig.reload()
        MessagesConfig.reload()

        TextFormatter.reload()
    }

    companion object {
        lateinit var classLoader: ClassLoader
            private set
        lateinit var instance: SD2
            private set
    }
}
