package org.simplemc.simpleannounce.message.sender

import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import org.simplemc.simpleannounce.message.Announcement

class BossBarSender(
    plugin: Plugin,
    announcement: Announcement,
    holdTime: Int,
    private val color: BarColor,
    private val style: BarStyle,
    private val animate: Boolean,
    private val reverse: Boolean,
) : AnnouncementSender(plugin, announcement) {

    private val holdTicks = holdTime * 20L // convert from seconds to ticks

    override fun run() {
        // create the bar
        val bar = Bukkit.createBossBar(announcement.getMessage(), color, style)
        bar.progress = if (reverse) 1.0 else 0.0

        // show bar to players
        Bukkit.getOnlinePlayers().filterNotNull().filter(this::shouldSendTo).forEach(bar::addPlayer)
        bar.isVisible = true

        // set up animation
        val animation = if (animate) {
            Bukkit.getScheduler().runTaskTimer(plugin, updateBarProgress(bar), 0, 1L)
        } else {
            null
        }

        // schedule removal
        Bukkit.getScheduler().runTaskLater(plugin, removeBar(bar, animation), holdTicks)
    }

    private fun updateBarProgress(bar: BossBar): () -> Unit = {
        bar.progress = bar.progress + (if (reverse) -1 else 1) * (1.0 / holdTicks).coerceIn(0.0, 1.0)
    }

    private fun removeBar(bar: BossBar, animation: BukkitTask?): () -> Unit = {
        bar.removeAll()
        animation?.cancel()
    }
}
