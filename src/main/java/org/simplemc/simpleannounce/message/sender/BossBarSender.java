package org.simplemc.simpleannounce.message.sender;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.plugin.Plugin;

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

    private int holdTime; // amount of time message should stay on boss bar

    /**
     * Init boss bar message sender
     *
     * @param plugin  the plugin instance
     * @param message message to send
     */
    public BossBarSender(Plugin plugin, Message message, int holdTime, BarColor color, BarStyle style)
    {
        super(plugin, message);

        this.holdTime = holdTime;

        // create boss bar
        bossBar = Bukkit.createBossBar(message.getMessage(), color, style);
        bossBar.setProgress(1);
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

        // schedule boss bar to go away
        Runnable hideBossBar = message instanceof RepeatingMessage ? bossBar::hide : bossBar::removeAll;
        Bukkit.getScheduler().runTaskLater(plugin, hideBossBar, holdTime * 20L);
    }
}
