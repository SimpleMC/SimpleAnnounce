package org.simplemc.simpleannounce.sender

import org.bukkit.plugin.Plugin
import org.simplemc.simpleannounce.config.SimpleAnnounceConfig.AnnouncementConfig.Chat

class ChatSender(
    plugin: Plugin,
    announcement: Chat,
) : AnnouncementSender<Chat.ChatMessage, Chat>(plugin, announcement) {
    override fun run() {
        val message = getNextMessage()
        send(message) { it.sendMessage(message.message) }
    }
}
