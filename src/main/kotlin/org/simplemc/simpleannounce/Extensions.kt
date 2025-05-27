package org.simplemc.simpleannounce

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfigurationOptions
import org.bukkit.configuration.file.YamlConfiguration
import kotlin.time.Duration

fun String.oneOf(ignoreCase: Boolean, vararg values: String): Boolean =
    values.any { it.equals(this, ignoreCase = ignoreCase) }

fun String.oneOf(vararg values: String): Boolean = oneOf(false, *values)

fun String.oneOfIgnoreCase(vararg values: String): Boolean = oneOf(true, *values)

val Duration.inTicks: Long get() = this.inWholeMilliseconds / 50

/**
 * Get a string list if and only if it exists in the config (ignoring defaults).
 * If the path does not exist, returns empty list
 */
fun ConfigurationSection.getStringListOrEmpty(path: String) = if (isSet(path)) {
    getStringList(path)
} else {
    emptyList()
}

/**
 * Set config header from a default `config.yml` resource' header
 */
fun FileConfigurationOptions.copyDefaultHeader(): FileConfigurationOptions {
    val defaultHeader = YamlConfiguration.loadConfiguration(
        checkNotNull(object {}.javaClass.classLoader.getResourceAsStream("config.yml")).reader(),
    ).options().header
    return setHeader(defaultHeader)
}
