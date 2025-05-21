package org.simplemc.simpleannounce.message

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

private val logger = KotlinLogging.logger("SimpleAnnounce Announcement")

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

    init {
        messages.ifEmpty { throw IllegalArgumentException("Cannot create an announcement with no messages!") }
        logger.trace { "Announcement '$label' created with ${messages.size} messages." }
    }

    fun getMessage() = when {
        messages.size == 1 -> messages[0]
        random -> messages[Random.nextInt(messages.size)]
        else -> messages[current.getAndIncrement().also { if (it == messages.lastIndex) current.set(0) }]
    }
}
