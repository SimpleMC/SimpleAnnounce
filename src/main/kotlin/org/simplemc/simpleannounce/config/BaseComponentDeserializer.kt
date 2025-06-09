package org.simplemc.simpleannounce.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer

class BaseComponentDeserializer : JsonDeserializer<BaseComponent>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BaseComponent? {
        val json = p.readValueAsTree<JsonNode>().toString()
        return try {
            ComponentSerializer.deserialize(json)
        } catch (_: IllegalArgumentException) {
            throw ctxt.weirdKeyException(
                BaseComponent::class.java,
                json,
                "Couldn't parse TextComponent",
            )
        }
    }
}
