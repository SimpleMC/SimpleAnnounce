package org.simplemc.simpleannounce.message;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import org.simplemc.simpleannounce.SimpleAnnounce;

public class Message implements Runnable
{
    private int delay; // delay in seconds for message

    private Logger logger; // our logger
    private Server server; // server running the plugin
    private String message, // message to send
            label; // unique message label
    private List<String> permissionIncludes, // permission nodes message receivers should include
            permissionExcludes; // permission nodes message receivers should exclude

    /**
     * Create delayed message
     *
     * @param message message to send
     * @param delay   message's delay
     */
    public Message(SimpleAnnounce plugin, String label, String message, int delay)
    {
        this.label = label;
        this.message = message;
        this.delay = delay;

        permissionIncludes = new LinkedList<String>();
        permissionExcludes = new LinkedList<String>();

        this.logger = plugin.getLogger();
        this.server = plugin.getServer();
        logger.finer("Message " + label + " created on delay " + delay);
    }

    /**
     * Add a permission include for the message receivers
     *
     * @param node node receivers should include
     */
    public void addPermissionIncl(String node)
    {
        permissionIncludes.add(node);
        logger.fine("PermissionIncludes: " + permissionIncludes.toString());
    }

    /**
     * Add permission includes for the message receivers
     *
     * @param nodes nodes receivers should include
     */
    public void addPermissionsIncl(List<String> nodes)
    {
        permissionIncludes.addAll(nodes);
        logger.fine("PermissionIncludes: " + permissionIncludes.toString());
    }

    /**
     * Add a permission exclude for the message receivers
     *
     * @param node node receivers should exclude
     */
    public void addPermissionExcl(String node)
    {
        permissionExcludes.add(node);
        logger.fine("PermissionExcludes: " + permissionExcludes.toString());
    }

    /**
     * Add permission exclude for the message receivers
     *
     * @param nodes nodes receivers should exclude
     */
    public void addPermissionsExcl(List<String> nodes)
    {
        permissionExcludes.addAll(nodes);
        logger.fine("PermissionExcludes: " + permissionExcludes.toString());
    }

    /**
     * @return message's label
     */
    public String getLabel()
    {
        return label;
    }

    /**
     * @return message's delay
     */
    public int getDelay()
    {
        return delay;
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
            return;
        }

        logger.fine("Running message " + label);

        // no includes or excludes so we can just broadcast, yay!
        if (permissionIncludes.isEmpty() && permissionExcludes.isEmpty())
        {
            server.broadcastMessage(message);
            logger.finer("Broadcasting message to everyone");
        }
        // send message to all users that satisfy includes/excludes
        else
        {
            // ensure player satisfies reqs to get messages
            boolean satisfiesIncl, satisfiesExcl;

            for (Player player : server.getOnlinePlayers())
            {
                // safe-guard
                if (player == null)
                    continue;

                // reset to true until proven false
                satisfiesIncl = true;
                satisfiesExcl = true;

                // go through includes, ensure user has all of the permission nodes
                for (String include : permissionIncludes)
                {
                    // does not have includes permission!
                    if (!player.hasPermission(include))
                    {
                        logger.finer("Not sending to " + player.getDisplayName() + ": Does not satisfy " + include + " permission include");
                        satisfiesIncl = false;
                        break; // no need to continue loop
                    }
                }

                // go through includes, ensure user has all of the permission nodes
                for (String exclude : permissionExcludes)
                {
                    // has excludes permission!
                    if (player.hasPermission(exclude))
                    {
                        logger.finer("Not sending to " + player.getDisplayName() + ": Does not satisfy " + exclude + " permission exclude");
                        satisfiesExcl = false;
                        break; // no need to continue loop
                    }
                }

                // satisfies requirements, send message
                if (satisfiesIncl && satisfiesExcl)
                {
                    logger.finest("Sending to " + player.getDisplayName());
                    player.sendMessage(message);
                }
            }
        }
    }
}
