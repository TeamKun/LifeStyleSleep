package com.github.tomodq.lifestyle;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

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
                players.forEach(player -> {
                    List<Integer> nowTime = castTime(player.getWorld().getTime());
                    TextComponent component = new TextComponent();
                    component.setText(nowTime.get(0) + "時" + nowTime.get(1) + "分");
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
                    List<Integer> sleepAndAwakeTime = playerEvent.getSleepAndAwakeTime(player.getName());
                    List<Integer> sleepTime = new ArrayList<Integer>();
                    sleepTime.add(sleepAndAwakeTime.get(0));
                    sleepTime.add(sleepAndAwakeTime.get(1));
                    List<Integer> awakeTime = new ArrayList<Integer>();
                    awakeTime.add(sleepAndAwakeTime.get(2));
                    awakeTime.add(sleepAndAwakeTime.get(3));
                    if(nowTime.get(0) == sleepTime.get(0) && nowTime.get(1) == sleepTime.get(1)) {
                        playerEvent.setSleep(player);
                        return;
                    }
                    if(nowTime.get(1) == 40 && sleepTime.get(1) == 0) {
                        if(nowTime.get(0) == 23  && sleepTime.get(0) == 0) {
                            playerEvent.setDizzy(player);
                            return;
                        }
                        if(nowTime.get(0)  == sleepTime.get(0) - 1) {
                            playerEvent.setDizzy(player);
                            return;
                        }
                    }
                    if(nowTime.get(0) == sleepTime.get(0) && nowTime.get(1) == sleepTime.get(1) - 20) {
                        playerEvent.setDizzy(player);
                        return;
                    }
                    if(nowTime.get(0) == awakeTime.get(0) && nowTime.get(1) == awakeTime.get(1)) {
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
