package org.simplemc.simpleannounce.message.sender

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.simplemc.simpleannounce.message.Announcement

class ChatSender(plugin: Plugin, announcement: Announcement) : AnnouncementSender(plugin, announcement) {
    override fun run() {
        Bukkit.getOnlinePlayers()
            .filterNotNull()
            .filter(this::shouldSendTo)
            .forEach { it.sendMessage(announcement.getMessage()) }
    }
}
