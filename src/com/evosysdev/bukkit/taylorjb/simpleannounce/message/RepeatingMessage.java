package com.evosysdev.bukkit.taylorjb.simpleannounce.message;

import com.evosysdev.bukkit.taylorjb.simpleannounce.SimpleAnnounce;

public class RepeatingMessage extends Message
{
    private int period; // period of the repeating message
    
    /**
     * Create delayed, repeating message
     * 
     * @param message
     *            message to send
     * @param delay
     *            message's delay
     * @param period
     *            repeating period
     */
    public RepeatingMessage(SimpleAnnounce plugin, String message, int delay, int period)
    {
        super(plugin, message, delay);
        
        this.period = period;
    }
    
    /**
     * @return repeating period
     */
    public int getPeriod()
    {
        return period;
    }
}
