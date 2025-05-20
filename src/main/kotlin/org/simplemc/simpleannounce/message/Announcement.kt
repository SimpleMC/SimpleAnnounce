package org.simplemc.simpleannounce.message

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

private val logger = KotlinLogging.logger {}

data class Announcement(
    internal val label: String,
    private val messages: List<String>,
    internal val permissionIncludes: List<String>,
    internal val permissionExcludes: List<String>,
    internal val delay: Int,
    internal val period: Int?,
    private val random: Boolean,
) {
    private val current = AtomicInteger()
    internal val isBroadcast = permissionIncludes.isEmpty() && permissionExcludes.isEmpty()

    init {
        messages.ifEmpty { throw IllegalArgumentException("Cannot create an announcement with no messages!") }
        logger.info { "Announcement '$label' created with ${messages.size} messages." }
    }

    fun getMessage() = when {
        messages.size == 1 -> messages[0]
        random -> messages[Random.nextInt(messages.size)]
        else -> messages[current.getAndIncrement().also { if (it == messages.lastIndex) current.set(0) }]
    }
}
