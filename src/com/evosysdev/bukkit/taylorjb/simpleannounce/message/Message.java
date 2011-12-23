package com.evosysdev.bukkit.taylorjb.simpleannounce.message;

import java.util.LinkedList;
import java.util.List;

public class Message implements Runnable
{
    private String message; // message to send
    private int delay; // delay in seconds for message
    private List<String> permissionIncludes; // permission nodes message receivers should include
    
    /**
     * Create delayed message
     * 
     * @param message
     *            message to send
     * @param delay
     *            message's delay
     */
    public Message(String message, int delay)
    {
        this.message = message;
        this.delay = delay;
        
        permissionIncludes = new LinkedList<String>();
    }
    
    /**
     * Add a permission include for the message receivers
     * 
     * @param node
     *            node receivers should include
     */
    public void addPermission(String node)
    {
        permissionIncludes.add(node);
    }
    
    /**
     * Add permission includes for the message receivers
     * 
     * @param nodes
     *            nodes receivers should include
     */
    public void addPermissions(List<String> nodes)
    {
        permissionIncludes.addAll(nodes);
    }
    
    /**
     * @return message's delay
     */
    public int getDelay()
    {
        return delay;
    }
    
    @Override
    public void run()
    {
        // TODO
    }
}
