package com.evosysdev.bukkit.taylorjb.simpleannounce;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import com.evosysdev.bukkit.taylorjb.simpleannounce.message.Message;
import com.evosysdev.bukkit.taylorjb.simpleannounce.message.RepeatingMessage;

public class SimpleAnnounce extends JavaPlugin
{
    private List<Message> messages; // the messages we are sending
    
    /**
     * Read in config file and set up scheduled tasks
     */
    public void onEnable()
    {
        messages = new LinkedList<Message>();
        
        loadConfig(); // load messages from config
        startMessages(); // get the messages going!
        
        Logger.getLogger("Minecraft").info(getDescription().getName() + " version " + getDescription().getVersion() + " enabled!");
    }
    
    /**
     * Read/load config
     */
    private void loadConfig()
    {
        Set<String> messageNodes; // message nodes
        ConfigurationSection messageSection; // configuration section of the messages
        try
        {
            messageSection = getConfig().getConfigurationSection("messages");
            messageNodes = messageSection.getKeys(false);
        }
        catch (NullPointerException npe)
        { // config file couldn't be read
            saveDefaultConfig();
            
            // now load the messages
            messageSection = getConfig().getConfigurationSection("messages");
            messageNodes = messageSection.getKeys(false);
        }
        
        addMessages(messageNodes, messageSection);
    }
    
    /**
     * Add messages from config to message list
     * 
     * @param nodes
     *            message nodes in config
     * @param section
     *            currnet config section
     */
    private void addMessages(Set<String> nodes, ConfigurationSection section)
    {
        // go through all message nodes and create tasks for them
        ConfigurationSection currentSec;
        Message current;
        String message;
        int delay;
        int repeat;
        
        // go through all message nodes and get data from it
        for (String messageNode : nodes)
        {
            // set current section
            currentSec = section.getConfigurationSection(messageNode);
            
            // get message info from nodes
            message = currentSec.getString("message");
            delay = currentSec.getInt("delay", 0);
            
            // repeating message
            if (currentSec.contains("repeat"))
            {
                repeat = currentSec.getInt("repeat"); // repeat specific
                
                // create repeating message
                current = new RepeatingMessage(message, delay, repeat);
            }
            else
            {
                // create message
                current = new Message(message, delay);
            }
            
            // let's add permission includes for the message now
            if (currentSec.contains("includesperms"))
            {
                current.addPermissionsIncl(currentSec.getList("includesperms"));
            }
            
            // let's add permission includes for the message now
            if (currentSec.contains("excludesperms"))
            {
                current.addPermissionsExcl(currentSec.getList("excludesperms"));
            }
            
            // and finally, add the message to our list
            messages.add(current);
        }
    }
    
    /**
     * Kick off/schedule messages
     */
    private void startMessages()
    {
        for (Message message : messages)
        {
            if (message instanceof RepeatingMessage)
            {
                getServer().getScheduler().scheduleAsyncRepeatingTask(this, message, message.getDelay() * 20L, ((RepeatingMessage) message).getPeriod() * 20L);
            }
            else
            {
                getServer().getScheduler().scheduleSyncDelayedTask(this, message, message.getDelay() * 20L);
            }
        }
    }
    
    /**
     * plugin disabled
     */
    public void onDisable()
    {
        Logger.getLogger("Minecraft").info("SimpleAnnounce disabled.");
    }
}
