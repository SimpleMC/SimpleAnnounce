package org.simplemc.simpleannounce.message;

import org.simplemc.simpleannounce.SimpleAnnounce;

import java.util.List;
import java.util.Random;

/**
 * @author Taylor Becker
 */
public class MessageGroup extends Message
{
    private List<String> messages; // messages in this message group
    private int current; // index of current message
    private Random random; // random generator to use if messages are in random order

    /**
     * Create message
     *
     * @param plugin   plugin instance
     * @param label    message label from config
     * @param messages messages in this message group
     * @param delay    message's delay
     * @param random   whether the message group should run in random order
     */
    public MessageGroup(SimpleAnnounce plugin, String label, List<String> messages, int delay, boolean random)
    {
        super(plugin, label, "", delay);

        this.messages = messages;

        // init random if message is meant to be random
        if (random)
        {
            this.random = new Random();
        }

        logger.finer(String.format("Message group %s created with %d messages. Random order? %b", label, messages.size(), random));
    }

    @Override
    public String getMessage()
    {
        String message;

        if (random != null)
        {
            message = messages.get(random.nextInt(messages.size()));
        }
        else
        {
            // get next message
            message = messages.get(current++);

            // if we've reached the end of the list, reset
            if (current == messages.size())
            {
                current = 0;
            }
        }

        return message;
    }
}
