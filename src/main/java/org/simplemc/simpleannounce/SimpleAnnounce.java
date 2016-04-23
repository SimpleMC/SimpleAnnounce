package org.simplemc.simpleannounce;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import org.simplemc.simpleannounce.message.Message;
import org.simplemc.simpleannounce.message.MessageGroup;
import org.simplemc.simpleannounce.message.sender.BossBarSender;
import org.simplemc.simpleannounce.message.sender.ChatMessageSender;
import org.simplemc.simpleannounce.message.sender.MessageSender;

/**
 * SimpleAnnounce main plugin
 * <p>
 * SimpleAnnounce is a simple and easy to use automated announcement plugin
 * </p>
 *
 * @author Taylor Becker
 */
public class SimpleAnnounce extends JavaPlugin
{
    /**
     * Read in config file and set up scheduled tasks
     */
    public void onEnable()
    {
        loadConfig(); // load messages from config

        getLogger().info(getDescription().getName() + " version " + getDescription().getVersion() + " enabled!");
    }

    /**
     * Read/load config
     */
    private void loadConfig()
    {
        Set<String> messageNodes; // message nodes
        ConfigurationSection messageSection; // configuration section of the messages

        // make sure config has all required things/update if necessary
        validateConfig();

        // delete all old tasks if they exist
        getServer().getScheduler().cancelTasks(this);

        // load debug mode
        if (getConfig().getBoolean("debug-mode", false))
        {
            getLogger().setLevel(Level.FINER);
        }

        // load auto-reload + create task to check again if necessary
        int reloadTime = getConfig().getInt("auto-reloadconfig", 0);
        if (reloadTime != 0)
        {
            getServer().getScheduler().runTaskLaterAsynchronously(this, () ->
            {
                reloadConfig();
                loadConfig();
            }, reloadTime * 60 * 20L);

            getLogger().fine("Will reload config in " + reloadTime + " minutes");
        }

        // load message nodes
        messageSection = getConfig().getConfigurationSection("messages");
        messageNodes = messageSection.getKeys(false);

        addMessages(messageNodes, messageSection);
    }

    /**
     * Validate nodes, if they don't exist or are wrong, set them
     * and resave config
     * <p>
     * Unfortunately we cannot use defaults because contains will
     * return true if node set in default OR config.
     * (thatssodumb.jpg, rage.mkv, etc etc)
     * </p>
     */
    private void validateConfig()
    {
        boolean updated = false; // track if we've updated config

        // settings
        if (!getConfig().contains("auto-reloadconfig") || !getConfig().isInt("auto-reloadconfig"))
        {
            getConfig().set("auto-reloadconfig", 20);
            updated = true;
        }

        if (!getConfig().contains("debug-mode") || !getConfig().isBoolean("debug-mode"))
        {
            getConfig().set("debug-mode", false);
            updated = true;
        }

        // messages
        if (!getConfig().contains("messages"))
        {
            // example repeating message
            getConfig().set("messages.repeating-example.message", "This is an automatically generated repeating message!");
            getConfig().set("messages.repeating-example.delay", 15);
            getConfig().set("messages.repeating-example.repeat", 60);

            // example repeating message with perms
            getConfig().set("messages.permissions-example.message", "This is another automatically generated repeating message for people with build permission!");
            getConfig().set("messages.permissions-example.delay", 30);
            getConfig().set("messages.permissions-example.repeat", 60);
            getConfig().set("messages.permissions-example.includesperms", Arrays.asList(new String[]{"permissions.build"}));

            // example one time message
            getConfig().set("messages.one-time-example.message", "This is an automatically generated one-time message!");
            getConfig().set("messages.one-time-example.delay", 45);

            // example message group with boss bar sender
            getConfig().set("messages.message-group-example.messages",
                    Arrays.asList(new String[]
                            {
                                    "This is an automatically generated sequential message group (1 of 3)!",
                                    "This is an automatically generated sequential message group (2 of 3)!",
                                    "This is an automatically generated sequential message group (3 of 3)!"
                            }));
            getConfig().set("messages.message-group-example.delay", 30);
            getConfig().set("messages.message-group-example.repeat", 30);
            getConfig().set("messages.message-group-example.sender", "bossbar");
            getConfig().set("messages.message-group-example.random-order", false);
            updated = true;
        }

        // if nodes have been updated, update header then save
        if (updated)
        {
            // set header for information
            getConfig().options().header("Config nodes:\n" +
                    "\n" +
                    "auto-reloadconfig(int): <Time in minutes to check/reload config for message updates(0 for off)>\n" +
                    "    NOTE: When config is reloaded, will reset delays for messages and cause one-time messages to resend\n" +
                    "debug-mode(boolean): <Should we pring debug to server.log(true/false)?>\n" +
                    "    NOTE: Look for fine and finer level log messages in server.log\n" +
                    "messages: Add messages below this, see below\n" +
                    "\n" +
                    "Messages config overview:\n" +
                    "-------------------------\n" +
                    "\n" +
                    "<message label>(String, must be unique):\n" +
                    "    message(String, required) OR messages(String list, see sample config): <Message to send>\n" +
                    "    random-order(boolean, optional): <if list of messages should be sent in random order>\n" +
                    "    sender(String, optional): <Message Sender(chat or boss, default: chat)>\n" +
                    "    bar(section, optional):\n" +
                    "        hold(int, optional): <Time in sections for bar to be displayed on announce>\n" +
                    "        color(String, optional): <bar color(https://hub.spigotmc.org/javadocs/spigot/org/bukkit/boss/BarColor.html)>\n" +
                    "        style(String, optional): <bar style(https://hub.spigotmc.org/javadocs/spigot/org/bukkit/boss/BarStyle.html)>\n" +
                    "        animate(section, optional):\n" +
                    "            enable(boolean, optional): <if bar should animate hold time>\n" +
                    "            reverse(boolean, optional): <if animation should be reversed>\n" +
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
            getLogger().info(getDescription().getName() + " config file updated, please check settings!");
        }

    }

    /**
     * Add messages from config to message list
     *
     * @param nodes   message nodes in config
     * @param section current config section
     */
    private void addMessages(Set<String> nodes, ConfigurationSection section)
    {
        ConfigurationSection currentSec; // current message config section
        Message current; // current message we're working with
        MessageSender sender; // message sender

        // go through all message nodes and get data from it
        for (String messageNode : nodes)
        {
            // set current section
            currentSec = section.getConfigurationSection(messageNode);

            // load message
            current = loadMessageBase(messageNode, currentSec); // message base info
            loadMessagePerms(current, currentSec); // includes and excludes perms
            sender = loadMessageSender(current, currentSec); // message sender

            // and finally, add/queue the message the message
            if (currentSec.contains("repeat"))
            {
                startRepeatingMessage(current, sender, currentSec.getInt("repeat"));
            }
            else
            {
                startMessage(current, sender);
            }
        }
    }

    /**
     * Load the base message information from a message configuration section
     * <p>
     * Loads message string(s), delay, and message group order(random or not)
     * </p>
     *
     * @param label          message label
     * @param messageSection configuration section to load message info from
     *
     * @return message loaded from config
     */
    private Message loadMessageBase(String label, ConfigurationSection messageSection)
    {
        Message message;
        // get message info from nodes
        int delay = messageSection.getInt("delay", 0);

        // message group
        if (messageSection.contains("messages"))
        {
            List<String> messages = messageSection.getStringList("messages");
            boolean random = messageSection.getBoolean("random-order", false);
            message = new MessageGroup(this, label, messages, delay, random);
        }
        // regular message
        else
        {
            String messageString = ChatColor.translateAlternateColorCodes('&', messageSection.getString("message"));
            message = new Message(this, label, messageString, delay);
        }

        return message;
    }

    /**
     * Load permissions (includes and excludes) for a message from a message configuration section
     *
     * @param message        message to add permissions to
     * @param messageSection configuration section to load permissions from
     */
    private void loadMessagePerms(Message message, ConfigurationSection messageSection)
    {
        // add permission includes for the message now
        if (messageSection.contains("includesperms"))
        {
            message.addPermissionsIncl(messageSection.getStringList("includesperms"));
        }

        // add permission excludes for the message now
        if (messageSection.contains("excludesperms"))
        {
            message.addPermissionsExcl(messageSection.getStringList("excludesperms"));
        }
    }

    /**
     * Load message sender for a message from a message configuration section
     *
     * @param message        message to load sender for
     * @param messageSection configuration section to load message sender config from
     */
    private MessageSender loadMessageSender(Message message, ConfigurationSection messageSection)
    {
        MessageSender sender;

        // get message sender
        String senderString = messageSection.getString("sender", "chat").toLowerCase();
        switch (senderString)
        {
            case "boss":
            case "bossbar":
                sender = new BossBarSender(this,
                        message,
                        messageSection.getInt("bar.hold", 5),
                        BarColor.valueOf(messageSection.getString("bar.color", "PURPLE").toUpperCase()),
                        BarStyle.valueOf(messageSection.getString("bar.style", "SOLID").toUpperCase()),
                        messageSection.getBoolean("bar.animate.enable", true),
                        messageSection.getBoolean("bar.animate.reverse", false));
                break;
            case "chat":
            default:
                sender = new ChatMessageSender(this, message);
                break;
        }

        return sender;
    }

    /**
     * Kick off/schedule messages
     *
     * @param message       message we are starting
     * @param messageSender sender for the message
     */
    private void startMessage(Message message, MessageSender messageSender)
    {
        getServer().getScheduler().runTaskLater(this, messageSender, message.getDelay() * 20);
    }

    /**
     * Kick off/schedule repeating messages
     *
     * @param message       message we are starting
     * @param messageSender sender for the message
     */
    private void startRepeatingMessage(Message message, MessageSender messageSender, int period)
    {
        getServer().getScheduler().runTaskTimer(this, messageSender, message.getDelay() * 20, period * 20);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (command.getName().equalsIgnoreCase("simpleannounce"))
        {
            if (sender.hasPermission("simpleannounce"))
            {
                // reload command
                if (args.length > 0 && (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("r")))
                {
                    if (sender.hasPermission("simpleannounce.reload"))
                    {
                        reloadConfig(); // reload file
                        loadConfig(); // read config

                        getLogger().fine("Config reloaded.");
                        sender.sendMessage("SimpleAnnounce config reloaded");
                    }
                    else
                    {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to do that!");
                    }
                }
                // help command
                else
                {
                    sender.sendMessage(ChatColor.AQUA + "/" + getCommand("simpleannounce").getName() + ChatColor.WHITE + " | " + ChatColor.BLUE
                            + getCommand("simpleannounce").getDescription());
                    sender.sendMessage("Usage: " + ChatColor.GRAY + getCommand("simpleannounce").getUsage());
                }
            }
            else
            {
                sender.sendMessage(ChatColor.RED + "You do not have permission to do that!");
            }
            return true;
        }

        return false;
    }

    /**
     * Plugin disabled
     */
    @Override
    public void onDisable()
    {
        getLogger().info("Cancelling running tasks...");
        getServer().getScheduler().cancelTasks(this);
        getLogger().info("SimpleAnnounce disabled.");
    }
}
