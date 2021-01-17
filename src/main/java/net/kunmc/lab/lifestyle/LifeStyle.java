package net.kunmc.lab.lifestyle;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;

public final class LifeStyle extends JavaPlugin{
    @Override
    public void onEnable() {
        // Plugin startup logic
        PlayerEvent playerEvent = new PlayerEvent(this);
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                List<Player> players = playerEvent.getPlayers();
                World world = playerEvent.getWorld() != null ? playerEvent.getWorld() : null;
                if(players.size() == 0 || world == null) {
                    return;
                }
                world.setTime(14000L);
                Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
                Objective sleep = sb.getObjective("sleep");
                Objective awake = sb.getObjective("awake");
                players.forEach(player -> {
                    //List<Integer> serverTime = castTime(player.getWorld().getTime());
                    //player.sendMessage("サーバ: "+ serverTime.get(0) + "時" + serverTime2.get(1) + "分");
                    List<Integer> nowTime = castTime(playerEvent.getTime());
                    player.setPlayerTime(playerEvent.getTime(), false);
                    int sleepTime = sleep == null ? 22: sleep.getScore(player.getName()) == null ? 22 : sleep.getScore(player.getName()).getScore();
                    int awakeTime = awake == null ? 5 : awake.getScore(player.getName()) == null ? 5 : awake.getScore(player.getName()).getScore();
                    playerEvent.setTime();
                    if(nowTime.get(0) == sleepTime) {
                        playerEvent.setSleep(player);
                        setActionBar(nowTime, playerEvent.getMessage(player.getName()), player);
                        return;
                    }
                    if(nowTime.get(1) == 40) {
                        if(nowTime.get(0) == 23  && sleepTime == 0) {
                            playerEvent.setDizzy(player);
                            setActionBar(nowTime, playerEvent.getMessage(player.getName()), player);
                            return;
                        }
                        if(nowTime.get(0)  == sleepTime - 1) {
                            playerEvent.setDizzy(player);
                            setActionBar(nowTime, playerEvent.getMessage(player.getName()), player);
                            return;
                        }
                    }
                    if(nowTime.get(0) == awakeTime) {
                        playerEvent.setAwake(player);
                        setActionBar(nowTime, playerEvent.getMessage(player.getName()), player);
                        return;
                    }
                    setActionBar(nowTime, playerEvent.getMessage(player.getName()), player);
                });

            }
        }, 0L, 2L);
    }

    public List<Integer> castTime(Long time) {
        int hours = 0;
        int minutes = 0;
        while (true) {
            if(time >= 1000) {
                time -= 1000;
                hours += 1;
                continue;
            }
            if(time >= 17) {
                time -= 17;
                minutes += 1;
                continue;
            }
            break;
        }
        hours = hours >= 18 ? hours - 18 : hours + 6;
        List<Integer> times = new ArrayList<Integer>();
        times.add(hours);
        times.add(minutes);
        return times;
    }

    public void setActionBar(List<Integer> nowTime, String message, Player player) {
        TextComponent component = new TextComponent();
        component.setText(nowTime.get(0) + "時" + nowTime.get(1) + "分" + " <" + message +"§f>");
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("speed-up")) {
            if (args.length == 0 || args.length > 1) {
                return false;
            }
            PlayerEvent.setSpeed(Long.parseLong(args[0]));
            sender.sendMessage("経過速度を" + args[0] + "倍に設定しました");
            return true;
        }
        return false;
    }


}
