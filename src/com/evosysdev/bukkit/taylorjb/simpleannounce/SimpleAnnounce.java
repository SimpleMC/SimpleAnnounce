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
        
        // if config is invalid or non-existent, throw in default
        if (!getConfig().contains("messages"))
        {
            generateDefaultConfig();
        }
        
        messageSection = getConfig().getConfigurationSection("messages");
        messageNodes = messageSection.getKeys(false);
        
        addMessages(messageNodes, messageSection);
    }
    
    /**
     * Generate a config file with default values
     * 
     * Unfortunately we cannot use defaults because contains will
     * return true if node set in default OR config.
     * (thatssodumb.jpg, rage.mkv, etc etc)
     */
    private void generateDefaultConfig()
    {
        // set node values
        getConfig().set("messages.default1.message", "This is an automatically generated repeating message!");
        getConfig().set("messages.default1.delay", 15);
        getConfig().set("messages.default1.repeat", 60);
        getConfig().set("messages.default2.message", "This is another automatically generated repeating message for people with build permission!");
        getConfig().set("messages.default2.delay", 30);
        getConfig().set("messages.default2.repeat", 60);
        List<String> df2Includes = new LinkedList<String>();
        df2Includes.add("permissions.build");
        getConfig().set("messages.default2.includesperms", df2Includes);
        getConfig().set("messages.default3.message", "This is an automatically generated one-time message!");
        getConfig().set("messages.default3.delay", 45);
        
        // set header for information
        getConfig().options().header(
                "Messages config overview:\n" +
                "-------------------------\n" +
                "\n" +
                "<message label>:\n" +
                "    message(String, required): <Message to send>\n" +
                "    delay(int, optional - default 0): <Delay to send message on in seconds>\n" +
                "    repeat(int, optional): <time between repeat sendings of the message in seconds>\n" +
                "    includesperms(String list, optional):\n" +
                "    - <only send to those with this perm>\n" +
                "    - <and this one>\n" +
                "    excludesperms(String list, optional):\n" +
                "    - <don't send to those with this perm>\n" +
                "    - <and this one>\n" +
                "\n" +
                "-------------------------\n" +
                "\n" +
                "add messages you would like under 'messages:' section\n" +
                "");
        
        // save
        saveConfig();
        
        Logger.getLogger("Minecraft").info(getDescription().getName() + " generated new config file.");
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
        ConfigurationSection currentSec; // current message config section
        Message current; // current message we're working with
        String message; // actual message text
        int delay; // delay of message
        int repeat; // repeat timer of message
        
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
                current = new RepeatingMessage(this, message, delay, repeat);
            }
            else
            {
                // create message
                current = new Message(this, message, delay);
            }
            
            // let's add permission includes for the message now
            if (currentSec.contains("includesperms"))
            {
                current.addPermissionsIncl(currentSec.getList("includesperms"));
            }
            
            // let's add permission excludes for the message now
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
