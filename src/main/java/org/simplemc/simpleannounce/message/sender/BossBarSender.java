package org.simplemc.simpleannounce.message.sender;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import org.simplemc.simpleannounce.message.Message;

import java.util.Objects;

/**
 * Boss bar announcement message sender
 *
 * @author Taylor Becker
 */
public class BossBarSender extends MessageSender
{
    // boss bar config
    private BarColor color; // color of the boss bar
    private BarStyle style; // style of the boss bar

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
        this.color = color;
        this.style = style;
        this.animate = animate;
        this.reverse = reverse;
    }

    @Override
    public void run()
    {
        // create boss bar
        BossBar bossBar = Bukkit.createBossBar(message.getMessage(), color, style);

        // set initial progress
        bossBar.setProgress(reverse ? 1 : 0);

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
            animationTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () ->
                            bossBar.setProgress(bossBar.getProgress() + ((reverse ? -1 : 1) * (1.0 / holdTime)))
                    , 0, 1L);
        }
        else
        {
            animationTask = null;
        }

        // schedule boss bar to go away
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () ->
        {
            // reset progress
            bossBar.setProgress(reverse ? 1 : 0);

            // remove bar
            bossBar.removeAll();

            // cancel animation task
            if (animationTask != null)
            {
                Bukkit.getScheduler().cancelTask(animationTask.getTaskId());
            }
        }, holdTime);
    }
}
