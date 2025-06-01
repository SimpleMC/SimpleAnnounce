package org.simplemc.simpleannounce.sender

import io.github.oshai.kotlinlogging.KotlinLogging
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.simplemc.simpleannounce.config.SimpleAnnounceConfig
import org.simplemc.simpleannounce.inTicks
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

private val logger = KotlinLogging.logger("SimpleAnnounce AnnouncementSender")

abstract class AnnouncementSender<MessageType, ConfigType : SimpleAnnounceConfig.AnnouncementConfig<MessageType>>(
    internal val plugin: Plugin,
    internal val announcement: ConfigType,
) : Runnable {
    private val currentIndex = AtomicInteger()
    private val scheduler = Bukkit.getScheduler()

    init {
        check(announcement.messages.isNotEmpty()) { "Cannot create an announcement with no messages!" }
        scheduleTask()
        logger.trace {
            "${this::class.java.simpleName} created with ${announcement.messages.size} messages (first message '${announcement.messages.firstOrNull()}'}!"
        }
    }

    private fun scheduleTask() = announcement.repeat?.inTicks?.let { repeatTicks ->
        scheduler.runTaskTimer(plugin, this, announcement.delay.inTicks, repeatTicks)
    } ?: scheduler.runTaskLater(plugin, this, announcement.delay.inTicks)

    internal fun shouldSendTo(player: Player): Boolean {
        val hasAllRequiredPermissions = announcement.includesPermissions.none { !player.hasPermission(it) }
        val hasNoExcludedPermissions = announcement.excludesPermissions.none { player.hasPermission(it) }
        return hasAllRequiredPermissions && hasNoExcludedPermissions
    }

    internal fun getNextAnnouncement(): MessageType = when {
        announcement.messages.size == 1 -> announcement.messages[0]
        announcement.random -> announcement.messages[Random.nextInt(announcement.messages.size)]
        else -> {
            val nextIndex = currentIndex.getAndIncrement()
            if (nextIndex == announcement.messages.lastIndex) currentIndex.set(0)
            announcement.messages[nextIndex]
        }
    }
}
