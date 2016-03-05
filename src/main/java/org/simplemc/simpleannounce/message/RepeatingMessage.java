package org.simplemc.simpleannounce.message;

import org.simplemc.simpleannounce.SimpleAnnounce;

/**
 * Repeating announcement message
 *
 * @author Taylor Becker
 */
public class RepeatingMessage extends Message
{
    private int period; // period of the repeating message

    /**
     * Create delayed, repeating message
     *
     * @param message message to send
     * @param delay   message's delay
     * @param period  repeating period
     */
    public RepeatingMessage(SimpleAnnounce plugin, String label, String message, int delay, int period)
    {
        super(plugin, label, message, delay);

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
