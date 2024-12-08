package ru.batr.sD2.config

import org.bukkit.configuration.file.YamlConfiguration
import ru.batr.sD2.SD2
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.logging.Level
import kotlin.reflect.KProperty

typealias Converter<T> = (Any?) -> T?

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
            container.setDefault(defaultConfig)
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
    val comments: List<String> = emptyList(),
) : Container<T> {
    abstract fun get(): T
    override fun getValue(thisRef: Any, property: KProperty<*>): T = get()
    abstract fun setDefault(config: YamlConfiguration)
}

abstract class AbstractEditableConfigContainer<T>(
    config: Config,
    path: String,
    comments: List<String> = emptyList(),
) : AbstractConfigContainer<T>(config, path, comments) {
    protected open fun setValue(value: T?, config0: YamlConfiguration = config.config) {
        config0.set(path, value)
    }
    operator fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
        setValue(value)
        config.save()
    }
}

abstract class AbstractConfigContainerCCT<T, C>(
    config: Config,
    path: String,
    comments: List<String> = emptyList(),
) : AbstractConfigContainer<T>(config, path, comments) {

    protected open fun setValue(value: C?, config0: YamlConfiguration = config.config) {
        config0.set(path, value)
    }
    fun set(value: C) {
        setValue(value)
        config.save()
    }
}

class ConfigContainer<T>(
    config: Config,
    path: String,
    val default: () -> T,
    val configDefault: () -> T? = default,
    comments: List<String> = emptyList(),
    val convertor: Converter<T>,
) : AbstractEditableConfigContainer<T>(config, path, comments) {
    override fun get() = if (config.isLoaded()) convertor(config.config.get(path)) ?: default() else default()
    override fun setDefault(config: YamlConfiguration) = setValue(configDefault(), config)
}

class ConfigContainerCCT<T, C>(
    config: Config,
    path: String,
    val convertor: Converter<T>,
    val default: () -> T,
    val configDefault: () -> C?,
    comments: List<String> = emptyList(),
) : AbstractConfigContainerCCT<T, C>(config, path, comments) {

    override fun get(): T = if (config.isLoaded()) convertor(config.config.get(path)) ?: default() else default()
    override fun setDefault(config: YamlConfiguration) = setValue(configDefault(), config)

}

//class ComponentContainer(
//    config: Config,
//    path: String,
//    val default: String,
//    val configDefaultString: String? = defaultString,
//    comments: List<String> = emptyList(),
//    val serializer: MiniMessage = TextFormatter.DEFAULT_SERIALIZER,
//    vararg val placeholders: TagResolver = emptyArray(),
//) : AbstractConfigContainer<Component>(
//    config, path,
//    serializer.deserialize(defaultString, *placeholders),
//    configDefaultString?.let { serializer.deserialize(it, *placeholders) },
//    comments
//) {
//    override fun convert(input: Any?) = serializer.deserialize(toString(input) ?: defaultString, *placeholders)
//
//    override fun save(value: Component) {
//        config.config.set(path, serializer.serialize(value))
//    }
//
//    override fun saveDefault(config: YamlConfiguration) {
//        config.set(path, configDefaultString)
//    }
//
//    fun add(vararg placeholders: TagResolver) = ComponentContainer(
//        config,
//        path,
//        defaultString,
//        configDefaultString,
//        comments,
//        serializer,
//        TagResolver.builder().resolvers(*this.placeholders).resolvers(*placeholders).build()
//    )
//}

//abstract class AbstractListContainer<T>(
//    config: Config,
//    path: String,
//    default: List<T> = emptyList(),
//    configDefault: List<T> = default,
//    comments: List<String> = emptyList(),
//) : AbstractConfigContainer<List<T>>(config, path, default, configDefault, comments) {
//    abstract fun convertT(input: Any?): T?
//    override fun convert(input: Any?): List<T> {
//        if (input is List<*>) {
//            val result = ArrayList<T>()
//            for (item in input) {
//                convertT(item)?.let { result.add(it) }
//            }
//            return result
//        }
//        return default
//    }
//}
//
//class ListContainer<T>(
//    config: Config,
//    path: String,
//    default: List<T> = emptyList(),
//    configDefault: List<T> = default,
//    comments: List<String> = emptyList(),
//    val convertor: Converter<T>,
//    val saveConvertor: SaveConvertor<T>? = null,
//) : AbstractListContainer<T>(config, path, default, configDefault, comments) {
//    override fun convertT(input: Any?) = convertor(input)
//    override fun save(value: List<T>) {
//        if (saveConvertor == null) return super.save(value)
//        saveList(saveConvertor)(config.config, path, value)
//    }
//
//    override fun saveDefault(config: YamlConfiguration) {
//        if (saveConvertor == null) {
//            super.saveDefault(config)
//            return
//        }
//        saveList(saveConvertor)(config, path, configDefault)
//    }
//}