package com.evosysdev.bukkit.taylorjb.simpleannounce.message;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.Player;

import com.evosysdev.bukkit.taylorjb.simpleannounce.SimpleAnnounce;

public class Message implements Runnable
{
    private SimpleAnnounce plugin; // instance of our plugin
    
    private String message; // message to send
    private int delay; // delay in seconds for message
    private List<String> permissionIncludes, // permission nodes message receivers should include
            permissionExcludes; // permission nodes message receivers should exclude
    
    /**
     * Create delayed message
     * 
     * @param message
     *            message to send
     * @param delay
     *            message's delay
     */
    public Message(SimpleAnnounce plugin, String message, int delay)
    {
        this.plugin = plugin;
        this.message = message;
        this.delay = delay;

        permissionIncludes = new LinkedList<String>();
        permissionExcludes = new LinkedList<String>();
    }
    
    /**
     * Add a permission include for the message receivers
     * 
     * @param node
     *            node receivers should include
     */
    public void addPermissionIncl(String node)
    {
        permissionIncludes.add(node);
    }
    
    /**
     * Add permission includes for the message receivers
     * 
     * @param nodes
     *            nodes receivers should include
     */
    public void addPermissionsIncl(List<String> nodes)
    {
        permissionIncludes.addAll(nodes);
    }
    
    /**
     * Add a permission exclude for the message receivers
     * 
     * @param node
     *            node receivers should exclude
     */
    public void addPermissionExcl(String node)
    {
        permissionExcludes.add(node);
    }
    
    /**
     * Add permission exclude for the message receivers
     * 
     * @param nodes
     *            nodes receivers should exclude
     */
    public void addPermissionsExcl(List<String> nodes)
    {
        permissionExcludes.addAll(nodes);
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
        // no includes or excludes so we can just broadcast, yay!
        if (permissionIncludes.isEmpty() && permissionExcludes.isEmpty())
        {
            plugin.getServer().broadcastMessage(message);
        }
        // send message to all users that satisfy includes/excludes
        else
        {
            // ensure player satisfies reqs to get messages
            boolean satisfiesIncl, satisfiesExcl;
            
            for (Player player : plugin.getServer().getOnlinePlayers())
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
                        satisfiesExcl = false;
                        break; // no need to continue loop
                    }
                }
                
                // satisfies requirements, send message
                if (satisfiesIncl && satisfiesExcl)
                {
                    player.sendMessage(message);
                }
            }
        }
    }
}
