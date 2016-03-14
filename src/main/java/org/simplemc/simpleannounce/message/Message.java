package org.simplemc.simpleannounce.message;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.simplemc.simpleannounce.SimpleAnnounce;

/**
 * Announcement Message container
 *
 * @author Taylor Becker
 */
public class Message
{
    private int delay; // delay in seconds for message

    protected Logger logger; // our logger
    private String message, // message to send
            label; // unique message label
    private List<String> permissionIncludes, // permission nodes message receivers should include
            permissionExcludes; // permission nodes message receivers should exclude

    /**
     * Create message
     *
     * @param plugin  plugin instance
     * @param label   message label from config
     * @param message message to send
     * @param delay   message's delay
     */
    public Message(SimpleAnnounce plugin, String label, String message, int delay)
    {
        this.label = label;
        this.message = message;
        this.delay = delay;

        permissionIncludes = new LinkedList<>();
        permissionExcludes = new LinkedList<>();

        this.logger = plugin.getLogger();

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

    /**
     * Get the (string) message to send
     *
     * @return the message to send
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * Get unmodifiable list of permission includes
     *
     * @return unmodifiable list of permission includes
     */
    public List<String> getPermissionIncludes()
    {
        return Collections.unmodifiableList(permissionIncludes);
    }

    /**
     * Get unmodifiable list of permission includes
     *
     * @return unmodifiable list of permission includes
     */
    public List<String> getPermissionExcludes()
    {
        return Collections.unmodifiableList(permissionExcludes);
    }

    /**
     * Check if message is a broadcast(all players) message
     *
     * @return if message is a broadcast(all players) message
     */
    public boolean isBroadcaseMessage()
    {
        return permissionIncludes.isEmpty() && permissionExcludes.isEmpty();
    }
}
