package org.simplemc.simpleannounce

import org.bukkit.configuration.ConfigurationSection

fun String.oneOf(ignoreCase: Boolean, vararg values: String): Boolean =
    values.any { it.equals(this, ignoreCase = ignoreCase) }

fun String.oneOf(vararg values: String): Boolean = oneOf(false, *values)

fun String.oneOfIgnoreCase(vararg values: String): Boolean = oneOf(true, *values)

/**
 * Get a string list if and only if it exists in the config (ignoring defaults).
 * If the path does not exist, returns empty list
 */
fun ConfigurationSection.getStringListOrEmpty(path: String) = if (isSet(path)) {
    getStringList(path)
} else {
    emptyList()
}
