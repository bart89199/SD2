package ru.batr.sD2.config

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import ru.batr.sD2.TextFormatter

val toString: Converter<String> = { input -> input?.toString() }
val toByte: Converter<Byte> = { input -> input?.toString()?.toByte() }
val toShort: Converter<Short> = { input -> input?.toString()?.toShort() }
val toInt: Converter<Int> = { input -> input?.toString()?.toInt() }
val toLong: Converter<Long> = { input -> input?.toString()?.toLong() }
val toFloat: Converter<Float> = { input -> input?.toString()?.toFloat() }
val toDouble: Converter<Double> = { input -> input?.toString()?.toDouble() }
val toChar: Converter<Char> = { input -> input?.toString()?.firstOrNull() }
val toLocation: Converter<Location> = { input -> if (input is Location) input else null }
val toItemStack: Converter<ItemStack> = { input -> if (input is ItemStack) input else null }
val toVector: Converter<Vector> = { input -> if (input is Vector) input else null }
val toOfflinePlayer: Converter<OfflinePlayer> = { input -> if (input is OfflinePlayer) input else null }
val toColor: Converter<Color> = { input -> if (input is Color) input else null }
val toBoolean: Converter<Boolean> = { input ->
    when (input) {
        "true" -> true
        "false" -> false
        "1" -> true
        "0" -> false
        "yes" -> true
        "no" -> false
        "да" -> true
        "нет" -> false
        else -> null
    }
}

fun toComponent(
    serializer: MiniMessage = TextFormatter.DEFAULT_SERIALIZER,
    vararg placeholders: TagResolver
): Converter<Component> = { input -> input?.let { serializer.deserialize(it.toString(), *placeholders) } }

fun fromComponent(serializer: MiniMessage): SaveConvertor<Component, String> = { it?.let { serializer.serialize(it) } }

fun <T> toList(convertor: Converter<T>): Converter<List<T>> = { input ->
    if (input !is List<*>) {
        null
    } else {
        val res = ArrayList<T>()
        for (el in input) {
            convertor(el)?.let { res.add(it) }
        }
        res
    }
}

fun <T> toMap(convertor: Converter<T>): Converter<Map<String, T>> = { input ->
    when (input) {
        is Map<*, *> -> {
            val res = HashMap<String, T>()
            for ((key, value) in input) {
                if (key == null) continue
                convertor(value)?.let { res[key.toString()] = it }
            }
            res
        }

        is ConfigurationSection -> {
            val res = HashMap<String, T>()
            for (key in input.getKeys(false)) {
                convertor(input.get(key))?.let { res[key] = it }
            }
            res
        }

        else -> null
    }
}

//fun <T> defaultSaver(): SaveConvertor<T> = { config, path, value -> config.set(path, value) }
//fun <T> saveList(saver: Saver<T>? = null): Saver<List<T>> = { config, path, value ->
//    if (value != null) {
//        if (saver == null) {
//            config.set(path, value)
//        } else {
//            val list = ArrayList<Any?>()
//            for (el in value) {
//                saver(config, "temp", el)
//                list.add(config.get("temp"))
//                config.set("temp", null)
//            }
//            defaultSaver<Any>()(config, path, list)
//        }
//    }
//}

//fun <T> saveMap(saveConvertor: SaveConvertor<T>? = null): SaveConvertor<Map<String, T>> = { config, path, value ->
//    if (value != null) {
//        for ((key, v) in value) {
//            (saveConvertor ?: defaultSaver<T>())(config, "$path.$key", v)
//        }
//    }
//}

//fun saveComponent(serializer: MiniMessage = TextFormatter.DEFAULT_SERIALIZER): SaveConvertor<Component> =
//    { config, path, value -> config.set(path, value?.let { serializer.serialize(it) }) }


fun <T> Config.container(
    path: String,
    default: () -> T,
    comments: List<String>,
    convertor: Converter<T>,
    configDefault: () -> T? = default,
) = ConfigContainer(this, path, default, configDefault, comments, convertor).also { containers.add(it) }

fun <T, C> Config.cctContainer(
    path: String,
    convertor: Converter<T>,
    saveConvertor: SaveConvertor<T, C>,
    default: () -> T,
    configDefault: () -> C? = { saveConvertor(default()) },
    comments: List<String>,
) = ConfigContainerCCT(
    this,
    path,
    saveConvertor,
    convertor,
    default,
    configDefault,
    comments
).also { containers.add(it) }

//fun <T, C> Config.cctContainer(
//    path: String,
//    convertor: Converter<T>,
//    saveConvertor: SaveConvertor<T, C>,
//    configDefault: () -> C,
//    default: () -> T = {
//        convertor(configDefault())
//            ?: throw IllegalArgumentException("Config parameter default value can't converting to null")
//    },
//    comments: List<String>,
//) = ConfigContainerCCT(
//    this,
//    path,
//    saveConvertor,
//    convertor,
//    default,
//    configDefault,
//    comments
//).also { containers.add(it) }

fun <T, C> Config.cctContainer(
    path: String,
    convertor: Converter<T>,
    saveConvertor: SaveConvertor<T, C>,
    default: () -> T,
    comments: List<String>,
    configDefaultT: () -> T? = default,
) = ConfigContainerCCT(
    this,
    path,
    saveConvertor,
    convertor,
    default,
    configDefaultT,
    comments
).also { containers.add(it) }

fun <T, C> Config.cctContainerC(
    path: String,
    convertor: Converter<T>,
    saveConvertor: SaveConvertor<T, C>,
    defaultC: () -> C,
    comments: List<String>,
    configDefault: () -> C? = defaultC,
) = ConfigContainerCCT(
    this,
    path,
    saveConvertor,
    convertor,
    {
        convertor(defaultC())
            ?: throw IllegalArgumentException("Config parameter default value can't converting to null")
    },
    configDefault,
    comments
).also { containers.add(it) }

fun Config.string(
    path: String,
    default: () -> String,
    comments: List<String> = emptyList(),
    configDefault: () -> String? = default,
) = container(path, default, comments, toString, configDefault)


fun Config.byte(
    path: String,
    default: () -> Byte,
    comments: List<String> = emptyList(),
    configDefault: () -> Byte? = default,
) = container(path, default, comments, toByte, configDefault)

fun Config.short(
    path: String,
    default: () -> Short,
    comments: List<String> = emptyList(),
    configDefault: () -> Short? = default,
) = container(path, default, comments, toShort, configDefault)

fun Config.int(
    path: String,
    default: () -> Int,
    comments: List<String> = emptyList(),
    configDefault: () -> Int? = default,
) = container(path, default, comments, toInt, configDefault)

fun Config.long(
    path: String,
    default: () -> Long,
    comments: List<String> = emptyList(),
    configDefault: () -> Long? = default,
) = container(path, default, comments, toLong, configDefault)

fun Config.float(
    path: String,
    default: () -> Float,
    comments: List<String> = emptyList(),
    configDefault: () -> Float? = default,
) = container(path, default, comments, toFloat, configDefault)

fun Config.double(
    path: String,
    default: () -> Double,
    comments: List<String> = emptyList(),
    configDefault: () -> Double? = default,
) = container(path, default, comments, toDouble, configDefault)

fun Config.boolean(
    path: String,
    default: () -> Boolean,
    comments: List<String> = emptyList(),
    configDefault: () -> Boolean? = default,
) = container(path, default, comments, toBoolean, configDefault)

fun Config.char(
    path: String,
    default: () -> Char,
    comments: List<String> = emptyList(),
    configDefault: () -> Char? = default,
) = container(path, default, comments, toChar, configDefault)

fun Config.location(
    path: String,
    default: () -> Location,
    comments: List<String> = emptyList(),
    configDefault: () -> Location? = default,
) = container(path, default, comments, toLocation, configDefault)

fun Config.itemStack(
    path: String,
    default: () -> ItemStack,
    comments: List<String> = emptyList(),
    configDefault: () -> ItemStack? = default,
) = container(path, default, comments, toItemStack, configDefault)

fun Config.vector(
    path: String,
    default: () -> Vector,
    comments: List<String> = emptyList(),
    configDefault: () -> Vector? = default,
) = container(path, default, comments, toVector, configDefault)

fun Config.offlinePlayer(
    path: String,
    default: () -> OfflinePlayer,
    comments: List<String> = emptyList(),
    configDefault: () -> OfflinePlayer? = default,
) = container(path, default, comments, toOfflinePlayer, configDefault)

fun Config.color(
    path: String,
    default: () -> Color,
    comments: List<String> = emptyList(),
    configDefault: () -> Color? = default,
) = container(path, default, comments, toColor, configDefault)

fun Config.component(
    path: String,
    default: () -> Component,
    configDefault: () -> String?,
    comments: List<String> = emptyList(),
    serializer: MiniMessage = TextFormatter.DEFAULT_SERIALIZER,
    vararg placeholders: TagResolver = emptyArray(),
) = cctContainer(
    path,
    toComponent(serializer, *placeholders),
    fromComponent(serializer),
    default,
    configDefault,
    comments,
)

fun Config.component(
    path: String,
    default: () -> Component,
    comments: List<String> = emptyList(),
    serializer: MiniMessage = TextFormatter.DEFAULT_SERIALIZER,
    vararg placeholders: TagResolver = emptyArray(),
) = cctContainer(
    path,
    toComponent(serializer, *placeholders),
    fromComponent(serializer),
    default,
    comments,
)

fun Config.componentS(
    path: String,
    defaultS: () -> String,
    configDefault: () -> String?,
    comments: List<String> = emptyList(),
    serializer: MiniMessage = TextFormatter.DEFAULT_SERIALIZER,
    vararg placeholders: TagResolver = emptyArray(),
) = cctContainerC(
    path,
    toComponent(serializer, *placeholders),
    fromComponent(serializer),
    defaultS,
    comments,
    configDefault,
)

fun Config.componentS(
    path: String,
    defaultS: () -> String,
    comments: List<String> = emptyList(),
    serializer: MiniMessage = TextFormatter.DEFAULT_SERIALIZER,
    vararg placeholders: TagResolver = emptyArray(),
) = cctContainerC(
    path,
    toComponent(serializer, *placeholders),
    fromComponent(serializer),
    defaultS,
    comments,
)

fun <T> Config.list(
    path: String,
    default: () -> List<T>,
    comments: List<String> = emptyList(),
    convertor: Converter<T>,
    configDefault: () -> List<T>? = default,
) = container(path, default, comments, toList(convertor), configDefault)

fun <T> Config.map(
    path: String,
    default: () -> Map<String, T>,
    comments: List<String> = emptyList(),
    convertor: Converter<T>,
    configDefault: () -> Map<String, T>? = default,
) = container(path, default, comments, toMap(convertor), configDefault)