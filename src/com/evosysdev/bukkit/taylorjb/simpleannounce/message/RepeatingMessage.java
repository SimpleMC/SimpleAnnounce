package com.evosysdev.bukkit.taylorjb.simpleannounce.message;

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
    public RepeatingMessage(String message, int delay, int period)
    {
        super(message, delay);
        
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
