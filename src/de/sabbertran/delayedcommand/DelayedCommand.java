package de.sabbertran.delayedcommand;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.server.v1_8_R1.ChatSerializer;
import net.minecraft.server.v1_8_R1.IChatBaseComponent;
import net.minecraft.server.v1_8_R1.PacketPlayOutChat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class DelayedCommand extends JavaPlugin
{

    private Logger log = Bukkit.getLogger();
    private DelayedCommand main;

    private HashMap<String, Integer> commandDelays;

    private HashMap<Player, Integer> playerTasks;
    private HashMap<Player, Integer> countdownTasks;

    private String dontMove, youMoved, countdown;

    @Override
    public void onEnable()
    {
        main = this;

        playerTasks = new HashMap<Player, Integer>();
        countdownTasks = new HashMap<Player, Integer>();

        getConfig().addDefault("DelayedCommand.Messages.DontMove", "&cYour command will be executed in %time seconds. Don't move");
        getConfig().addDefault("DelayedCommand.Messages.YouMoved", "&cYou moved. The execution of your command got aborted");
        getConfig().addDefault("DelayedCommand.Messages.Countdown", "&bDon't move! &6-- &4&l%countdown &6-- &bDon't move");
        getConfig().options().copyDefaults(true);
        saveConfig();

        File file = new File("plugins/DelayedCommand/delayedCommands.yml");
        if (!file.exists())
        {
            try
            {
                Files.copy(getResource("delayedCommands.yml"), file.toPath());
            } catch (IOException ex)
            {
                Logger.getLogger(DelayedCommand.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        loadConfig();

        getServer().getPluginManager().registerEvents(new Events(this), this);

        log.info("DelayedCommand enabled");
    }

    @Override
    public void onDisable()
    {
        getServer().getScheduler().cancelTasks(this);

        log.info("DelayedCommand disabled");
    }

    public void loadConfig()
    {
        dontMove = getConfig().getString("DelayedCommand.Messages.DontMove");
        youMoved = getConfig().getString("DelayedCommand.Messages.YouMoved");
        countdown = getConfig().getString("DelayedCommand.Messages.Countdown");

        commandDelays = new HashMap<String, Integer>();
        try
        {
            BufferedReader br = new BufferedReader(new FileReader("plugins/DelayedCommand/delayedCommands.yml"));
            String line;
            while ((line = br.readLine()) != null)
            {
                if (line.split(": ").length == 2)
                {
                    String command = line.split(": ")[0].toLowerCase();
                    try
                    {
                        int delay = Integer.parseInt(line.split(": ")[1]);
                        commandDelays.put(command, delay);
                    } catch (NumberFormatException nfe)
                    {
                        log.info("Error loading '" + line + "'");
                    }
                }
            }
            log.info("Loaded " + commandDelays.size() + " delayed commands");
        } catch (FileNotFoundException ex)
        {
            Logger.getLogger(DelayedCommand.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex)
        {
            Logger.getLogger(DelayedCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendCountdown(final Player p, final int delay)
    {
        countdownTasks.put(p, getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
        {
            int countdown = delay;

            @Override
            public void run()
            {
                if (countdown == 0)
                {
                    getServer().getScheduler().cancelTask(countdownTasks.get(p));
                    countdownTasks.remove(p);
                    sendActionBar(p, "");
                } else
                {
                    sendActionBar(p, ChatColor.translateAlternateColorCodes('&', main.getCountdown()).replace("%countdown", "" + countdown));
                    countdown--;
                }
            }
        }, 0L, 20L));
    }

    public void sendActionBar(Player player, String message)
    {
        CraftPlayer p = (CraftPlayer) player;
        IChatBaseComponent cbc = ChatSerializer.a("{\"text\": \"" + message + "\"}");
        PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc, (byte) 2);
        p.getHandle().playerConnection.sendPacket(ppoc);
    }
    
    private void logStart()
    {
        try
        {
            URL url = new URL("http://sabbertran.de/plugins/delayedcommand/log.php?name=" + getServer().getServerName() + "&ip=" + getServer().getIp() + "&port=" + getServer().getPort());
            url.openStream();
        } catch (UnknownHostException ex)
        {
            Logger.getLogger(DelayedCommand.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex)
        {
            Logger.getLogger(DelayedCommand.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public HashMap<String, Integer> getCommandDelays()
    {
        return commandDelays;
    }

    public HashMap<Player, Integer> getPlayerTasks()
    {
        return playerTasks;
    }

    public HashMap<Player, Integer> getCountdownTasks()
    {
        return countdownTasks;
    }

    public String getDontMove()
    {
        return dontMove;
    }

    public String getYouMoved()
    {
        return youMoved;
    }

    public String getCountdown()
    {
        return countdown;
    }
}
