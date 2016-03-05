package org.simplemc.simpleannounce.message.sender;

import org.bukkit.Server;
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
        this.message = message;

        this.logger = plugin.getLogger();
        this.server = plugin.getServer();

        // log creation
        logger.finer(String.format("%s for message '%s' created!", getClass().getSimpleName(), message.getLabel()));
    }
}
