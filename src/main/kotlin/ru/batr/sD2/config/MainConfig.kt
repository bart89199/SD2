package ru.batr.sD2.config

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.inventory.ItemStack


object MainConfig : Config("config.yml") {
    val prefix by componentS(
        "prefix",
        {"<bold><#37bf94>{<#ffed66>S</#ffed66><#4bdb84>D</#4bdb84><bold><#3973c4>}"},
        listOf(
            "This prefix will appear in plugin messages",
            "For decorations we use MiniMessage",
            "Editor - https://webui.advntr.dev"
        ),
        MiniMessage.miniMessage(),
    )
    val discord by componentS(
        "discord",
        {"<click:open_url:'https://discord.gg/4TFFhqFFDR'>https://discord.gg/4TFFhqFFDR"},
        listOf("Link to discord server"),
        MiniMessage.miniMessage(),
    )
    var testItem by itemStack(
        "test.item",
        {ItemStack(Material.STONE)},
        configDefault = {null}
    )
    var testList by list(
        "test.list",
        {emptyList()},
        listOf("Just test"),
        toString
    )
    var testMap by map(
        "test.map",
        {mapOf("1" to ItemStack(Material.STONE), "2" to ItemStack(Material.WOLF_ARMOR))},
        listOf("Just test"),
        toItemStack
    )
}