package com.github.tomodq.lifestyle;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
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
                if(players.size() == 0) {
                    return;
                }
                Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
                Objective sleep = sb.getObjective("sleep");
                Objective awake = sb.getObjective("awake");
                players.forEach(player -> {
                    List<Integer> nowTime = castTime(player.getWorld().getTime());
                    TextComponent component = new TextComponent();
                    component.setText(nowTime.get(0) + "時" + nowTime.get(1) + "分");
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
                    int sleepTime = sleep.getScore(player.getName()).getScore();
                    int awakeTime = awake.getScore(player.getName()).getScore();
                    if(nowTime.get(0) == sleepTime) {
                        playerEvent.setSleep(player);
                        return;
                    }
                    if(nowTime.get(1) == 40) {
                        if(nowTime.get(0) == 23  && sleepTime == 0) {
                            playerEvent.setDizzy(player);
                            return;
                        }
                        if(nowTime.get(0)  == sleepTime - 1) {
                            playerEvent.setDizzy(player);
                            return;
                        }
                    }
                    if(nowTime.get(0) == awakeTime) {
                        playerEvent.setAwake(player);
                    }
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


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


}
