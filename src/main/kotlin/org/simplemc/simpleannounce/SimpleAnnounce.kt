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
        saveDefaultConfig()
        loadConfig()
        checkNotNull(getCommand("simpleannouncereload")).setExecutor(::reloadCommand)
        logger.info { "${description.name} version ${description.version} enabled!" }
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
        val reloadTime = config.getLong("auto-reloadconfig", 0L)
        if (reloadTime > 0L) {
            val reloadTicks = reloadTime * 60 * 20
            server.scheduler.runTaskTimerAsynchronously(this, ::reload, reloadTicks, reloadTicks)

            logger.fine { "Will reload config every $reloadTime minutes" }
        }

        config.getConfigurationSection("announcements")?.getKeys(false)?.let { keys ->
            keys.mapNotNull { config.getConfigurationSection("announcements.$it") }
                .forEach {
                    logger.fine { "Loading announcement '${it.name}'" }
                    loadAnnouncementSender(loadAnnouncement(it), it)
                }
        }
    }

    private fun updateConfig() {
        var updated = false

        // migrate each config version until we're at the latest (no more updates)
        while (configVersionMigration()) {
            updated = true
        }

        if (updated) {
            config.options().copyDefaultHeader()
            saveConfig()
            logger.info { "${description.name} config updated, please check the settings!" }
        }
    }

    private fun configVersionMigration() = when (config["config-version", 0]) {
        1 -> false // this is the current version
        else -> {
            // invalid or no config version set, bring in defaults
            config.options().copyDefaults(true)
            config["config-version"] = 1
            true
        }
    }

    private fun loadAnnouncement(announcementConfig: ConfigurationSection) = Announcement(
        label = announcementConfig.name,
        messages = announcementConfig.getStringList("messages").map { ChatColor.translateAlternateColorCodes('&', it) },
        permissionIncludes = announcementConfig.getStringListOrEmpty("includesperms"),
        permissionExcludes = announcementConfig.getStringListOrEmpty("excludesperms"),
        delay = announcementConfig.getInt("delay", 0),
        period = if (announcementConfig.isSet("repeat")) announcementConfig.getInt("repeat") else null,
        random = announcementConfig.getBoolean("random", false),
    )

    private fun loadAnnouncementSender(announcement: Announcement, announcementConfig: ConfigurationSection) =
        when (announcementConfig.getString("sender", "chat")) {
            "boss", "bossbar" -> BossBarSender(
                plugin = this,
                announcement = announcement,
                holdTime = announcementConfig.getInt("bar.hold", 5),
                color = BarColor.valueOf(checkNotNull(announcementConfig.getString("bar.color", "PURPLE")).uppercase()),
                style = BarStyle.valueOf(checkNotNull(announcementConfig.getString("bar.style", "SOLID")).uppercase()),
                animate = announcementConfig.getBoolean("bar.animate.enable", true),
                reverse = announcementConfig.getBoolean("bar.animate.reverse", false),
            )

            "chat" -> ChatSender(this, announcement)
            else ->
                throw IllegalArgumentException("Invalid message sender configured on message '${announcement.label}'!")
        }

    override fun onDisable() {
        server.scheduler.cancelTasks(this)
        logger.info { "${description.name} disabled." }
    }

    private fun reloadCommand(sender: CommandSender, command: Command, label: String, vararg args: String): Boolean {
        reloadConfig()
        loadConfig()

        logger.fine { "Config reloaded" }
        sender.sendMessage("SimpleAnnounce config reloaded.")

        return true
    }
}
