package org.simplemc.simpleannounce.message.sender

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.simplemc.simpleannounce.message.Announcement

abstract class AnnouncementSender(internal val plugin: Plugin, internal val announcement: Announcement) : Runnable {
    init {
        scheduleTask()
        plugin.logger.finer("${this::class.java.simpleName} for announcement '${announcement.label}' created!")
    }

    private fun scheduleTask() = announcement.period?.let {
        Bukkit.getScheduler().runTaskTimer(plugin, this, announcement.delay * 20L, announcement.period * 20L)
    } ?: Bukkit.getScheduler().runTaskLater(plugin, this, announcement.delay * 20L)

    internal fun shouldSendTo(player: Player) =
        !(announcement.permissionIncludes.any { !player.hasPermission(it) } ||
                announcement.permissionExcludes.any(player::hasPermission))
}
