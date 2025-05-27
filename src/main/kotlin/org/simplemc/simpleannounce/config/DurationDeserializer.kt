package org.simplemc.simpleannounce.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import kotlin.time.Duration

class DurationDeserializer : JsonDeserializer<Duration>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Duration? {
        val text = p.text.trim()
        return try {
            Duration.Companion.parse(text)
        } catch (_: IllegalArgumentException) {
            throw ctxt.weirdStringException(
                text,
                Duration::class.java,
                "Cannot parse duration: '$text'. Expected format like '10s', '5m', '1h', or ISO-8601 duration.",
            )
        }
    }
}
