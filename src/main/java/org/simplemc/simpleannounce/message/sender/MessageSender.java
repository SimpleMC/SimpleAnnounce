package org.simplemc.simpleannounce.message.sender;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import org.simplemc.simpleannounce.message.Message;

import java.util.logging.Logger;

/**
 * Runnable Message sender
 *
 * @author Taylor Becker
 */
public abstract class MessageSender implements Runnable
{
    protected Plugin plugin; // plugin instance
    protected Logger logger; // our logger
    protected Server server; // server running the plugin
    protected Message message; // the message to send

    /**
     * Init sender
     *
     * @param plugin  the plugin instance
     * @param message message to send
     */
    public MessageSender(Plugin plugin, Message message)
    {
        this.plugin = plugin;
        this.message = message;

        this.logger = plugin.getLogger();
        this.server = plugin.getServer();

        // log creation
        logger.finer(String.format("%s for message '%s' created!", getClass().getSimpleName(), message.getLabel()));
    }

    /**
     * Determine if message should be sent to given player
     *
     * @param player player to check for permission to receive message
     *
     * @return if message should be sent to given player
     */
    protected boolean sendToPlayer(Player player)
    {
        return !(message.getPermissionIncludes().stream().anyMatch(perm -> !player.hasPermission(perm)) ||
                message.getPermissionExcludes().stream().anyMatch(player::hasPermission));
    }
}
