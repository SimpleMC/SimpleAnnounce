package org.simplemc.simpleannounce

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import org.simplemc.simpleannounce.config.DurationDeserializer
import org.simplemc.simpleannounce.config.DurationSerializer
import org.simplemc.simpleannounce.config.SimpleAnnounceConfig
import org.simplemc.simpleannounce.sender.BossBarSender
import org.simplemc.simpleannounce.sender.ChatSender
import org.simplemc.simpleannounce.sender.TitleSender
import java.io.File
import java.util.logging.Level
import kotlin.time.Duration

class SimpleAnnounce : JavaPlugin() {
    companion object {
        private const val CONFIG_FILE_NAME = "config.yml"
        private const val CONFIG_VERSION_KEY = "config-version"
        private const val CURRENT_CONFIG_VERSION = 1
        private const val RELOAD_COMMAND = "simpleannouncereload"
    }

    private val objectMapper: ObjectMapper = YAMLMapper.builder()
        .disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID)
        .build()
        .registerKotlinModule()
        .registerModule(
            SimpleModule()
                .addDeserializer(Duration::class.java, DurationDeserializer())
                .addSerializer(DurationSerializer()),
        )
        .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

    override fun onEnable() {
        saveDefaultConfig()
        loadAnnouncementsFromConfig()
        checkNotNull(getCommand(RELOAD_COMMAND)).setExecutor(::reloadCommand)
        logger.info { "${description.name} version ${description.version} enabled!" }
    }

    private fun reload() {
        reloadConfig()
        loadAnnouncementsFromConfig()
    }

    private fun loadAnnouncementsFromConfig() {
        server.scheduler.cancelTasks(this) // clear any old tasks
        migrateConfigToLatest()

        val announcementConfig = objectMapper.readValue<SimpleAnnounceConfig>(File(dataFolder, CONFIG_FILE_NAME))

        // load debug mode
        if (announcementConfig.debugMode) {
            logger.level = Level.FINER
        }

        // load auto-reload + create task to check again if necessary
        if (announcementConfig.autoReload?.isPositive() == true) {
            val reloadTicks = announcementConfig.autoReload.inTicks
            server.scheduler.runTaskTimerAsynchronously(this, ::reload, reloadTicks, reloadTicks)

            logger.fine { "Will reload config every ${announcementConfig.autoReload}" }
        }

        // set up senders
        announcementConfig.announcements.forEach { config ->
            when (config) {
                is SimpleAnnounceConfig.AnnouncementConfig.Chat -> ChatSender(this, config)
                is SimpleAnnounceConfig.AnnouncementConfig.Boss -> BossBarSender(this, config)
                is SimpleAnnounceConfig.AnnouncementConfig.Title -> TitleSender(this, config)
            }
        }
    }

    private fun migrateConfigToLatest() {
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

    private fun configVersionMigration() = when (config[CONFIG_VERSION_KEY, 0]) {
        CURRENT_CONFIG_VERSION -> false // this is the current version
        else -> {
            // invalid or no config version set, bring in defaults
            config.options().copyDefaults(true)
            config[CONFIG_VERSION_KEY] = CURRENT_CONFIG_VERSION
            true
        }
    }

    override fun onDisable() {
        server.scheduler.cancelTasks(this)
        logger.info { "${description.name} disabled." }
    }

    private fun reloadCommand(sender: CommandSender, command: Command, label: String, vararg args: String): Boolean {
        reload()
        logger.fine { "Config reloaded" }
        sender.sendMessage("SimpleAnnounce config reloaded.")
        return true
    }
}
