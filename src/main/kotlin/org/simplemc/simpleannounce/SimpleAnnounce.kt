package org.simplemc.simpleannounce

import org.bukkit.ChatColor
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.plugin.java.JavaPlugin
import org.simplemc.simpleannounce.message.Announcement
import org.simplemc.simpleannounce.message.sender.BossBarSender
import org.simplemc.simpleannounce.message.sender.ChatSender
import java.util.logging.Level

class SimpleAnnounce : JavaPlugin() {
    override fun onEnable() {
        loadConfig()
        logger.info("${description.name} version ${description.version} enabled!")
    }

    private fun reload() {
        reloadConfig()
        loadConfig()
    }

    private fun loadConfig() {
        // cancel any old tasks
        server.scheduler.cancelTasks(this)

        updateConfig()

        // load debug mode
        if (config.getBoolean("debug-mode", false)) {
            logger.level = Level.FINER
        }

        // load auto-reload + create task to check again if necessary
        val reloadTime = config.getInt("auto-reloadconfig", 0)
        if (reloadTime != 0) {
            server.scheduler.runTaskLaterAsynchronously(this, ::reload, reloadTime.toLong() * 60 * 20L)

            logger.fine("Will reload config in $reloadTime minutes")
        }

        config.getConfigurationSection("announcements")?.getKeys(false)?.let { keys ->
            keys.mapNotNull { config.getConfigurationSection("announcements.$it") }
                .forEach {
                    logger.fine("Loading announcement '${it.name}'")
                    loadAnnouncementSender(loadAnnouncement(it), it)
                }
        }
    }

    private fun updateConfig() {
        var updated = false

        // migrate each config version until we're at the latest (no more updates)
        while (configVersionMigration()) { updated = true }

        if (updated) {
            config.options().copyHeader(true)
            saveConfig()
            logger.info("${description.name} config updated, please check the settings!")
        }
    }

    private fun configVersionMigration() = when (config["config-version", 0]) {
        1 -> false // this is the current version
        else -> {
            // invalid or no config version set, set it
            config["config-version"] = 1

            // update each announcement's config
            config.getConfigurationSection("messages")?.getKeys(false)?.forEach { label ->
                // update single `message` values to a list of one message
                config["messages.$label.message", null]?.let {
                    config["messages.$label.messages"] = listOf(it)
                }
            }

            // update root messages section to 'announcements'
            config["announcements"] = config.get("messages")
            config["messages"] = null
            true
        }
    }

    private fun loadAnnouncement(announcementConfig: ConfigurationSection) = Announcement(
        plugin = this,
        label = announcementConfig.name,
        messages = announcementConfig.getStringList("messages").map { ChatColor.translateAlternateColorCodes('&', it) },
        permissionIncludes = announcementConfig.getStringListOrEmpty("includesperms"),
        permissionExcludes = announcementConfig.getStringListOrEmpty("excludesperms"),
        delay = announcementConfig.getInt("delay", 0),
        period = if (announcementConfig.isSet("repeat")) announcementConfig.getInt("repeat") else null,
        random = announcementConfig.getBoolean("random", false)
    )

    private fun loadAnnouncementSender(announcement: Announcement, announcementConfig: ConfigurationSection) =
        when (announcementConfig.getString("sender", "chat")) {
            "boss", "bossbar" -> BossBarSender(
                plugin = this,
                announcement = announcement,
                holdTime = announcementConfig.getInt("bar.hold", 5),
                color = BarColor.valueOf(checkNotNull(announcementConfig.getString("bar.color", "PURPLE")).toUpperCase()),
                style = BarStyle.valueOf(checkNotNull(announcementConfig.getString("bar.style", "SOLID")).toUpperCase()),
                animate = announcementConfig.getBoolean("bar.animate.enable", true),
                reverse = announcementConfig.getBoolean("bar.animate.reverse", false)
            )
            "chat" -> ChatSender(this, announcement)
            else ->
                throw IllegalArgumentException("Invalid message sender configured on message '${announcement.label}'!")
        }

    override fun onDisable() {
        server.scheduler.cancelTasks(this)
        logger.info("${description.name} disabled.")
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        vararg args: String
    ): Boolean {
        if (sender.hasPermission("simpleannounce.reload")) {
            reloadConfig()
            loadConfig()

            logger.fine("Config reloaded")
            sender.sendMessage("SimpleAnnounce config reloaded.")
        } else {
            sender.sendMessage("${ChatColor.RED}You do not have permission to do that!")
        }

        return true
    }
}
