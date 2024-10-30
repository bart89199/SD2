package ru.batr.sD2

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import ru.batr.sD2.config.MainConfig

class TextFormatter {

    companion object {
        var DEFAULT_SERIALIZER = MiniMessage.builder().tags(
            TagResolver.builder()
                .resolver(TagResolver.standard())
                .resolver(Placeholder.component("prefix", MainConfig.prefix))
                .resolver(Placeholder.component("discord", MainConfig.discord))
                .build()
        ).build()
            private set

        fun reload() {
            DEFAULT_SERIALIZER = MiniMessage.builder().tags(
                TagResolver.builder()
                    .resolver(TagResolver.standard())
                    .resolver(Placeholder.component("prefix", MainConfig.prefix))
                    .resolver(Placeholder.component("discord", MainConfig.discord))
                    .build()
            ).build()
        }

        fun format(
            input: String,
            vararg placeholders: TagResolver = emptyArray(),
            serializer: MiniMessage = DEFAULT_SERIALIZER
        ) = serializer.deserialize(input, *placeholders)

    }
}