package de.sabbertran.delayedcommand;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Events implements Listener
{

    private DelayedCommand main;

    public Events(DelayedCommand main)
    {
        this.main = main;
    }

    @EventHandler
    public void onCommandPreprocess(final PlayerCommandPreprocessEvent ev)
    {
        final Player p = ev.getPlayer();

        String command = ev.getMessage().split(" ")[0];
        command = command.substring(1, command.length());
        if (main.getCommandDelays().containsKey(command.toLowerCase()))
        {
            int delay = main.getCommandDelays().get(command.toLowerCase());
            ev.setCancelled(true);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getDontMove()).replace("%time", "" + delay));
            main.sendCountdown(p, delay);
            main.getPlayerTasks().put(p, main.getServer().getScheduler().scheduleSyncDelayedTask(main, new Runnable()
            {
                @Override
                public void run()
                {
                    main.getServer().dispatchCommand(p, ev.getMessage().substring(1, ev.getMessage().length()));
                }
            }, (long) delay * 20));
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent ev)
    {
        Player p = ev.getPlayer();
        if (ev.getFrom().getBlockX() != ev.getTo().getBlockX() || ev.getFrom().getBlockZ() != ev.getTo().getBlockZ())
        {
            if (main.getPlayerTasks().containsKey(p) && main.getCountdownTasks().containsKey(p))
            {
                main.getServer().getScheduler().cancelTask(main.getPlayerTasks().get(p));
                main.getPlayerTasks().remove(p);
                main.getServer().getScheduler().cancelTask(main.getCountdownTasks().get(p));
                main.getCountdownTasks().remove(p);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getYouMoved()));
                main.sendActionBar(p, "");
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent ev)
    {
        Player p = ev.getPlayer();
        if (main.getPlayerTasks().containsKey(p) && main.getCountdownTasks().containsKey(p))
        {
            main.getServer().getScheduler().cancelTask(main.getPlayerTasks().get(p));
            main.getPlayerTasks().remove(p);
            main.getServer().getScheduler().cancelTask(main.getCountdownTasks().get(p));
            main.getCountdownTasks().remove(p);
        }
    }
}
