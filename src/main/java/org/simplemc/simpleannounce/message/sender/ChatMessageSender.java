package org.simplemc.simpleannounce.message.sender;

import org.bukkit.plugin.Plugin;

import org.simplemc.simpleannounce.message.Message;

/**
 * Chat box Message sender
 *
 * @author Taylor Becker
 */
public class ChatMessageSender extends MessageSender
{
    /**
     * Init chat sender
     *
     * @param plugin  the plugin instance
     * @param message message to send
     */
    public ChatMessageSender(Plugin plugin, Message message)
    {
        super(plugin, message);
    }

    @Override
    /**
     * Send the message!
     */
    public void run()
    {
        // if no one is online, no sense in running the message
        if (server.getOnlinePlayers().size() == 0)
        {
            logger.fine("Skipping message...no players online.");
        }
        // no includes or excludes so we can just broadcast, yay!
        else if (message.isBroadcaseMessage())
        {
            server.broadcastMessage(message.getMessage());
            logger.finer("Broadcasting message to everyone");
        }
        // only one permission includes and no excludes, can still use broadcast
        else if (message.getPermissionIncludes().size() == 1 &&
                message.getPermissionExcludes().isEmpty())
        {
            server.broadcast(message.getMessage(), message.getPermissionIncludes().get(0));
            logger.finer("Broadcasting message to everyone");
        }
        // send message to all users that satisfy includes/excludes
        else
        {
            // go through all players online
            server.getOnlinePlayers().stream().filter(player -> player != null).forEach(player ->
            {
                // ensure player is neither missing any excludes
                boolean skipPlayer =
                        message.getPermissionIncludes().stream().anyMatch(perm -> !player.hasPermission(perm)) ||
                                message.getPermissionExcludes().stream().anyMatch(player::hasPermission);

                // send message if player has appropriate permissions
                if (!skipPlayer)
                {
                    logger.finest("Sending to " + player.getDisplayName());
                    player.sendMessage(message.getMessage());
                }
                else
                {
                    logger.finer(player.getDisplayName() + " lacks permission to receive this message, not sending.");
                }
            });
        }
    }
}
