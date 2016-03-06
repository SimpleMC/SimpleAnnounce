package org.simplemc.simpleannounce.message.sender;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import org.simplemc.simpleannounce.message.Message;
import org.simplemc.simpleannounce.message.RepeatingMessage;

import java.util.Objects;

/**
 * Boss bar announcement message sender
 *
 * @author Taylor Becker
 */
public class BossBarSender extends MessageSender
{
    private BossBar bossBar; // the boss bar

    private long holdTime; // amount of time message should stay on boss bar
    private boolean animate, // if the bar should animate or not(displaying remaining hold time)
            reverse; // if the animation should be moving in reverse

    /**
     * Init boss bar message sender
     *
     * @param plugin   the plugin instance
     * @param message  message to send
     * @param holdTime time (in seconds) to keep boss bar on screen
     * @param color    color of the boss bar
     * @param style    style of the boss bar
     * @param animate  if the bar should animate or not(displaying remaining hold time)
     * @param reverse  if the animation should be moving in reverse
     */
    public BossBarSender(Plugin plugin, Message message, int holdTime, BarColor color, BarStyle style, boolean animate, boolean reverse)
    {
        super(plugin, message);

        this.holdTime = holdTime * 20L;
        this.animate = animate;
        this.reverse = reverse;

        // create boss bar
        bossBar = Bukkit.createBossBar(message.getMessage(), color, style);

        // set initial progress
        bossBar.setProgress(reverse ? 1 : 0);
    }

    @Override
    public void run()
    {
        // add all players that should receive the message to the boss bar
        Bukkit.getOnlinePlayers().stream()
                .filter(Objects::nonNull)
                .filter(this::sendToPlayer)
                .forEach(bossBar::addPlayer);

        // show the boss bar
        bossBar.show();

        // animate
        final BukkitTask animationTask;
        if (animate)
        {
            animationTask = Bukkit.getScheduler().runTaskTimer(plugin, () ->
                            bossBar.setProgress(bossBar.getProgress() + ((reverse ? -1 : 1) * (1.0 / holdTime)))
                    , 0, 1L);
        }
        else
        {
            animationTask = null;
        }

        // schedule boss bar to go away
        Runnable hideBossBar = message instanceof RepeatingMessage ? bossBar::hide : bossBar::removeAll;
        Bukkit.getScheduler().runTaskLater(plugin, () ->
        {
            // reset progress
            bossBar.setProgress(reverse ? 1 : 0);

            // hide bar
            hideBossBar.run();

            // cancel animation task
            if (animationTask != null)
            {
                Bukkit.getScheduler().cancelTask(animationTask.getTaskId());
            }
        }, holdTime);
    }
}
