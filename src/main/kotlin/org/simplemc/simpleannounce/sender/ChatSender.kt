package org.simplemc.simpleannounce.sender

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.simplemc.simpleannounce.config.SimpleAnnounceConfig.AnnouncementConfig.Chat

class ChatSender(
    plugin: Plugin,
    announcement: Chat,
) : AnnouncementSender<String, Chat>(plugin, announcement) {
    override fun run() {
        Bukkit.getOnlinePlayers()
            .filterNotNull()
            .filter(this::shouldSendTo)
            .forEach { it.sendMessage(getNextAnnouncement()) }
    }
}
