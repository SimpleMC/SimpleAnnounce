package org.simplemc.simpleannounce.sender

import org.bukkit.plugin.Plugin
import org.simplemc.simpleannounce.config.SimpleAnnounceConfig.AnnouncementConfig.Title

class TitleSender(
    plugin: Plugin,
    announcement: Title,
) : AnnouncementSender<Title.TitleMessage, Title>(plugin, announcement) {
    override fun run() {
        val message = getNextMessage()
        val titleConfig = message.titleConfig ?: announcement.titleConfig

        send(message) {
            it.sendTitle(
                message.title,
                message.subtitle,
                titleConfig.fadeInTicks,
                titleConfig.stayTicks,
                titleConfig.fadeOutTicks,
            )
        }
    }
}
