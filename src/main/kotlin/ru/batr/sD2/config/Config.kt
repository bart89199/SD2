package ru.batr.sD2.config

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import ru.batr.sD2.SD2
import ru.batr.sD2.TextFormatter
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.logging.Level

typealias Converter<T> = (Any?) -> T?
typealias Saver<T> = (YamlConfiguration, String, T?) -> Unit

abstract class Config(name: String) {
    var name: String
        private set
    lateinit var file: File
        protected set
    lateinit var config: YamlConfiguration
        protected set
    protected lateinit var defaultConfig: YamlConfiguration
    val containers: MutableList<AbstractConfigContainer<*>> = ArrayList()

    init {
        this.name = name.replace("/", File.separator)
    }

    fun load() {
        file = File(SD2.instance.dataFolder, name)
        loadDefaultConfig()
        reload()
    }

    fun isLoaded() = ::file.isInitialized && ::config.isInitialized && ::defaultConfig.isInitialized

    fun loadDefaultConfig() {
        defaultConfig =
            getResource(name)?.let {
                YamlConfiguration.loadConfiguration(InputStreamReader(it, Charsets.UTF_8))
            } ?: YamlConfiguration()
        for (container in containers) {
            container.saveDefault(defaultConfig)
            defaultConfig.setComments(container.path, container.comments)
        }
    }

    fun getResource(filename: String): InputStream? {
        try {
            val url = SD2.classLoader.getResource(filename) ?: return null
            val connection = url.openConnection()
            connection.useCaches = false
            return connection.getInputStream()
        } catch (e: IOException) {
            return null
        }
    }

    fun reload() {
        if (!file.exists()) {
            try {
                defaultConfig.save(file)
            } catch (e: IOException) {
                SD2.instance.logger.log(Level.SEVERE, "Could not save default config", e)
            }
        }
        config = YamlConfiguration.loadConfiguration(file)
        //   config.setDefaults(defaultConfig)
    }

    fun save() {
        try {
            config.save(file)
        } catch (ex: IOException) {
            SD2.instance.logger.log(Level.SEVERE, "Could not save $name", ex)
        }
    }
}

abstract class AbstractConfigContainer<T>(
    val config: Config,
    val path: String,
    val default: T,
    val configDefault: T? = default,
    val comments: List<String> = emptyList(),
) : Container<T> {
    override var value: T
        get() = if (config.isLoaded()) convert(config.config.get(path)) ?: default else default
        set(value) {
            save(value)
            config.save()
        }

    abstract fun convert(input: Any?): T
    open fun save(value: T) {
        defaultSaver<T>()(config.config, path, value)
    }

    open fun saveDefault(config: YamlConfiguration) {
        defaultSaver<T>()(config, path, configDefault)
    }
}

class ConfigContainer<T>(
    config: Config,
    path: String,
    default: T,
    configDefault: T? = default,
    comments: List<String> = emptyList(),
    val convertor: Converter<T>,
    val saver: Saver<T>? = null
) :
    AbstractConfigContainer<T>(config, path, default, configDefault, comments) {
    override fun convert(input: Any?): T = convertor(input) ?: default
    override fun save(value: T) {
        saver?.let { it(config.config, path, value) } ?: super.save(value)
    }

    override fun saveDefault(config: YamlConfiguration) {
        saver?.let { it(config, path, configDefault) } ?: super.saveDefault(config)
    }
}

class ComponentContainer(
    config: Config,
    path: String,
    val defaultString: String,
    val configDefaultString: String? = defaultString,
    comments: List<String> = emptyList(),
    val serializer: MiniMessage = TextFormatter.DEFAULT_SERIALIZER,
    vararg val placeholders: TagResolver = emptyArray(),
) : AbstractConfigContainer<Component>(
    config, path,
    serializer.deserialize(defaultString, *placeholders),
    configDefaultString?.let { serializer.deserialize(it, *placeholders) },
    comments
) {
    override fun convert(input: Any?) = serializer.deserialize(toString(input) ?: defaultString, *placeholders)

    override fun save(value: Component) {
        config.config.set(path, serializer.serialize(value))
    }

    override fun saveDefault(config: YamlConfiguration) {
        config.set(path, configDefaultString)
    }

    fun add(vararg placeholders: TagResolver) = ComponentContainer(
        config,
        path,
        defaultString,
        configDefaultString,
        comments,
        serializer,
        TagResolver.builder().resolvers(*this.placeholders).resolvers(*placeholders).build()
    )
}


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

fun <T> defaultSaver(): Saver<T> = { config, path, value -> config.set(path, value) }
fun <T> saveList(saver: Saver<T>? = null): Saver<List<T>> = { config, path, value ->
    if (value != null) {
        if (saver == null) {
            config.set(path, value)
        } else {
            val list = ArrayList<Any?>()
            for (el in value) {
                saver(config, "temp", el)
                config.get(path)?.let { list.add(it) }
                config.set("temp", null)
            }
            config.set(path, list)
        }
    }
}

fun <T> saveMap(saver: Saver<T>? = null): Saver<Map<String, T>> = { config, path, value ->
    if (value != null) {
        for ((key, v) in value) {
            (saver ?: defaultSaver<T>())(config, "$path.$key", v)
        }
    }
}

fun saveComponent(serializer: MiniMessage = TextFormatter.DEFAULT_SERIALIZER): Saver<Component> =
    { config, path, value -> config.set(path, value?.let { serializer.serialize(it) }) }


fun <T> Config.container(
    path: String,
    default: T,
    comments: List<String>,
    convertor: Converter<T>,
    configDefault: T? = default,
    saver: Saver<T>? = null,
) = ConfigContainer(this, path, default, configDefault, comments, convertor, saver).also { containers.add(it) }

fun Config.string(
    path: String,
    default: String,
    comments: List<String> = emptyList(),
    convertor: Converter<String> = toString,
    configDefault: String? = default,
    saver: Saver<String>? = null,
) = container(path, default, comments, convertor, configDefault, saver)


fun Config.byte(
    path: String,
    default: Byte,
    comments: List<String> = emptyList(),
    convertor: Converter<Byte> = toByte,
    configDefault: Byte? = default,
    saver: Saver<Byte>? = null,
) = container(path, default, comments, convertor, configDefault, saver)

fun Config.short(
    path: String,
    default: Short,
    comments: List<String> = emptyList(),
    convertor: Converter<Short> = toShort,
    configDefault: Short? = default,
    saver: Saver<Short>? = null,
) = container(path, default, comments, convertor, configDefault, saver)

fun Config.int(
    path: String,
    default: Int,
    comments: List<String> = emptyList(),
    convertor: Converter<Int> = toInt,
    configDefault: Int? = default,
    saver: Saver<Int>? = null,
) = container(path, default, comments, convertor, configDefault, saver)

fun Config.long(
    path: String,
    default: Long,
    comments: List<String> = emptyList(),
    convertor: Converter<Long> = toLong,
    configDefault: Long? = default,
    saver: Saver<Long>? = null,
) = container(path, default, comments, convertor, configDefault, saver)

fun Config.float(
    path: String,
    default: Float,
    comments: List<String> = emptyList(),
    convertor: Converter<Float> = toFloat,
    configDefault: Float? = default,
    saver: Saver<Float>? = null,
) = container(path, default, comments, convertor, configDefault, saver)

fun Config.double(
    path: String,
    default: Double,
    comments: List<String> = emptyList(),
    convertor: Converter<Double> = toDouble,
    configDefault: Double? = default,
    saver: Saver<Double>? = null,
) = container(path, default, comments, convertor, configDefault, saver)

fun Config.boolean(
    path: String,
    default: Boolean,
    comments: List<String> = emptyList(),
    convertor: Converter<Boolean> = toBoolean,
    configDefault: Boolean? = default,
    saver: Saver<Boolean>? = null,
) = container(path, default, comments, convertor, configDefault, saver)

fun Config.char(
    path: String,
    default: Char,
    comments: List<String> = emptyList(),
    convertor: Converter<Char> = toChar,
    configDefault: Char? = default,
    saver: Saver<Char>? = null,
) = container(path, default, comments, convertor, configDefault, saver)

fun Config.location(
    path: String,
    default: Location,
    comments: List<String> = emptyList(),
    convertor: Converter<Location> = toLocation,
    configDefault: Location? = default,
    saver: Saver<Location>? = null,
) = container(path, default, comments, convertor, configDefault, saver)

fun Config.itemStack(
    path: String,
    default: ItemStack,
    comments: List<String> = emptyList(),
    convertor: Converter<ItemStack> = toItemStack,
    configDefault: ItemStack? = default,
    saver: Saver<ItemStack>? = null,
) = container(path, default, comments, convertor, configDefault, saver)

fun Config.vector(
    path: String,
    default: Vector,
    comments: List<String> = emptyList(),
    convertor: Converter<Vector> = toVector,
    configDefault: Vector? = default,
    saver: Saver<Vector>? = null,
) = container(path, default, comments, convertor, configDefault, saver)

fun Config.offlinePlayer(
    path: String,
    default: OfflinePlayer,
    comments: List<String> = emptyList(),
    convertor: Converter<OfflinePlayer> = toOfflinePlayer,
    configDefault: OfflinePlayer? = default,
    saver: Saver<OfflinePlayer>? = null,
) = container(path, default, comments, convertor, configDefault, saver)

fun Config.color(
    path: String,
    default: Color,
    comments: List<String> = emptyList(),
    convertor: Converter<Color> = toColor,
    configDefault: Color? = default,
    saver: Saver<Color>? = null,
) = container(path, default, comments, convertor, configDefault, saver)

fun Config.component(
    path: String,
    default: String,
    comments: List<String> = emptyList(),
    serializer: MiniMessage = TextFormatter.DEFAULT_SERIALIZER,
    vararg placeholders: TagResolver = emptyArray(),
    configDefault: String? = default,
) = ComponentContainer(
    this,
    path,
    default,
    configDefault,
    comments,
    serializer,
    *placeholders,
).also { containers.add(it) }

fun <T> Config.list(
    path: String,
    default: List<T>,
    comments: List<String> = emptyList(),
    convertor: Converter<T>,
    configDefault: List<T>? = default,
    saver: Saver<T>? = null,
) = container(path, default, comments, toList(convertor), configDefault, saveList(saver))

fun <T> Config.map(
    path: String,
    default: Map<String, T>,
    comments: List<String> = emptyList(),
    convertor: Converter<T>,
    configDefault: Map<String, T>? = default,
    saver: Saver<T>? = null,
) = container(path, default, comments, toMap(convertor), configDefault, saveMap(saver))