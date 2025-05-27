package org.simplemc.simpleannounce.sender

import org.bukkit.Bukkit
import org.bukkit.boss.BossBar
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import org.simplemc.simpleannounce.config.SimpleAnnounceConfig.AnnouncementConfig.Boss

class BossBarSender(
    plugin: Plugin,
    announcement: Boss,
) : AnnouncementSender<Boss.BossBarMessage, Boss>(plugin, announcement) {
    override fun run() {
        val message = getNextAnnouncement()
        val barConfig = message.barConfig ?: announcement.barConfig

        // create the bar
        val bar = Bukkit.createBossBar(message.message, barConfig.color, barConfig.style)
        bar.progress = if (barConfig.reverseAnimation) 1.0 else 0.0

        // show bar to players
        Bukkit.getOnlinePlayers().filterNotNull().filter(this::shouldSendTo).forEach(bar::addPlayer)
        bar.isVisible = true

        // set up animation
        val animation = if (barConfig.animate) {
            Bukkit.getScheduler().runTaskTimer(plugin, updateBarProgress(bar, barConfig), 0, 1L)
        } else {
            null
        }

        // schedule removal
        Bukkit.getScheduler().runTaskLater(plugin, removeBar(bar, animation), barConfig.holdTicks)
    }

    private fun updateBarProgress(bar: BossBar, config: Boss.BarConfig): () -> Unit {
        val progressPerTick = 1.0 / config.holdTicks
        val animationDirection = if (config.reverseAnimation) -1.0 else 1.0
        return {
            val newProgress = bar.progress + (animationDirection * progressPerTick)
            bar.progress = newProgress.coerceIn(0.0, 1.0)
        }
    }

    private fun removeBar(bar: BossBar, animation: BukkitTask?): () -> Unit = {
        bar.removeAll()
        animation?.cancel()
    }
}
